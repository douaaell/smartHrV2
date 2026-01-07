package service;

import dao.EmployeDAO;
import model.Employe;

import java.util.List;

public class AuthService {

    private final EmployeDAO employeDAO = new EmployeDAO();

    public boolean authenticate(String email, String password, String role) {
        try {
            System.out.println("🔐 Tentative d'authentification - Email: " + email + ", Rôle: " + role);
            System.out.println("🔑 Mot de passe fourni: '" + password + "'");
            
            // Get all employees
            List<Employe> employees = employeDAO.getAllEmployes();
            System.out.println("👥 Nombre d'employés dans la base: " + employees.size());
            
            // List all employees for debugging
            for (Employe emp : employees) {
                System.out.println("📋 Employé trouvé: " + emp.getEmail() + " | Rôle: " + emp.getRole() + " | MP: '" + emp.getPassword() + "'");
            }
            
            // Find user by email and role
            for (Employe emp : employees) {
                if (emp.getEmail().equalsIgnoreCase(email) && 
                    emp.getPassword().equals(password) && 
                    emp.getRole().equalsIgnoreCase(role)) {
                    
                    System.out.println("✅ Utilisateur trouvé et authentifié: " + emp.getNom() + " " + emp.getPrenom());
                    return true;
                }
            }
            
            System.out.println("❌ Aucun utilisateur trouvé avec ces identifiants");
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'authentification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Méthode pour créer un utilisateur par défaut si la base est vide
    public void createDefaultUsers() {
        try {
            List<Employe> employees = employeDAO.getAllEmployes();
            
            if (employees.isEmpty()) {
                System.out.println("📝 Création des utilisateurs par défaut...");
                
                // Créer un utilisateur RH par défaut
                Employe rhUser = new Employe(
                    1, 
                    "Admin", 
                    "RH", 
                    "rh@smarthr.com", 
                    "admin123", 
                    "RH", 
                    "Administrateur"
                );
                rhUser.setRole("RH");
                employeDAO.addEmploye(rhUser);
                
                // Créer un utilisateur Employé par défaut
                Employe empUser = new Employe(
                    2, 
                    "Jean", 
                    "Dupont", 
                    "jean.dupont@smarthr.com", 
                    "emp123", 
                    "IT", 
                    "Développeur"
                );
                empUser.setRole("EMPLOYE");
                employeDAO.addEmploye(empUser);
                
                System.out.println("✅ Utilisateurs par défaut créés:");
                System.out.println("  - RH: rh@smarthr.com / admin123");
                System.out.println("  - EMPLOYE: jean.dupont@smarthr.com / emp123");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création des utilisateurs par défaut: " + e.getMessage());
        }
    }
}
