package de.wassersportclub.wcg;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlauesbandZeitnahmeManuell extends AppCompatActivity {

    TextView willkommenstextTV, textViewName, textViewBootstyp, textViewYardstick;
    Button btnTeilnehmerBBauswählen, passwortÄndernBTN, logoutBTN, zeitBestätigenBtn;
    EditText editTextZeit;
    boolean[] checked;
    int Yardstick;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference mDatabase;
    DatabaseReference BlauesbandZeitenRef;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blauesband_zeitnahme_manuell);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid());
        BlauesbandZeitenRef = FirebaseDatabase.getInstance().getReference().child("Blauesband-Zeiten").child(mAuth.getUid());

        willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
        textViewName = findViewById(R.id.textViewName);
        textViewBootstyp = findViewById(R.id.textViewBootstyp);
        textViewYardstick = findViewById(R.id.textViewYardstick);
        btnTeilnehmerBBauswählen = findViewById(R.id.btnVereinsbootBBauswählen);
        passwortÄndernBTN = findViewById(R.id.passwortÄndernBTN);
        logoutBTN = findViewById(R.id.HEADERlogoutBTN);
        editTextZeit = findViewById(R.id.editTextZeit);
        zeitBestätigenBtn = findViewById(R.id.zeitBestätigenBtn);


        willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());




        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                textViewName.setText(dataSnapshot.child("Vorname").getValue().toString() + " " + dataSnapshot.child("Nachname").getValue().toString());
                textViewBootstyp.setText(dataSnapshot.child("Bootstyp").getValue().toString());
                textViewYardstick.setText(dataSnapshot.child("Yardstick").getValue().toString());
                Yardstick = Integer.parseInt(dataSnapshot.child("Yardstick").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        doListen();

    }

    public void doListen() {

        logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        passwortÄndernBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BlauesbandZeitnahmeManuell.this, Passwortaendern.class);
                startActivity(intent);
            }
        });

        btnTeilnehmerBBauswählen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectUserData();
            }
        });

        zeitBestätigenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editTextZeit.getText().toString().isEmpty()) {
                    if(!editTextZeit.getText().toString().equals("00:00:00")){
                        if (!(editTextZeit.getText().toString().length() <= 7)) {
                            Zeitberechnung();
                        }else{
                            Toast.makeText(BlauesbandZeitnahmeManuell.this, "Es muss eine plausible Zeit eingetragen werden " , Toast.LENGTH_LONG).show();

                        }
                    }
                }
            }
        });

    }

    public void Zeitberechnung() {

        String zeit = editTextZeit.getText().toString();
        String[] split = zeit.trim().split(":");

        int[] numbers = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            numbers[i] = Integer.parseInt(split[i]);

        }


        if (numbers.length == 3) {
            if (numbers[0] <= 24) {
                if (numbers[1] <= 60) {
                    if (numbers[2] <= 60) {

                        int zeitInSekunden;
                        int berechneteZeit;
                        final String[] secondString = new String[3];

                        zeitInSekunden =  numbers[2];
                        zeitInSekunden += numbers[1] * 60;
                        zeitInSekunden += numbers[0] * 60 * 60;

                        berechneteZeit = (zeitInSekunden*100)/Yardstick ;

                        final int a = berechneteZeit;
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
                        Toast.makeText(BlauesbandZeitnahmeManuell.this, "Die mit dem Yardstick verrechnete Zeit wurde Hochgeladen " , Toast.LENGTH_LONG).show();

                        BlauesbandZeitenRef.setValue(secondString[2] + ":" + secondString[1] + ":" + secondString[0]);


                    }

                }

            }

        }

    }



    public void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //Liste erstellen mit allen Auswählbaren Benutzern
    public void SelectUserData() {
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference();
        UserRef.keepSynced(true);

        final List<String> users = new ArrayList<>();
        final List<Integer> numbers = new ArrayList<>();


        UserRef.child("Vereinsboote").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    StringBuffer buffer = new StringBuffer();
                    numbers.add(Integer.parseInt(dataSnapshotChild.getValue().toString()));

                    buffer.append("Name: " + dataSnapshotChild.getKey() + "\n");


                    users.add(buffer.toString());
                 }

                if (checked == null) {
                    checked = new boolean[users.size()];
                }

                String[] userlist = new String[users.size()];
                userlist = users.toArray(userlist);
                displaySelectView("Verreinsboot auswählen:", userlist, checked, numbers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // for handling database error
                Toast.makeText(BlauesbandZeitnahmeManuell.this, "Eventuell keine Vereinsboote vorhanden", Toast.LENGTH_LONG).show();
            }
        });
    }


    public void displaySelectView(final String title, final String[] userlist, final boolean[] checked, final List<Integer> numbers) {

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


            }
        });
        builder.setNegativeButton("Abbrechen", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}


