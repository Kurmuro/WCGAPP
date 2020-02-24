package de.wassersportclub.wcg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class RegattaAuswahl extends AppCompatActivity {

    TextView willkommenstextTV;

    Button logoutBTN, neueRegattaBTN, neuerLaufBTN, passwortÄndernBTN;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regatta_auswahl);
        willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
        willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

        neueRegattaBTN = findViewById(R.id.NeueRegattaBTN);
        neuerLaufBTN = findViewById(R.id.NeuerLaufBTN);
        logoutBTN = findViewById(R.id.HEADERlogoutBTN);
        passwortÄndernBTN = findViewById(R.id.passwortÄndernBTN);

        mDatabase.child("regatten").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int regatten = 0;
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    regatten++;
                }
                if(regatten == 0){
                    neuerLaufBTN.setAlpha(0);
                }
                doListen();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void doListen(){
        logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        neueRegattaBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegattaAuswahl.this, Regatta.class);
                intent.putExtra("auswahl", "Neue Regatta");
                startActivity(intent);
            }
        });
        neuerLaufBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegattaAuswahl.this, Regatta.class);
                intent.putExtra("auswahl", "Neuer Lauf");
                startActivity(intent);
            }
        });
        passwortÄndernBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegattaAuswahl.this, Passwortaendern.class);
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
}
