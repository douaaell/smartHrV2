package service;

import dao.DocumentDAO;
import model.Document;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentService {
    private final DocumentDAO documentDAO;

    public DocumentService() {
        this.documentDAO = new DocumentDAO();
    }

    public void saveDocument(Document document) throws IOException {
        if (document == null || document.getNom() == null || document.getType() == null) {
            throw new IllegalArgumentException("Le document et ses propriétés ne peuvent pas être nuls");
        }

        if (document.getId() == 0) {
            int newId = 1;
            List<Document> existingDocs = documentDAO.findAll();
            if (!existingDocs.isEmpty()) {
                newId = existingDocs.stream()
                        .mapToInt(Document::getId)
                        .max()
                        .orElse(0) + 1;
            }
            document.setId(newId);
        }

        if (document.getDateUpload() == null) {
            document.setDateUpload(new Date());
        }

        documentDAO.save(document);
    }

    public List<Document> loadDocuments() throws IOException {
        return documentDAO.findAll();
    }

    public Document findDocumentById(int id) throws IOException {
        return documentDAO.findAll().stream()
                .filter(doc -> doc.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public boolean deleteDocument(int id) throws IOException {
        List<Document> allDocuments = documentDAO.findAll();
        boolean removed = allDocuments.removeIf(doc -> doc.getId() == id);
        
        if (removed) {
            documentDAO.updateAll(allDocuments);
        }
        return removed;
    }

    public List<Document> getDocumentsByType(String type) throws IOException {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Le type ne peut pas être vide");
        }
        return documentDAO.findAll().stream()
                .filter(doc -> type.equalsIgnoreCase(doc.getType()))
                .collect(Collectors.toList());
    }

    public List<Document> getDocumentsByEmployee(String email) throws IOException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email ne peut pas être vide");
        }
        return documentDAO.findAll().stream()
                .filter(doc -> email.equalsIgnoreCase(doc.getEmployeeEmail()))
                .collect(Collectors.toList());
    }

    public void updateDocument(Document updatedDoc) throws IOException {
        List<Document> allDocuments = documentDAO.findAll();
        allDocuments = allDocuments.stream()
            .map(doc -> doc.getId() == updatedDoc.getId() ? updatedDoc : doc)
            .collect(Collectors.toList());
        documentDAO.updateAll(allDocuments);
    }

    public void shutdown() {
        documentDAO.shutdown();
    }
}