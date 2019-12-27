package de.wassersportclub.wcg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TeilnehmerErstellTabelle extends AppCompatActivity {

    TextView willkommenstextTV, emailView, vornameView, nachnameView, bootstyp, yardstick;
    Button logoutBTN, finishBTN;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teilnehmer_erstell_tabelle);
        willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
        willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

        logoutBTN = findViewById(R.id.HEADERlogoutBTN);
        finishBTN = findViewById(R.id.TABLEhinzufügenBTN);
        emailView = findViewById(R.id.TABLEemail);
        vornameView = findViewById(R.id.TABLEvorname);
        nachnameView = findViewById(R.id.TABLEnachname);
        bootstyp = findViewById(R.id.TABLEbootstyp);
        yardstick = findViewById(R.id.TABLEyardstick);

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
        finishBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

                    mDatabase.child("users").child("hhjghk").setValue("wert");
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
