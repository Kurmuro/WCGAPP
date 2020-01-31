package de.wassersportclub.wcg;

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
import androidx.appcompat.app.AlertDialog;
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

public class Startseite extends AppCompatActivity {

    TextView willkommenstextTV;
    Button logoutBTN, stegbelegungBTN, verwaltungBTN, blauesbandBTN, regattaBTN, ranglisteAuswahl;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    int regatten;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminstartseite);
        willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
        willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

        logoutBTN = findViewById(R.id.HEADERlogoutBTN);
        stegbelegungBTN = findViewById(R.id.STARTSEITEstegbelegungBTN);
        verwaltungBTN = findViewById(R.id.STARTSEITEverwaltungBTN);
        blauesbandBTN = findViewById(R.id.STARTSEITEblauesbandBTN);
        regattaBTN = findViewById(R.id.STARTSEITEregattaBTN);
        ranglisteAuswahl = findViewById(R.id.ranglisteWechelBtn);

        //Startet den Listener für alle buttons
        doListen();

        regattenAnzahlErmitteln();

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
        verwaltungBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Startseite.this, TeilnehmerVerwaltung.class);
                startActivity(intent);
            }
        });
        blauesbandBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Startseite.this, Blauesband.class);
                startActivity(intent);
            }
        });
        regattaBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Startseite.this, Regatta.class);
                startActivity(intent);
            }
        });
        ranglisteAuswahl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] liste = new String[regatten];
                for (int i = regatten; i>0; i--) {
                    liste[i-1] = "Regatta "+i;
                }
                    AlertDialog.Builder builder = new AlertDialog.Builder(Startseite.this);
                    builder.setCancelable(true);
                    builder.setTitle("Stand bis");
                    builder.setItems(liste, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position = which+1;
                            ranglisteAuswahl.setText("Stand bis Regattalauf "+position);
                            ranglisteErstellen(position);
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
                    regatten++;
                }
                if(regatten != 0){
                    ranglisteErstellen(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void ranglisteErstellen(int anzahlRegatten){

        final Map<String, String> useridListe = new HashMap<>();
        final Map<String, Integer> unsortMap = new HashMap<>();

        final List<String> rang = new ArrayList<>();
        final List<String> name = new ArrayList<>();
        final List<String> punkte = new ArrayList<>();
        name.add("Teilnehmer");
        punkte.add("Punkte");
        rang.add("Rang");

        for (int i = anzahlRegatten; i>0; i--) {
            final int p = i;
            mDatabase.child("regatten").child(Integer.toString(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();

                    while (dataSnapshots.hasNext()) {
                        DataSnapshot dataSnapshotChild = dataSnapshots.next();

                        if(!unsortMap.containsKey(dataSnapshotChild.getKey())) {
                            unsortMap.put(dataSnapshotChild.getKey(), Integer.parseInt(dataSnapshotChild.child("Punkte").getValue().toString()));
                        }else{
                            unsortMap.put(dataSnapshotChild.getKey(),unsortMap.get(dataSnapshotChild.getKey())+Integer.parseInt(dataSnapshotChild.child("Punkte").getValue().toString()));
                        }

                        if(!useridListe.containsKey(dataSnapshotChild.getKey())) {
                            useridListe.put(dataSnapshotChild.getKey(), dataSnapshotChild.child("Name").getValue().toString());
                        }
                    }

                    if(p == 1){
                        Map<String, Integer> sortedMap = sortByValue(unsortMap);
                        int i = 0;
                        for (String key : sortedMap.keySet()) {
                            i++;
                            rang.add(Integer.toString(i));
                            name.add(useridListe.get(key));
                            punkte.add(Integer.toString(sortedMap.get(key)));

                        }

                        ListView list = findViewById(R.id.rangliste);
                        list.setAdapter(new MyListAdapterRang(Startseite.this, R.layout.rangliste_zeile, name, punkte, rang));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
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
        if(convertView == null){
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
        }
        else{
            mainViewHolder = (ViewHolder) convertView.getTag();
            mainViewHolder.name.setText(getItem(position));
        }

        return convertView;
    }

}
class ViewHolderRang{
    TextView rang;
    TextView name;
    TextView punktzahl;

}

