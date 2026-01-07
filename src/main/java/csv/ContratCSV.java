package csv;

import model.Contrat;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ContratCSV {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CSV_HEADER = "ID,Employe,Type,Date Debut,Date Fin,Salaire,Poste,Departement,Statut,Avantages,Fichier PDF";

    public void exportToCsv(List<Contrat> contrats, String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Write header
            writer.write(CSV_HEADER);
            writer.newLine();

            // Write data
            for (Contrat contrat : contrats) {
                StringBuilder line = new StringBuilder();
                
                line.append(escapeCsvField(contrat.getId())).append(",");
                line.append(escapeCsvField(contrat.getEmployeeEmail())).append(",");
                line.append(escapeCsvField(contrat.getType())).append(",");
                line.append(escapeCsvField(contrat.getStartDate() != null ? 
                    contrat.getStartDate().format(DATE_FORMATTER) : "")).append(",");
                line.append(escapeCsvField(contrat.getEndDate() != null ? 
                    contrat.getEndDate().format(DATE_FORMATTER) : "")).append(",");
                line.append(escapeCsvField(String.valueOf(contrat.getSalary()))).append(",");
                line.append(escapeCsvField(contrat.getPosition())).append(",");
                line.append(escapeCsvField(contrat.getDepartment())).append(",");
                line.append(escapeCsvField(contrat.getStatus())).append(",");
                line.append(escapeCsvField(contrat.getBenefits())).append(",");
                line.append(escapeCsvField(contrat.getFileNamePdf() != null ? 
                    contrat.getFileNamePdf() : ""));
                
                writer.write(line.toString());
                writer.newLine();
            }

            System.out.println("✅ Export CSV réussi: " + contrats.size() + " contrats exportés vers " + filename);
        }
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        
        // Escape quotes and commas
        String escaped = field.replace("\"", "\"\"");
        
        // If field contains comma or quote, wrap in quotes
        if (escaped.contains(",") || escaped.contains("\"")) {
            escaped = "\"" + escaped + "\"";
        }
        
        return escaped;
    }
}
