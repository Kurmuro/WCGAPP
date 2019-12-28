package de.wassersportclub.wcg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TeilnehmerErstellTabelle extends AppCompatActivity {

    TextView willkommenstextTV, emailView, vornameView, nachnameView, bootstypView, yardstickView;
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
        bootstypView = findViewById(R.id.TABLEbootstyp);
        yardstickView = findViewById(R.id.TABLEyardstick);

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
                final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();


                final String vorname = vornameView.getText().toString();
                final String nachname = nachnameView.getText().toString();
                final String bootstyp = bootstypView.getText().toString();
                final String email = emailView.getText().toString();
                int yardstick = 0;

                if(!yardstickView.getText().toString().isEmpty()) {
                    yardstick = Integer.parseInt(yardstickView.getText().toString());
                }


                if(email != null && !email.isEmpty() && vorname != null && !vorname.isEmpty() && nachname != null && !nachname.isEmpty() &&
                        bootstyp != null && !bootstyp.isEmpty() && yardstick >= 1 && yardstick <= 9999 ) {

                    mAuth.createUserWithEmailAndPassword(email, "123456")
                            .addOnCompleteListener(TeilnehmerErstellTabelle.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = task.getResult().getUser();
                                        mDatabase.child(user.getUid()).child("Email").setValue(email);
                                        mDatabase.child(user.getUid()).child("Vorname").setValue(vorname);
                                        mDatabase.child(user.getUid()).child("Nachname").setValue(nachname);
                                        mDatabase.child(user.getUid()).child("Bootstyp").setValue(bootstyp);
                                        mDatabase.child(user.getUid()).child("Yardstick").setValue(Integer.parseInt(yardstickView.getText().toString()));
                                        Intent intent = getIntent();
                                        finish();
                                        startActivity(intent);
                                        Toast.makeText(TeilnehmerErstellTabelle.this, "Benutzer hinzugefügt", Toast.LENGTH_SHORT).show();

                                    } else {
                                        // Registrieren schief gegangen
                                        Log.w("test", "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(TeilnehmerErstellTabelle.this, "Hinzufügen hat nicht geklappt", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    Toast.makeText(TeilnehmerErstellTabelle.this, "Fehler: Alles richtig eingegeben?", Toast.LENGTH_LONG).show();
                }
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
