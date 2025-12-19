package service;

import dao.CongeDAO;
import model.Conge;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CongeServiceImpl implements CongeService {
    private static final int JOURS_CONGES_ANNUELS = 25;
    private final CongeDAO congeDAO;

    public CongeServiceImpl() {
        this.congeDAO = new CongeDAO();
    }

    @Override
    public Conge saveConge(Conge conge) {
        if (conge == null) {
            throw new IllegalArgumentException("Le congé ne peut pas être null");
        }

        if (!conge.estValide()) {
            throw new IllegalArgumentException("Les dates du congé ne sont pas valides");
        }

        if (conge.getType().equals("ANNUEL")) {
            int joursDemandes = (int) ChronoUnit.DAYS.between(
                conge.getDateDebut().toLocalDate(),
                conge.getDateFin().toLocalDate()) + 1;
            
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
        congeDAO.delete(id);
    }

    @Override
    public int getRemainingVacationDays(int employeeId) {
        int joursPris = congeDAO.findByEmployeeId(employeeId).stream()
            .filter(c -> "VALIDE".equals(c.getStatut()) && "ANNUEL".equals(c.getType()))
            .mapToInt(this::calculerJoursOuvres)
            .sum();
            
        return Math.max(0, JOURS_CONGES_ANNUELS - joursPris);
    }

    private int calculerJoursOuvres(Conge conge) {
        LocalDate date = conge.getDateDebut().toLocalDate();
        LocalDate fin = conge.getDateFin().toLocalDate();
        int jours = 0;
        
        while (!date.isAfter(fin)) {
            if (date.getDayOfWeek().getValue() < 6) { // Lundi à Vendredi
                jours++;
            }
            date = date.plusDays(1);
        }
        return jours;
    }

    @Override
    public void exportToCsv(String filePath) {
        congeDAO.exportToCsv(filePath);
    }

    @Override
    public List<Conge> importFromCsv(String filePath) {
        List<Conge> conges = congeDAO.importFromCsv(filePath);
        conges.forEach(this::saveConge);
        return conges;
    }
}