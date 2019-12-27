package de.wassersportclub.wcg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class Startseite extends AppCompatActivity {

    TextView willkommenstextTV;
    Button logoutBTN, stegbelegungBTN, verwaltungBTN, blauesbandBTN, regattaBTN;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminstartseite);
        willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
        willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

        logoutBTN = findViewById(R.id.HEADERlogoutBTN);
        stegbelegungBTN = findViewById(R.id.STARTSEITEstegbelegungBTN);
        verwaltungBTN = findViewById(R.id.STARTSEITEverwaltungBTN);
        blauesbandBTN = findViewById(R.id.STARTSEITEblauesbandBTN);
        regattaBTN = findViewById(R.id.STARTSEITEregattaBTN);

        //Startet den Listener für alle buttons
        doListen();

    }

    //Hört ob knöpfe gedrückt wurden
    public void doListen(){
        logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        stegbelegungBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Startseite.this, Steganlage.class);
                startActivity(intent);
            }
        });
        verwaltungBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Startseite.this, TeilnehmerVerwaltung.class);
                startActivity(intent);
            }
        });
        blauesbandBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Startseite.this, Blauesband.class);
                startActivity(intent);
            }
        });
        regattaBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Startseite.this, Regatta.class);
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
