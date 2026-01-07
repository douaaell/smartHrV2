package dao;

import model.Document;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {
    private static final String INSERT_SQL = "INSERT INTO document (nom, type, chemin, date_upload, date_absence, statut, employee_email) VALUES (?, ?, ?, ?, ?, 'EN_ATTENTE', ?)";
    private static final String SELECT_ALL_SQL = "SELECT * FROM document";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM document WHERE id = ?";
    private static final String SELECT_BY_EMPLOYEE_EMAIL = "SELECT * FROM document WHERE employee_email = ?";
    private static final String UPDATE_SQL = "UPDATE document SET nom = ?, type = ?, chemin = ?, date_upload = ?, date_absence = ?, statut = ?, employee_email = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM document WHERE id = ?";

    public void save(Document document) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, document.getNom());
            stmt.setString(2, document.getType());
            stmt.setString(3, document.getChemin());
            stmt.setTimestamp(4, new Timestamp(document.getDateUpload().getTime()));
            if (document.getDateAbsence() != null) {
                stmt.setDate(5, new java.sql.Date(document.getDateAbsence().getTime()));
            } else {
                stmt.setDate(5, null);
            }
            stmt.setString(6, document.getEmployeeEmail());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Échec de la création du document, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    document.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Échec de la création du document, aucun ID obtenu.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la sauvegarde du document", e);
        }
    }

    public List<Document> findAll() {
        List<Document> documents = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des documents", e);
        }

        return documents;
    }

    public Document findById(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDocument(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la recherche du document par ID", e);
        }
        return null;
    }

    public List<Document> findByEmployeeEmail(String email) {
        List<Document> documents = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMPLOYEE_EMAIL)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    documents.add(mapResultSetToDocument(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la recherche des documents par email employé", e);
        }

        return documents;
    }

    public void update(Document document) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, document.getNom());
            stmt.setString(2, document.getType());
            stmt.setString(3, document.getChemin());
            stmt.setTimestamp(4, new Timestamp(document.getDateUpload().getTime()));
            if (document.getDateAbsence() != null) {
                stmt.setDate(5, new java.sql.Date(document.getDateAbsence().getTime()));
            } else {
                stmt.setDate(5, null);
            }
            if (document.getStatut() != null) {
                stmt.setString(6, document.getStatut().name());
            } else {
                stmt.setString(6, "EN_ATTENTE");
            }
            stmt.setString(7, document.getEmployeeEmail());
            stmt.setInt(8, document.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour du document", e);
        }
    }

    public void delete(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du document", e);
        }
    }

    private Document mapResultSetToDocument(ResultSet rs) throws SQLException {
        Document document = new Document();
        document.setId(rs.getInt("id"));
        document.setNom(rs.getString("nom"));
        document.setType(rs.getString("type"));
        document.setChemin(rs.getString("chemin"));
        document.setDateUpload(rs.getTimestamp("date_upload"));
        java.sql.Date dateAbs = rs.getDate("date_absence");
        if (dateAbs != null) {
            document.setDateAbsence(new Date(dateAbs.getTime()));
        }
        String statutStr = rs.getString("statut");
        if (statutStr != null) {
            document.setStatut(Document.StatutDocument.valueOf(statutStr));
        }
        document.setEmployeeEmail(rs.getString("employee_email"));
        return document;
    }

    // Méthodes pour l'export/import CSV
    public void exportToCsv(String filePath) {
        try {
            List<Document> documents = findAll();
            csv.DocumentCSV.exportToCsv(documents, filePath).get(); // Attendre la fin de l'export
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'export CSV: " + e.getMessage(), e);
        }
    }

    public List<Document> importFromCsv(String filePath) {
        try {
            List<Document> documents = csv.DocumentCSV.importFromCsv(filePath).get(); // Attendre la fin de l'import
            for (Document doc : documents) {
                save(doc);
            }
            return documents;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'import CSV: " + e.getMessage(), e);
        }
    }
}