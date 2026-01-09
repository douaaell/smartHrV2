package dao;

import model.Contrat;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContratDAO {

    public void addContrat(Contrat c) {
        String sql = "INSERT INTO contrat (id, employee_email, type, start_date, end_date, salary, position, department, benefits, status, file_name_pdf) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getId());
            stmt.setString(2, c.getEmployeeEmail());
            stmt.setString(3, c.getType());
            stmt.setDate(4, c.getStartDate() != null ? Date.valueOf(c.getStartDate()) : null);
            stmt.setDate(5, c.getEndDate() != null ? Date.valueOf(c.getEndDate()) : null);
            stmt.setDouble(6, c.getSalary());
            stmt.setString(7, c.getPosition());
            stmt.setString(8, c.getDepartment());
            stmt.setString(9, c.getBenefits());
            stmt.setString(10, c.getStatus());
            stmt.setString(11, c.getFileNamePdf());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateContrat(Contrat c) {
        String sql = "UPDATE contrat SET type=?, start_date=?, end_date=?, salary=?, position=?, department=?, benefits=?, status=?, file_name_pdf=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getType());
            stmt.setDate(2, c.getStartDate() != null ? Date.valueOf(c.getStartDate()) : null);
            stmt.setDate(3, c.getEndDate() != null ? Date.valueOf(c.getEndDate()) : null);
            stmt.setDouble(4, c.getSalary());
            stmt.setString(5, c.getPosition());
            stmt.setString(6, c.getDepartment());
            stmt.setString(7, c.getBenefits());
            stmt.setString(8, c.getStatus());
            stmt.setString(9, c.getFileNamePdf());
            stmt.setString(10, c.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteContrat(String id) {
        String sql = "DELETE FROM contrat WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Contrat> getAllContrats() {
        List<Contrat> list = new ArrayList<>();
        String sql = "SELECT * FROM contrat";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractContratFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Contrat> findContratsByEmployee(String email) {
        List<Contrat> list = new ArrayList<>();
        String sql = "SELECT * FROM contrat WHERE employee_email=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractContratFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Contrat findContratActifByEmail(String email) {
        String sql = "SELECT * FROM contrat WHERE employee_email=? AND status='Actif'";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractContratFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Contrat extractContratFromResultSet(ResultSet rs) throws SQLException {
        Date startDate = rs.getDate("start_date");
        Date endDate = rs.getDate("end_date");

        return new Contrat(
                rs.getString("id"),
                rs.getString("employee_email"),
                rs.getString("type"),
                startDate != null ? startDate.toLocalDate() : null,
                endDate != null ? endDate.toLocalDate() : null,
                rs.getDouble("salary"),
                rs.getString("position"),
                rs.getString("department"),
                rs.getString("benefits"),
                rs.getString("status"),
                rs.getString("file_name_pdf"));
    }
}
