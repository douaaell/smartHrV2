package csv;

import model.Conge;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CongeCSV {
    private static final String CSV_SEPARATOR = ";";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    private static final Object exportLock = new Object();
    private static final Object importLock = new Object();

    public static Future<Void> exportToCsv(List<Conge> conges, String filePath) {
        return executor.submit(() -> {
            synchronized (exportLock) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    writer.write("ID" + CSV_SEPARATOR + "ID Employé" + CSV_SEPARATOR + "Type" + CSV_SEPARATOR 
                               + "Date Début" + CSV_SEPARATOR + "Date Fin" + CSV_SEPARATOR 
                               + "Statut" + CSV_SEPARATOR + "Motif\n");
                    
                    for (Conge conge : conges) {
                        writer.write(escapeCsv(String.valueOf(conge.getId())) + CSV_SEPARATOR);
                        writer.write(escapeCsv(String.valueOf(conge.getEmployeeId())) + CSV_SEPARATOR);
                        writer.write(escapeCsv(conge.getType().name()) + CSV_SEPARATOR);
                        writer.write(escapeCsv(DATE_FORMAT.format(conge.getDateDebut())) + CSV_SEPARATOR);
                        writer.write(escapeCsv(DATE_FORMAT.format(conge.getDateFin())) + CSV_SEPARATOR);
                        writer.write(escapeCsv(conge.getStatut().name()) + CSV_SEPARATOR);
                        writer.write(escapeCsv(conge.getMotif() == null ? "" : conge.getMotif()) + "\n");
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Erreur lors de l'export CSV des congés: " + e.getMessage(), e);
                }
                return null;
            }
        });
    }

    public static Future<List<Conge>> importFromCsv(String filePath) {
        return executor.submit(() -> {
            synchronized (importLock) {
                List<Conge> conges = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                    br.readLine(); // Skip header
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(CSV_SEPARATOR, -1);
                        if (data.length >= 7) {
                            Conge conge = new Conge();
                            conge.setId(Integer.parseInt(data[0].trim()));
                            conge.setEmployeeId(Integer.parseInt(data[1].trim()));
                            conge.setType(Conge.TypeConge.valueOf(data[2].trim()));
                            conge.setDateDebut(DATE_FORMAT.parse(data[3].trim()));
                            conge.setDateFin(DATE_FORMAT.parse(data[4].trim()));
                            conge.setStatut(Conge.StatutConge.valueOf(data[5].trim()));
                            conge.setMotif(data[6].trim().isEmpty() ? null : data[6].trim());
                            conges.add(conge);
                        }
                    }
                    return conges;
                } catch (Exception e) {
                    throw new RuntimeException("Erreur lors de la lecture du fichier CSV: " + e.getMessage(), e);
                }
            }
        });
    }

    private static String escapeCsv(String input) {
        if (input == null) return "";
        return "\"" + input.replace("\"", "\"\"") + "\"";
    }

    public static void shutdown() {
        executor.shutdown();
    }
}