package dao;

import model.Conge;
import csv.CongeCSV;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import utils.DatabaseConnection;

public class CongeDAO {
    private static final String INSERT_SQL = "INSERT INTO conge (employee_id, type, date_debut, date_fin, statut, motif, date_demande) VALUES (?, ?, ?, ?, ?, ?, NOW())";
    private static final String SELECT_ALL_SQL = "SELECT * FROM conge";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM conge WHERE id = ?";
    private static final String SELECT_BY_EMPLOYEE_ID = "SELECT * FROM conge WHERE employee_id = ?";
    private static final String UPDATE_SQL = "UPDATE conge SET employee_id = ?, type = ?, date_debut = ?, date_fin = ?, statut = ?, motif = ?, date_demande = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM conge WHERE id = ?";

    public void save(Conge conge) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, conge.getEmployeeId());
            stmt.setString(2, conge.getType().name());
            stmt.setDate(3, new java.sql.Date(conge.getDateDebut().getTime()));
            stmt.setDate(4, new java.sql.Date(conge.getDateFin().getTime()));
            stmt.setString(5, conge.getStatut().name());
            stmt.setString(6, conge.getMotif());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Échec de la création du congé.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    conge.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Échec de la création du congé.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la sauvegarde du congé", e);
        }
    }

    public List<Conge> findAll() {
        List<Conge> conges = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                conges.add(mapResultSetToConge(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des congés", e);
        }

        return conges;
    }

    public Conge findById(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToConge(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la recherche du congé par ID", e);
        }
        return null;
    }

    public List<Conge> findByEmployeeId(int employeeId) {
        List<Conge> conges = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMPLOYEE_ID)) {

            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    conges.add(mapResultSetToConge(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la recherche des congés par ID employé", e);
        }

        return conges;
    }

    public void update(Conge conge) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setInt(1, conge.getEmployeeId());
            stmt.setString(2, conge.getType().name());
            stmt.setDate(3, new java.sql.Date(conge.getDateDebut().getTime()));
            stmt.setDate(4, new java.sql.Date(conge.getDateFin().getTime()));
            stmt.setString(5, conge.getStatut().name());
            stmt.setString(6, conge.getMotif());
            if (conge.getDateDemande() != null) {
                stmt.setDate(7, new java.sql.Date(conge.getDateDemande().getTime()));
            } else {
                stmt.setDate(7, new java.sql.Date(System.currentTimeMillis()));
            }
            stmt.setInt(8, conge.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour du congé", e);
        }
    }

    public void delete(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du congé", e);
        }
    }

    private Conge mapResultSetToConge(ResultSet rs) throws SQLException {
        Conge conge = new Conge();
        conge.setId(rs.getInt("id"));
        conge.setEmployeeId(rs.getInt("employee_id"));
        conge.setType(Conge.TypeConge.valueOf(rs.getString("type")));
        conge.setDateDebut(rs.getDate("date_debut"));
        conge.setDateFin(rs.getDate("date_fin"));
        conge.setStatut(Conge.StatutConge.valueOf(rs.getString("statut")));
        conge.setMotif(rs.getString("motif"));
        conge.setDateDemande(rs.getDate("date_demande"));
        return conge;
    }

    // Méthodes pour l'export/import CSV
    public void exportToCsv(String filePath) {
        try {
            List<Conge> conges = findAll();
            CongeCSV.exportToCsv(conges, filePath).get(); // Attendre la fin de l'export
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Erreur lors de l'export CSV: " + e.getMessage(), e);
        }
    }

    public List<Conge> importFromCsv(String filePath) {
        try {
            List<Conge> conges = CongeCSV.importFromCsv(filePath).get(); // Attendre la fin de l'import
            for (Conge conge : conges) {
                save(conge);
            }
            return conges;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Erreur lors de l'import CSV: " + e.getMessage(), e);
        }
    }

    // Méthode utilitaire pour mettre à jour le statut d'un congé
    public void updateStatus(int congeId, Conge.StatutConge newStatus) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE conge SET statut = ? WHERE id = ?")) {

            stmt.setString(1, newStatus.name());
            stmt.setInt(2, congeId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour du statut du congé", e);
        }
    }
}