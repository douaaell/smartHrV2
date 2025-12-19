package service;

import dao.DocumentDAO;
import model.Document;
import csv.DocumentCSV;
import java.util.List;
import java.util.Optional;

public class DocumentServiceImpl implements DocumentService {
    private final DocumentDAO documentDao;

    public DocumentServiceImpl() {
        this.documentDao = new DocumentDAO();
    }

    @Override
    public Document saveDocument(Document document) {
        if (document.getId() == 0) {
            // Nouveau document
            documentDao.save(document);
        } else {
            // Mise à jour d'un document existant
            documentDao.update(document);
        }
        return document;
    }

    @Override
    public Optional<Document> findDocumentById(int id) {
        return Optional.ofNullable(documentDao.findById(id));
    }

    @Override
    public List<Document> findDocumentsByEmployeeEmail(String email) {
        return documentDao.findByEmployeeEmail(email);
    }

    @Override
    public List<Document> findAllDocuments() {
        return documentDao.findAll();
    }

    @Override
    public void updateDocument(Document document) {
        documentDao.update(document);
    }

    @Override
    public void deleteDocument(int id) {
        documentDao.delete(id);
    }

    @Override
    public void exportToCsv(String filePath) {
        documentDao.exportToCsv(filePath);
    }

    @Override
    public List<Document> importFromCsv(String filePath) {
        return documentDao.importFromCsv(filePath);
    }
}