package service;

import dao.EmployeDAO;
import model.Employe;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class EmployeService {

    private final EmployeDAO dao = new EmployeDAO();

    // Pool de threads
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    // 🔒 Synchronisation sur l'objet service
    public synchronized void ajouterEmploye(Employe e) {
        executor.execute(() -> {
            dao.addEmploye(e);
        });
    }

    public synchronized void modifierEmploye(Employe e) {
        executor.execute(() -> {
            dao.updateEmploye(e);
        });
    }

    public synchronized void supprimerEmploye(int id) {
        executor.execute(() -> {
            dao.deleteEmploye(id);
        });
    }

    // 👇 PAS besoin de synchronized (lecture seulement)
    public List<Employe> listerEmployes() {
        return dao.getAllEmployes();
    }

    // 👇 Streams = thread-safe ici car liste locale
    public List<Employe> rechercherParNom(String nom) {
        return dao.getAllEmployes()
                .stream()
                .filter(e -> e.getNom().equalsIgnoreCase(nom))
                .collect(Collectors.toList());
    }

    // Fermeture propre du pool
    public void shutdown() {
        executor.shutdown();
    }
}
