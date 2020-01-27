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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Regatta extends AppCompatActivity {

    Button btnStartTime, btnTeilnehmerAuswählen, btnRegattaFertig;
    Timer stoppuhr;
    TextView timeview;
    boolean[] checked;
    static long start;
    static boolean timerisrunning;

    static HashMap<String, String> zeitTabelle = new HashMap<>();
    static HashMap<String, Boolean> userclickable = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regatta);

        timeview = findViewById(R.id.EtTime);
        btnStartTime = findViewById(R.id.btnStartTime);
        stoppuhr = new Timer();
        btnTeilnehmerAuswählen = findViewById(R.id.btnTeilnehmerAuswählen);
        btnRegattaFertig = findViewById(R.id.btnRegattaFertig);

        doListen();

    }

    //Listener
    private void doListen() {
        btnStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStoppuhr();
            }
        });
        btnTeilnehmerAuswählen.setOnClickListener(new View.OnClickListener() {
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
            for(Map.Entry e : zeitTabelle.entrySet()){
                if(e.getValue().toString() != "00:00:00") {
                    System.out.println("test " + e.getKey() + " = " + e.getValue());
                }
            }
        }
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
                stoppuhr.cancel();
                checked = null;
                timerisrunning = false;
                finish();
            }
        }









    //Liste erstellen mit allen Auswählbaren Benutzern
    public void SelectUserData() {
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference();
        UserRef.keepSynced(true);

        final List<String> users = new ArrayList<String>();
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

                        System.out.println("test1" + " " + users);
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

                ListView list = findViewById(R.id.rangliste);
                list.setAdapter(new MyListAdapter(Regatta.this, R.layout.regatta_items, users, usersid));
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Wenn ein Fehler auftritt:
            }
        });
    }


    //regatta beenden
    public void regattaBeenden(View view){

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
        ViewHolder mainViewHolder = null;
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.regatta_name);
            viewHolder.name.setText(object.get(position));
            viewHolder.time = (TextView) convertView.findViewById(R.id.regatta_timer);

            viewHolder.btnClear =(Button) convertView.findViewById(R.id.regatta_btn_clear);
            viewHolder.btnStop = (Button) convertView.findViewById(R.id.regatta_btn_stop);
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
