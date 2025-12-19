package csv;

import model.Conge;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CongeCSV {
    private static final String FILE_PATH = "conges.csv";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final Object fileLock = new Object();

    public void save(Conge conge) {
        executor.submit(() -> {
            synchronized (fileLock) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
                    writer.write(formatCongeLine(conge) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public List<Conge> load() throws IOException {
        List<Conge> loadedConges = new ArrayList<>();
        File file = new File(FILE_PATH);
        
        if (!file.exists()) {
            return loadedConges;
        }

        synchronized (fileLock) {
            try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
                String line;
                while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                    Conge conge = parseCongeLine(line);
                    if (conge != null) {
                        loadedConges.add(conge);
                    }
                }
            }
        }
        return loadedConges;
    }

    public void saveAll(List<Conge> conges) {
        executor.submit(() -> {
            synchronized (fileLock) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
                    for (Conge conge : conges) {
                        writer.write(formatCongeLine(conge) + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String formatCongeLine(Conge conge) {
        return String.format("%d;%d;%s;%s;%s;%s;%s",
            conge.getId(),
            conge.getEmployeeId(),
            conge.getType(),
            conge.getDateDebut() != null ? dateFormat.format(conge.getDateDebut()) : "",
            conge.getDateFin() != null ? dateFormat.format(conge.getDateFin()) : "",
            conge.getStatut() != null ? conge.getStatut() : "En attente",
            conge.getMotif() != null ? conge.getMotif() : ""
        );
    }

    private Conge parseCongeLine(String line) {
        try {
            String[] parts = line.split(";", -1);
            if (parts.length >= 6) {
                int id = Integer.parseInt(parts[0]);
                int employeeId = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                String type = parts.length > 2 ? parts[2] : "";
                
                Date dateDebut = null;
                Date dateFin = null;
                
                if (parts.length > 3 && !parts[3].isEmpty()) {
                    dateDebut = dateFormat.parse(parts[3]);
                }
                if (parts.length > 4 && !parts[4].isEmpty()) {
                    dateFin = dateFormat.parse(parts[4]);
                }
                
                String statut = parts.length > 5 ? parts[5] : "En attente";
                String motif = parts.length > 6 ? parts[6] : "";
                
                return new Conge(id, employeeId, type, dateDebut, dateFin, statut, motif);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture d'une ligne de congé: " + e.getMessage());
        }
        return null;
    }
}