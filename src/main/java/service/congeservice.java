package service;

import dao.CongeDAO;
import model.Conge;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CongeService {
    private static final int CONGES_ANNELS = 30;
    private final CongeDAO congeDAO;

    public CongeService() {
        this.congeDAO = new CongeDAO();
    }

    public void saveConge(Conge conge, int employeeId) throws IOException {
        if (!conge.estValide()) {
            throw new IllegalArgumentException("Le congé n'est pas valide. La date de fin doit être après ou égale à la date de début.");
        }
        
        if ("Annuel".equals(conge.getType())) {
            int joursDemandes = conge.calculerDuree();
            int joursRestants = getJoursCongesRestants(employeeId);
            
            if (joursDemandes > joursRestants) {
                throw new IllegalArgumentException(
                    String.format("Vous n'avez que %d jours de congés restants", joursRestants));
            }
        }
        
        int newId = 1;
        List<Conge> allConges = congeDAO.findAll();
        if (!allConges.isEmpty()) {
            newId = allConges.stream()
                           .mapToInt(Conge::getId)
                           .max()
                           .orElse(0) + 1;
        }
        conge.setId(newId);
        congeDAO.save(conge);
    }

    public List<Conge> loadConges() throws IOException {
        return congeDAO.findAll();
    }

    public Conge findCongeById(int id) throws IOException {
        return congeDAO.findAll().stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public boolean deleteConge(int id) throws IOException {
        List<Conge> allConges = congeDAO.findAll();
        boolean removed = allConges.removeIf(c -> c.getId() == id);
        
        if (removed) {
            congeDAO.updateAll(allConges);
        }
        return removed;
    }

    public List<Conge> getCongesByStatut(String statut) throws IOException {
        return congeDAO.findAll().stream()
                .filter(c -> c.getStatut().equalsIgnoreCase(statut))
                .collect(Collectors.toList());
    }

    public List<Conge> getCongesByType(String type) throws IOException {
        return congeDAO.findAll().stream()
                .filter(c -> c.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public void traiterDemande(int congeId, String statut, String motifRefus) throws IOException {
        List<Conge> allConges = congeDAO.findAll();
        
        allConges.stream()
            .filter(c -> c.getId() == congeId)
            .findFirst()
            .ifPresent(c -> {
                c.setStatut(statut);
                if ("Refusé".equals(statut)) {
                    c.setMotif("Refusé: " + (motifRefus != null ? motifRefus : "Raison non spécifiée"));
                }
            });
        
        congeDAO.updateAll(allConges);
    }

    public List<Conge> getCongesByEmployee(int employeeId) throws IOException {
        return congeDAO.findAll().stream()
                .filter(c -> c.getEmployeeId() == employeeId)
                .collect(Collectors.toList());
    }
    
    public int getJoursCongesRestants(int employeeId) throws IOException {
        int joursPris = congeDAO.findAll().stream()
                .filter(c -> c.getEmployeeId() == employeeId)
                .filter(c -> "Annuel".equals(c.getType()))
                .filter(c -> "Validé".equals(c.getStatut()))
                .mapToInt(Conge::calculerDuree)
                .sum();
                
        return CONGES_ANNELS - joursPris;
    }
    
    public List<Conge> getCongesEnAttente() throws IOException {
        return getCongesByStatut("En attente");
    }

    public List<Conge> getCongesValides() throws IOException {
        return getCongesByStatut("Validé");
    }

    public List<Conge> getCongesRefuses() throws IOException {
        return getCongesByStatut("Refusé");
    }
    
    public void updateConge(Conge updatedConge) throws IOException {
        List<Conge> allConges = congeDAO.findAll();
        
        allConges = allConges.stream()
            .map(c -> c.getId() == updatedConge.getId() ? updatedConge : c)
            .collect(Collectors.toList());
        
        congeDAO.updateAll(allConges);
    }
}