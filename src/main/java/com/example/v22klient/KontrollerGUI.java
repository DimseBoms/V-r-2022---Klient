package com.example.v22klient;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * KontrollerGUI er klassen som bestemmer hvilken komponenter og panes som blir bruket
 */
public class KontrollerGUI extends Application {
    public static final int WIDTH = 600;
    public static final int HEIGHT = 800;
    public static VBox root = new VBox();
    //Ikke lov med oddetall
    public static int feltAntall = 34;
    protected static Lykkehjul lykkeHjul = new Lykkehjul(feltAntall);
    FlowPane velgTallPane;
    VBox rekkePane;
    static Tilkobling tilkobling;
    protected static Bruker bruker;
    private static int tallFraToggle;

    // variabler fra leo
    private static RekkePanelVisning rekkePanel;
    private static VBox rekkeRamme;
    private boolean nokTall;
    private static int antallTrukket;
    private ArrayList<HBox> rekkePaneler = new ArrayList<>();
    private static Label sum;
    private static boolean innlesingGodkjent;
    private static ArrayList<Integer> kontrollAvDuplikat = new ArrayList<>();

    /**
     * Oppretter tilkobling til tjener
     */
    public static void kobleTilTjener() {
        try {
            tilkobling = new Tilkobling();
        } catch (IOException e) {
            // TODO: Må lage feilhåndtering for feil ved tilkobling her. Gjerne GUI Komponenter
            System.out.println("Klarte ikke koble til tjener");
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<Lottorekke> valgteRekker;


    @Override
    public void start(Stage stage) throws IOException {
        root.setBackground(new Background(new BackgroundFill(Color.DARKSLATEGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        root.getChildren().add(KomponenterGUI.lagInnloggingPane());
        root.setPadding(new Insets(0, 10,10,10));
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void loggInn(String fornavn, String etternavn, String tlf, String epost){
        if(kontrollerTlfNr(tlf) && kontrollerEpost(epost) && kontrollerNavn(fornavn) && kontrollerNavn(etternavn)) {
            // Kobler til tjener
            kobleTilTjener();
            // Oppretter statisk brukerobjekt på Tilkobling
            bruker = new Bruker(fornavn, etternavn, epost, tlf);
            tilkobling.loggInnBruker(bruker);
            // Sjekker om bruker ble suksessfullt logget inn
            if (tilkobling.brukerLoggetInn()) {
                // Viser hovedvindu
                root.getChildren().clear();
                rekkeRamme = new VBox();
                rekkeRamme.setPadding(new Insets(10));
                rekkeRamme.setSpacing(5);
                root.getChildren().addAll(
                        menyBar(),
            //            visSummering(),
            //            KomponenterGUI.lagLykkeHjulPane(lykkeHjul),
                        KomponenterGUI.lagVelgInputPane()
            //            KomponenterGUI.lagVelgTallPane(feltAntall),
            //            rekkeRamme
                );
                //testSendingAvRekke(bruker);
                // Viser velkomstmelding
                Alert utilgjengeligBrukerAlert = new Alert(Alert.AlertType.CONFIRMATION);
                utilgjengeligBrukerAlert.setHeaderText("Velkommen " + tilkobling.getBruker().getFornavn());
                utilgjengeligBrukerAlert.showAndWait();
            } else {
                // Viser feilmelding ved innlogging
                Alert utilgjengeligBrukerAlert = new Alert(Alert.AlertType.ERROR);
                utilgjengeligBrukerAlert.setHeaderText("Manglende samsvar mellom epost og telefon");
                utilgjengeligBrukerAlert.showAndWait();
            }
        } else {
            // Viser feilmelding angående formattering
            Alert utilgjengeligBrukerAlert = new Alert(Alert.AlertType.INFORMATION);
            utilgjengeligBrukerAlert.setHeaderText("Sjekk formattering på innloggingsinformasjon");
            utilgjengeligBrukerAlert.showAndWait();
        }
    }

    // TODO: Skal fjerne en rekke fra rekkeObjektListe. Skal den ta imot en id av noe slag??
    private static void fjernRekke() {
        System.out.println("Startet fjernRekke()");
    }

    /**
     * Kontroll av format på telefonnummer.
     * Bør utvides til å akseptere flere formater
     */
    private static boolean kontrollerTlfNr(String tlf) {
        Pattern pattern = Pattern.compile("^\\d{8}$");
        Matcher matcher = pattern.matcher(tlf);
        return matcher.matches();
    }

    private static boolean kontrollerEpost(String epost) {
        Pattern pattern = Pattern.compile("^(.+)@(.+)$");
        Matcher matcher = pattern.matcher(epost);
        return matcher.matches();
    }

    private static boolean kontrollerNavn(String navn) {
        Pattern pattern = Pattern.compile("^[A-Za-z]\\w{1,60}$");
        Matcher matcher = pattern.matcher(navn);
        return matcher.matches();
    }

    /**
     *
     * @param antallTall
     * Komponenten for å lage tallene i en rekke
     */
    public static void velgTall(int antallTall){
        for(int k = 0; k < antallTall; k++){
            if(KomponenterGUI.tallKnapperListe.get(k).isSelected() && KomponenterGUI.rekkeTall.size() < 7){
                if(!KomponenterGUI.rekkeTall.contains(KomponenterGUI.tallKnapperListe.indexOf(KomponenterGUI.tallKnapperListe.get(k)) + 1)) {
                    KomponenterGUI.rekkeTall.add(KomponenterGUI.tallKnapperListe.indexOf(KomponenterGUI.tallKnapperListe.get(k)) + 1);
                    tallFraToggle = Integer.parseInt(KomponenterGUI.tallKnapperListe.get(k).getText());
                    System.out.println(tallFraToggle);
                }
            }
            if(KomponenterGUI.rekkeTall.size() == 7){
                // Sender utfylt rekke til metode som legger til rekke for sending.
                int placeholderInt = 30;
                new Rekke(KomponenterGUI.rekkeTall, placeholderInt, bruker);
                // Nullstiller
                KomponenterGUI.resetToggle();
                KomponenterGUI.rekkeTall.clear();
                kontrollAvDuplikat.clear(); // DENNE MÅ MED
            }
        }
        // KODESNUTT FOR Å KONTROLLERE DUPLIKATER PR REKKE MED BALLER
        if (kontrollAvDuplikat.contains(tallFraToggle)) {
            System.out.print("Duplikat: stopp videre");
            return;
        } else {
            kontrollAvDuplikat.add(tallFraToggle);
        }
        trekkTall2();
        rekkeRamme.setStyle("-fx-background-color: dimgrey");
        System.out.println(KomponenterGUI.rekkeTall);
    }

    /**
     * Visningen for å hente rekker gjennom filer
     */
    private static void filVisning() {
        KomponenterGUI.lagLastOppFilPane();
        root.getChildren().clear();
        root.getChildren().addAll(
                menyBar(),
                visSummering(),
                KomponenterGUI.lagLykkeHjulPane(lykkeHjul),
                KomponenterGUI.lagVelgInputPane(),
        //        KomponenterGUI.lagVelgTallPane(feltAntall),
                rekkeRamme
        );
    }

    /**
     * Metoden legger til en ny kule (RekkeVisning instans) i tallRamme
     * Tallet trekkes tilfeldig fra 1 til 34
     */
    private static void fyllRekke() {
        if (rekkePanel == null || antallTrukket == 7) {
            rekkePanel = new RekkePanelVisning();
            rekkeRamme.getChildren().add(rekkePanel);
        }
        //   rekkePanel.getChildren().add(new TallTrekkVisning(new Random().nextInt(34)));
        // Legger til ny tallball gjennom RekkePanelVisning slik at alle tallballer har en ref. til et RekkePanel
        rekkePanel.leggTilResponsivBall(new TallTrekkVisning(tallFraToggle));
    }

    /**
     * Metoden legger til en ny kule (RekkeVisning instans) i tallRamme
     * Tallet trekkes tilfeldig fra 1 til 34
     */
    private static void trekkTall2() {
        if (antallTrukket <= 7) {
            fyllRekke();
            antallTrukket++;
            if (antallTrukket >= 7) {
                RekkePanelVisning r = new RekkePanelVisning();
                rekkeRamme.getChildren().add(r);
                rekkePanel = r;
                antallTrukket = 0;
            }
        }
    }

    /**
     * Metoden fjerner slettet RekkePanelVisning fra GUI
     * @param rammePanel
     */
    public static void OppdaterRekkeVisning(RekkePanelVisning rammePanel) {
        rekkeRamme.getChildren().remove(rammePanel);
    }

    /**
     * Metoden viser en Label med summeringer i GUI
     * @return
     */
    public static Label visSummering() {
        sum = new Label();
        sum.setAlignment(Pos.TOP_RIGHT);
        sum.setPadding(new Insets(10, 0, 0, 10));
        sum.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        sum.setTextFill(Color.WHITE);
        return sum;
    }

    /**
     * Metoden oppdaterer tekst i Label som viser summeringer
     * @param sumType
     * @param nySum
     */
    public static void oppdaterSum(int sumType, double nySum) {
        if (sumType == 1)
            sum.setText("Samlet innsats: " + nySum);
        if (sumType == 2)
            sum.setText("Samlet gevinst: " + nySum);
    }

    /**
     * Metoden genererer en drop down meny på topplinjen
     * Events utløser funksjoner i programmet
     * @return
     */
    private static MenuBar menyBar() {
        MenuBar mb = new MenuBar();
        mb.setStyle("-fx-background-color: darkgrey");
        mb.setPadding(new Insets(0,10,0,10));

        Menu meny1 = new Menu("Menyvalg");

        MenuItem miA0 = new MenuItem("Spinn hjulet");
        MenuItem miA1 = new MenuItem("Trekk tall");
        MenuItem miA2 = new MenuItem("Generer sum innsats");
        MenuItem miA3 = new MenuItem("Generer sum gevinst");
        MenuItem mia4 = new MenuItem("Send rekker og satser");
        MenuItem mia5 = new MenuItem("Spinn (Random Generator)");
        MenuItem mia6 = new MenuItem("Tilbakestill hjul");


        miA0.setOnAction(e-> lykkeHjul.spin(true));
        miA1.setOnAction(e-> velgTall(34) );
        //miA2.setOnAction(e-> oppdaterSum(1, RekkePanelVisning.hentInnsats()));
        mia4.setOnAction(e-> tilkobling.sendRekke(bruker));
//        miA3.setOnAction(e-> oppdaterSum(2, RekkePanelVisning.aggregerInnsats()));
        mia5.setOnAction(e-> lykkeHjul.spin(false));
        //Tilbakestiller hjulet
        mia6.setOnAction(e-> {
            if (!lykkeHjul.getAktivSpin()) {
                lykkeHjul.tilbakeStillHjul();
            } else {
                System.out.println("Hjulet kan ikke tilbakestilles mens den spinner");
            }
        });

        meny1.getItems().addAll(miA0, miA1, miA2, miA3, mia4, mia5, mia6);

        mb.getMenus().addAll(meny1);
        return mb;
    }

    /**
     * Hvis bruker ønsker å lage nye rekker selv uten filer
     */
    public static void visVelgSelv() {
        root.getChildren().clear();
        root.getChildren().addAll(
                menyBar(),
                visSummering(),
                KomponenterGUI.lagLykkeHjulPane(lykkeHjul),
                KomponenterGUI.lagVelgTallPane(feltAntall),
                rekkeRamme
        );
    }

    /**
     *
     * @param rekkerFraFil
     * @return
     */
    protected static VBox visRekkerFraFil(ArrayList<ArrayList<Integer>> rekkerFraFil) {
        VBox boks = new VBox();
        boks.setPadding(new Insets(10));
        boks.setSpacing(5);
        boks.setStyle("-fx-background-color: dimgrey");
        for(ArrayList<Integer> liste : rekkerFraFil){
            RekkePanelVisning rekke = new RekkePanelVisning();
            for (int k = 0; k < 7; k++){
                rekke.getChildren().add(new TallTrekkVisning(liste.get(k)));
            }
            boks.getChildren().add(rekke);
        }
        return boks;
    }

    /**
     *
     * @param rekker
     * Viser rekkene som er avlest i en fil visuelt
     */
    private static void visFraFil(ArrayList<ArrayList<Integer>> rekker) {
        root.getChildren().clear();
        root.getChildren().addAll(
                menyBar(),
                visSummering(),
                KomponenterGUI.lagLykkeHjulPane(lykkeHjul),
                RekkePanelVisning.visRekkerFraFil(rekker)
        );
    }

    /**
     *
     * @param filnavn
     * @return
     * Metoden for å lese filer. Trenger en filnavn
     * Gjør innholdet om til en ArrayList
     */
    public static ArrayList<ArrayList<Integer>> lesFil (String filnavn) {
        innlesingGodkjent = true;
        ArrayList<ArrayList<Integer>> rekker = new ArrayList<>();
        File fil = new File(filnavn);
        int antallRekker = 0;
        try {
            Scanner scanner = new Scanner(fil);
            while (scanner.hasNextLine()){
                antallRekker++;
                scanner.nextLine();
            }
            scanner.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        String[] linjer = new String[antallRekker];
        try{
            Scanner scanner = new Scanner(fil);
            for(int i = 0; i < antallRekker; i++){
                linjer[i] = scanner.nextLine();
            }
        }catch (Exception e){
            System.out.println("Problemer med fil");
            e.printStackTrace();
        }
        for(int i = 0; i < linjer.length; i++){
            ArrayList<Integer> arr = new ArrayList<>();
            try {
                String[] linje = linjer[i].split(" ");
                for (int k = 0; k < 7; k++) {
                    arr.add(Integer.parseInt(linje[k]));
                }
                rekker.add(arr);
            }catch (Exception e){
                System.out.println("Det er en feil i filen. Sjekk tekstfilen og prøv igjen.");
                varsleBruker("Det er en feil i tekstfilen. Sjekk tekstfilen og prøv på nytt");
                innlesingGodkjent = false;
                rekker.clear();
                break;
            }
        }

        for(ArrayList<Integer> arr : rekker){
            Set<Integer> set = new HashSet<Integer>(arr);
            if(set.size() < arr.size()){
                varsleBruker("En eller flere rekker i tekstfilen inneholder like tall. Alle rekker må inneholde unike tall");
                rekker.clear();
                innlesingGodkjent = false;
                break;
            }
            for(Integer tall : arr){
                if(tall < 1 || tall > 34){
                    varsleBruker("Det er tall mindre enn 0, eller større enn 34 i rekkene. Sjekk rekkene på nytt");
                    rekker.clear();
                    innlesingGodkjent = false;
                    break;
                }
            }
        }

        System.out.println(rekker);
        if(innlesingGodkjent){
            visFraFil(rekker);
        }
        return rekker;
    }

    /**
     * Meldinger til bruker med class Alert
     * @param melding - tar imot og viser String
     */
    private static void varsleBruker(String melding) {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("");
        alert.setHeaderText("Melding til bruker:");
        alert.setContentText(melding);
        alert.showAndWait().ifPresent((btnType) -> {});
    }

}

// OK TODO: Tekst for rekker: Text, Font, Farge
// OK TODO: Når full rekke: untoggle alle valgte toggleButtons (toggle bør gi fargebytte knapper)
// TODO: VINNERTALL SAMMENLIGNES MOT ALLE TALL, UAVHENGIG AV REKKE OG REKKEFØLGE
// TODO: match på vinnertall = grønne kuler
// TODO: Total innsats pr spill må summeres og vises + TOTAL PREMIE
// TODO: Knapp for å spille "KJØR TREKNING" (når rekker og innsats er ok)
// TODO: Automatisk etablering av rekker fra fil (sleep, fargebytter, OBS: IKKE EDITERING)
// TODO: Skal man kunne slette kuler i siste rad? (MINDE VIKTIG?)
// TODO: INNSATS: mellom 1 - 100 kroner, 5 kroner er default
// TODO: Hvis gevinst på rekke, vis premie for rekke i tekstfelt (innsats)


// TODO: DEBUGGING, ALLE
// TODO: Metode for å bytte ut komponenter i visning - eget knappepanel for å velge
// TODO: Implementere kode for innlesing fra fil (husk FileNotFoundEx)
// TODO: RYDDE I KODE: KONSTANTER I STORE BOKSTAVER, MINDRE STATIC?, GETTERE/ SETTERE
// TODO: Regex sjekk av datafelt på SERVER (Ligger allerede på klient, men krav om server)
// TODO: Lydeffekter lykkehjul?
// TODO: Args til doc: Lykkehjul frittstående komponent (delte egenskaper: arc, kule, tall, rekke)
// TODO: Args til doc:
// TODO: RAPPORT i PDF: SKJERMDUMPER + KORTE BESKRIVELSER AV KLIENTPROGRAMMET I ULIKE SITUASJONER
// TODO: RAPPORT I PDF: BESKRIVE DATABASESTRUKTUREN (ER-DIAGRAM?)
// TODO: I RAPPORT: HVA MED MOBILE ENHETER?
// TODO: RAPPORT I PDF (VALFRITT): KLASSEDIAGRAM
//   KomponenterGUI.tallKnapperListe.get(k).setSelected(false);