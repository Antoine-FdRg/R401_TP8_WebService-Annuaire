package fr.uca.iut.but2.tp8.mots.dico;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;

public class DicoImportExport {

    private ObjectMapper mapper;
    private Dictionnary dictionnary;

    public DicoImportExport() {
        setMapper(new ObjectMapper());
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        setDico(new Dictionnary());
    }

    /**
     * pour enregistrer le dictionnaire courant dans un fichier json
     * @param filename
     */
    public void export(String filename) {
        try {

            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), dictionnary);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * pour charger un fichier texte (un mot par ligne)
     * @param stream le flux du fichier texte
     * @throws IOException
     */
    public void load(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {
            while (reader.ready()) {
                String line = reader.readLine();
                if (! dictionnary.addWord(line)) System.out.println("mot rejeté : "+line);
            }
            reader.close();
        }
        catch (IOException e) {
            // pour mise au point // e.printStackTrace();
            dictionnary = new Dictionnary();
            reader.close();
        }
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
        this.mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public void setDico(Dictionnary dico) {
        this.dictionnary = dico;
    }

    public Dictionnary getDico() {
        return dictionnary;
    }
}
