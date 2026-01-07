package model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Conge implements Serializable {
    public enum TypeConge {
        ANNUEL("Annuel"),
        MALADIE("Maladie"),
        MATERNITE("Maternité"),
        PATERNITE("Paternité"),
        SANS_SOLDE("Sans solde"),
        FORMATION("Formation"),

        AUTRE("Autre");

        private final String libelle;

        TypeConge(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public enum StatutConge {
        EN_ATTENTE("En attente"),
        VALIDE("Validé"),
        REFUSE("Refusé"),
        ANNULE("Annulé");

        private final String libelle;

        StatutConge(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    private static final long serialVersionUID = 1L;

    private int id;
    private int employeeId;
    private TypeConge type;
    private Date dateDebut;
    private Date dateFin;
    private StatutConge statut;
    private String motif;
    private Date dateDemande;

    // Constructeurs
    public Conge() {
        // Constructeur par défaut nécessaire pour la désérialisation
    }

    public Conge(int id, int employeeId, TypeConge type, Date dateDebut, Date dateFin, StatutConge statut,
            String motif) {
        this.id = id;
        this.employeeId = employeeId;
        setType(type);
        setDateDebut(dateDebut);
        setDateFin(dateFin);
        setStatut(statut);
        setMotif(motif);
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public TypeConge getType() {
        return type;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public StatutConge getStatut() {
        return statut;
    }

    public String getMotif() {
        return motif;
    }

    // Setters avec validation
    public void setId(int id) {
        this.id = id;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public void setType(TypeConge type) {
        this.type = type != null ? type : TypeConge.AUTRE;
    }

    public void setDateDebut(Date dateDebut) {
        if (dateDebut != null && dateFin != null && dateDebut.after(dateFin)) {
            throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
        }
        this.dateDebut = dateDebut;
    }

    public void setDateFin(Date dateFin) {
        if (dateDebut != null && dateFin != null && dateFin.before(dateDebut)) {
            throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début");
        }
        this.dateFin = dateFin;
    }

    public void setStatut(StatutConge statut) {
        this.statut = statut != null ? statut : StatutConge.EN_ATTENTE;
    }

    public void setMotif(String motif) {
        this.motif = motif != null ? motif.trim() : "";
    }

    public void setMotifRefus(String motifRefus) {
        this.motif = motifRefus != null ? motifRefus.trim() : "";
    }

    public Date getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(Date dateDemande) {
        this.dateDemande = dateDemande;
    }

    // Méthodes métier
    public int calculerDuree() {
        if (dateDebut != null && dateFin != null && !dateFin.before(dateDebut)) {
            long diffInMillies = dateFin.getTime() - dateDebut.getTime();
            return (int) (diffInMillies / (1000 * 60 * 60 * 24)) + 1; // +1 pour inclure le jour de début
        }
        return 0;
    }

    public boolean estValide() {
        if (dateDebut == null || dateFin == null) {
            return false;
        }
        return !dateFin.before(dateDebut);
    }

    // equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Conge))
            return false;
        Conge conge = (Conge) o;
        return id == conge.id &&
                employeeId == conge.employeeId &&
                type == conge.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employeeId, type);
    }

    @Override
    public String toString() {
        return "Conge{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", type=" + (type != null ? type.getLibelle() : "null") +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", statut=" + (statut != null ? statut.getLibelle() : "null") +
                '}';
    }
}