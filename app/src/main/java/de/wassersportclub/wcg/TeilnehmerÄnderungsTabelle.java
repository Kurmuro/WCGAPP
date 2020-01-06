package de.wassersportclub.wcg;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class TeilnehmerÄnderungsTabelle extends AppCompatActivity {

    TextView willkommenstextTV, emailView, vornameView, nachnameView, bootstypView, yardstickView;
    Button logoutBTN, finishBTN;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teilnehmer_erstell_tabelle);
        mAuth = FirebaseAuth.getInstance();
        willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
        willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());

        logoutBTN = findViewById(R.id.HEADERlogoutBTN);
        finishBTN = findViewById(R.id.TABLEhinzufügenBTN);
        emailView = findViewById(R.id.TABLEemail);
        vornameView = findViewById(R.id.TABLEvorname);
        nachnameView = findViewById(R.id.TABLEnachname);
        bootstypView = findViewById(R.id.TABLEbootstyp);
        yardstickView = findViewById(R.id.TABLEyardstick);
    }

}
