package controller;

import model.Contrat;
import service.ContratService;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ContratController {

    private final ContratService service = new ContratService();
    private final Scanner sc = new Scanner(System.in);

    public void menuRH() {
        int choix;
        do {
            System.out.println("\n=== Menu Contrats RH ===");
            System.out.println("1. Ajouter un contrat");
            System.out.println("2. Modifier un contrat");
            System.out.println("3. Supprimer un contrat");
            System.out.println("4. Lister tous les contrats");
            System.out.println("5. Filtrer par employé");
            System.out.println("6. Contrats fin proche (alerte)");
            System.out.println("0. Quitter");
            System.out.print("Choix : ");
            choix = sc.nextInt();
            sc.nextLine();

            switch (choix) {
                case 1 -> ajouter();
                case 2 -> modifier();
                case 3 -> supprimer();
                case 4 -> lister();
                case 5 -> filtrerEmploye();
                case 6 -> contratsFinProche();
            }
        } while (choix != 0);
    }

    private void ajouter() {
        System.out.print("ID : "); String id = sc.nextLine();
        System.out.print("Email employé : "); String email = sc.nextLine();
        System.out.print("Type de contrat : "); String type = sc.nextLine();
        System.out.print("Date début (YYYY-MM-DD) : "); LocalDate start = LocalDate.parse(sc.nextLine());
        System.out.print("Date fin (YYYY-MM-DD) : "); LocalDate end = LocalDate.parse(sc.nextLine());
        System.out.print("Salaire : "); double salary = sc.nextDouble(); sc.nextLine();
        System.out.print("Poste : "); String pos = sc.nextLine();
        System.out.print("Département : "); String dep = sc.nextLine();
        System.out.print("Avantages : "); String benefits = sc.nextLine();
        System.out.print("Statut : "); String status = sc.nextLine();
        System.out.print("Nom fichier PDF : "); String pdf = sc.nextLine();

        Contrat c = new Contrat(id, email, type, start, end, salary, pos, dep, benefits, status, pdf);
        service.ajouterContrat(c);
        System.out.println("✅ Contrat ajouté !");
    }

    private void modifier() {
        System.out.print("ID du contrat à modifier : "); String id = sc.nextLine();
        System.out.print("Nouveau type : "); String type = sc.nextLine();
        System.out.print("Nouvelle date fin (YYYY-MM-DD) : "); LocalDate end = LocalDate.parse(sc.nextLine());
        System.out.print("Nouveau salaire : "); double salary = sc.nextDouble(); sc.nextLine();
        System.out.print("Nouveau poste : "); String pos = sc.nextLine();
        System.out.print("Nouveau département : "); String dep = sc.nextLine();
        System.out.print("Nouveaux avantages : "); String benefits = sc.nextLine();
        System.out.print("Nouveau statut : "); String status = sc.nextLine();
        System.out.print("Nom fichier PDF : "); String pdf = sc.nextLine();

        Contrat c = new Contrat(id, "", type, null, end, salary, pos, dep, benefits, status, pdf);
        service.modifierContrat(c);
        System.out.println("✅ Contrat modifié !");
    }

    private void supprimer() {
        System.out.print("ID du contrat à supprimer : "); String id = sc.nextLine();
        service.supprimerContrat(id);
        System.out.println("✅ Contrat supprimé !");
    }

    private void lister() {
        List<Contrat> list = service.listerContrats();
        list.forEach(System.out::println);
    }

    private void filtrerEmploye() {
        System.out.print("Email employé : "); String email = sc.nextLine();
        List<Contrat> res = service.filtrerParEmploye(email);
        res.forEach(System.out::println);
    }

    private void contratsFinProche() {
        System.out.print("Nombre de jours pour alerte : "); int jours = sc.nextInt(); sc.nextLine();
        List<Contrat> res = service.contratsFinProche(jours);
        res.forEach(System.out::println);
    }
}
