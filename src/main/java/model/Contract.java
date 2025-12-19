package model;

public class Contract {
    private String id;
    private String employeeEmail;
    private String type;
    private String startDate;
    private String endDate;
    private String fileNamePDF;

    // Constructeur
    public Contract(String id, String employeeEmail, String type, String startDate, String endDate, String fileNamePDF) {
        this.id = id;
        this.employeeEmail = employeeEmail;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fileNamePDF = fileNamePDF;
    }

    // Getters
    public String getId() { return id; }
    public String getEmployeeEmail() { return employeeEmail; }
    public String getType() { return type; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getFileNamePDF() { return fileNamePDF; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }
    public void setType(String type) { this.type = type; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setFileNamePDF(String fileNamePDF) { this.fileNamePDF = fileNamePDF; }
}
 
public static Contract fromCSV(String line) {
    String[] data = line.split(",");
    return new Contract(
        data[0], // id
        data[1], // employeeEmail
        data[2], // type
        data[3], // startDate
        data[4], // endDate
        data[5]  // fileNamePDF
    );
}