package de.wassersportclub.wcg;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class TeilnehmerVerwaltung extends AppCompatActivity {

    TextView willkommenstextTV;
    Button logoutBTN, addBTN, editBTN, deleteBTN, resetBTN, passwortÄndernBTN;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    SharedPreferences sharedPreferences;

    String orginemail;
    int regatten = 0;
    int lauf = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teilnehmer_verwaltung);

        willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
        willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

        sharedPreferences = this.getSharedPreferences("de.wassersportclub.wcg", MODE_PRIVATE);
        orginemail = mAuth.getCurrentUser().getEmail();

        logoutBTN = findViewById(R.id.HEADERlogoutBTN);
        addBTN = findViewById(R.id.VERWALTUNGhinzufügenBTN);
        editBTN = findViewById(R.id.VERWALTUNGändernBTN);
        deleteBTN = findViewById(R.id.VERWALTUNGlöschenBTN);
        resetBTN = findViewById(R.id.VERWALTUNGsaisonresetBTN);
        passwortÄndernBTN = findViewById(R.id.passwortÄndernBTN);


        //Startet den Listener für alle buttons
        doListen();
    }

    //Hört ob knöpfe gedrückt wurden
    public void doListen() {
        logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //logout();
                willkommenstextTV.setText("leckmich am arsch " + mAuth.getCurrentUser().getEmail());
            }
        });
        addBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeilnehmerVerwaltung.this, TeilnehmerErstellTabelle.class);
                startActivity(intent);
            }
        });
        editBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllUsersFromFirebase("edit");

            }
        });
        deleteBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllUsersFromFirebase("delete");

            }
        });
        resetBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayConfirmView();
            }
        });

        passwortÄndernBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeilnehmerVerwaltung.this, Passwortaendern.class);
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




    public void getAllUsersFromFirebase(final String option) {
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

                    buffer.append("Name: "+dataSnapshotChild.child("Vorname").getValue().toString() +" "+ dataSnapshotChild.child("Nachname").getValue().toString() + "\n");
                    buffer.append("Bootstyp: "+dataSnapshotChild.child("Bootstyp").getValue() + "\n");

                    users.add( buffer.toString());
                    passwort.add(dataSnapshotChild.child("Passwort").getValue().toString());
                    email.add(dataSnapshotChild.child("Email").getValue().toString());
                    useruid.add(dataSnapshotChild.getKey());


                }
                String[] userlist = new String[users.size()];
                userlist = users.toArray(userlist);
                if(option.equals("delete")) {
                    displayDeleteView("Teilnehmer auswählen:", userlist, passwort, email, useruid);
                }else if(option.equals("edit")){
                    displayEditView("Teilnehmer auswählen:", userlist, passwort, email, useruid);
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // for handling database error
                Toast.makeText(TeilnehmerVerwaltung.this, "Eventuel keine Teilnehmer vorhanden", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void displayDeleteView(String title, String[] userlist, final List<String> passwort, final List<String> email, final List<String> useruid){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setItems(userlist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeUser(passwort, email, useruid, which);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void displayConfirmView(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Sicher?");

        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mDatabase.child("regatten").removeValue();
                Toast.makeText(TeilnehmerVerwaltung.this, "Regatten Gelöscht!", Toast.LENGTH_LONG).show();

            }
        });
        builder.setNegativeButton("Abbrechen", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void displayEditView(String title, String[] userlist, final List<String> passwort, final List<String> email, final List<String> useruid){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setItems(userlist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editUser(useruid.get(which));
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void removeUser(final List<String> passwort, final List<String> email, final List<String> useruid, final int which){
        mAuth.signInWithEmailAndPassword(email.get(which),passwort.get(which))
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Einloggen erfolgreich
                            FirebaseDatabase.getInstance().getReference().child("users").child(useruid.get(which)).removeValue();
                            mAuth.getCurrentUser().delete();

                            mDatabase.child("regatten").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    regatten = 0;
                                    Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                                    while (dataSnapshots.hasNext()) {
                                        DataSnapshot dataSnapshotChild = dataSnapshots.next();
                                        regatten++;
                                    }
                                    if(regatten == 0){
                                        mAuth.signInWithEmailAndPassword(orginemail, sharedPreferences.getString("passwort", "keinpasswortvorhanden"));
                                        Toast.makeText(TeilnehmerVerwaltung.this, "Benutzer gelöscht", Toast.LENGTH_SHORT).show();
                                    }else{
                                        weiter(task.getResult().getUser().getUid());
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else {
                            // Einloggen schief gegangen
                        }
                    }
                });
    }

    int hilfsvariable;
    private void weiter(final String user){
        mDatabase.child("regatten").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(int i= 1; i<=regatten;i++) {
                    lauf = 0;
                    hilfsvariable = i;
                    Iterator<DataSnapshot> dataSnapshots = dataSnapshot.child(Integer.toString(hilfsvariable)).getChildren().iterator();
                    while (dataSnapshots.hasNext()) {
                        DataSnapshot dataSnapshotChild = dataSnapshots.next();
                        lauf++;
                        mDatabase.child("regatten").child(Integer.toString(hilfsvariable)).child(Integer.toString(lauf)).child(user).removeValue();
                    }
                }
                mAuth.signInWithEmailAndPassword(orginemail, sharedPreferences.getString("passwort", "keinpasswortvorhanden"));
                Toast.makeText(TeilnehmerVerwaltung.this, "Benutzer gelöscht", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void editUser(final String useruid){
        Intent intent = new Intent(this, TeilnehmerÄnderungsTabelle.class);
        intent.putExtra("useruid", useruid);
        startActivity(intent);
    }
}


