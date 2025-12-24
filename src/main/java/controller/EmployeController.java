package controller;

import model.Employe;
import service.EmployeService;

import java.util.List;
import java.util.Scanner;

public class EmployeController {

    private final EmployeService service = new EmployeService();
    private final Scanner sc = new Scanner(System.in);

    public void menuRH() {
        int choix;
        do {
            System.out.println("\n=== Menu RH ===");
            System.out.println("1. Ajouter un employé");
            System.out.println("2. Modifier un employé");
            System.out.println("3. Supprimer un employé");
            System.out.println("4. Lister tous les employés");
            System.out.println("5. Rechercher un employé par nom");
            System.out.println("0. Quitter");
            System.out.print("Choix : ");
            choix = sc.nextInt();
            sc.nextLine(); // nettoyer le buffer

            switch (choix) {
                case 1 -> ajouter();
                case 2 -> modifier();
                case 3 -> supprimer();
                case 4 -> lister();
                case 5 -> rechercher();
            }

        } while (choix != 0);
    }

    private void ajouter() {
        System.out.print("ID : "); int id = sc.nextInt(); sc.nextLine();
        System.out.print("Nom : "); String nom = sc.nextLine();
        System.out.print("Prenom : "); String prenom = sc.nextLine();
        System.out.print("Email : "); String email = sc.nextLine();
        System.out.print("Password : "); String password = sc.nextLine();
        System.out.print("Departement : "); String dep = sc.nextLine();
        System.out.print("Position : "); String pos = sc.nextLine();

        Employe e = new Employe(id, nom, prenom, email, password, dep, pos);
        service.ajouterEmploye(e);
        System.out.println("✅ Employé ajouté !");
    }

    private void modifier() {
        System.out.print("ID à modifier : "); int id = sc.nextInt(); sc.nextLine();
        System.out.print("Nouveau nom : "); String nom = sc.nextLine();
        System.out.print("Nouveau prenom : "); String prenom = sc.nextLine();
        System.out.print("Nouveau email : "); String email = sc.nextLine();
        System.out.print("Nouveau password : "); String password = sc.nextLine();
        System.out.print("Nouveau departement : "); String dep = sc.nextLine();
        System.out.print("Nouvelle position : "); String pos = sc.nextLine();

        Employe e = new Employe(id, nom, prenom, email, password, dep, pos);
        service.modifierEmploye(e);
        System.out.println("✅ Employé modifié !");
    }

    private void supprimer() {
        System.out.print("ID à supprimer : "); int id = sc.nextInt(); sc.nextLine();
        service.supprimerEmploye(id);
        System.out.println("✅ Employé supprimé !");
    }

    private void lister() {
        List<Employe> list = service.listerEmployes();
        list.forEach(System.out::println);
    }

    private void rechercher() {
        System.out.print("Nom à rechercher : "); String nom = sc.nextLine();
        List<Employe> res = service.rechercherParNom(nom);
        res.forEach(System.out::println);
    }
}
