package controller;

import model.Document;
import service.DocumentService;
import java.io.IOException;
import java.util.List;

public class DocumentController {
    private final DocumentService documentService;

    public DocumentController() {
        this.documentService = new DocumentService();
    }

    public void ajouterDocument(Document document) throws IOException {
        documentService.saveDocument(document);
    }

    public List<Document> listerDocuments() throws IOException {
        return documentService.loadDocuments();
    }

    public Document obtenirDocument(int id) throws IOException {
        return documentService.findDocumentById(id);
    }

    public boolean supprimerDocument(int id) throws IOException {
        return documentService.deleteDocument(id);
    }

    public List<Document> listerDocumentsParType(String type) throws IOException {
        return documentService.getDocumentsByType(type);
    }

    public List<Document> listerDocumentsEmploye(String email) throws IOException {
        return documentService.getDocumentsByEmployee(email);
    }

    public void mettreAJourDocument(Document document) throws IOException {
        documentService.updateDocument(document);
    }

    public void fermer() {
        documentService.shutdown();
    }
}