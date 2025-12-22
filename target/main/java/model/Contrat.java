package model;

import java.time.LocalDate;

public class Contrat {

    private String id;
    private String employeeEmail;
    private String type; // CDD, CDI, intérim
    private LocalDate startDate;
    private LocalDate endDate;
    private double salary;
    private String position;
    private String department;
    private String benefits;
    private String status; // ACTIF, SUSPENDU, TERMINE
    private String fileNamePdf;

    public Contrat(String id, String employeeEmail, String type, LocalDate startDate, LocalDate endDate,
                   double salary, String position, String department, String benefits, String status, String fileNamePdf) {
        this.id = id;
        this.employeeEmail = employeeEmail;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.salary = salary;
        this.position = position;
        this.department = department;
        this.benefits = benefits;
        this.status = status;
        this.fileNamePdf = fileNamePdf;
    }

    // Getters et Setters
    public String getId() { return id; }
    public String getEmployeeEmail() { return employeeEmail; }
    public String getType() { return type; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getSalary() { return salary; }
    public String getPosition() { return position; }
    public String getDepartment() { return department; }
    public String getBenefits() { return benefits; }
    public String getStatus() { return status; }
    public String getFileNamePdf() { return fileNamePdf; }

    public void setType(String type) { this.type = type; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setSalary(double salary) { this.salary = salary; }
    public void setPosition(String position) { this.position = position; }
    public void setDepartment(String department) { this.department = department; }
    public void setBenefits(String benefits) { this.benefits = benefits; }
    public void setStatus(String status) { this.status = status; }
    public void setFileNamePdf(String fileNamePdf) { this.fileNamePdf = fileNamePdf; }

    @Override
    public String toString() {
        return id + "," + employeeEmail + "," + type + "," + startDate + "," + endDate + "," + salary + "," +
               position + "," + department + "," + benefits + "," + status + "," + fileNamePdf;
    }
}
