package service;

import dao.ContratDAO;
import model.Contrat;
import csv.ContratCSV;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ContratService {

    private final ContratDAO dao = new ContratDAO();
    private final ContratCSV csvExporter = new ContratCSV();
    // Création d'un pool de threads avec 5 threads (tu peux ajuster)
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    // Ajouter un contrat de façon asynchrone
    public synchronized void ajouterContrat(Contrat c) {
        executor.execute(() -> dao.addContrat(c));
    }

    // Modifier un contrat de façon asynchrone
    public synchronized void modifierContrat(Contrat c) {
        executor.execute(() -> dao.updateContrat(c));
    }

    // Supprimer un contrat de façon asynchrone
    public synchronized void supprimerContrat(String id) {
        executor.execute(() -> dao.deleteContrat(id));
    }

    // Liste tous les contrats
    public List<Contrat> listerContrats() {
        return dao.getAllContrats();
    }

    // Filtrer par employé
    public List<Contrat> filtrerParEmploye(String email) {
        return dao.getAllContrats()
                  .stream()
                  .filter(c -> c.getEmployeeEmail().equalsIgnoreCase(email))
                  .collect(Collectors.toList());
    }

    // Contrats dont la fin est proche
    public List<Contrat> contratsFinProche(int jours) {
        LocalDate today = LocalDate.now();
        return dao.getAllContrats()
                  .stream()
                  .filter(c -> !c.getStatus().equalsIgnoreCase("TERMINE"))
                  .filter(c -> !c.getEndDate().isBefore(today))
                  .filter(c -> c.getEndDate().isBefore(today.plusDays(jours)))
                  .collect(Collectors.toList());
    }

    // Exporter en CSV
    public void exportToCsv(String filename) {
        try {
            List<Contrat> contrats = dao.getAllContrats();
            csvExporter.exportToCsv(contrats, filename);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'export CSV: " + e.getMessage(), e);
        }
    }

    // Fermer proprement le pool de threads quand on quitte l'application
    public void shutdown() {
        executor.shutdown();
    }
}
