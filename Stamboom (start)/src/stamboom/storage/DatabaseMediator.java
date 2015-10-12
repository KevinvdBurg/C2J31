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
        
        try {
            statement = conn.createStatement();
            String SQLPersonen = "Select * from `Personen`";
            
            ResultSet executeQuery = statement.executeQuery(SQLPersonen);
            
            while (executeQuery.next()) {
                ArrayList<String> voornamen = new ArrayList<String>();
                voornamen.add(executeQuery.getString(2));
                
                String[] voornamenArr = new String[voornamen.size()];
                voornamenArr = voornamen.toArray(voornamenArr);
                
                String achternaam = executeQuery.getString(3);
                String tussenvoegsel = executeQuery.getString(4);
                
                Date birthDate = new SimpleDateFormat("dd-MM-yyyy").parse(executeQuery.getString(5));
                
                String geboorteplaats = executeQuery.getString(6);
                Geslacht geslacht;
                if (executeQuery.getString(6) == "M") {
                    geslacht = Geslacht.MAN;
                }
                else{
                    geslacht = Geslacht.VROUW;
                }
                Gezin oudergezin = null;
                if (executeQuery.getString(7) != null) {
                    oudergezin = new Gezin(); //todo
                }
                
                admin.addPersoon(geslacht, voornamenArr, achternaam, tussenvoegsel, DateToCalendar(birthDate), geboorteplaats, oudergezin);
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

                    g = new Gezin(executeQuery.getInt(1), admin.getPersoon(executeQuery.getInt(2)),ouder2);
                    
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
        } catch (Exception e) {
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
                //prepStatement = conn.prepareStatement("INSERT INTO PERSONEN VALUES (?,?,?,?,?,?,?,?)"); 
                
                //prepStatement = conn.prepareStatement("INSERT INTO `Personen` (`persoonsNummer`,`achternaam`,`voornamen`,`tussenvoegsel`,`geboortedatum`,`geboorteplaats`,`geslacht`,`ouders`) VALUES (?,?,?,?,?,?,?,?);"); 
                
                //prepStatement = conn.prepareStatement("INSERT INTO `Personen` (`persoonsNummer`,`achternaam`,`voornamen`,`tussenvoegsel`,`geboortedatum`,`geboorteplaats`,`geslacht`,`ouders`) VALUES (?,?,?,?,?,?,?,?);");
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
                
                //System.out.print("INSERT INTO `Personen` (`persoonsNummer`,`achternaam`,`voornamen`,`tussenvoegsel`,`geboortedatum`,`geboorteplaats`,`geslacht`,`ouders`) VALUES ("+nr+","+Achternaam+","+Voornaam+","+Tussenvoegsel+","+gbString+","+GebPlaats+","+Geslacht+",NULL);");
                //System.out.print("INSERT INTO `Personen` (`persoonsNummer`,`achternaam`,`voornamen`,`tussenvoegsel`,`geboortedatum`,`geboorteplaats`,`geslacht`,`ouders`) VALUES ('"+nr+"','"+Achternaam+"','"+Voornaam+"','"+Tussenvoegsel+"','"+Datum+"','"+GebPlaats+"','"+Geslacht+"',NULL);\r\n");
                
                String SQL = "INSERT INTO `Personen` (`persoonsNummer`,`achternaam`,`voornamen`,`tussenvoegsel`,`geboortedatum`,`geboorteplaats`,`geslacht`,`ouders`) VALUES ('"+nr+"','"+Achternaam+"','"+Voornaam+"','"+Tussenvoegsel+"','"+Datum+"','"+GebPlaats+"','"+Geslacht+"',NULL);";
                
//                prepStatement.setInt(1, nr);
//                prepStatement.setString(2, Voornaam);
//                prepStatement.setString(3, Achternaam);
//                prepStatement.setString(4, Tussenvoegsel);
//                prepStatement.setString(5, Datum);
//                prepStatement.setString(6, GebPlaats);
//                prepStatement.setString(7, Geslacht);
//                prepStatement.setNull(8, java.sql.Types.NULL);                
//                
//                prepStatement.executeQuery();
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
                
//                prepStatement.setInt(1, huidig.getNr());
//                prepStatement.setInt(2, huidig.getOuder1().getNr());
//                prepStatement.setInt(3, huidig.getOuder2().getNr());
                

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
