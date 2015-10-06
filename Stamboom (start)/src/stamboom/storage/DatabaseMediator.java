/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import stamboom.domain.Administratie;

public class DatabaseMediator implements IStorageMediator {

    private Properties props;
    private Connection conn;
    String result = "";
    InputStream inputStream;
    

    @Override
    public Administratie load() throws IOException {
        props = getPropValues(); 
        configure(props);
        return null;
    }

    @Override
    public void save(Administratie admin) throws IOException {
        //todo opgave 4     
    }

    /**
     * Laadt de instellingen, in de vorm van een Properties bestand, en controleert
     * of deze in de correcte vorm is, en er verbinding gemaakt kan worden met
     * de database.
     * @param props
     * @return
     */
    @Override
    public final boolean configure(Properties props) {
        this.props = props;
        if (!isCorrectlyConfigured()) {
            System.err.println("props mist een of meer keys");
            return false;
        }

        try {
            initConnection();
            return true;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            this.props = null;
            return false;
        } finally {
            closeConnection();
        }
    }

    @Override
    public Properties config() {
        return props;
    }

    @Override
    public boolean isCorrectlyConfigured() {
        if (props == null) {
            return false;
        }
        if (!props.containsKey("driver")) {
            return false;
        }
        if (!props.containsKey("url")) {
            return false;
        }
        if (!props.containsKey("username")) {
            return false;
        }
        if (!props.containsKey("password")) {
            return false;
        }
        return true;
    }

    private void initConnection() throws SQLException {
        String driver = props.getProperty("driver");
        String url = props.getProperty("url");
        String username = props.getProperty("username");
        String password = props.getProperty("password");
        
        System.out.print(driver);
        try {
            
            
            
            
        } catch (Exception e) {
            System.out.print(e);
        }
    }

    private void closeConnection() {
        try {
            conn.close();
            conn = null;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
   public Properties getPropValues() throws IOException {
           Properties newProp = null;     
            try {
                    String propFileName = "database.properties";
                    inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

                    if (inputStream != null) 
                    {
                        newProp.load(inputStream);
                    } 
                    else 
                    {
                        throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
                    }

		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
            return newProp;
	}
}
