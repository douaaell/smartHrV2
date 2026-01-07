package service;

import dao.CongeDAO;
import model.Conge;
import java.time.LocalDate;
import java.time.ZoneId;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CongeService {
    private static final int JOURS_CONGES_ANNUELS = 25;
    private final CongeDAO congeDAO;

    public CongeService() {
        this.congeDAO = new CongeDAO();
    }

    public Conge saveConge(Conge conge) {
        if (conge == null) {
            throw new IllegalArgumentException("Le congé ne peut pas être null");
        }

        if (!estValide(conge)) {
            throw new IllegalArgumentException("Les dates du congé ne sont pas valides");
        }

        if (conge.getType().name().equals("ANNUEL")) {
            int joursDemandes = calculerJoursOuvres(conge);
            int joursRestants = getRemainingVacationDays(conge.getEmployeeId());

            if (joursDemandes > joursRestants) {
                throw new IllegalStateException(
                        String.format("Nombre de jours demandés (%d) dépasse les jours restants (%d)",
                                joursDemandes, joursRestants));
            }
        }

        if (conge.getId() == 0) {
            congeDAO.save(conge);
            return conge;
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
        // Convert java.util.Date to LocalDate using constructor (sql.Date extends
        // util.Date)
        LocalDate debut = new java.sql.Date(conge.getDateDebut().getTime()).toLocalDate();
        LocalDate fin = new java.sql.Date(conge.getDateFin().getTime()).toLocalDate();

        int jours = 0;
        while (!debut.isAfter(fin)) {
            if (debut.getDayOfWeek().getValue() < 6) { // Lundi à Vendredi
                jours++;
            }
            debut = debut.plusDays(1);
        }
        return jours;
    }

    public Optional<Conge> findCongeById(int id) {
        return Optional.ofNullable(congeDAO.findById(id));
    }

    public List<Conge> findCongesByEmployeeId(int employeeId) {
        return congeDAO.findByEmployeeId(employeeId).stream()
                .sorted((c1, c2) -> c2.getDateDebut().compareTo(c1.getDateDebut()))
                .collect(Collectors.toList());
    }

    public List<Conge> findAllConges() {
        return congeDAO.findAll().stream()
                .sorted((c1, c2) -> c2.getDateDebut().compareTo(c1.getDateDebut()))
                .collect(Collectors.toList());
    }

    public List<Conge> findCongesByStatus(Conge.StatutConge statut) {
        if (statut == null) {
            throw new IllegalArgumentException("Le statut ne peut pas être null");
        }

        return congeDAO.findAll().stream()
                .filter(c -> c.getStatut() == statut)
                .sorted((c1, c2) -> c2.getDateDebut().compareTo(c1.getDateDebut()))
                .collect(Collectors.toList());
    }

    public List<Conge> findPendingConges() {
        return findCongesByStatus(Conge.StatutConge.EN_ATTENTE);
    }

    public void updateConge(Conge conge) {
        if (conge == null || conge.getId() == 0) {
            throw new IllegalArgumentException("Congé invalide pour la mise à jour");
        }
        congeDAO.update(conge);
    }

    public void updateCongeStatus(int congeId, Conge.StatutConge statut, String motifRefus) {
        if (statut == null) {
            throw new IllegalArgumentException("Le statut ne peut pas être null");
        }

        Conge conge = congeDAO.findById(congeId);
        if (conge == null) {
            throw new IllegalArgumentException("Aucun congé trouvé avec l'ID: " + congeId);
        }

        conge.setStatut(statut);

        if (motifRefus != null && !motifRefus.isEmpty()) {
            conge.setMotifRefus(motifRefus);
        }

        congeDAO.update(conge);
    }

    public void deleteConge(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de congé invalide");
        }
        congeDAO.delete(id);
    }

    public int getRemainingVacationDays(int employeeId) {
        List<Conge> conges = congeDAO.findByEmployeeId(employeeId);
        int joursPris = conges.stream()
                .filter(c -> c.getStatut() == Conge.StatutConge.VALIDE &&
                        c.getType() == Conge.TypeConge.ANNUEL)
                .mapToInt(this::calculerJoursOuvres)
                .sum();

        return Math.max(0, JOURS_CONGES_ANNUELS - joursPris);
    }

    public void exportToCsv(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin du fichier ne peut pas être vide");
        }
        congeDAO.exportToCsv(filePath);
    }

    public List<Conge> importFromCsv(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin du fichier ne peut pas être vide");
        }
        List<Conge> conges = congeDAO.importFromCsv(filePath);
        conges.forEach(this::saveConge);
        return conges;
    }
}