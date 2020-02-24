package de.wassersportclub.wcg;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

public class Passwortaendern extends AppCompatActivity {

    TextView willkommenstextTV, altesPW, neuesPasswort1, neuesPasswort2;

    Button logoutBTN, passwortÄndernBTN, bestätigen;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference user;
    String aktuellesPasswort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passwortaendern);
        willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
        willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

        logoutBTN = findViewById(R.id.HEADERlogoutBTN);
        altesPW = findViewById(R.id.altesPasswort);
        neuesPasswort1 = findViewById(R.id.neuesPasswort);
        neuesPasswort2 = findViewById(R.id.neuesPasswort2);
        passwortÄndernBTN = findViewById(R.id.passwortÄndernBTN);
        passwortÄndernBTN.setAlpha(0);
        bestätigen = findViewById(R.id.passwortBestaetigen);
        String vergleich = mAuth.getCurrentUser().getEmail();
        if(!vergleich.equals("marcelianer36@gmail.com") && !vergleich.equals("ma.walter@bhg-mobile.de")) {
            user = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
            user.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    aktuellesPasswort = dataSnapshot.child("Passwort").getValue().toString();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        doListen();
    }

    public void doListen(){
        logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        bestätigen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwortÄndern();
            }
        });
    }

    private void passwortÄndern() {
        String altesPWString = altesPW.getText().toString();
        String neuesPWString1 = neuesPasswort1.getText().toString();
        String neuesPWString2 = neuesPasswort2.getText().toString();

        if(aktuellesPasswort != null) {
            if (altesPWString != null && !altesPWString.isEmpty() && neuesPWString1 != null && !neuesPWString1.isEmpty() && neuesPWString2 != null && !neuesPWString2.isEmpty()) {
                if (altesPWString.equals(aktuellesPasswort)) {
                    if (neuesPWString1.equals(neuesPWString2)) {
                        mAuth.getCurrentUser().updatePassword(neuesPWString1);
                        user.child("Passwort").setValue(neuesPWString1);
                        Toast.makeText(this, "Passwort geändert", Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Passwortaendern.this.finish();
                            }
                        }, 2000);


                    } else {
                        Toast.makeText(Passwortaendern.this, "Fehler: Passwörter unterschiedlich", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(Passwortaendern.this, "Fehler: Falsches Passwort", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(Passwortaendern.this, "Fehler: Alles richtig eingegeben?", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(Passwortaendern.this, "Fehler: Du bist ein Admin...", Toast.LENGTH_LONG).show();
        }
    }

    //Loggt den benutzer aus
    public void logout(){
        mAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
