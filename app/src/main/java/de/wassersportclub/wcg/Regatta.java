package de.wassersportclub.wcg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Regatta extends AppCompatActivity {

    Button btnStartTime, btnTeilnehmerAuswählen;
    Timer stoppuhr;
    TextView timeview;
    //  boolean[] checked;
    static long start;
    static boolean timerisrunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regatta);

        timeview = findViewById(R.id.EtTime);
        btnStartTime = findViewById(R.id.btnStartTime);
        stoppuhr = new Timer();
        btnTeilnehmerAuswählen = findViewById(R.id.btnTeilnehmerAuswählen);

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
                getAllUsersFromFirebase();
            }
        });


    }

    //timer
    public void startStoppuhr() {


        final String[] secondString = new String[3];

        if (!timerisrunning) {
            // if(checked != null) {
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
            //  }
        } else {
            stoppuhr.cancel();
            // checked = null;
            timerisrunning = false;
        }
    }

    public void getAllUsersFromFirebase() {
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference();
        UserRef.keepSynced(true);
        final List<String> users = new ArrayList<>();
        final List<String> passwort = new ArrayList<>();
        final List<String> email = new ArrayList<>();
        final List<String> useruid = new ArrayList<>();

        UserRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    StringBuffer buffer = new StringBuffer();

                    buffer.append("Name: " + dataSnapshotChild.child("Vorname").getValue().toString() + " " + dataSnapshotChild.child("Nachname").getValue().toString() + "\n");
                    buffer.append("Bootstyp: " + dataSnapshotChild.child("Bootstyp").getValue() + "\n");

                    users.add(buffer.toString());
                    email.add(dataSnapshotChild.child("Email").getValue().toString());
                    useruid.add(dataSnapshotChild.getKey());


                }
                String[] userlist = new String[users.size()];
                userlist = users.toArray(userlist);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // for handling database error
                Toast.makeText(Regatta.this, "Eventuel keine Teilnehmer vorhanden", Toast.LENGTH_LONG).show();
            }
        });
    }

    //Regattateilnehmer Liste

    public void regattaTeilnehmer(final String title, final String[] userlist, final boolean[] checked, final List<Integer> numbers) {

        final List<Integer> usersid = new ArrayList<>();

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
                //addUserstoList(usersid);
            }
        });
        builder.setNegativeButton("Abbrechen", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    class MyListAdapter extends ArrayAdapter<String> {

        int layout;
        List<String> object;
        List<Integer> useduserid;

        public MyListAdapter(@NonNull Context context, int resource, @NonNull List<String> objects, List<Integer> usedusersid) {
            super(context, resource, objects);
            layout = resource;
            object = objects;
            useduserid = usedusersid;
        }

    }
}

