package stamboom.domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import javafx.collections.FXCollections;
import static javafx.collections.FXCollections.*;
import javafx.collections.ObservableList;

public class Administratie implements Serializable {

    //************************datavelden*************************************
    private int nextGezinsNr = 0;
    private int nextPersNr = 0;
    private final List<Persoon> personen;
    private final List<Gezin> gezinnen;
    private transient ObservableList<Persoon> observablePersonen;
    private transient ObservableList<Gezin> observableGezinnen;
    private final static long serialVersionUID = -3123920467171138433L;

    //***********************constructoren***********************************
    /**
     * er wordt een administratie gecreeerd met 0 personen en dus 0 gezinnen
     * personen en gezinnen die in de toekomst zullen worden gecreeerd, worden
     * elk opvolgend genummerd vanaf 1
     */
    public Administratie() {
        personen = new ArrayList<>();
        gezinnen = new ArrayList<>();
        observablePersonen = observableList(personen);
        observableGezinnen = observableList(gezinnen);
    }

    //**********************methoden****************************************
    /**
     * er wordt een persoon met een gegeven geslacht, met als voornamen vnamen,
     * achternaam anaam, tussenvoegsel tvoegsel, geboortedatum gebdat,
     * geboorteplaats gebplaats en een gegeven ouderlijk gezin gecreeerd; de persoon
     * krijgt een uniek nummer toegewezen de persoon is voortaan ook bij het
     * ouderlijk gezin bekend. Voor de voornamen, achternaam en gebplaats geldt
     * dat de eerste letter naar een hoofdletter en de resterende letters naar
     * een kleine letter zijn geconverteerd; het tussenvoegsel is zo nodig in
     * zijn geheel geconverteerd naar kleine letters; overbodige spaties zijn 
     * verwijderd
     *
     * @param geslacht
     * @param vnamen vnamen.length>0; alle strings zijn niet leeg
     * @param anaam niet leeg
     * @param tvoegsel
     * @param gebdat
     * @param gebplaats niet leeg
     * @param ouderlijkGezin mag de waarde null (=onbekend) hebben
     *
     * @return als de persoon al bekend was (op basis van combinatie van getNaam(),
     * geboorteplaats en geboortedatum), wordt er null geretourneerd, anders de 
     * nieuwe persoon
     */
    public Persoon addPersoon(Geslacht geslacht, String[] vnamen, String anaam,
            String tvoegsel, Calendar gebdat,
            String gebplaats, Gezin ouderlijkGezin) {
        
        if (vnamen.length == 0) {
            throw new IllegalArgumentException("ten minst 1 voornaam");
        }
        for (String voornaam : vnamen) {
            if (voornaam.trim().isEmpty()) {
                throw new IllegalArgumentException("lege voornaam is niet toegestaan");
            }
        }

        if (anaam.trim().isEmpty()) {
            throw new IllegalArgumentException("lege achternaam is niet toegestaan");
        }

        if (gebplaats.trim().isEmpty()) {
            throw new IllegalArgumentException("lege geboorteplaats is niet toegestaan");
        }

        nextPersNr++;
        Persoon newPersoon = new Persoon(this.nextPersNr, vnamen, anaam, tvoegsel, gebdat, gebplaats, geslacht, ouderlijkGezin);
        if (getPersoon(vnamen, anaam, tvoegsel, gebdat, gebplaats) != null) {
            return null;
        }
        
        observablePersonen.add(newPersoon);
        
        if(ouderlijkGezin != null)
        {
            newPersoon.getOuderlijkGezin().breidUitMet(newPersoon);
        }
        
        return newPersoon;
    }

    /**
     * er wordt, zo mogelijk (zie return) een (kinderloos) ongehuwd gezin met
     * ouder1 en ouder2 als ouders gecreeerd; de huwelijks- en scheidingsdatum
     * zijn onbekend (null); het gezin krijgt een uniek nummer toegewezen; dit
     * gezin wordt ook bij de afzonderlijke ouders geregistreerd;
     *
     * @param ouder1
     * @param ouder2 mag null zijn
     *
     * @return null als ouder1 = ouder2 of als de volgende voorwaarden worden
     * overtreden: 1) een van de ouders is op dit moment getrouwd 2) het koppel
     * uit een ongehuwd gezin kan niet tegelijkertijd als koppel bij een ander
     * ongehuwd gezin betrokken zijn anders het gewenste gezin
     */
    public Gezin addOngehuwdGezin(Persoon ouder1, Persoon ouder2) {
        Calendar nu = Calendar.getInstance();
        
        if (ouder1 == ouder2) 
        {
            return null;
        }
        
        if(ongehuwdGezinBestaat(ouder1, ouder2))
        {
            return null;
        }

        if (ouder1.isGetrouwdOp(nu)) 
        {
            return null;
        } else if (ouder2 != null && ouder2.isGetrouwdOp(nu)) 
        {
            return null;
        } else if (ongehuwdGezinBestaat(ouder1, ouder2)) 
        {
            return null;
        }

        nextGezinsNr++; 
        Gezin gezin = new Gezin(nextGezinsNr, ouder1, ouder2);

        observableGezinnen.add(gezin);
        ouder1.wordtOuderIn(gezin);
        if (ouder2 != null) 
        {
            ouder2.wordtOuderIn(gezin);
        }
                
        return gezin;
    }

    /**
     * Als het ouderlijk gezin van persoon nog onbekend is dan wordt persoon een
     * kind van ouderlijkGezin en tevens wordt persoon als kind in dat gezin
     * geregistreerd; <br>
     * Als de ouders bij aanroep al bekend zijn, verandert er
     * niets
     *
     * @param persoon
     * @param ouderlijkGezin
     */
    public void setOuders(Persoon persoon, Gezin ouderlijkGezin) {
        persoon.setOuders(ouderlijkGezin);
    }

    /**
     * als de ouders van dit gezin gehuwd zijn en nog niet gescheiden en datum
     * na de huwelijksdatum ligt, wordt dit de scheidingsdatum. Anders gebeurt
     * er niets.
     *
     * @param gezin
     * @param datum
     * @return true als scheiding geaccepteerd, anders false
     */
    public boolean setScheiding(Gezin gezin, Calendar datum) {
        return gezin.setScheiding(datum);
    }

    /**
     * registreert het huwelijk, mits gezin nog geen huwelijk is en beide ouders
     * op deze datum mogen trouwen (pas op: ook de toekomst kan hierbij een rol
     * spelen omdat toekomstige gezinnen eerder zijn geregisteerd)
     *
     * @param gezin
     * @param datum de huwelijksdatum
     * @return false als huwelijk niet mocht worden voltrokken, anders true
     */
    public boolean setHuwelijk(Gezin gezin, Calendar datum) {
        return gezin.setHuwelijk(datum);
    }

    /**
     *
     * @param ouder1
     * @param ouder2
     * @return true als dit koppel (ouder1,ouder2) al een ongehuwd gezin vormt
     */
    boolean ongehuwdGezinBestaat(Persoon ouder1, Persoon ouder2) {
        return ouder1.heeftOngehuwdGezinMet(ouder2) != null;
    }

    /**
     * als er al een ongehuwd gezin voor dit koppel bestaat, wordt het huwelijk
     * voltrokken, anders wordt er zo mogelijk (zie return) een (kinderloos)
     * gehuwd gezin met ouder1 en ouder2 als ouders gecreeerd; de
     * scheidingsdatum is onbekend (null); het gezin krijgt een uniek nummer
     * toegewezen; dit gezin wordt ook bij de afzonderlijke ouders
     * geregistreerd;
     *
     * @param ouder1
     * @param ouder2
     * @param huwdatum
     * @return null als ouder1 = ouder2 of als een van de ouders getrouwd is
     * anders het gehuwde gezin
     */
    public Gezin addHuwelijk(Persoon ouder1, Persoon ouder2, Calendar huwdatum) {
        if (ouder1 == ouder2) {
            return null;
        }
        
        if (!ouder1.kanTrouwenOp(huwdatum) || !ouder2.kanTrouwenOp(huwdatum)) {
            return null;
        }
        
        Gezin toeTeVoegenGezin;
        boolean alOngehuwdGezin = false;
        for (Gezin huidig: observableGezinnen) {
            if (huidig.getOuder1() == ouder1 && huidig.getOuder2() == ouder2) {
                toeTeVoegenGezin = huidig;
                toeTeVoegenGezin.setHuwelijk(huwdatum);
                alOngehuwdGezin = true;
                return toeTeVoegenGezin;
            }
            if (huidig.getOuder2() == ouder1 && huidig.getOuder1() == ouder2) {
                toeTeVoegenGezin = huidig;
                toeTeVoegenGezin.setHuwelijk(huwdatum);
                alOngehuwdGezin = true;
                return toeTeVoegenGezin;
            }
        }
        
        if (!alOngehuwdGezin) {
            nextGezinsNr++;
            toeTeVoegenGezin = new Gezin(nextGezinsNr, ouder1, ouder2);
            ouder1.wordtOuderIn(toeTeVoegenGezin);
            ouder2.wordtOuderIn(toeTeVoegenGezin);
                
            if (toeTeVoegenGezin.setHuwelijk(huwdatum)) {
                observableGezinnen.add(toeTeVoegenGezin);
                return toeTeVoegenGezin;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    /**
     *
     * @return het aantal geregistreerde personen
     */
    public int aantalGeregistreerdePersonen() {
        return nextPersNr ;
    }

    /**
     *
     * @return het aantal geregistreerde gezinnen
     */
    public int aantalGeregistreerdeGezinnen() {
        return nextGezinsNr ;
    }

    /**
     *
     * @param nr
     * @return de persoon met nummer nr, als die niet bekend is wordt er null
     * geretourneerd
     */
    public Persoon getPersoon(int nr) {
        for (Persoon huidig: observablePersonen) {
            if (huidig.getNr() == nr) {
                return huidig;
            }
        }
        return null;
    }

    /**
     * @param achternaam
     * @return alle personen met een achternaam gelijk aan de meegegeven
     * achternaam (ongeacht hoofd- en kleine letters)
     */
    public ArrayList<Persoon> getPersonenMetAchternaam(String achternaam) {
        ArrayList<Persoon> gevondenPersonen = new ArrayList<>();
        for (Persoon huidig: observablePersonen) {
            if (huidig.getAchternaam().toLowerCase().equals(achternaam.toLowerCase())) {
                gevondenPersonen.add(huidig);
            }
        }
        
        return gevondenPersonen;
    }

    /**
     *
     * @return de geregistreerde personen
     */
    public ObservableList<Persoon> getPersonen() {
        return (ObservableList<Persoon>)FXCollections.unmodifiableObservableList(observablePersonen);
    }

    /**
     *
     * @param vnamen
     * @param anaam
     * @param tvoegsel
     * @param gebdat
     * @param gebplaats
     * @return de persoon met dezelfde initialen, tussenvoegsel, achternaam,
     * geboortedatum en -plaats mits bekend (ongeacht hoofd- en kleine letters),
     * anders null
     */
    public Persoon getPersoon(String[] vnamen, String anaam, String tvoegsel,
            Calendar gebdat, String gebplaats) {
        ArrayList<Persoon> p = new ArrayList<>();
        for (Persoon huidig: observablePersonen) {
            
            String initialen = "";
            for (String voornaam:vnamen) {
                initialen += voornaam.substring(0, 1) + ".";
            }
            
            if (gebplaats.toLowerCase().equals(huidig.getGebPlaats().toLowerCase())
                    && gebdat.equals(huidig.getGebDat())
                    && tvoegsel.toLowerCase().equals(huidig.getTussenvoegsel().toLowerCase())
                    && anaam.toLowerCase().equals(huidig.getAchternaam().toLowerCase())
                    && initialen.toLowerCase().equals(huidig.getInitialen().toLowerCase())) {
                p.add(huidig);
                return p.get(0);
            }
        }
        
        return null;
    }

    /**
     *
     * @return de geregistreerde gezinnen
     */
    public ObservableList<Gezin> getGezinnen() {
        return (ObservableList<Gezin>)FXCollections.unmodifiableObservableList(observableGezinnen);
    }

    /**
     *
     * @param gezinsNr
     * @return het gezin met nummer nr. Als dat niet bekend is wordt er null
     * geretourneerd
     */
    public Gezin getGezin(int gezinsNr) {
        // aanname: er worden geen gezinnen verwijderd
        if (1 <= gezinsNr && 1 <= observableGezinnen.size()) {
            return observableGezinnen.get(gezinsNr - 1);
        }
        return null;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
        ois.defaultReadObject();
        observablePersonen = observableList(personen);
        observableGezinnen = observableList(gezinnen);
    }
    
    public void addDBPersoon (Persoon p)
    {
        observablePersonen.add(p);
    }
}
