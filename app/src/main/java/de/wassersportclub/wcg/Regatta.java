package de.wassersportclub.wcg;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Regatta extends AppCompatActivity {

    MyListAdapter myListAdapter;

    Button btnStartTime, btnTeilnehmerAuswaehlen, btnRegattaFertig;
    Timer stoppuhr;
    TextView timeview, auswahlview;
    boolean[] checked;
    static long start;
    static boolean timerisrunning;
    int berechnungsZähler;

    int regatten = 0;
    int lauf = 0;
    int auswahlAnzahl = 0;
    ListView list;

    String auswahl = "Auswahl";
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

    Map<String, Double> alleUser = new HashMap<>();
    Map<String, Integer> yardStick = new HashMap<>();
    static HashMap<String, String> zeitTabelle = new HashMap<>();
    static HashMap<String, String> berechnetteYardstickZeit = new HashMap<>();
    static HashMap<String, Boolean> userclickable = new HashMap<>();


    static HashMap<String, Integer> runde = new HashMap<>();
    static HashMap<String, Integer> zurückgedrückt = new HashMap<>();
    static HashMap<String, Boolean> isUserEditable = new HashMap<>();
    static HashMap<String, List<String>> userLastTime = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regatta);

        auswahlview = findViewById(R.id.auswahlTV);
        timeview = findViewById(R.id.EtTime);
        btnStartTime = findViewById(R.id.btnStartTime);
        stoppuhr = new Timer();
        btnTeilnehmerAuswaehlen = findViewById(R.id.btnTeilnehmerAuswählen);
        btnRegattaFertig = findViewById(R.id.btnRegattaFertig);
        berechnungsZähler = 0;
        list = findViewById(R.id.auswahlliste);

        auswahl = getIntent().getStringExtra("auswahl");
        auswahlview.setText(auswahl);


        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    alleUser.put(dataSnapshotChild.getKey(), 99.);
                    yardStick.put(dataSnapshotChild.getKey(), Integer.parseInt(dataSnapshotChild.child("Yardstick").getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabase.child("regatten").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                regatten = 0;
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    regatten++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        doListen();

    }

    public void regattaAbbrechen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Sicher?");
        builder.setMessage("Die bisherigen gespeicherten Daten gehen dabei verloren!");

        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stoppuhr.cancel();
                checked = null;
                timerisrunning = false;
                zeitTabelle.clear();
                Regatta.this.finish();
                System.exit(0);
            }
        });
        builder.setNegativeButton("Abbrechen", null);

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void regattaBeendet() {
        Toast.makeText(Regatta.this, "Speichern OK!", Toast.LENGTH_LONG).show();
        stoppuhr.cancel();
        checked = null;
        timerisrunning = false;
        zeitTabelle.clear();
        TimerExit te = new TimerExit();
        te.execute();
    }

    public class TimerExit extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(2000); // 1000 = eine sek.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Regatta.this.finish();
            System.exit(0);
        }
    }


    @Override
    public void onBackPressed() {
        regattaAbbrechen();
    }

    //Listener
    private void doListen() {
        btnStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStoppuhr();
            }
        });
        btnTeilnehmerAuswaehlen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectUserData();
            }
        });
        btnRegattaFertig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rundenAnzahlBerechnung();
            }
        });
    }

    public void rundenAnzahlBerechnung() {
        if (!userLastTime.isEmpty()) {
            int p = 0;
            for (boolean e : checked) {
                if (e) {
                    p++;
                }
            }
            if (p == userLastTime.size()) {
                ArrayList<Integer> userRundenAnzahl = new ArrayList<>();
                for (String e : userLastTime.keySet()) {
                    int i = 0;
                    for (String s : userLastTime.get(e)) {
                        if (!s.equals("00:00:00")) {
                            i++;
                        }
                    }
                    if (i > 0) {
                        userRundenAnzahl.add(i);
                    }
                }
                Collections.sort(userRundenAnzahl);
                displayConfirmView(userRundenAnzahl.get(0));
            } else {
                Toast.makeText(Regatta.this, "Du musst alle Teilnehmer stoppen!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(Regatta.this, "Du musst erst einen Teilnehmer stoppen!", Toast.LENGTH_LONG).show();
        }
    }


    private boolean Connection() {
        boolean Wifi = false;
        boolean Mobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo NI : netInfo) {
            if (NI.getTypeName().equalsIgnoreCase("WIFI")) {
                if (NI.isConnected()) {
                    Wifi = true;
                }
            }
            if (NI.getTypeName().equalsIgnoreCase("MOBILE"))
                if (NI.isConnected()) {
                    Mobile = true;
                }
        }
        return Wifi || Mobile;
    }


    public void displayConfirmView(final int runde) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Sicher?");
        builder.setMessage("Die " + runde + " Runde wird ausgewählt.\n\nDer Übertragungsvorgang kann ein paar Sekunden in Anspruch nehmen. Bitte habe etwas geduld!");

        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Connection()) {

                    connectedRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean connected = snapshot.getValue(Boolean.class);
                            if (connected) {
                                zeitBerechnung(runde);
                            } else {
                                Toast.makeText(Regatta.this, "Keine Verbindung zur Datenbank. Probiere es erneut", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                } else {
                    Toast.makeText(Regatta.this, "Prüfe deine Internet verbindung", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Abbrechen", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void zeitBerechnung(int runde) {

        for (String e : userLastTime.keySet()) {
            zeitTabelle.put(e, userLastTime.get(e).get(runde - 1));
        }

        final Map<String, Integer> unsortMap = new HashMap<>();

        for (boolean e : checked) {
            if (e) {
                auswahlAnzahl++;
            }
        }


        for (final Map.Entry e : zeitTabelle.entrySet()) {
            if (!e.getValue().toString().equals("00:00:00")) {


                int zeitInSekunden;
                int berechneteYardstickzeit;

                String numbers = e.getValue().toString();
                String[] split = numbers.split(":");
                zeitInSekunden = Integer.parseInt(split[2]);
                zeitInSekunden += Integer.parseInt(split[1]) * 60;
                zeitInSekunden += Integer.parseInt(split[0]) * 60 * 60;
                berechneteYardstickzeit = (zeitInSekunden * 100) / yardStick.get(e.getKey());
                berechnungsZähler++;
                unsortMap.put(e.getKey().toString(), berechneteYardstickzeit);

                final String[] secondString = new String[3];


                final int a = berechneteYardstickzeit;
                final int stunden = a / 3600;
                final int minuten = (a % 3600) / 60;
                final int sekunden = (a % 3600) % 60;


                secondString[0] = Integer.toString(sekunden);
                if (sekunden <= 9) {
                    secondString[0] = "0" + sekunden;

                }
                secondString[1] = Integer.toString(minuten);
                if (minuten <= 9) {
                    secondString[1] = "0" + minuten;

                }
                secondString[2] = Integer.toString(stunden);
                if (stunden <= 9) {
                    secondString[2] = "0" + stunden;

                }

                berechnetteYardstickZeit.put(e.getKey().toString(), secondString[2] + ":" + secondString[1] + ":" + secondString[0]);


                //berechnung fertig
                if (berechnungsZähler >= auswahlAnzahl) {
                    Map<String, Integer> sortedMap = sortByValue(unsortMap);


                    int i = 0;
                    for (String key : sortedMap.keySet()) {
                        i++;
                        if (i == 1) {
                            alleUser.put(key, 0.);
                        }
                        if (i == 2) {
                            alleUser.put(key, 3.);
                        }
                        if (i == 3) {
                            alleUser.put(key, 5.7);
                        }
                        if (i == 4) {
                            alleUser.put(key, 8.);
                        }
                        if (i == 5) {
                            alleUser.put(key, 10.);
                        }
                        if (i == 6) {
                            alleUser.put(key, 11.7);
                        }
                        if (i >= 7) {
                            alleUser.put(key, i + 6.);
                        }
                    }

                    //bei neue Regatta
                    if (auswahl.equals("Neue Regatta")) {
                        for (String key : alleUser.keySet()) {
                            mDatabase.child("regatten").child(Integer.toString(regatten + 1)).child("1").child(key).setValue(alleUser.get(key));
                            if (!zeitTabelle.get(key).equals("00:00:00")) {
                                mDatabase.child("regatten").child(Integer.toString(regatten + 1)).child("1").child("NormaleZeit").child(key).setValue(zeitTabelle.get(key));
                            }
                            mDatabase.child("regatten").child(Integer.toString(regatten + 1)).child("1").child("VerrechneteZeit").child(key).setValue(berechnetteYardstickZeit.get(key));
                        }
                        regattaBeendet();
                    }


                    //Bei Neuer Lauf
                    else if (auswahl.equals("Neuer Lauf")) {
                        mDatabase.child("regatten").child(Integer.toString(regatten)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                lauf = 0;
                                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                                while (dataSnapshots.hasNext()) {
                                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                                    lauf++;
                                }
                                for (String key : alleUser.keySet()) {
                                    mDatabase.child("regatten").child(Integer.toString(regatten)).child(Integer.toString(lauf + 1)).child(key).setValue(alleUser.get(key));
                                    if (!zeitTabelle.get(key).equals("00:00:00")) {
                                        mDatabase.child("regatten").child(Integer.toString(regatten + 1)).child("1").child("NormaleZeit").child(key).setValue(zeitTabelle.get(key));
                                    }
                                    mDatabase.child("regatten").child(Integer.toString(regatten + 1)).child("1").child("VerrechneteZeit").child(key).setValue(berechnetteYardstickZeit.get(key));
                                }
                                regattaBeendet();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                }
            }
        }
    }


    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        /*
        //classic iterator example
        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }*/


        return sortedMap;
    }


    //timer
    public void startStoppuhr() {


        final String[] secondString = new String[3];

        if (!timerisrunning) {
            if (checked != null) {
                start = System.currentTimeMillis();
                btnStartTime.setText("Abbrechen");
                timerisrunning = true;
                stoppuhr.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        final long longseconds = (System.currentTimeMillis() - start) / 1000;
                        final int a = (int) longseconds;
                        final int stunden = a / 3600;
                        final int minuten = (a % 3600) / 60;
                        final int sekunden = (a % 3600) % 60;


                        secondString[0] = Integer.toString(sekunden);
                        if (sekunden <= 9) {
                            secondString[0] = "0" + sekunden;

                        }
                        secondString[1] = Integer.toString(minuten);
                        if (minuten <= 9) {
                            secondString[1] = "0" + minuten;

                        }
                        secondString[2] = Integer.toString(stunden);
                        if (stunden <= 9) {
                            secondString[2] = "0" + stunden;

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                timeview.setText(secondString[2] + ":" + secondString[1] + ":" + secondString[0]);
                            }
                        });
                    }
                }, 0, 1000);
            }
        } else {
            regattaAbbrechen();
        }
    }


    //Liste erstellen mit allen Auswählbaren Benutzern
    public void SelectUserData() {
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference();
        UserRef.keepSynced(true);

        final List<String> users = new ArrayList<>();
        final List<String> numbers = new ArrayList<>();


        UserRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    StringBuffer buffer = new StringBuffer();
                    numbers.add(dataSnapshotChild.getKey());

                    if (!zeitTabelle.containsKey(dataSnapshotChild.getKey())) {
                        zeitTabelle.put(dataSnapshotChild.getKey(), "00:00:00");
                    }

                    buffer.append("Name: " + dataSnapshotChild.child("Vorname").getValue().toString() + " " + dataSnapshotChild.child("Nachname").getValue().toString() + "\n");

                    users.add(buffer.toString());

                }

                if (checked == null) {
                    checked = new boolean[users.size()];
                }

                String[] userlist = new String[users.size()];
                userlist = users.toArray(userlist);
                displaySelectView("Teilnehmer auswählen:", userlist, checked, numbers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // for handling database error
                Toast.makeText(Regatta.this, "Eventuell keine Teilnehmer vorhanden", Toast.LENGTH_LONG).show();
            }
        });
    }


    //Liste der Teilnehmenden Benutzer anzeigen
    public void displaySelectView(final String title, final String[] userlist, final boolean[] checked, final List<String> numbers) {

        final List<String> usersid = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMultiChoiceItems(userlist, checked, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < checked.length; i++) {
                    if (checked[i]) {
                        usersid.add(numbers.get(i));
                    }
                }
                addUserstoList(usersid);


            }
        });
        builder.setNegativeButton("Abbrechen", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Ausgewählte Benutzer in Liste einfügen
    public void addUserstoList(final List<String> usersid) {

        //Datenbank Reference herstellen
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference();
        UserRef.keepSynced(true);

        //Teilnehmerliste erstellen <vorname+nachname>
        final List<String> users = new ArrayList<>();
        //Für jeden ausgewählten Benutzer Vor und Nachname in users abspeichern
        for (String i : usersid) {
            UserRef.child("users").child(i).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Wenn auf Datenbank zugegriffen werden kann:

                    StringBuffer buffer = new StringBuffer();

                    buffer.append(dataSnapshot.child("Vorname").getValue().toString() + " ");
                    buffer.append(dataSnapshot.child("Nachname").getValue().toString());

                    users.add(buffer.toString());
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //Wenn ein Fehler auftritt:
                }
            });
        }

        //User Liste eintragen
        UserRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Wenn auf Datenbank zugegriffen werden kann:

                myListAdapter = new MyListAdapter(Regatta.this, R.layout.regatta_items, users, usersid);
                list.setAdapter(myListAdapter);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Wenn ein Fehler auftritt:
            }
        });
    }
}

class MyListAdapter extends ArrayAdapter<String> {

    int layout;
    List<String> object;
    List<String> useduserid;

    public MyListAdapter(@NonNull Context context, int resource, @NonNull List<String> objects, List<String> usedusersid) {
        super(context, resource, objects);
        layout = resource;
        object = objects;
        useduserid = usedusersid;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder mainViewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);
            final ViewHolder viewHolder = new ViewHolder();

            //initalisieren
            viewHolder.name = convertView.findViewById(R.id.regatta_name);
            viewHolder.time = convertView.findViewById(R.id.regatta_timer);
            viewHolder.btnRundeHinzufügen = convertView.findViewById(R.id.rundeHinzufügen);
            viewHolder.btnRundeEntfernen = convertView.findViewById(R.id.rundeEntfernen);
            viewHolder.rundeTV = convertView.findViewById(R.id.rundeTV);
            viewHolder.btnClear = convertView.findViewById(R.id.btnClearTime);
            viewHolder.btnStop = convertView.findViewById(R.id.btnTimeStop);

            //ID und Name des Spielers eintragen
            viewHolder.id = useduserid.get(position);
            viewHolder.name.setText(object.get(position));

            /*static HashMap<String, Integer> runde = new HashMap<>();
            static HashMap<String, Integer> zurückgedrückt = new HashMap<>();
            static HashMap<String, Boolean> isUserClickable = new HashMap<>();
            static HashMap<String, List<String>> userLastTime = new HashMap<>();

             */
            if (Regatta.userLastTime.containsKey(viewHolder.id)) {
                viewHolder.runde = Regatta.runde.get(viewHolder.id);
                viewHolder.zurückGedrückt = Regatta.zurückgedrückt.get(viewHolder.id);
                viewHolder.editable = Regatta.isUserEditable.get(viewHolder.id);
                viewHolder.lastTime = Regatta.userLastTime.get(viewHolder.id);
                viewHolder.time.setText(viewHolder.lastTime.get(viewHolder.runde - 1));
                viewHolder.rundeTV.setText("Rnd: " + viewHolder.runde);
            } else {

                //Jeder Spieler der teilnimmt bekommt die Zeit von Runde eins auf null gesetzt
                viewHolder.lastTime.add(0, "00:00:00");

                //Wenn der Spieler in der Map existiert:
                // HashMap<String, Boolean> userclickable = new HashMap<>();
                if (Regatta.userclickable.get(viewHolder.id) != null) {
                    //Wenn der Spieler schon gestoppt wurde trage es im Viewholder ein
                    if (!Regatta.userclickable.get(viewHolder.id)) {
                        viewHolder.editable = false;
                        viewHolder.übertrageDaten();
                    }
                }
            }

            //String Array für die Zeit
            final String[] secondString = new String[3];
            //Zeitberechnung wenn der Spieler gestoppt wurde
            viewHolder.btnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Wenn die Zeit gestartet wurde und der Benutzer nicht schon gestoppt wurde
                    if (Regatta.timerisrunning && viewHolder.editable) {
                        Toast.makeText(getContext(), "Teilnehmer " + object.get(position) + " im Ziel", Toast.LENGTH_SHORT).show();

                        //die Sekunden werden umgerechnet im Stunden, Minuten und Sekunden
                        final long longseconds = (System.currentTimeMillis() - Regatta.start) / 1000;
                        final int a = (int) longseconds;
                        final int stunden = a / 3600;
                        final int minuten = (a % 3600) / 60;
                        final Integer sekunden = (a % 3600) % 60;

                        secondString[0] = Integer.toString(sekunden);
                        if (sekunden <= 9) {
                            secondString[0] = "0" + sekunden;
                        }

                        secondString[1] = Integer.toString(minuten);
                        if (minuten <= 9) {
                            secondString[1] = "0" + minuten;
                        }

                        secondString[2] = Integer.toString(stunden);
                        if (stunden <= 9) {
                            secondString[2] = "0" + stunden;
                        }

                        //Umgerechnete Zeit
                        String timestamp = secondString[2] + ":" + secondString[1] + ":" + secondString[0];

                        //Gestoppte Zeit wird dem Benutzer angezeigt
                        viewHolder.time.setText(timestamp);

                        //Benutzer soll nicht versehendelich mehrfach Gestoppt werden können
                        viewHolder.editable = false;
                        Regatta.userclickable.put(viewHolder.id, false);

                        //Speichere die Zeit
                        viewHolder.lastTime.set(viewHolder.runde - 1, timestamp);
                        //Regatta.userLastTime.put(viewHolder.id, viewHolder.lastTime);
                        viewHolder.übertrageDaten();

                    } else if (!viewHolder.editable) {
                        Toast.makeText(getContext(), "Teilnehmer wurde schon gestoppt", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Du musst zuerst die Zeit Starten", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //Neue Runde hinzufügen
            viewHolder.btnRundeHinzufügen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //wenn die Zeit gestartet wurde
                    if (Regatta.timerisrunning) {
                        //abfrage ob die momentane Runde schon gestoppt worden ist
                        if (!viewHolder.lastTime.get(viewHolder.runde - 1).equals("00:00:00")) {

                            //wenn das die letzte runde war füge eine neue runde hinzu mit dem wert null
                            if (viewHolder.zurückGedrückt == 0) {
                                viewHolder.lastTime.add(viewHolder.runde, "00:00:00");
                                viewHolder.editable = true;

                                //falls das nicht die letzte runde war
                            } else {
                                viewHolder.zurückGedrückt--;
                            }

                            //ändere die angezeigte runde und zeige den wert der "neuen" runde an
                            viewHolder.runde++;
                            viewHolder.rundeTV.setText("Rnd: " + viewHolder.runde);
                            viewHolder.time.setText(viewHolder.lastTime.get(viewHolder.runde - 1));
                            viewHolder.übertrageDaten();

                        } else {
                            Toast.makeText(getContext(), "Du musst zuerst die Zeit Stoppen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            //Gehe zur vorherigen runde
            viewHolder.btnRundeEntfernen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Es dürfen keine negativen runden möglich sein
                    if (viewHolder.runde > 1) {
                        //zeige die vorherige runde an, spieler darf nicht editierbar sein
                        viewHolder.runde--;
                        viewHolder.rundeTV.setText("Rnd: " + viewHolder.runde);
                        viewHolder.zurückGedrückt++;
                        viewHolder.editable = false;
                        viewHolder.time.setText(viewHolder.lastTime.get(viewHolder.runde - 1));
                        viewHolder.übertrageDaten();

                    }
                }
            });

            //Lösche die Genommene Zeit
            viewHolder.btnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //darf nur in der ersten oder letzten runde gelöscht werden. (alles löschen oder nur die letzte runde)
                    if (viewHolder.zurückGedrückt == 0 || viewHolder.runde == 1) {

                        viewHolder.time.setText("00:00:00");
                        viewHolder.editable = true;
                        //Regatta.userclickable.put(viewHolder.id, null);
                        viewHolder.lastTime.set(viewHolder.runde - 1, "00:00:00");
                        Regatta.userLastTime.put(viewHolder.id, viewHolder.lastTime);
                        viewHolder.übertrageDaten();

                    }
                    //wenn es die erste runde ist, werden die anderen runden auch gelöscht.
                    if (viewHolder.runde == 1) {

                        viewHolder.lastTime.clear();
                        viewHolder.lastTime.add(0, "00:00:00");
                        //Regatta.userLastTime.put(viewHolder.id, viewHolder.lastTime);
                        viewHolder.zurückGedrückt = 0;
                        viewHolder.übertrageDaten();

                    }
                }
            });
            convertView.setTag(viewHolder);
        } else {
            mainViewHolder = (ViewHolder) convertView.getTag();
            mainViewHolder.name.setText(getItem(position));
        }

        return convertView;
    }

}

class ViewHolder {
    TextView name, time, rundeTV;
    Button btnStop, btnClear, btnRundeEntfernen, btnRundeHinzufügen;

    //Ist der Spieler editierbar (Wurde die Zeit schon für den Spieler gestoppt?)
    Boolean editable = true;
    //ID des Spielers
    String id;
    //Die gespeicherten Runden Zeiten
    List<String> lastTime = new ArrayList<>();
    //die momentan angezeigte Runde
    int runde = 1;
    //Wie oft wurde zurückgedrückt (welche Runde wird momentan angezeigt)
    int zurückGedrückt = 0;

    public void übertrageDaten() {
        Regatta.isUserEditable.put(id, editable);
        Regatta.runde.put(id, runde);
        Regatta.zurückgedrückt.put(id, zurückGedrückt);
        Regatta.userLastTime.put(id, lastTime);
    }
}
