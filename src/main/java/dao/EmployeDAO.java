package dao;

import model.Employe;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeDAO {

    // Ajouter un employé
    public boolean addEmploye(Employe e) {
        String sql = "INSERT INTO employe (id, nom, prenom, email, password, departement, position, telephone, adresse, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            System.out.println("🔄 Tentative d'ajout d'employé : " + e.getNom() + " " + e.getPrenom());

            stmt.setInt(1, e.getId());
            stmt.setString(2, e.getNom());
            stmt.setString(3, e.getPrenom());
            stmt.setString(4, e.getEmail());
            stmt.setString(5, e.getPassword());
            stmt.setString(6, e.getDepartement());
            stmt.setString(7, e.getPosition());
            stmt.setString(8, e.getTelephone());
            stmt.setString(9, e.getAdresse());
            stmt.setString(10, e.getRole());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Employé ajouté avec succès dans la base de données !");
                return true;
            } else {
                System.out.println("⚠️ Aucune ligne n'a été affectée lors de l'ajout");
                return false;
            }

        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de l'ajout de l'employé : " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    // Modifier un employé
    public void updateEmploye(Employe e) {
        String sql = "UPDATE employe SET nom=?, prenom=?, email=?, password=?, departement=?, position=?, telephone=?, adresse=?, role=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, e.getNom());
            stmt.setString(2, e.getPrenom());
            stmt.setString(3, e.getEmail());
            stmt.setString(4, e.getPassword());
            stmt.setString(5, e.getDepartement());
            stmt.setString(6, e.getPosition());
            stmt.setString(7, e.getTelephone());
            stmt.setString(8, e.getAdresse());
            stmt.setString(9, e.getRole());
            stmt.setInt(10, e.getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Supprimer un employé
    public void deleteEmploye(int id) {
        String sql = "DELETE FROM employe WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Lister tous les employés
    public List<Employe> getAllEmployes() {
        List<Employe> list = new ArrayList<>();
        String sql = "SELECT * FROM employe";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractEmployeFromResultSet(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Rechercher par nom
    public List<Employe> findEmployeByNom(String nom) {
        List<Employe> list = new ArrayList<>();
        String sql = "SELECT * FROM employe WHERE nom LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nom + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractEmployeFromResultSet(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // Rechercher par ID
    public Employe findEmployeeById(int id) {
        String sql = "SELECT * FROM employe WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractEmployeFromResultSet(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Rechercher par Email
    public Employe findEmployeeByEmail(String email) {
        String sql = "SELECT * FROM employe WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractEmployeFromResultSet(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Helper pour extraire un employé d'un ResultSet
    private Employe extractEmployeFromResultSet(ResultSet rs) throws SQLException {
        String telephone = "";
        String adresse = "";
        try {
            telephone = rs.getString("telephone");
        } catch (SQLException ignore) {
        }
        try {
            adresse = rs.getString("adresse");
        } catch (SQLException ignore) {
        }

        Employe e = new Employe(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("departement"),
                rs.getString("position"),
                telephone,
                adresse);

        String role = null;
        try {
            role = rs.getString("role");
        } catch (SQLException ignore) {
        }
        if (role != null) {
            e.setRole(role);
        } else {
            e.setRole("EMPLOYE");
        }

        return e;
    }
}

