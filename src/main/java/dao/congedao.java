package dao;

import csv.CongeCSV;
import model.Conge;
import java.io.IOException;
import java.util.List;

public class CongeDAO {
    private final CongeCSV congeCSV;

    public CongeDAO() {
        this.congeCSV = new CongeCSV();
    }

    public void save(Conge conge) {
        congeCSV.save(conge);
    }

    public List<Conge> findAll() throws IOException {
        return congeCSV.load();
    }

    public void updateAll(List<Conge> conges) {
        congeCSV.saveAll(conges);
    }
}