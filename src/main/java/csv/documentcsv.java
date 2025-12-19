package csv;

import model.Document;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class DocumentCSV {
    private static final String FILE_PATH = "documents.csv";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final Object fileLock = new Object();

    public void save(Document document) {
        executor.submit(() -> {
            synchronized (fileLock) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
                    writer.write(formatDocumentLine(document) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public List<Document> load() throws IOException {
        List<Document> loadedDocs = new ArrayList<>();
        File file = new File(FILE_PATH);
        
        if (!file.exists()) {
            return loadedDocs;
        }

        synchronized (fileLock) {
            try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
                String line;
                while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                    Document doc = parseDocumentLine(line);
                    if (doc != null) {
                        loadedDocs.add(doc);
                    }
                }
            }
        }
        return loadedDocs;
    }

    public void saveAll(List<Document> documents) {
        executor.submit(() -> {
            synchronized (fileLock) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
                    for (Document doc : documents) {
                        writer.write(formatDocumentLine(doc) + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String formatDocumentLine(Document doc) {
        return String.format("%d;%s;%s;%s;%s;%s",
            doc.getId(),
            doc.getNom() != null ? doc.getNom().replace(";", ",") : "",
            doc.getType() != null ? doc.getType().replace(";", ",") : "",
            doc.getChemin() != null ? doc.getChemin().replace(";", ",") : "",
            doc.getDateUpload() != null ? dateFormat.format(doc.getDateUpload()) : "",
            doc.getEmployeeEmail() != null ? doc.getEmployeeEmail().replace(";", ",") : ""
        );
    }

    private Document parseDocumentLine(String line) {
        try {
            String[] parts = line.split(";", -1);
            if (parts.length >= 4) {
                int id = Integer.parseInt(parts[0]);
                String nom = parts[1];
                String type = parts[2];
                String chemin = parts[3];
                
                Date dateUpload = null;
                if (parts.length > 4 && !parts[4].isEmpty()) {
                    dateUpload = dateFormat.parse(parts[4]);
                }
                
                String employeeEmail = parts.length > 5 ? parts[5] : "";
                
                return new Document(id, nom, type, chemin, dateUpload, employeeEmail);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture d'une ligne de document: " + e.getMessage());
        }
        return null;
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}