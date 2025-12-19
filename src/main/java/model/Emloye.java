package model;

public class Employe extends user {

    private int id;
    private String position;

    public Employe(int id, String nom, String prenom, String email, String password, String departement, String position) {
        super(nom, prenom, email, password, departement, "EMPLOYE");
        this.id = id;
        this.position = position;
    }

    public int getId() { return id; }
    public String getPosition() { return position; }

    public void setPosition(String position) { this.position = position; }

    @Override
    public String toString() {
        return id + "," + getNom() + "," + getPrenom() + "," + getEmail() + "," + getPassword() + "," + getDepartement() + "," + position;
    }
  // cette fonction from csv de type employe sert a convertir les ligne du fichier employe.csv en objet java pour faciliter l'utilisation des methode crud car java comprend juste les objet et ne comprend pas les ligne ecrite dans le fichier 
    public static Employe fromCSV(String line) {
        String[] data = line.split(",");// static ici veut dire Tu peux l’appeler sans créer un objet Employe.
        return new Employe(
            Integer.parseInt(data[0]),// on recupere les données du fichier sous forme de String et puis on convertit l'id on int 
            data[1], // nom
            data[2], // prenom
            data[3], // email
            data[4], // password
            data[5], // departement
            data[6]  // position
        );
    }
}