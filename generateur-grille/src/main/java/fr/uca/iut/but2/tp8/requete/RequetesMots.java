package fr.uca.iut.but2.tp8.requete;

public interface RequetesMots {
    String[] languesDiponibles();
    String obtenirUnMot(String langue, int longueur);

    void setUrl(String url);

}
