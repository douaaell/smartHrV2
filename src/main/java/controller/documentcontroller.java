package controller;

import model.Document;
import service.DocumentService;
import java.util.List;

public class DocumentController {
    private final DocumentService documentService;

    public DocumentController() {
        this.documentService = new DocumentService();
    }

    public Document ajouterDocument(Document document) {
        return documentService.saveDocument(document);
    }

    public Document obtenirDocument(int id) {
        return documentService.findDocumentById(id)
            .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'ID: " + id));
    }

    public List<Document> listerDocuments() {
        return documentService.findAllDocuments();
    }

    public List<Document> listerDocumentsParEmploye(String email) {
        return documentService.findDocumentsByEmployeeEmail(email);
    }

    public void mettreAJourDocument(Document document) {
        documentService.updateDocument(document);
    }

    public void supprimerDocument(int id) {
        documentService.deleteDocument(id);
    }

    public void exporterVersCsv(String cheminFichier) {
        documentService.exportToCsv(cheminFichier);
    }

    public List<Document> importerDepuisCsv(String cheminFichier) {
        return documentService.importFromCsv(cheminFichier);
    }
}