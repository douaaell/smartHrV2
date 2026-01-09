package service;

import model.Contrat;
import dao.ContratDAO;
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

    // Ajouter un contrat avec validation des règles métier
    public synchronized void ajouterContrat(Contrat c) {
        // 1. Règle : Interdire la création si un contrat ACTIF existe déjà
        Contrat actif = dao.findContratActifByEmail(c.getEmployeeEmail());
        if (actif != null) {
            throw new IllegalStateException("Cet employé possède déjà un contrat ACTIF (" + actif.getId() + ").");
        }

        // 2. Règle : Validation des dates selon le type
        if (c.getStartDate() == null) {
            throw new IllegalArgumentException("La date de début est obligatoire.");
        }

        if (c.getType().equalsIgnoreCase("CDI")) {
            // CDI : Pas de date de fin
            c.setEndDate(null);
        } else if (c.getType().equalsIgnoreCase("CDD") || c.getType().equalsIgnoreCase("Stage")) {
            // CDD/STAGE : Date de fin obligatoire
            if (c.getEndDate() == null) {
                throw new IllegalArgumentException(
                        "La date de fin est obligatoire pour un contrat de type " + c.getType());
            }
            if (c.getEndDate().isBefore(c.getStartDate())) {
                throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début.");
            }
        }

        // 3. Forcer le statut à Actif pour un nouveau contrat
        c.setStatus("Actif");

        // Exécution asynchrone
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

    // Contrats dont la fin est proche ou expirés
    public List<Contrat> contratsFinProche(int jours) {
        LocalDate today = LocalDate.now();
        return dao.getAllContrats()
                .stream()
                .filter(c -> !c.getStatus().equalsIgnoreCase("TERMINE") && !c.getStatus().equalsIgnoreCase("Expire"))
                .filter(c -> c.getEndDate() != null && !c.getEndDate().isBefore(today))
                .filter(c -> c.getEndDate().isBefore(today.plusDays(jours)))
                .collect(Collectors.toList());
    }

    // Règle métiers : Passer automatiquement à EXPIRE à la date de fin
    public void actualiserStatutsContrats() {
        List<Contrat> contrats = dao.getAllContrats();
        LocalDate today = LocalDate.now();

        for (Contrat c : contrats) {
            if ("Actif".equalsIgnoreCase(c.getStatus()) && c.getEndDate() != null && c.getEndDate().isBefore(today)) {
                c.setStatus("Expire");
                dao.updateContrat(c);
                System.out.println("✅ Contrat " + c.getId() + " mis à jour : EXPIRE");
            }
        }
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
