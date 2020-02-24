package de.wassersportclub.wcg;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class TeilnehmerErstellTabelle extends AppCompatActivity {

    TextView willkommenstextTV, emailView, vornameView, nachnameView, bootstypView, yardstickView;
    Button logoutBTN, finishBTN, passwortÄndernBTN;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    SharedPreferences sharedPreferences;

    String orginemail;
    int regatten = 0;
    int lauf = 0;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teilnehmer_erstell_tabelle);
        mAuth = FirebaseAuth.getInstance();
        willkommenstextTV = findViewById(R.id.HEADERwillkommenstextTV);
        willkommenstextTV.setText("Willkommen " + mAuth.getCurrentUser().getEmail());
        orginemail = mAuth.getCurrentUser().getEmail();
        intent = getIntent();

        logoutBTN = findViewById(R.id.HEADERlogoutBTN);
        finishBTN = findViewById(R.id.TABLEhinzufügenBTN);
        emailView = findViewById(R.id.TABLEemail);
        vornameView = findViewById(R.id.TABLEvorname);
        nachnameView = findViewById(R.id.TABLEnachname);
        bootstypView = findViewById(R.id.TABLEbootstyp);
        yardstickView = findViewById(R.id.TABLEyardstick);
        passwortÄndernBTN = findViewById(R.id.passwortÄndernBTN);

        sharedPreferences = this.getSharedPreferences("de.wassersportclub.wcg", MODE_PRIVATE);

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
        passwortÄndernBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeilnehmerErstellTabelle.this, Passwortaendern.class);
                startActivity(intent);
            }
        });
        finishBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                                public void onComplete(@NonNull final Task<AuthResult> task) {
                                    if (task.isSuccessful()) {


                                        final FirebaseUser user = task.getResult().getUser();
                                        mDatabase.child("users").child(user.getUid()).child("Email").setValue(email);
                                        mDatabase.child("users").child(user.getUid()).child("Vorname").setValue(vorname);
                                        mDatabase.child("users").child(user.getUid()).child("Nachname").setValue(nachname);
                                        mDatabase.child("users").child(user.getUid()).child("Bootstyp").setValue(bootstyp);
                                        mDatabase.child("users").child(user.getUid()).child("Passwort").setValue("123456");
                                        mDatabase.child("users").child(user.getUid()).child("Yardstick").setValue(Integer.parseInt(yardstickView.getText().toString()));

                                        mDatabase.child("regatten").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                regatten = 0;
                                                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                                                while (dataSnapshots.hasNext()) {
                                                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                                                    regatten++;
                                                }
                                                if(regatten == 0){
                                                    Toast.makeText(TeilnehmerErstellTabelle.this, "Benutzer hinzugefügt", Toast.LENGTH_SHORT).show();
                                                    loginRefresh();
                                                }else{
                                                    weiter(task.getResult().getUser().getUid());
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
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
    int hilfsvariable;
    private void weiter(final String user){
            mDatabase.child("regatten").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(int i= 1; i<=regatten;i++) {
                        lauf = 0;
                        hilfsvariable = i;
                        Iterator<DataSnapshot> dataSnapshots = dataSnapshot.child(Integer.toString(hilfsvariable)).getChildren().iterator();
                        while (dataSnapshots.hasNext()) {
                            DataSnapshot dataSnapshotChild = dataSnapshots.next();
                            lauf++;
                            mDatabase.child("regatten").child(Integer.toString(hilfsvariable)).child(Integer.toString(lauf)).child(user).setValue(99);
                        }
                    }
                    Toast.makeText(TeilnehmerErstellTabelle.this, "Benutzer hinzugefügt", Toast.LENGTH_SHORT).show();
                    loginRefresh();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
    }

    public void loginRefresh(){
        String pw = sharedPreferences.getString("passwort", "keinpasswortvorhanden");
        mAuth.signInWithEmailAndPassword(orginemail, pw)
                .addOnCompleteListener(TeilnehmerErstellTabelle.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            finish();
                            startActivity(intent);
                        } else {
                            // Registrieren schief gegangen

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
