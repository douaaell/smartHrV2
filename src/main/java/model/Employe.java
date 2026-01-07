package model;

public class Employe {

    // Champs hérités de user (maintenant directs)
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String departement;
    private String role;

    // Champs spécifiques à Employe
    private int id;
    private String position;
    private String telephone;
    private String adresse;

    public Employe(int id, String nom, String prenom, String email, String password, String departement,
            String position, String telephone, String adresse) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.departement = departement;
        this.position = position;
        this.telephone = telephone;
        this.adresse = adresse;
        this.role = "EMPLOYE"; // Valeur par défaut
    }

    // Constructor for backward compatibility
    public Employe(int id, String nom, String prenom, String email, String password, String departement,
            String position) {
        this(id, nom, prenom, email, password, departement, position, "", "");
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getDepartement() {
        return departement;
    }

    public String getRole() {
        return role;
    }

    public String getPosition() {
        return position;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    // Setters
    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    @Override
    public String toString() {
        return id + "," + nom + "," + prenom + "," + email + "," + password + "," + departement
                + "," + position + "," + telephone + "," + adresse;
    }

    public static Employe fromCSV(String line) {
        String[] data = line.split(",");
        // Handle potential missing fields in old CSVs
        String tel = data.length > 7 ? data[7] : "";
        String adr = data.length > 8 ? data[8] : "";

        return new Employe(
                Integer.parseInt(data[0]),
                data[1],
                data[2],
                data[3],
                data[4],
                data[5],
                data[6],
                tel,
                adr);
    }
}