package de.wassersportclub.wcg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Blauesband extends AppCompatActivity {

    TextView willkommenstextTV;

    Button logoutBTN, regelnBTN, passwortÄndernBTN, ZeitnahmeGpsBtn, ZeitnahmeManuellBtn;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blauesband);
        willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);

        willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

        regelnBTN = findViewById(R.id.BLAUESBANDregelnBTN);
        logoutBTN = findViewById(R.id.HEADERlogoutBTN);
        passwortÄndernBTN = findViewById(R.id.passwortÄndernBTN);
        ZeitnahmeGpsBtn = findViewById(R.id.zeitnahmeGpsBtn);
        ZeitnahmeManuellBtn = findViewById(R.id.zeitnahmeManuellBtn);

        doListen();
    }

    public void doListen(){
        logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        passwortÄndernBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Blauesband.this, Passwortaendern.class);
                startActivity(intent);
            }
        });

        regelnBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Blauesband.this, BlauesbandRegeln.class);
                startActivity(intent);

            }
        });

        ZeitnahmeGpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Blauesband.this, BlauesbandZeitnahmeGps.class);
                startActivity(intent);
            }
        });

        ZeitnahmeManuellBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Blauesband.this, BlauesbandZeitnahmeManuell.class);
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
