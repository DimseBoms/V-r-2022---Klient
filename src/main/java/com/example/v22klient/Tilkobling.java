package com.example.v22klient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


public class Tilkobling {
    // Tilkoblingsvariabler
    public final String IP = "localhost";
    protected static ArrayList<Integer> svarListe;

    protected static ArrayList<Integer> innsatsListe;
    public final int PORT = 8000;
    // Etablerer variabler for ObjectStreams som vil bli benyttet til overføring av data mellom klient og tjener
    private Socket socket;
    private ObjectInputStream innStrøm;
    private ObjectOutputStream utStrøm;
    //endret til static for testing av spin()
    protected static Bruker bruker;
    private boolean brukerLoggetInn = false;
    protected static ArrayList<Integer> gevinnstListe;

    /**
     * Oppretter tilkobling mellom klient og tjener
     * @throws IOException
     */
    public Tilkobling() throws IOException {
        System.out.println("Forsøker å koble til");
        try {
            this.socket = new Socket(IP, PORT);
            System.out.println("Koblet til tjener " + socket);
            this.utStrøm = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            // Forsøker å lukke strømmene
            utStrøm.close();
            socket.close();
        }
    }

    /**
     * Logger inn bruker
     */
    public void loggInnBruker() {
        try {
            System.out.println("Startet loggInnBruker");
            // Oppretter og sender HashMap med brukerinfo
            HashMap<Object, Object> brukerMap = new HashMap<>();
            brukerMap.put("query", "loggInn");
            brukerMap.put("fornavn", bruker.getFornavn());
            brukerMap.put("etternavn", bruker.getEtternavn());
            brukerMap.put("epost", bruker.getEpost());
            brukerMap.put("tlf", bruker.getTlf());
            System.out.println("Sender innloggingsforsøk til serveren " + brukerMap);
            utStrøm.writeObject(brukerMap);
            // Tar imot og behandler svar fra tjeneren
            this.innStrøm = new ObjectInputStream(socket.getInputStream());
            HashMap<Object, Object> svar = (HashMap<Object, Object>) innStrøm.readObject();
            System.out.println("Svar fra tjener " + svar.toString());
            if ((int) svar.get("feilkode") == 0) {
                this.brukerLoggetInn = true;
            }
        } catch (IOException e) {
            // Gracefully close everything.
            closeEverything(socket, innStrøm, utStrøm);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Overlastet metode for å oppdatere brukervariabel før kall på leggInnBruker()
     * @param bruker
     */
    public void loggInnBruker(Bruker bruker) {
        this.bruker = bruker;
        loggInnBruker();
    }

    /**
     * Send rekker til tjener
     * @param bruker
     */
    public void sendRekke(Bruker bruker) {
        try {
            // Oppretter og sender HashMap med bruker sine rekker
            System.out.println("Sender rekker...");
            System.out.println("Rekkeliste: " + bruker.rekkeListe);
            System.out.println("Satsliste: " + bruker.innsatsListe);
            HashMap<Object, Object> brukerMap = new HashMap<>();
            brukerMap.put("query", "sendRekke");
            brukerMap.put("rekker", bruker.rekkeListe);
            brukerMap.put("innsats", bruker.innsatsListe);
            utStrøm.writeObject(brukerMap);
            // Tar imot og behandler svar fra tjeneren
            System.out.println("Venter på respons fra server med svar på rekker...");
            HashMap<Object, Object> svar = (HashMap<Object, Object>) innStrøm.readObject();
            System.out.println(svar.toString());
            sjekkVunnet(svar);
        } catch (IOException e) {
            // Gracefully close everything.
            closeEverything(socket, innStrøm, utStrøm);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Denne metoden skal iterere gjennom alle verdiene i svar og sjekke alle verdiene i RekkePanelene
     * For deretter å endre farge på vinnerkulene til grønn
     * @param svar
     */
    private void sjekkVunnet(HashMap<Object, Object> svar) {
        svarListe = (ArrayList<Integer>) svar.get("vinnerRekke");
        innsatsListe = (ArrayList<Integer>) svar.get("innsats");
        gevinnstListe= (ArrayList<Integer>) svar.get("gevinst");
        System.out.println(svarListe);
        for (int vinnerTall : svarListe) {
            for (RekkePanelVisning rad : RekkePanelVisning.rammer) {
                for (TallTrekkVisning tallBall : rad.getTallBallArray()) {
                    if (tallBall.getVerdi() == vinnerTall) tallBall.setVunnet();
                }
            }
        }
    }

    /**
     * Hjelpemetode for å lukke alle åpne strømmer og sockets
     * @param socket
     * @param innStrøm
     * @param utStrøm
     */
    public void closeEverything(Socket socket, ObjectInputStream innStrøm, ObjectOutputStream utStrøm) {
        try {
            if (innStrøm != null) {
                innStrøm.close();
            }
            if (utStrøm != null) {
                utStrøm.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean brukerLoggetInn() {
        return brukerLoggetInn;
    }

    public Bruker getBruker() {
        return bruker;
    }
}
