package de.wassersportclub.wcg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    EditText emailET, passwortET;
    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();

        emailET = findViewById(R.id.LOGINemailET);
        passwortET = findViewById(R.id.LOGINpasswortET);

        sharedPreferences = this.getSharedPreferences("de.wassersportclub.wcg", MODE_PRIVATE);

        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference();
        UserRef.keepSynced(true);

        //abfrage ob der benutzer schon eingelogt ist/war
        if(mAuth.getCurrentUser() != null){
            login();
        }
    }


    //Loggt den benutzer ein
    public void clickOnLogin(View view) {
        if (!emailET.getText().toString().isEmpty() && !passwortET.getText().toString().isEmpty()) {
            mAuth.signInWithEmailAndPassword(emailET.getText().toString(), passwortET.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Einloggen erfolgreich
                                sharedPreferences.edit().putString("passwort", passwortET.getText().toString()).apply();
                                login();
                            } else {
                                // Einloggen schief gegangen
                                Log.w("test", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(MainActivity.this, "Login hat nicht geklappt", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    //wechsle zur nächsten activity
    public void login(){
        Intent intent = new Intent(this, Startseite.class);
        startActivity(intent);
        finish();
    }

    public void onBackPressed(){
        return;
    }
}


