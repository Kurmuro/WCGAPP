package de.wassersportclub.wcg;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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


public class TeilnehmerVerwaltung extends AppCompatActivity {

    TextView willkommenstextTV;
    Button logoutBTN, addBTN, editBTN, deleteBTN, resetBTN;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teilnehmer_verwaltung);

        willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
        willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

        logoutBTN = findViewById(R.id.HEADERlogoutBTN);
        addBTN = findViewById(R.id.VERWALTUNGhinzufügenBTN);
        editBTN = findViewById(R.id.VERWALTUNGändernBTN);
        deleteBTN = findViewById(R.id.VERWALTUNGlöschenBTN);
        resetBTN = findViewById(R.id.VERWALTUNGsaisonresetBTN);


        //Startet den Listener für alle buttons
        doListen();
    }

    //Hört ob knöpfe gedrückt wurden
    public void doListen() {
        logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
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

            }
        });
        deleteBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllUsersFromFirebase();

            }
        });
        resetBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    //Loggt den benutzer aus
    public void logout(){
        mAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }




    public void getAllUsersFromFirebase() {
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("users");
        UserRef.keepSynced(true);
        final List<String> users = new ArrayList<>();
        final List<String> usersUid = new ArrayList<>();

        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    StringBuffer buffer = new StringBuffer();

                    buffer.append("Name: "+dataSnapshotChild.child("Vorname").getValue().toString() +" "+ dataSnapshotChild.child("Nachname").getValue().toString() + "\n");
                    buffer.append("Bootstyp: "+dataSnapshotChild.child("Bootstyp").getValue() + "\n");

                    users.add( buffer.toString());
                    usersUid.add(dataSnapshotChild.getValue().toString());


                }
                String[] userlist = new String[users.size()];
                userlist = users.toArray(userlist);
                displayDeleteView("Teilnehmer auswählen:", userlist, usersUid);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // for handling database error
                Toast.makeText(TeilnehmerVerwaltung.this, "Eventuel keine Teilnehmer vorhanden", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void displayDeleteView(String title, String[] userlist, final List<String> numbers){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setItems(userlist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
