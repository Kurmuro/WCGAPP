package de.wassersportclub.wcg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TeilnehmerÄnderungsTabelle extends AppCompatActivity {

    TextView willkommenstextTV, emailView, vornameView, nachnameView, bootstypView, yardstickView;
    Button logoutBTN, finishBTN, passwortÄndernBTN;
    String useruid, vorname, nachname, bootstyp;
    Integer yardstick = 0;

    FirebaseAuth mAuth;
    DatabaseReference user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teilnehmer_erstell_tabelle);
        mAuth = FirebaseAuth.getInstance();
        willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
        willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

        useruid = getIntent().getStringExtra("useruid");
        user = FirebaseDatabase.getInstance().getReference().child("users").child(useruid);

        logoutBTN = findViewById(R.id.HEADERlogoutBTN);
        finishBTN = findViewById(R.id.TABLEhinzufügenBTN);
        emailView = findViewById(R.id.TABLEemail);
        emailView.setEnabled(false);
        vornameView = findViewById(R.id.TABLEvorname);
        nachnameView = findViewById(R.id.TABLEnachname);
        bootstypView = findViewById(R.id.TABLEbootstyp);
        yardstickView = findViewById(R.id.TABLEyardstick);
        passwortÄndernBTN = findViewById(R.id.passwortÄndernBTN);

        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                emailView.setText(dataSnapshot.child("Email").getValue().toString());
                vornameView.setText(dataSnapshot.child("Vorname").getValue().toString());
                nachnameView.setText(dataSnapshot.child("Nachname").getValue().toString());
                bootstypView.setText(dataSnapshot.child("Bootstyp").getValue().toString());
                yardstickView.setText(dataSnapshot.child("Yardstick").getValue().toString());
                finishBTN.setText("Änderungen Speichern");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        doListen();
    }

    private void doListen() {
        logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        passwortÄndernBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeilnehmerÄnderungsTabelle.this, Passwortaendern.class);
                startActivity(intent);
            }
        });
        finishBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                vorname = vornameView.getText().toString();
                nachname = nachnameView.getText().toString();
                bootstyp = bootstypView.getText().toString();

                if(!yardstickView.getText().toString().isEmpty()) {
                    yardstick = Integer.parseInt(yardstickView.getText().toString());
                }


                if(vorname != null && !vorname.isEmpty() && nachname != null && !nachname.isEmpty() &&
                        bootstyp != null && !bootstyp.isEmpty() && yardstick >= 1 && yardstick <= 9999 ) {
                    user.child("Vorname").setValue(vorname);
                    user.child("Nachname").setValue(nachname);
                    user.child("Bootstyp").setValue(bootstyp);
                    user.child("Yardstick").setValue(Integer.parseInt(yardstickView.getText().toString()));
                    Toast.makeText(TeilnehmerÄnderungsTabelle.this, "Benutzer geändert", Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Toast.makeText(TeilnehmerÄnderungsTabelle.this, "Fehler: Alles richtig eingegeben?", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Loggt den benutzer aus
    public void logout(){
        mAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
