package de.wassersportclub.wcg;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
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

public class Historie extends AppCompatActivity {

    TextView willkommenstextTV;

    Button logoutBTN, regattaBTN, laufBTN, passwortÄndernBTN;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    int regatten;
    int lauf;
    int aktuellerLäufe = 1;
    int gesammtAnzahlLäufe = 0;
    double vorherigePunktzahl = 0;

    HashMap<String, Double> allePunkte = new HashMap<>();
    HashMap<String, List<Double>> einzelnePunkte= new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historie);
        willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
        willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

        regattaBTN = findViewById(R.id.RegattaAuswahlBtn);
        logoutBTN = findViewById(R.id.HEADERlogoutBTN);
        laufBTN = findViewById(R.id.LaufAuswahlBtn);
        passwortÄndernBTN = findViewById(R.id.passwortÄndernBTN);


        regattaBTN.setText("Regatta: 1");
        laufBTN.setText("Lauf: 1");



        regattenAnzahlErmitteln();

        doListen();
    }

    public void doListen(){
        logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        passwortÄndernBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Historie.this, Passwortaendern.class);
                startActivity(intent);
            }
        });
        regattaBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] regattaListe = new String[regatten];
                for (int i = 1; i <= regatten; i++) {
                    regattaListe[i-1] = "Regatta: "+i;
                }
                    AlertDialog.Builder builder = new AlertDialog.Builder(Historie.this);
                    builder.setCancelable(true);
                    builder.setTitle("Regatten");
                    builder.setItems(regattaListe, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    regattaBTN.setText("Regatta: " + (which + 1));
                                    laufBTN.setText("Lauf: 1");
                                    mDatabase.child("regatten").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            aktuellerLäufe = 0;
                                            Iterator<DataSnapshot> dataSnapshots = dataSnapshot.child(Integer.toString(hilfsvariable)).getChildren().iterator();
                                            while (dataSnapshots.hasNext()) {
                                                DataSnapshot dataSnapshotChild = dataSnapshots.next();
                                                aktuellerLäufe++;
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();

            }
        });
        laufBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] laufListe = new String[aktuellerLäufe];
                for (int i = 1; i <= aktuellerLäufe; i++) {
                    laufListe[i-1] = "Lauf: "+i;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(Historie.this);
                builder.setCancelable(true);
                builder.setTitle("Läufe");
                builder.setItems(laufListe, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        laufBTN.setText("Lauf: " + (which + 1));
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    //Loggt den benutzer aus
    public void logout(){
        mAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void regattenAnzahlErmitteln(){
        mDatabase.child("regatten").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                regatten = 0;
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    regatten++
                    ;
                }
                if(regatten != 0){
                    ranglisteErstellen(regatten);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public List<Double> get(String key) {
        return einzelnePunkte.get(key);
    }

    public void put(String key, Double value) {
        List<Double> list = get(key);
        if(list == null){
            list = new ArrayList<Double>();
            einzelnePunkte.put(key, list);
        }
        list.add(value);
    }

    int hilfsvariable;
    public void ranglisteErstellen(final int anzahlRegatten){

        final Map<String, String> useridListe = new HashMap<>();

        final List<String> rang = new ArrayList<>();
        final List<String> name = new ArrayList<>();
        final List<String> punkte = new ArrayList<>();
        name.add("Teilnehmer");
        punkte.add("Punkte");
        rang.add("Rang");

        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.child("").getChildren().iterator();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    useridListe.put(dataSnapshotChild.getKey(), dataSnapshotChild.child("Vorname").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        mDatabase.child("regatten").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //regatten anzahl
                for(int i= 1; i<=anzahlRegatten;i++) {
                    lauf = 0;
                    hilfsvariable = i;
                    //für jede regatta
                    Iterator<DataSnapshot> dataSnapshots = dataSnapshot.child(Integer.toString(hilfsvariable)).getChildren().iterator();
                    while (dataSnapshots.hasNext()) {
                        DataSnapshot dataSnapshotChild = dataSnapshots.next();
                        lauf++;
                        gesammtAnzahlLäufe++;

                        //für jeden lauf
                        Iterator<DataSnapshot> dataSnapshots2 = dataSnapshotChild.child("").getChildren().iterator();
                        while (dataSnapshots2.hasNext()) {
                            DataSnapshot dataSnapshotChild2 = dataSnapshots2.next();
                            put(dataSnapshotChild2.getKey(), Double.parseDouble(dataSnapshotChild2.getValue().toString()));
                            if (!allePunkte.containsKey(dataSnapshotChild2.getKey())) {
                                allePunkte.put(dataSnapshotChild2.getKey(), Double.parseDouble(dataSnapshotChild2.getValue().toString()));
                            }else{
                                allePunkte.put(dataSnapshotChild2.getKey(),allePunkte.get(dataSnapshotChild2.getKey())+Double.parseDouble(dataSnapshotChild2.getValue().toString()));
                            }
                        }
                    }
                }

                System.out.println("test "+allePunkte.toString());
                for (String key : allePunkte.keySet()) {
                    List<Double> list = get(key);
                    if(list == null){
                        list = new ArrayList<Double>();
                    }
                    Collections.sort(list);
                    if(allePunkte.get(key) != regatten*gesammtAnzahlLäufe*99) {
                        if (gesammtAnzahlLäufe == 5) {
                            allePunkte.put(key, allePunkte.get(key) - list.get(list.size() - 1));
                        } else if (gesammtAnzahlLäufe == 7) {
                            allePunkte.put(key, allePunkte.get(key) - list.get(list.size() - 1) - list.get(list.size() - 2));
                        }else if (gesammtAnzahlLäufe == 10) {
                            allePunkte.put(key, allePunkte.get(key) - list.get(list.size() - 1) - list.get(list.size() - 2) - list.get(list.size() - 3));
                        }
                    }
                }
                System.out.println("test "+allePunkte.toString());

                Map<String, Double> sortedMap = sortByValue(allePunkte);

                int i = 1;
                for (String key : sortedMap.keySet()) {
                    if(sortedMap.get(key) != regatten*gesammtAnzahlLäufe*99) {
                        if(vorherigePunktzahl != sortedMap.get(key) && (vorherigePunktzahl != 0)) {
                            i++;
                        }
                        vorherigePunktzahl = sortedMap.get(key);
                        rang.add(Integer.toString(i));
                        name.add(useridListe.get(key));
                        double temp = sortedMap.get(key) * 100;
                        temp = Math.round(temp);
                        temp = temp / 100;
                        punkte.add(Double.toString(temp));
                    }
                }
                ListView list = findViewById(R.id.rangliste);
                list.setAdapter(new MyListAdapterRang(Historie.this, R.layout.rangliste_zeile, name, punkte, rang));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    private static Map<String, Double> sortByValue(Map<String, Double> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Double>> list =
                new LinkedList<>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
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
}