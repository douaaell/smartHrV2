package controller;

import model.Conge;
import service.CongeService;
import java.util.List;
import model.Conge.StatutConge;

public class CongeController {
    private final CongeService congeService;

    public CongeController() {
        this.congeService = new CongeService();
    }

    public Conge demanderConge(Conge conge) {
        return congeService.saveConge(conge);
    }

    public Conge obtenirConge(int id) {
        return congeService.findCongeById(id)
            .orElseThrow(() -> new RuntimeException("Congé non trouvé avec l'ID: " + id));
    }

    public List<Conge> listerTousLesConges() {
        return congeService.findAllConges();
    }

    public List<Conge> listerCongesEmploye(int employeeId) {
        return congeService.findCongesByEmployeeId(employeeId);
    }

    public List<Conge> listerCongesParStatut(StatutConge statut) {
        if (statut == null) {
            throw new IllegalArgumentException("Le statut ne peut pas être null");
        }
        return congeService.findCongesByStatus(statut);
    }

    public List<Conge> listerCongesEnAttente() {
        return congeService.findPendingConges();
    }

    public void traiterDemandeConge(int congeId, StatutConge statut, String motifRefus) {
        if (statut == null) {
            throw new IllegalArgumentException("Le statut ne peut pas être null");
        }
        congeService.updateCongeStatus(congeId, statut, motifRefus);
    }

    public int obtenirJoursCongesRestants(int employeeId) {
        return congeService.getRemainingVacationDays(employeeId);
    }

    public void supprimerConge(int id) {
        congeService.deleteConge(id);
    }

    public void exporterVersCsv(String cheminFichier) {
        congeService.exportToCsv(cheminFichier);
    }

    public List<Conge> importerDepuisCsv(String cheminFichier) {
        return congeService.importFromCsv(cheminFichier);
    }
}