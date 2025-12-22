package model;

import java.util.Date;

public class Contract {
    private int id;
    private String employeeEmail;
    private String type;
    private Date startDate;
    private Date endDate;
    private String pdfFileName;

    // Constructeur par défaut
    public Contract() {
    }

    // Constructeur avec paramètres
    public Contract(int id, String employeeEmail, String type, Date startDate, Date endDate, String pdfFileName) {
        this.id = id;
        this.employeeEmail = employeeEmail;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pdfFileName = pdfFileName;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getPdfFileName() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }
}