package fr.uca.iut.but2.tp8.verification;

public class Abonnement {
    private String id;
    private String service;

    public Abonnement() {

    }

    public Abonnement(String id, String service) {
        setId(id);
        setService(service);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
