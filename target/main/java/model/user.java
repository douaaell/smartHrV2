package model;

public class user {
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String departement;
    private String role;

public user (String nom , String prenom , String email , String password, String departement ,String role){
    this.nom=nom;
    this.prenom=prenom;
    this.email=email;
    this.password=password;
    this.departement =departement ;
    this.role=role;
} 
    // getters 
    public String getNom() {
    return nom;
}
    public String getPrenom(){
        return prenom;
    }
    public String getEmail(){
        return email;
    }
    public String getPassword(){
        return password;
    }
    public String getRole(){
        return role;
    }
    public String getDepartement(){
        return departement ;
    }

   //Setters 
   public void setNom(String nom){
    this.nom =nom;
   }
   public void setPrenom(String prenom){
    this.prenom=prenom;
   }
   public void setEmail(String email){
    this.email =email;
   }
   public void setRole (String role){
    this.role = role;
   }
   public void setDepartement (String departement){
    this.departement =departement;
   }
}
