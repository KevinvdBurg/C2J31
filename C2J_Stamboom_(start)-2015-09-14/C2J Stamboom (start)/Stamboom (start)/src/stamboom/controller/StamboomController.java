/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import stamboom.domain.Administratie;
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
        
        try {
            FileOutputStream file = new FileOutputStream(bestand); 
            OutputStream buffer = new BufferedOutputStream(file); 
            ObjectOutput output = new ObjectOutputStream(buffer);
            output.writeObject(admin);
            output.close();
            file.close();
            System.out.printf("File is saved as: admin.ser");
            
        } 
        catch(IOException ex){
           System.out.printf("Cannot perform output.", ex.toString());
        }
        
        
        
        //ERIC TO THE RESCUE!
        
    }

    /**
     * administratie wordt vanuit geserialiseerd bestand gevuld
     *
     * @param bestand
     * @throws IOException
     */
    public void deserialize(File bestand) throws IOException {        
        try {
            FileInputStream file = new FileInputStream(bestand); 
            ObjectInputStream input = new ObjectInputStream(file);
            admin = (Administratie) input.readObject();
            input.close();
            file.close();
        } 
        catch(IOException ex){
           System.out.printf("Cannot perform output.", ex.toString());
           
        } 
        catch (ClassNotFoundException ex) {
            Logger.getLogger(SerializationMediator.class.getName()).log(Level.SEVERE, null, ex);
            System.out.printf("Class not found");
        }
    }
    
    // opgave 4
    private void initDatabaseMedium() throws IOException {
//        if (!(storageMediator instanceof DatabaseMediator)) {
//            Properties props = new Properties();
//            try (FileInputStream in = new FileInputStream("database.properties")) {
//                props.load(in);
//            }
//            storageMediator = new DatabaseMediator(props);
//        }
    }
    
    /**
     * administratie wordt vanuit standaarddatabase opgehaald
     *
     * @throws IOException
     */
    public void loadFromDatabase() throws IOException {
        //todo opgave 4
    }

    /**
     * administratie wordt in standaarddatabase bewaard
     *
     * @throws IOException
     */
    public void saveToDatabase() throws IOException {
        //todo opgave 4
    }

}
