/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import stamboom.domain.*;
import stamboom.storage.DatabaseMediator;
import stamboom.storage.IStorageMediator;
import stamboom.storage.SerializationMediator;

public class StamboomController {

    private Administratie admin;
    private IStorageMediator storageMediator;

    /**
     * creatie van stamboomcontroller met lege administratie en onbekend
     * opslagmedium
     */
    public StamboomController() {
        admin = new Administratie();
        storageMediator = null;
    }

    public Administratie getAdministratie() {
        return admin;
    }

    /**
     * administratie wordt leeggemaakt (geen personen en geen gezinnen)
     */
    public void clearAdministratie() {
        admin = new Administratie();
    }

    /**
     * administratie wordt in geserialiseerd bestand opgeslagen
     *
     * @param bestand
     * @throws IOException
     */

    public void serialize(File bestand) throws IOException {
        //todo opgave 2
        Properties serProps = new Properties();
       
        serProps.put("file", bestand);
        
        storageMediator = new SerializationMediator();
        storageMediator.configure(serProps);
        
        storageMediator.save(admin);
    }

    /**
     * administratie wordt vanuit geserialiseerd bestand gevuld
     *
     * @param bestand
     * @throws IOException
     */
    public void deserialize(File bestand) throws IOException {
        //todo opgave 2       
        storageMediator = new SerializationMediator();
        Properties loadedProps = new Properties();
        loadedProps.put("file", bestand);
        storageMediator.configure(loadedProps);
        
        admin = storageMediator.load();
    }
    
    // opgave 4
    private void initDatabaseMedium() throws IOException {
        if (!(storageMediator instanceof DatabaseMediator)) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream("database.properties")) {
                props.load(in);
            }
            storageMediator = new DatabaseMediator(props);
        }
    }
    
    /**
     * administratie wordt vanuit standaarddatabase opgehaald
     *
     * @throws IOException
     */
    public void loadFromDatabase() throws IOException {
        initDatabaseMedium();
        admin = storageMediator.load();
    }

    /**
     * administratie wordt in standaarddatabase bewaard
     *
     * @throws IOException
     */
    public void saveToDatabase() throws IOException {
        initDatabaseMedium();
        storageMediator.save(admin);
    }
}
