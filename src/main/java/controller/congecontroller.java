package controller;

import model.Conge;
import service.CongeService;
import java.io.IOException;
import java.util.List;

public class CongeController {
    private final CongeService congeService;

    public CongeController() {
        this.congeService = new CongeService();
    }

    public void demanderConge(Conge conge, int employeeId) throws IOException {
        congeService.saveConge(conge, employeeId);
    }

    public List<Conge> listerConges() throws IOException {
        return congeService.loadConges();
    }

    public Conge obtenirConge(int id) throws IOException {
        return congeService.findCongeById(id);
    }

    public boolean supprimerConge(int id) throws IOException {
        return congeService.deleteConge(id);
    }

    public List<Conge> listerCongesParStatut(String statut) throws IOException {
        return congeService.getCongesByStatut(statut);
    }

    public List<Conge> listerCongesParType(String type) throws IOException {
        return congeService.getCongesByType(type);
    }

    public void traiterDemandeConge(int congeId, String statut, String motifRefus) throws IOException {
        congeService.traiterDemande(congeId, statut, motifRefus);
    }

    public List<Conge> listerCongesEmploye(int employeeId) throws IOException {
        return congeService.getCongesByEmployee(employeeId);
    }

    public int obtenirJoursCongesRestants(int employeeId) throws IOException {
        return congeService.getJoursCongesRestants(employeeId);
    }

    public List<Conge> listerCongesEnAttente() throws IOException {
        return congeService.getCongesEnAttente();
    }

    public List<Conge> listerCongesValides() throws IOException {
        return congeService.getCongesValides();
    }

    public List<Conge> listerCongesRefuses() throws IOException {
        return congeService.getCongesRefuses();
    }

    public void mettreAJourConge(Conge conge) throws IOException {
        congeService.updateConge(conge);
    }
}