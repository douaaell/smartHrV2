package service;

import dao.EmployeDAO;
import model.Employe;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class EmployeService {

    private final EmployeDAO dao = new EmployeDAO();

    // Pool de threads
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    // 🔒 Synchronisation sur l'objet service
    public synchronized boolean ajouterEmploye(Employe e) {
        System.out.println("🔄 Service: Ajout de l'employé " + e.getNom() + " en cours...");
        try {
            boolean success = dao.addEmploye(e);
            if (success) {
                System.out.println("✅ Service: Employé ajouté avec succès !");
            } else {
                System.out.println("❌ Service: Échec de l'ajout de l'employé");
            }
            return success;
        } catch (Exception ex) {
            System.err.println("❌ Service: Erreur lors de l'ajout de l'employé : " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public synchronized boolean modifierEmploye(Employe e) {
        System.out.println("🔄 Service: Modification de l'employé " + e.getNom() + " en cours...");
        try {
            executor.execute(() -> {
                dao.updateEmploye(e);
            });
            System.out.println("✅ Service: Employé modifié avec succès !");
            return true;
        } catch (Exception ex) {
            System.err.println("❌ Service: Erreur lors de la modification de l'employé : " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public synchronized boolean supprimerEmploye(int id) {
        System.out.println("🔄 Service: Suppression de l'employé ID " + id + " en cours...");
        try {
            executor.execute(() -> {
                dao.deleteEmploye(id);
            });
            System.out.println("✅ Service: Employé supprimé avec succès !");
            return true;
        } catch (Exception ex) {
            System.err.println("❌ Service: Erreur lors de la suppression de l'employé : " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    // 👇 PAS besoin de synchronized (lecture seulement)
    public List<Employe> listerEmployes() {
        return dao.getAllEmployes();
    }

    // 👇 Streams = thread-safe ici car liste locale
    public List<Employe> rechercherParNom(String nom) {
        return dao.getAllEmployes()
                .stream()
                .filter(e -> e.getNom().equalsIgnoreCase(nom))
                .collect(Collectors.toList());
    }

    public Employe findEmployeeById(int id) {
        return dao.getAllEmployes()
                .stream()
                .filter(e -> e.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Employe findEmployeeByEmail(String email) {
        return dao.getAllEmployes()
                .stream()
                .filter(e -> e.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    // Fermeture propre du pool
    public void shutdown() {
        executor.shutdown();
    }

    public void exportToCsv(String filePath) throws java.io.IOException {
        List<Employe> employes = listerEmployes();
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.File(filePath))) {
            StringBuilder sb = new StringBuilder();
            // Header
            sb.append("ID,Nom,Prenom,Email,Departement,Position,Role,Telephone,Adresse\n");

            for (Employe emp : employes) {
                sb.append(emp.getId()).append(",");
                sb.append(escapeSpecialCharacters(emp.getNom())).append(",");
                sb.append(escapeSpecialCharacters(emp.getPrenom())).append(",");
                sb.append(escapeSpecialCharacters(emp.getEmail())).append(",");
                sb.append(escapeSpecialCharacters(emp.getDepartement())).append(",");
                sb.append(escapeSpecialCharacters(emp.getPosition())).append(",");
                sb.append(escapeSpecialCharacters(emp.getRole())).append(",");
                sb.append(escapeSpecialCharacters(emp.getTelephone())).append(",");
                sb.append(escapeSpecialCharacters(emp.getAdresse())).append("\n");
            }
            writer.write(sb.toString());
        }
    }

    private String escapeSpecialCharacters(String data) {
        if (data == null)
            return "";
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
