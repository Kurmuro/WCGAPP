package de.wassersportclub.wcg;

import android.content.Context;
import android.content.DialogInterface;
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

    String auswahl = "Auswahl";
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    Map<String, Double> alleUser = new HashMap<>();
    static HashMap<String, String> zeitTabelle = new HashMap<>();
    static HashMap<String, Boolean> userclickable = new HashMap<>();

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

        auswahl = getIntent().getStringExtra("auswahl");
        auswahlview.setText(auswahl);

        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    alleUser.put(dataSnapshotChild.getKey(), 99.);
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
        stoppuhr.cancel();
        checked = null;
        timerisrunning = false;
        zeitTabelle.clear();
        finish();
        System.exit(0);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
                zeitBerechnung();
            }
        });

    }

    private void zeitBerechnung() {
        if(!zeitTabelle.isEmpty()){

            final Map<String, Integer> unsortMap = new HashMap<>();

            for (boolean e :checked){
                if(e){
                    auswahlAnzahl++;
                }
            }


            for(final Map.Entry e : zeitTabelle.entrySet()){
                if(!e.getValue().toString().equals("00:00:00")) {

                    //mit dem yardstick berechnen
                    mDatabase.child("users").child(e.getKey().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int yardstickDb = Integer.parseInt(dataSnapshot.child("Yardstick").getValue().toString());

                            int zeitInSekunden;
                            int berechneteYardstickzeit;

                            String numbers = e.getValue().toString();
                            String[] split = numbers.split(":");
                            zeitInSekunden = Integer.parseInt(split[2]);
                            zeitInSekunden += Integer.parseInt(split[1])*60;
                            zeitInSekunden += Integer.parseInt(split[0])*60*60;
                            berechneteYardstickzeit = (zeitInSekunden*100)/yardstickDb;
                            berechnungsZähler++;
                            unsortMap.put(e.getKey().toString(),berechneteYardstickzeit);//hilfe


                            //berechnung fertig
                            if (berechnungsZähler >= auswahlAnzahl){
                                Map<String, Integer> sortedMap = sortByValue(unsortMap);


                                int i = 0;
                                for (String key : sortedMap.keySet()) {
                                    i++;
                                    if (i == 1){
                                        alleUser.put(key, 1.);
                                    }
                                    if(i == 2){
                                        alleUser.put(key, 3.);
                                    }
                                    if(i == 3){
                                        alleUser.put(key, 5.7);
                                    }
                                    if(i == 4){
                                        alleUser.put(key, 8.);
                                    }
                                    if(i == 5){
                                        alleUser.put(key, 10.);
                                    }
                                    if(i == 6){
                                        alleUser.put(key, 11.7);
                                    }
                                    if(i >= 7){
                                        alleUser.put(key, i+6.);
                                    }
                                }








                                //bei neue Regatta
                                if(auswahl.equals("Neue Regatta")){
                                    for (String key : alleUser.keySet()) {
                                        mDatabase.child("regatten").child(Integer.toString(regatten+1)).child("1").child(key).setValue(alleUser.get(key));
                                        Toast.makeText(Regatta.this, "Erfolgreich gespeichert!", Toast.LENGTH_LONG).show();
                                        regattaAbbrechen();
                                    }
                                }


                                //Bei Neuer Lauf
                                else if(auswahl.equals("Neuer Lauf")){
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
                                                mDatabase.child("regatten").child(Integer.toString(regatten)).child(Integer.toString(lauf+1)).child(key).setValue(alleUser.get(key));
                                                Toast.makeText(Regatta.this, "Erfolgreich gespeichert!", Toast.LENGTH_LONG).show();
                                                regattaAbbrechen();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }







                            }




                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
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

                    if(!zeitTabelle.containsKey(dataSnapshotChild.getKey())){
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
                Toast.makeText(Regatta.this, "Eventuel keine Teilnehmer vorhanden", Toast.LENGTH_LONG).show();
            }
        });
    }


    //Liste der Teilnehmenden Benutzer anzeigen
    public void displaySelectView(final String title, final String[] userlist, final boolean[] checked, final List<String> numbers){

        final List<String> usersid = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMultiChoiceItems(userlist, checked , new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for(int i = 0; i < checked.length; i++){
                    if(checked[i]){
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

                ListView list = findViewById(R.id.auswahlliste);
                list.setAdapter(new MyListAdapter(Regatta.this, R.layout.regatta_items, users, usersid));
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
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = convertView.findViewById(R.id.regatta_name);
            viewHolder.name.setText(object.get(position));
            viewHolder.time = convertView.findViewById(R.id.regatta_timer);

            viewHolder.btnClear = convertView.findViewById(R.id.regatta_btn_clear);
            viewHolder.btnStop = convertView.findViewById(R.id.regatta_btn_stop);
            viewHolder.id = useduserid.get(position);
            if(!Regatta.zeitTabelle.get(viewHolder.id).contains("00:00:00")){
                viewHolder.time.setText(Regatta.zeitTabelle.get(viewHolder.id));
            }
            if(Regatta.userclickable.get(viewHolder.id) != null) {
                if (!Regatta.userclickable.get(viewHolder.id)) {
                    viewHolder.editable = false;
                }
            }
            final String[] secondString = new String[3];
            viewHolder.btnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean test = Regatta.timerisrunning;
                    if(test && viewHolder.editable) {
                        Toast.makeText(getContext(), "Teilnehmer " + object.get(position) + " im Ziel", Toast.LENGTH_SHORT).show();
                        final long longseconds = (System.currentTimeMillis() - Regatta.start)/1000;
                        final int a = (int)longseconds;
                        final int stunden = a / 3600;
                        final int minuten = (a % 3600) / 60;
                        final Integer sekunden = (a % 3600) % 60;


                        secondString[0] = Integer.toString(sekunden);
                        if(sekunden <=9) {
                            secondString[0] = "0" + sekunden;

                        }
                        secondString[1] = Integer.toString(minuten);
                        if(minuten <=9) {
                            secondString[1] = "0" + minuten;

                        }
                        secondString[2] = Integer.toString(stunden);
                        if(stunden <=9) {
                            secondString[2] = "0" + stunden;

                        }
                        String timestamp = secondString[2]+":"+secondString[1]+":"+ secondString[0];
                        viewHolder.time.setText(timestamp);
                        Regatta.zeitTabelle.put(viewHolder.id, timestamp);
                        viewHolder.editable = false;
                        Regatta.userclickable.put(viewHolder.id,false);
                    }else if(!viewHolder.editable){
                        Toast.makeText(getContext(), "Teilnehmer wurde schon gestoppt", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getContext(), "Du musst zuerst die Zeit Starten", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            viewHolder.btnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.time.setText("00:00:00");
                    Regatta.zeitTabelle.put(viewHolder.id,"00:00:00");
                    viewHolder.editable = true;
                    Regatta.userclickable.put(viewHolder.id,null);
                }
            });
            convertView.setTag(viewHolder);
        }
        else{
            mainViewHolder = (ViewHolder) convertView.getTag();
            mainViewHolder.name.setText(getItem(position));
        }

        return convertView;
    }

}
class ViewHolder{
    Boolean editable = true;
    TextView name;
    TextView time;
    Button btnStop;
    Button btnClear;
    String id;
}
