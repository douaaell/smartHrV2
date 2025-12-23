package csv;

import model.Document;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DocumentCSV {
    private static final String CSV_SEPARATOR = ";";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    private static final Object exportLock = new Object();
    private static final Object importLock = new Object();

    public static Future<Void> exportToCsv(List<Document> documents, String filePath) {
        return executor.submit(() -> {
            synchronized (exportLock) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    writer.write("ID" + CSV_SEPARATOR + "Nom" + CSV_SEPARATOR + "Type" + CSV_SEPARATOR 
                               + "Chemin" + CSV_SEPARATOR + "Date Upload" + CSV_SEPARATOR + "Email Employé\n");
                    
                    for (Document doc : documents) {
                        writer.write(escapeCsv(String.valueOf(doc.getId())) + CSV_SEPARATOR);
                        writer.write(escapeCsv(doc.getNom()) + CSV_SEPARATOR);
                        writer.write(escapeCsv(doc.getType()) + CSV_SEPARATOR);
                        writer.write(escapeCsv(doc.getChemin()) + CSV_SEPARATOR);
                        writer.write(escapeCsv(DATE_FORMAT.format(doc.getDateUpload())) + CSV_SEPARATOR);
                        writer.write(escapeCsv(doc.getEmployeeEmail()) + "\n");
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Erreur lors de l'export CSV des documents: " + e.getMessage(), e);
                }
                return null;
            }
        });
    }

    public static Future<List<Document>> importFromCsv(String filePath) {
        return executor.submit(() -> {
            synchronized (importLock) {
                List<Document> documents = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                    br.readLine(); // Skip header
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(CSV_SEPARATOR, -1);
                        if (data.length >= 6) {
                            Document doc = new Document();
                            doc.setId(Integer.parseInt(data[0].trim()));
                            doc.setNom(data[1].trim());
                            doc.setType(data[2].trim());
                            doc.setChemin(data[3].trim());
                            doc.setDateUpload(DATE_FORMAT.parse(data[4].trim()));
                            doc.setEmployeeEmail(data[5].trim());
                            documents.add(doc);
                        }
                    }
                    return documents;
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