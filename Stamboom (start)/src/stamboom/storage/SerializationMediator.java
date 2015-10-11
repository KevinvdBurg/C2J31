/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import stamboom.domain.Administratie;

public class SerializationMediator implements IStorageMediator {

    private Properties props;

    /**
     * creation of a non configured serialization mediator
     */
    public SerializationMediator() {
        props = null;
    }

    @Override
    public Administratie load() throws IOException {
        if (!isCorrectlyConfigured()) {
            throw new RuntimeException("Serialization mediator isn't initialized correctly.");
        }
        
        // todo opgave 2
        Administratie loaded = null;
        
        FileInputStream f_in = new FileInputStream((File)props.get("file"));
        ObjectInputStream obj_in = new ObjectInputStream (f_in);
        //obj_in.close();
        
        try
        {
            Object obj = obj_in.readObject();
            
            if (obj != null) 
            {
                loaded = (Administratie)obj;
            }
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Class not found.");
        }
        f_in.close();
        return loaded;
    }

    @Override
    public void save(Administratie admin) throws IOException {
        if (!isCorrectlyConfigured()) {
            throw new RuntimeException("Serialization mediator isn't initialized correctly.");
        }

        // todo opgave 2
        Object obj= props.get("file");
        File file;
        if (obj instanceof File) {
            file=(File)obj; 
            FileOutputStream f_out = new FileOutputStream(file);
            ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
            obj_out.writeObject(admin);
            //obj_out.close();
            f_out.close();
        }
        //File file=props.getProperty("file");
  
    }

    @Override
    public boolean configure(Properties props) {
        this.props = props;
        return isCorrectlyConfigured();
    }

    @Override
    public Properties config() {
        return props;
    }

    /**
     *
     * @return true if config() contains at least a key "file" and the
     * corresponding value is a File-object, otherwise false
     */
    @Override
    public boolean isCorrectlyConfigured() {
        if (props == null) {
            return false;
        }
        if (props.containsKey("file")) {
            return props.get("file") instanceof File;
        } else {
            return false;
        }
    }
}
