package de.wassersportclub.wcg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

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
}
