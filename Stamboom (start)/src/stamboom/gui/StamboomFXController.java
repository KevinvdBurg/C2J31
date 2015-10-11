/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import stamboom.controller.StamboomController;
import stamboom.domain.*;
import stamboom.util.StringUtilities;

/**
 *
 * @author frankpeeters
 */
public class StamboomFXController extends StamboomController implements Initializable {

    //MENUs en TABs
    @FXML MenuBar menuBar;
    @FXML MenuItem miNew;
    @FXML MenuItem miOpen;
    @FXML MenuItem miSave;
    @FXML CheckMenuItem cmDatabase;
    @FXML MenuItem miClose;
    @FXML Tab tabPersoon;
    @FXML Tab tabGezin;
    @FXML Tab tabPersoonInvoer;
    @FXML Tab tabGezinInvoer;

    //PERSOON
    @FXML ComboBox cbPersonen;
    @FXML TextField tfPersoonNr;
    @FXML TextField tfVoornamen;
    @FXML TextField tfTussenvoegsel;
    @FXML TextField tfAchternaam;
    @FXML TextField tfGeslacht;
    @FXML TextField tfGebDatum;
    @FXML TextField tfGebPlaats;
    @FXML ComboBox cbOuderlijkGezin;
    @FXML ListView lvAlsOuderBetrokkenBij;
    @FXML Button btStamboom;
    @FXML TextArea txtareaStamboom;

    //INVOER GEZIN
    @FXML ComboBox cbOuder1Invoer;
    @FXML ComboBox cbOuder2Invoer;
    @FXML TextField tfHuwelijkInvoer;
    @FXML TextField tfScheidingInvoer;
    @FXML Button btOKGezinInvoer;
    @FXML Button btCancelGezinInvoer;
    
    //GEZIN
    @FXML ComboBox cbGezin;
    @FXML TextField txtNr;
    @FXML TextField txtOuder1;
    @FXML TextField txtOuder2;
    @FXML ListView lvKinderen;
    
    //INVOER PERSOON
    @FXML TextField txtGeslacht;
    @FXML TextField txtVoornamen;
    @FXML TextField txtTussenvoegsel;
    @FXML TextField txtAchternaam;
    @FXML TextField txtGeboortedatum;
    @FXML TextField txtGeboorteplaats;
    @FXML ComboBox cbKiesOuderlijkGezin;
    @FXML Button btnVoegToe;
    
    //opgave 4
    private boolean withDatabase;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initComboboxes();
        withDatabase = false;
    }

    private void initComboboxes() {
        //todo opgave 3 
        cbPersonen.setItems(getAdministratie().getPersonen());
        cbOuder1Invoer.setItems(getAdministratie().getPersonen());
        cbOuder2Invoer.setItems(getAdministratie().getPersonen());
        cbGezin.setItems(getAdministratie().getGezinnen());
        cbKiesOuderlijkGezin.setItems(getAdministratie().getGezinnen());
    }

    public void selectPersoon(Event evt) {
        Persoon persoon = (Persoon) cbPersonen.getSelectionModel().getSelectedItem();
        showPersoon(persoon);
    }

    private void showPersoon(Persoon persoon) {
        if (persoon == null) {
            clearTabPersoon();
        } else {
            tfPersoonNr.setText(persoon.getNr() + "");
            tfVoornamen.setText(persoon.getVoornamen());
            tfTussenvoegsel.setText(persoon.getTussenvoegsel());
            tfAchternaam.setText(persoon.getAchternaam());
            tfGeslacht.setText(persoon.getGeslacht().toString());
            tfGebDatum.setText(StringUtilities.datumString(persoon.getGebDat()));
            tfGebPlaats.setText(persoon.getGebPlaats());
            if (persoon.getOuderlijkGezin() != null) {
                cbOuderlijkGezin.getSelectionModel().select(persoon.getOuderlijkGezin());
            } else {
                cbOuderlijkGezin.getSelectionModel().clearSelection();
            }

            lvAlsOuderBetrokkenBij.setItems(persoon.getAlsOuderBetrokkenIn());
        }
    }

    public void setOuders(Event evt) {
        if (tfPersoonNr.getText().isEmpty()) {
            return;
        }
        Gezin ouderlijkGezin = (Gezin) cbOuderlijkGezin.getSelectionModel().getSelectedItem();
        if (ouderlijkGezin == null) {
            return;
        }

        int nr = Integer.parseInt(tfPersoonNr.getText());
        Persoon p = getAdministratie().getPersoon(nr);
        getAdministratie().setOuders(p, ouderlijkGezin);
    }

    public void selectGezin(Event evt) {
        // todo opgave 3

    }

    private void showGezin(Gezin gezin) {
        // todo opgave 3

    }

    public void okGezinInvoer(Event evt) {
        String scheidingsdatumString = tfScheidingInvoer.getText();
        Persoon ouder1 = (Persoon) cbOuder1Invoer.getSelectionModel().getSelectedItem();
        if (ouder1 == null) {
            showDialog("Warning", "eerste ouder is niet ingevoerd");
            return;
        }
        Persoon ouder2 = (Persoon) cbOuder2Invoer.getSelectionModel().getSelectedItem();
        Calendar huwdatum;
        try {
            huwdatum = StringUtilities.datum(tfHuwelijkInvoer.getText());
        } catch (IllegalArgumentException exc) {
            showDialog("Warning", "huwelijksdatum :" + exc.getMessage());
            return;
        }
        Gezin g;
        if (huwdatum != null) {
            g = getAdministratie().addHuwelijk(ouder1, ouder2, huwdatum);
            if (g == null) {
                showDialog("Warning", "Invoer huwelijk is niet geaccepteerd");
            } 
            else 
            {
                Date scheidingsdatum;
                Calendar cal = Calendar.getInstance();
                try 
                {
                    scheidingsdatum = new SimpleDateFormat("dd-mm-yyyy").parse(tfScheidingInvoer.getText());
                    cal.setTime(scheidingsdatum);
                    getAdministratie().setScheiding(g, cal);
                } 
                catch (Exception exc) 
                {
                    showDialog("Warning", "scheidingsdatum :" + exc.getMessage());
                }
            }
        } 
        else 
        {
            g = getAdministratie().addOngehuwdGezin(ouder1, ouder2);
            if (g == null) {
                showDialog("Warning", "Invoer ongehuwd gezin is niet geaccepteerd");
            }
        }

        clearTabGezinInvoer();
    }

    public void cancelGezinInvoer(Event evt) {
        clearTabGezinInvoer();
    }

    
    public void showStamboom(Event evt) {
        txtareaStamboom.setText("test");
        try 
        {
            Persoon stamboomPersoon = (Persoon)cbPersonen.getSelectionModel().getSelectedItem();
            txtareaStamboom.setText(stamboomPersoon.stamboomAlsString());
        }
        catch(Exception ex)
        {
            JOptionPane.showConfirmDialog(null, "Selecteer eerst een persoon!");
        }
    }

    public void createEmptyStamboom(Event evt) {
        this.clearAdministratie();
        clearTabs();
        initComboboxes();
    }

    
    public void openStamboom(Event evt) {
        if (withDatabase) 
        {
            openFromDb();
        }
        else
        {
            openFromFile();
        }
    }

    
    public void saveStamboom(Event evt) {
        if (withDatabase) {
            saveToDb();
        }
        else
        {
            saveToFile();
        }
    }

    
    public void closeApplication(Event evt) {
        saveStamboom(evt);
        getStage().close();
    }

   
    public void configureStorage(Event evt) {
        withDatabase = cmDatabase.isSelected();
    }

 
    public void selectTab(Event evt) {
        Object source = evt.getSource();
        if (source == tabPersoon) {
            clearTabPersoon();
        } else if (source == tabGezin) {
            clearTabGezin();
        } else if (source == tabPersoonInvoer) {
            clearTabPersoonInvoer();
        } else if (source == tabGezinInvoer) {
            clearTabGezinInvoer();
        }
    }

    private void clearTabs() {
        clearTabPersoon();
        clearTabPersoonInvoer();
        clearTabGezin();
        clearTabGezinInvoer();
    }

    private void clearTabGezinInvoer() {
        cbOuder1Invoer.getSelectionModel().clearSelection();
        cbOuder2Invoer.getSelectionModel().clearSelection();
        tfScheidingInvoer.setText("");
        tfHuwelijkInvoer.setText("");
    }

    private void clearTabPersoon() {
        cbPersonen.getSelectionModel().clearSelection();
        tfPersoonNr.clear();
        tfVoornamen.clear();
        tfTussenvoegsel.clear();
        tfAchternaam.clear();
        tfGeslacht.clear();
        tfGebDatum.clear();
        tfGebPlaats.clear();
        cbOuderlijkGezin.getSelectionModel().clearSelection();
        lvAlsOuderBetrokkenBij.setItems(FXCollections.emptyObservableList());
        txtareaStamboom.setText("");
    }

    
    private void clearTabGezin() {
        // todo opgave 3
       
    }

    private void showDialog(String type, String message) {
        Stage myDialog = new Dialog(getStage(), type, message);
        myDialog.show();
    }

    private Stage getStage() {
        return (Stage) menuBar.getScene().getWindow();
    }
    
    public void handleBtnVoegToeEvent(Event event)
    {
        String[] voornamen = txtVoornamen.getText().split(" ");
        Geslacht geslacht = null;
        boolean invoerIsValid = true;
        Date geboortedatum;
        Calendar cal = null;
        String errormessage = "";
        
        //check geslacht
        if ("m".equals(txtGeslacht.getText().toLowerCase())) {
            geslacht = Geslacht.MAN;
        }
        else if ("v".equals(txtGeslacht.getText().toLowerCase()))
        {
            geslacht = Geslacht.VROUW;
        }
        else
        {
            invoerIsValid = false;
            errormessage = "Dit geslacht is niet geldig!";
        }
        
        //check geboortedatum
        try
        {
            geboortedatum = new SimpleDateFormat("dd-mm-yyyy").parse(txtGeboortedatum.getText());
            cal = Calendar.getInstance();
            cal.setTime(geboortedatum);
        }
        catch (Exception ex)
        {
            invoerIsValid = false;
            errormessage = "Deze geboortedatum is niet geldig!";
        }
        
        //set ouderlijk gezin
        Gezin gevondenGezin = null;
        for (Gezin huidig: getAdministratie().getGezinnen()) {
            if (huidig == (Gezin)cbKiesOuderlijkGezin.getSelectionModel().getSelectedItem()) {
                gevondenGezin = huidig;
            }
        }
        
        //add persoon
        if (invoerIsValid) {
            try
            {
                getAdministratie().addPersoon(geslacht, voornamen, txtAchternaam.getText(), txtTussenvoegsel.getText(), cal, txtGeboorteplaats.getText(), gevondenGezin);
            }
            catch (IllegalArgumentException ex)
            {
                errormessage = ex.getMessage();
            }
        }
        
        if (!"".equals(errormessage)) {
            JOptionPane.showConfirmDialog(null, errormessage);
        }
        else
        {
            JOptionPane.showMessageDialog(null, "Persoon toegevoegd!");
            clearTabPersoonInvoer(); 
        }
    }
    
    private void clearTabPersoonInvoer()
    {
        txtAchternaam.setText("");
        txtGeboortedatum.setText("");
        txtGeboorteplaats.setText("");
        txtGeslacht.setText("");
        txtVoornamen.setText("");
        txtTussenvoegsel.setText("");
        cbKiesOuderlijkGezin.getSelectionModel().clearSelection();
    }
    
    public void handleCbGezinEvent(Event event)
    {
        for (Gezin huidig: getAdministratie().getGezinnen()) {
            if (huidig == (Gezin)cbGezin.getSelectionModel().getSelectedItem()) 
            {
                txtNr.setText(String.valueOf(huidig.getNr()));
                txtOuder1.setText(huidig.getOuder1().toString());
                txtOuder2.setText(huidig.getOuder2().toString());
                lvKinderen.setItems(huidig.getKinderen());
            }
        }
    }
    
    private void openFromFile()
    {
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.substring(0, path.length() - 38) + "/administratieFiles";
        
        JFileChooser fileChooser = new JFileChooser(path);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Administraties", "administratie");
        fileChooser.setFileFilter(filter);
        //JOptionPane.showConfirmDialog(null, path);
        
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) 
        {
            File file = fileChooser.getSelectedFile();
            JOptionPane.showConfirmDialog(null, file.getName());
            try
            {
                deserialize(file);
                Administratie check = getAdministratie();
                clearTabs();
                initComboboxes();
            }
            catch (IOException ex)
            {
                JOptionPane.showConfirmDialog(null, "Bestand kon niet geladen worden: " + ex.getMessage());
            }
        }
    }
    
    private void saveToFile()
    {
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.substring(0, path.length() - 38) + "/administratieFiles";
        
        JFileChooser fileChooser = new JFileChooser(path);
        
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
          File file = fileChooser.getSelectedFile();
                    
            try
            {
                serialize(file);
            }
            catch (IOException ex)
            {
                JOptionPane.showConfirmDialog(null, "Bestand kon niet opgeslagen worden: " + ex.getMessage());
            }
        }
    }
    
    private void openFromDb()
    {
        try
        {
            loadFromDatabase();
        }
        catch (IOException ex)
        {
            System.out.println("Database could not be loaded.");
        }
    }
    
    private void saveToDb()
    {
        try
        {
            saveToDatabase();
        }
        catch (IOException ex)
        {
            System.out.println("Database could not be saved.");
        }
    }
}
