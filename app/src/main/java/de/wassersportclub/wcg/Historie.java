package de.wassersportclub.wcg;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    int aktuelleRegatta = 1;
    int LäufeAktuellerRegatta = 1;
    int gesammtAnzahlLäufe = 0;
    double vorherigePunktzahl = 0;

    Map<String, String> useridListe = new HashMap<>();
    Map<String, String> crewmitglieder = new HashMap<>();
    HashMap<String, Double> Punkte = new HashMap<>();
    Map<String, String> normaleZeit = new HashMap<>();
    Map<String, String> berechneteZeit = new HashMap<>();

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
                                public void onClick(DialogInterface dialog, final int which) {
                                    regattaBTN.setText("Regatta: " + (which + 1));
                                    aktuelleRegatta = which + 1;
                                    laufBTN.setText("Lauf: 1");
                                    mDatabase.child("regatten").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            LäufeAktuellerRegatta = 0;
                                            Iterator<DataSnapshot> dataSnapshots = dataSnapshot.child(Integer.toString(which + 1)).getChildren().iterator();
                                            while (dataSnapshots.hasNext()) {
                                                DataSnapshot dataSnapshotChild = dataSnapshots.next();
                                                LäufeAktuellerRegatta++;
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                    ranglisteErstellen(which + 1, 1);
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();

            }
        });
        laufBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] laufListe = new String[LäufeAktuellerRegatta];
                for (int i = 1; i <= LäufeAktuellerRegatta; i++) {
                    laufListe[i-1] = "Lauf: "+i;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(Historie.this);
                builder.setCancelable(true);
                builder.setTitle("Läufe");
                builder.setItems(laufListe, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        laufBTN.setText("Lauf: " + (which + 1));
                        ranglisteErstellen(aktuelleRegatta, which + 1);
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

    //regattenanzahl und läufe der ersten regatta ermitteln
    public void regattenAnzahlErmitteln(){


        mDatabase.child("regatten").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //regattenanzahl ermitteln
                regatten = 0;
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    regatten++
                    ;
                }

                if(regatten != 0) {
                    LäufeAktuellerRegatta = 0;
                    Iterator<DataSnapshot> dataSnapshots2 = dataSnapshot.child("1").getChildren().iterator();
                    while (dataSnapshots2.hasNext()) {
                        DataSnapshot dataSnapshotChild = dataSnapshots2.next();
                        LäufeAktuellerRegatta++;
                    }

                    ranglisteErstellen(1, 1);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //useridListe Befüllen (id und vorname)
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.child("").getChildren().iterator();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    useridListe.put(dataSnapshotChild.getKey(), dataSnapshotChild.child("Vorname").getValue().toString());
                    crewmitglieder.put(dataSnapshotChild.getKey(), dataSnapshotChild.child("Nachname").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public void ranglisteErstellen(final int RegattaNummer, final int LaufNummer){

        Punkte.clear();

        final List<String> rang = new ArrayList<>();
        final List<String> name = new ArrayList<>();
        final List<String> crew = new ArrayList<>();
        final List<String> punkte = new ArrayList<>();
        final List<String> sortiertenormaleZeit = new ArrayList<>();
        final List<String> sortierteberechneteZeit = new ArrayList<>();





        mDatabase.child("regatten").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {






                //gehe alle teilnehmer vom lauf durch
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.child(Integer.toString(RegattaNummer)).child(Integer.toString(LaufNummer)).getChildren().iterator();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    if(!dataSnapshotChild.getKey().equals("NormaleZeit")) {
                        if (!dataSnapshotChild.getKey().equals("VerrechneteZeit")) {
                            Punkte.put(dataSnapshotChild.getKey(), Double.parseDouble(dataSnapshotChild.getValue().toString()));
                        }
                    }

                    if(dataSnapshotChild.getKey().equals("NormaleZeit")) {
                        //gehe alle teilnehmer von NormaleZeit durch
                        Iterator<DataSnapshot> dataSnapshots2 = dataSnapshot.child(Integer.toString(RegattaNummer)).child(Integer.toString(LaufNummer)).child("NormaleZeit").getChildren().iterator();
                        while (dataSnapshots2.hasNext()) {
                            DataSnapshot dataSnapshotChild2 = dataSnapshots2.next();
                            normaleZeit.put(dataSnapshotChild2.getKey(), dataSnapshotChild2.getValue().toString());
                        }
                    }

                    if (!dataSnapshotChild.getKey().equals("VerrechneteZeit")) {
                        //gehe alle teilnehmer von BerechnetteZeit durch
                        Iterator<DataSnapshot> dataSnapshots3 = dataSnapshot.child(Integer.toString(RegattaNummer)).child(Integer.toString(LaufNummer)).child("VerrechneteZeit").getChildren().iterator();
                        while (dataSnapshots3.hasNext()) {
                            DataSnapshot dataSnapshotChild3 = dataSnapshots3.next();
                            berechneteZeit.put(dataSnapshotChild3.getKey(), dataSnapshotChild3.getValue().toString());
                        }
                    }
                }

                Map<String, Double> sortedMap = sortByValue(Punkte);

                boolean firsttime = true;
                int i = 1;
                for (String key : sortedMap.keySet()) {
                    if(normaleZeit.containsKey(key)) {
                        sortiertenormaleZeit.add(normaleZeit.get(key));
                        sortierteberechneteZeit.add(berechneteZeit.get(key));
                    }else{
                        sortiertenormaleZeit.add(" ");
                        sortierteberechneteZeit.add(" ");
                    }
                    if (firsttime == false) {
                            if (vorherigePunktzahl != sortedMap.get(key) && (vorherigePunktzahl != -1)) {
                                i++;
                            }
                            vorherigePunktzahl = sortedMap.get(key);
                            rang.add(Integer.toString(i));
                            name.add(useridListe.get(key));
                            crew.add(crewmitglieder.get(key));
                            double temp = sortedMap.get(key) * 100;
                            temp = Math.round(temp);
                            temp = temp / 100;
                            punkte.add(Double.toString(temp));

                    } else {
                        vorherigePunktzahl = sortedMap.get(key);
                        firsttime = false;
                        rang.add(Integer.toString(i));
                        name.add(useridListe.get(key));
                        crew.add(crewmitglieder.get(key));
                        double temp = sortedMap.get(key) * 100;
                        temp = Math.round(temp);
                        temp = temp / 100;
                        punkte.add(Double.toString(temp));
                    }
                }
                ListView list = findViewById(R.id.rangliste);
                list.setAdapter(new MyListAdapterRangHistorie(Historie.this, R.layout.historie_zeile, name, punkte, crew, rang, sortiertenormaleZeit, sortierteberechneteZeit));
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

    class MyListAdapterRangHistorie extends ArrayAdapter<String> {

        int layout;
        List<String> crew;
        List<String> name;
        List<String> punktzahl;
        List<String> rang;
        List<String> normaleZeit;
        List<String> berechneteZeit;


        public MyListAdapterRangHistorie(@NonNull Context context, int resource, @NonNull List<String> teilnehmer, List<String> punkte,List<String>  crewm, List<String> rangwert, List<String> sortiertenormaleZeit, List<String> sortierteberechneteZeit) {
            super(context, resource, teilnehmer);
            layout = resource;
            name = teilnehmer;
            crew = crewm;
            punktzahl = punkte;
            rang = rangwert;
            normaleZeit = sortiertenormaleZeit;
            berechneteZeit = sortierteberechneteZeit;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder mainViewHolder;
            //if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);
            final ViewHolderRang viewHolder = new ViewHolderRang();

            viewHolder.name = convertView.findViewById(R.id.ranglisteNameTV);
            viewHolder.name.setText(name.get(position));

            viewHolder.crew = convertView.findViewById(R.id.ranglisteCrew);
            viewHolder.crew.setText(crew.get(position));

            viewHolder.rang = convertView.findViewById(R.id.ranglisteRangTV);
            viewHolder.rang.setText(rang.get(position));

            viewHolder.punktzahl = convertView.findViewById(R.id.ranglistePunkteTV);
            viewHolder.punktzahl.setText(punktzahl.get(position));

            viewHolder.normaleZeit = convertView.findViewById(R.id.ranglisteGestoppteZeitTV);
            viewHolder.normaleZeit.setText(normaleZeit.get(position));

            viewHolder.berechneteZeit = convertView.findViewById(R.id.ranglisteBerechneteZeitTV);
            viewHolder.berechneteZeit.setText(berechneteZeit.get(position));




            convertView.setTag(viewHolder);
            //}
            //else{
            //    mainViewHolder = (ViewHolder) convertView.getTag();
            //   mainViewHolder.name.setText(getItem(position));
            //}

            return convertView;
        }

    }
    class ViewHolderRang{
        TextView rang, name,crew, punktzahl, normaleZeit, berechneteZeit;

    }
}