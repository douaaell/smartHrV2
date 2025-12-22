package service;

import dao.CongeDAO;
import model.Conge;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CongeService implements CongeService {
    private static final int JOURS_CONGES_ANNUELS = 25;
    private final CongeDAO congeDAO;

    public CongeService() {
        this.congeDAO = new CongeDAO();
    }

    @Override
    public Conge saveConge(Conge conge) {
        if (conge == null) {
            throw new IllegalArgumentException("Le congé ne peut pas être null");
        }

        if (!estValide(conge)) {
            throw new IllegalArgumentException("Les dates du congé ne sont pas valides");
        }

        if (conge.getType().equals("ANNUEL")) {
            int joursDemandes = calculerJoursOuvres(conge);
            int joursRestants = getRemainingVacationDays(conge.getEmployeeId());
            
            if (joursDemandes > joursRestants) {
                throw new IllegalStateException(
                    String.format("Nombre de jours demandés (%d) dépasse les jours restants (%d)", 
                    joursDemandes, joursRestants));
            }
        }

        if (conge.getId() == 0) {
            return congeDAO.save(conge);
        } else {
            congeDAO.update(conge);
            return conge;
        }
    }

    private boolean estValide(Conge conge) {
        if (conge.getDateDebut() == null || conge.getDateFin() == null) {
            return false;
        }
        return !conge.getDateDebut().after(conge.getDateFin());
    }

    private int calculerJoursOuvres(Conge conge) {
        LocalDate debut = conge.getDateDebut().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
        LocalDate fin = conge.getDateFin().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
        
        int jours = 0;
        while (!debut.isAfter(fin)) {
            if (debut.getDayOfWeek().getValue() < 6) { // Lundi à Vendredi
                jours++;
            }
            debut = debut.plusDays(1);
        }
        return jours;
    }

    @Override
    public Optional<Conge> findCongeById(int id) {
        return Optional.ofNullable(congeDAO.findById(id));
    }

    @Override
    public List<Conge> findCongesByEmployeeId(int employeeId) {
        return congeDAO.findByEmployeeId(employeeId).stream()
            .sorted((c1, c2) -> c2.getDateDebut().compareTo(c1.getDateDebut()))
            .collect(Collectors.toList());
    }

    @Override
    public List<Conge> findAllConges() {
        return congeDAO.findAll().stream()
            .sorted((c1, c2) -> c2.getDateDebut().compareTo(c1.getDateDebut()))
            .collect(Collectors.toList());
    }

    @Override
    public List<Conge> findCongesByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Le statut ne peut pas être vide");
        }
        
        return congeDAO.findAll().stream()
            .filter(c -> c.getStatut().equalsIgnoreCase(status))
            .sorted((c1, c2) -> c2.getDateDebut().compareTo(c1.getDateDebut()))
            .collect(Collectors.toList());
    }

    @Override
    public List<Conge> findPendingConges() {
        return findCongesByStatus("EN_ATTENTE");
    }

    @Override
    public void updateConge(Conge conge) {
        if (conge == null || conge.getId() == 0) {
            throw new IllegalArgumentException("Congé invalide pour la mise à jour");
        }
        congeDAO.update(conge);
    }

    @Override
    public void updateCongeStatus(int congeId, String status, String motifRefus) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Le statut ne peut pas être vide");
        }

        Conge conge = congeDAO.findById(congeId);
        if (conge == null) {
            throw new IllegalArgumentException("Aucun congé trouvé avec l'ID: " + congeId);
        }
        
        conge.setStatut(status);
        if (motifRefus != null && !motifRefus.isEmpty()) {
            conge.setMotifRefus(motifRefus);
        }
        
        congeDAO.update(conge);
    }

    @Override
    public void deleteConge(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de congé invalide");
        }
        congeDAO.delete(id);
    }

    @Override
    public int getRemainingVacationDays(int employeeId) {
        List<Conge> conges = congeDAO.findByEmployeeId(employeeId);
        int joursPris = conges.stream()
            .filter(c -> "VALIDE".equals(c.getStatut()) && "ANNUEL".equals(c.getType()))
            .mapToInt(this::calculerJoursOuvres)
            .sum();
            
        return Math.max(0, JOURS_CONGES_ANNUELS - joursPris);
    }

    @Override
    public void exportToCsv(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin du fichier ne peut pas être vide");
        }
        congeDAO.exportToCsv(filePath);
    }

    @Override
    public List<Conge> importFromCsv(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin du fichier ne peut pas être vide");
        }
        List<Conge> conges = congeDAO.importFromCsv(filePath);
        conges.forEach(this::saveConge);
        return conges;
    }
}