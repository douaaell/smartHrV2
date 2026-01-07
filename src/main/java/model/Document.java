package model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Document implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum StatutDocument {
        EN_ATTENTE("En attente"),
        VALIDE("Validé"),
        REFUSE("Refusé");

        private final String libelle;

        StatutDocument(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    private int id;
    private String nom;
    private String type;
    private String chemin;
    private Date dateUpload;
    private Date dateAbsence;
    private StatutDocument statut;
    private String employeeEmail;

    // Constructeurs
    public Document() {
        // Constructeur par défaut nécessaire pour la désérialisation
    }

    public Document(int id, String nom, String type, String chemin, Date dateUpload, String employeeEmail) {
        this.id = id;
        setNom(nom);
        setType(type);
        this.chemin = chemin;
        this.dateUpload = dateUpload;
        setEmployeeEmail(employeeEmail);
    }

    public Document(String nom, String type, String chemin, Date dateUpload, String employeeEmail) {
        this(0, nom, type, chemin, dateUpload, employeeEmail);
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getType() {
        return type;
    }

    public String getChemin() {
        return chemin;
    }

    public Date getDateUpload() {
        return dateUpload;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    // Setters avec validation
    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du document ne peut pas être vide");
        }
        this.nom = nom.trim();
    }

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Le type de document ne peut pas être vide");
        }
        this.type = type.trim();
    }

    public void setChemin(String chemin) {
        this.chemin = chemin != null ? chemin.trim() : null;
    }

    public void setDateUpload(Date dateUpload) {
        this.dateUpload = dateUpload;
    }

    public void setEmployeeEmail(String employeeEmail) {
        if (employeeEmail == null || employeeEmail.trim().isEmpty() || !employeeEmail.contains("@")) {
            throw new IllegalArgumentException("L'email de l'employé n'est pas valide");
        }
        this.employeeEmail = employeeEmail.trim().toLowerCase();
    }

    public Date getDateAbsence() {
        return dateAbsence;
    }

    public void setDateAbsence(Date dateAbsence) {
        this.dateAbsence = dateAbsence;
    }

    public StatutDocument getStatut() {
        return statut;
    }

    public void setStatut(StatutDocument statut) {
        this.statut = statut != null ? statut : StatutDocument.EN_ATTENTE;
    }

    // Méthodes métier
    public void upload() {
        if (this.dateUpload == null) {
            this.dateUpload = new Date();
        }
        // Logique d'upload à implémenter
    }

    public void download() {
        // Logique de téléchargement à implémenter
    }

    public void supprimer() {
        // Logique de suppression à implémenter
    }

    // equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Document))
            return false;
        Document document = (Document) o;
        return id == document.id &&
                Objects.equals(nom, document.nom) &&
                Objects.equals(employeeEmail, document.employeeEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, employeeEmail);
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                ", employeeEmail='" + employeeEmail + '\'' +
                '}';
    }
}