/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.storage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import stamboom.domain.*;

public class DatabaseMediator implements IStorageMediator {

    private Properties props;
    private Connection conn;
    private Statement statement;

    public DatabaseMediator(Properties props)
    {
        configure(props);
    }
    
    @Override
    public Administratie load() throws IOException {
        Administratie admin = new Administratie();
        
        ArrayList<String> personenMetOuder = new ArrayList<>();
        try {
            statement = conn.createStatement();
            String SQLPersonen = "Select * from `Personen`";
            
            ResultSet executeQuery = statement.executeQuery(SQLPersonen);
            
            while (executeQuery.next()) {
                String[] voornamen = executeQuery.getString(3).split(" ");
                
                String achternaam = executeQuery.getString(2);
                String tussenvoegsel = executeQuery.getString(4);
                
                Date birthDate = new SimpleDateFormat("dd-MM-yyyy").parse(executeQuery.getString(5));
                
                String geboorteplaats = executeQuery.getString(6);
                Geslacht geslacht;
                String sex = executeQuery.getString(7);
                if ("M".equals(sex.trim())) {
                    geslacht = Geslacht.MAN;
                }
                else{
                    geslacht = Geslacht.VROUW;
                }
                
                Gezin oudergezin = null;
                int OuderNummer = executeQuery.getInt(8);
                int PersoonNummer = executeQuery.getInt(1);
                
                if (OuderNummer != 0) {
                    String persoonMetOuder =  PersoonNummer +","+ OuderNummer;
                    personenMetOuder.add(persoonMetOuder);
                }

                admin.addPersoon(geslacht, voornamen, achternaam, tussenvoegsel, DateToCalendar(birthDate), geboorteplaats, oudergezin);
            }
            
        } catch (SQLException ex) {
            
        } catch (ParseException ex) {
            Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            statement = conn.createStatement();

            String SQLGezinnen = "Select * from `Gezinnen`";
            ResultSet executeQuery = statement.executeQuery(SQLGezinnen);
            
            while (executeQuery.next()) {
                Gezin g;
                if(executeQuery.getInt(2) > 0){
                    
                    Persoon ouder2 = null;
                    
                    if (executeQuery.getInt(3) == -1) {
                        ouder2 = admin.getPersoon(executeQuery.getInt(3));
                    }

                    admin.addOngehuwdGezin(admin.getPersoon(executeQuery.getInt(2)),ouder2);
                    
                    g = admin.getGezin(admin.aantalGeregistreerdeGezinnen());
                    
                    
                    if (!executeQuery.getString(4).isEmpty()) {
                        Date huwlijksDate = new SimpleDateFormat("dd-MM-yyyy").parse(executeQuery.getString(4));
                        g.setHuwelijk(DateToCalendar(huwlijksDate));
                    }
                    
                    if (!executeQuery.getString(5).isEmpty()) {
                        Date scheidingDate = new SimpleDateFormat("dd-MM-yyyy").parse(executeQuery.getString(5));
                        g.setScheiding(DateToCalendar(scheidingDate));
                    }
                }
            }
        } 
        catch (Exception e) {
        }
        
        for (String peroon : personenMetOuder) {
            
           int persnumb = Integer.parseInt(peroon.split(",")[0]); //First is Personnummer, Second is ParentNummer
           int gezinnumb = Integer.parseInt(peroon.split(",")[1]); 
           
           Persoon persoon = admin.getPersoon(persnumb);
           
           admin.setOuders(persoon, admin.getGezin(gezinnumb)); 
        }
        
       return admin; //check if class is correct! 
       
    }

    @Override
    public void save(Administratie admin) throws IOException {
        
        try
        {
            SimpleDateFormat format = new SimpleDateFormat("DD-MM-YYYY");
            
            for (Persoon huidig: admin.getPersonen()) 
            {                               
                
                statement = conn.createStatement();
                
                int nr = huidig.getNr();
                String Voornaam = huidig.getVoornamen();
                String Achternaam = huidig.getAchternaam();
                String Tussenvoegsel = huidig.getTussenvoegsel();
                
                if (Tussenvoegsel.isEmpty()) {
                    Tussenvoegsel = "";
                }
                
                String Datum = format.format(huidig.getGebDat().getTime());
                String GebPlaats = huidig.getGebPlaats();
                String Geslacht = huidig.getGeslacht().name().substring(0,1);
           
                String SQL = "INSERT INTO `Personen` (`persoonsNummer`,`achternaam`,`voornamen`,`tussenvoegsel`,`geboortedatum`,`geboorteplaats`,`geslacht`,`ouders`) VALUES ('"+nr+"','"+Achternaam+"','"+Voornaam+"','"+Tussenvoegsel+"','"+Datum+"','"+GebPlaats+"','"+Geslacht+"',NULL);";
                
                System.out.println("Query Persoon\r\n");
                System.out.println(SQL + "\r\n");
                statement.executeUpdate(SQL);
                
                System.out.println("Query Insterted");
            }
            
            for (Gezin huidig: admin.getGezinnen()) 
            {
                //prepStatement = conn.prepareStatement("INSERT INTO GEZINNEN VALUES (?,?,?,?,?)");
                statement = conn.createStatement();
                
                int nr = huidig.getNr();
                int ouder1nr;
                int ouder2nr;
                String huwelijk = "";
                String scheiding = "";
                
                ouder1nr = huidig.getOuder1().getNr();
                
                if(huidig.getOuder2() == null){
                    ouder2nr = -1;
                }
                else{
                    ouder2nr = huidig.getOuder2().getNr();
                }

                if (huidig.getHuwelijksdatum() != null) 
                {
                    huwelijk = format.format(huidig.getHuwelijksdatum().getTime());
                }
                
                if (huidig.getScheidingsdatum() != null) 
                {
                    scheiding = format.format(huidig.getScheidingsdatum().getTime());
                }
                
                String SQL = "INSERT INTO `Gezinnen`(`gezinsNummer`,`ouder1`,`ouder2`,`huwelijksdatum`,`scheidingsdatum`) VALUES ('"+nr+"','"+ouder1nr+"','"+ouder2nr+"','"+huwelijk+"','"+scheiding+"');";
                
                System.out.println("Query Gezin\r\n");
                System.out.println(SQL + "\r\n");
                statement.executeUpdate(SQL);
                System.out.println("Query Insterted");
            }
             
            for (Persoon huidig: admin.getPersonen()) 
            {
                if (huidig.getOuderlijkGezin() != null) 
                {
                    if (huidig.getOuderlijkGezin() != null) 
                    {
                        statement = conn.createStatement();
                        
                        //prepStatement = conn.prepareStatement("UPDATE PERSONEN SET OUDERS = ? WHERE PERSOONSNUMMER = ?");
                        
                        int ouderlijkGezin = huidig.getOuderlijkGezin().getNr();
                        int nr = huidig.getNr();
                        
                        String SQL = "UPDATE `Personen` SET `ouders`='"+ouderlijkGezin+"' WHERE `PersoonsNummer`= '"+nr+"' ;";
                        
                        System.out.println("Query Update Persoon\r\n");
                        System.out.println(SQL + "\r\n");
                        statement.executeUpdate(SQL);
                        System.out.println("Query Insterted");
                    }
                }
            }
            
            JOptionPane.showConfirmDialog(null, "Saving completed");
        }
        catch (SQLException ex)
        {
            System.out.print(ex + "\r\n");
            JOptionPane.showConfirmDialog(null, "Saving failed.");
        }
    }

    @Override
    public final boolean configure(Properties props) {
        this.props = props;

        try {
            initConnection();
            return isCorrectlyConfigured();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            this.props = null;
            return false;
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
        if (!props.containsKey("user")) {
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
        
        try
        {
            Class.forName(driver);
        }
        catch(ClassNotFoundException ex)
        {
            System.out.println("JDBC Driver not found!");
        }
        
        try
        {
            conn = DriverManager.getConnection(url);
        }
        catch (SQLException ex)
        {
            System.out.println("Connection failed.");
        }        
        
        if (conn == null) {
            System.out.println("Connection could not be made.");
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
    
    public static Calendar DateToCalendar(Date date){ 
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}
