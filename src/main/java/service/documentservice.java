package service;

import dao.DocumentDAO;
import model.Document;
import java.util.List;
import java.util.Optional;

public class DocumentService implements DocumentService {
    private final DocumentDAO documentDao;

    public DocumentService() {
        this.documentDao = new DocumentDAO();
    }

    @Override
    public Document saveDocument(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Le document ne peut pas être null");
        }

        if (document.getId() == 0) {
            return documentDao.save(document);
        } else {
            documentDao.update(document);
            return document;
        }
    }

    @Override
    public Optional<Document> findDocumentById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de document invalide");
        }
        return Optional.ofNullable(documentDao.findById(id));
    }

    @Override
    public List<Document> findDocumentsByEmployeeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email ne peut pas être vide");
        }
        return documentDao.findByEmployeeEmail(email);
    }

    @Override
    public List<Document> findAllDocuments() {
        return documentDao.findAll();
    }

    @Override
    public void updateDocument(Document document) {
        if (document == null || document.getId() == 0) {
            throw new IllegalArgumentException("Document invalide pour la mise à jour");
        }
        documentDao.update(document);
    }

    @Override
    public void deleteDocument(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de document invalide");
        }
        documentDao.delete(id);
    }

    @Override
    public void exportToCsv(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin du fichier ne peut pas être vide");
        }
        documentDao.exportToCsv(filePath);
    }

    @Override
    public List<Document> importFromCsv(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin du fichier ne peut pas être vide");
        }
        List<Document> documents = documentDao.importFromCsv(filePath);
        documents.forEach(this::saveDocument);
        return documents;
    }
}