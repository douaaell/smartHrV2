package dao;

import model.Employe;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeDAO {

    // Ajouter un employé
    public void addEmploye(Employe e) {
        String sql = "INSERT INTO employe (id, nom, prenom, email, password, departement, position) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, e.getId());
            stmt.setString(2, e.getNom());
            stmt.setString(3, e.getPrenom());
            stmt.setString(4, e.getEmail());
            stmt.setString(5, e.getPassword());
            stmt.setString(6, e.getDepartement());
            stmt.setString(7, e.getPosition());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Modifier un employé
    public void updateEmploye(Employe e) {
        String sql = "UPDATE employe SET nom=?, prenom=?, email=?, password=?, departement=?, position=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, e.getNom());
            stmt.setString(2, e.getPrenom());
            stmt.setString(3, e.getEmail());
            stmt.setString(4, e.getPassword());
            stmt.setString(5, e.getDepartement());
            stmt.setString(6, e.getPosition());
            stmt.setInt(7, e.getId());
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
                Employe e = new Employe(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("departement"),
                        rs.getString("position")
                );
                list.add(e);
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
                list.add(new Employe(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("departement"),
                        rs.getString("position")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }
}
