package de.wassersportclub.wcg;

import android.content.Context;
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
import java.util.Timer;
import java.util.TimerTask;

public class Startseite extends AppCompatActivity {

    TextView willkommenstextTV;
    Button logoutBTN, stegbelegungBTN, verwaltungBTN, blauesbandBTN, regattaBTN, historieBTN, passwortÄndernBTN;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    int regatten;
    int lauf;
    int gesammtAnzahlLäufe = 0;
    double vorherigePunktzahl = 0;
    ListView list;
    Timer t = new Timer();
    HashMap<String, Double> allePunkte = new HashMap<>();
    HashMap<String, List<Double>> einzelnePunkte= new HashMap<>();
    List<String> rang = new ArrayList<>();
    List<String> name = new ArrayList<>();
    List<String> punkte = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.benutzerstartseite);

        if(mAuth.getCurrentUser().getEmail().equals("marcelianer36@gmail.com") || mAuth.getCurrentUser().getEmail().equals("ma.walter@bhg-mobile.de")) {
            setContentView(R.layout.adminstartseite);
            willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
            willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

            logoutBTN = findViewById(R.id.HEADERlogoutBTN);
            stegbelegungBTN = findViewById(R.id.STARTSEITEstegbelegungBTN);
            verwaltungBTN = findViewById(R.id.STARTSEITEverwaltungBTN);
            blauesbandBTN = findViewById(R.id.STARTSEITEblauesbandBTN);
            regattaBTN = findViewById(R.id.STARTSEITEregattaBTN);
            historieBTN = findViewById(R.id.HistorieBtn);
            passwortÄndernBTN = findViewById(R.id.passwortÄndernBTN);
            list = findViewById(R.id.rangliste);

            //Startet den Listener für alle buttons
            doListen();



            t.schedule(new TimerTask(){

                @Override
                public void run() {
                    regattenAnzahlErmitteln();
                }

            }, 0, 5000); //alle 5 sekunden...


            return;
        }

        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.child("").getChildren().iterator();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    if(mAuth.getCurrentUser().getUid().equals(dataSnapshotChild.getKey())) {
                        if (dataSnapshotChild.child("Admin").exists()) {
                            if (dataSnapshotChild.child("Admin").getValue().toString().equals("true")) {
                                setContentView(R.layout.adminstartseite);
                            }
                        }
                    }
                }

                willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
                willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

                logoutBTN = findViewById(R.id.HEADERlogoutBTN);
                stegbelegungBTN = findViewById(R.id.STARTSEITEstegbelegungBTN);
                verwaltungBTN = null;
                blauesbandBTN = findViewById(R.id.STARTSEITEblauesbandBTN);
                regattaBTN = null;
                historieBTN = findViewById(R.id.HistorieBtn);
                passwortÄndernBTN = findViewById(R.id.passwortÄndernBTN);
                list = findViewById(R.id.rangliste);

                //Startet den Listener für alle buttons
                doListen();

                t.schedule(new TimerTask(){

                    @Override
                    public void run() {
                        regattenAnzahlErmitteln();
                    }

                }, 0, 5000);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
                willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

                logoutBTN = findViewById(R.id.HEADERlogoutBTN);
                stegbelegungBTN = findViewById(R.id.STARTSEITEstegbelegungBTN);
                verwaltungBTN = findViewById(R.id.STARTSEITEverwaltungBTN);
                blauesbandBTN = findViewById(R.id.STARTSEITEblauesbandBTN);
                regattaBTN = findViewById(R.id.STARTSEITEregattaBTN);
                historieBTN = findViewById(R.id.HistorieBtn);
                passwortÄndernBTN = findViewById(R.id.passwortÄndernBTN);
                list = findViewById(R.id.rangliste);

                //Startet den Listener für alle buttons
                doListen();

                t.schedule(new TimerTask(){

                    @Override
                    public void run() {
                        regattenAnzahlErmitteln();
                    }

                }, 0, 5000);
            }
        });

    }

    public void onBackPressed(){
        return;
    }

    //Hört ob knöpfe gedrückt wurden
    public void doListen(){
        logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        stegbelegungBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Startseite.this, Steganlage.class);
                startActivity(intent);
            }
        });
        if(regattaBTN != null) {
            verwaltungBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Startseite.this, TeilnehmerVerwaltung.class);
                    startActivity(intent);
                }
            });
        }
        blauesbandBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Startseite.this, Blauesband.class);
                startActivity(intent);
            }
        });
        if(regattaBTN != null) {
            regattaBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Startseite.this, RegattaAuswahl.class);
                    finish();
                    startActivity(intent);
                }
            });
        }
        historieBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Startseite.this, Historie.class);
                startActivity(intent);
            }
        });
        passwortÄndernBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Startseite.this, Passwortaendern.class);
                startActivity(intent);
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
        punkte.clear();
        rang.clear();
        name.clear();
        allePunkte.clear();
        einzelnePunkte.clear();
        regatten = 0;
        lauf = 0;
        gesammtAnzahlLäufe = 0;
        vorherigePunktzahl = 0;
        mDatabase.child("regatten").addListenerForSingleValueEvent(new ValueEventListener()
        {
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
        final Map<String, Integer> unsortMap = new HashMap<>();


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
                for (String key : allePunkte.keySet()) {
                    List<Double> list = get(key);
                    if(list == null){
                        list = new ArrayList<Double>();
                    }
                    Collections.sort(list);
                    if(allePunkte.get(key) != gesammtAnzahlLäufe*99) {
                        if (gesammtAnzahlLäufe == 5) {
                            allePunkte.put(key, allePunkte.get(key) - list.get(list.size() - 1));
                        } else if (gesammtAnzahlLäufe == 7) {
                            allePunkte.put(key, allePunkte.get(key) - list.get(list.size() - 1) - list.get(list.size() - 2));
                        }else if (gesammtAnzahlLäufe == 10) {
                            allePunkte.put(key, allePunkte.get(key) - list.get(list.size() - 1) - list.get(list.size() - 2) - list.get(list.size() - 3));
                        }
                    }
                }

                Map<String, Double> sortedMap = sortByValue(allePunkte);

                int i = 1;
                for (String key : sortedMap.keySet()) {
                    if(sortedMap.get(key) != gesammtAnzahlLäufe*99) {
                        if(vorherigePunktzahl != sortedMap.get(key)){// && (vorherigePunktzahl != 0)) { falls fehler mit gleichem rang
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
                list.setAdapter(new MyListAdapterRang(Startseite.this, R.layout.rangliste_zeile, name, punkte, rang));
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

class MyListAdapterRang extends ArrayAdapter<String> {

    int layout;
    List<String> name;
    List<String> punktzahl;
    List<String> rang;

    public MyListAdapterRang(@NonNull Context context, int resource, @NonNull List<String> teilnehmer, List<String> punkte, List<String> rangwert) {
        super(context, resource, teilnehmer);
        layout = resource;
        name = teilnehmer;
        punktzahl = punkte;
        rang = rangwert;
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

            viewHolder.rang = convertView.findViewById(R.id.ranglisteRangTV);
            viewHolder.rang.setText(rang.get(position));

            viewHolder.punktzahl = convertView.findViewById(R.id.ranglistePunkteTV);
            viewHolder.punktzahl.setText(punktzahl.get(position));


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
    TextView rang;
    TextView name;
    TextView punktzahl;

}

