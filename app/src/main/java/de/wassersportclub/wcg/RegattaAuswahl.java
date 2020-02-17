package de.wassersportclub.wcg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegattaAuswahl extends AppCompatActivity {

    TextView willkommenstextTV;

    Button logoutBTN, neueRegattaBTN, neuerLaufBTN;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regatta_auswahl);
        willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
        willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

        neueRegattaBTN = findViewById(R.id.NeueRegattaBTN);
        neuerLaufBTN = findViewById(R.id.NeuerLaufBTN);
        logoutBTN = findViewById(R.id.HEADERlogoutBTN);

        doListen();
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

    }

    //Loggt den benutzer aus
    public void logout(){
        mAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
