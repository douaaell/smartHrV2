package controller;

import model.Conge;
import service.CongeService;
import service.CongeServiceImpl;
import java.util.List;

public class CongeController {
    private final CongeService congeService;

    public CongeController() {
        this.congeService = new CongeServiceImpl();
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

    public List<Conge> listerCongesParStatut(String statut) {
        return congeService.findCongesByStatus(statut);
    }

    public List<Conge> listerCongesEnAttente() {
        return congeService.findPendingConges();
    }

    public void traiterDemandeConge(int congeId, String statut, String motifRefus) {
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