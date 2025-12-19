package dao;

import csv.DocumentCSV;
import model.Document;
import java.io.IOException;
import java.util.List;

public class DocumentDAO {
    private final DocumentCSV documentCSV;

    public DocumentDAO() {
        this.documentCSV = new DocumentCSV();
    }

    public void save(Document document) {
        documentCSV.save(document);
    }

    public List<Document> findAll() throws IOException {
        return documentCSV.load();
    }

    public void updateAll(List<Document> documents) {
        documentCSV.saveAll(documents);
    }

    public void shutdown() {
        documentCSV.shutdown();
    }
}