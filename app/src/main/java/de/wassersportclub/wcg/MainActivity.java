package de.wassersportclub.wcg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    EditText emailET, passwortET;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailET = findViewById(R.id.LOGINemailET);
        passwortET = findViewById(R.id.LOGINpasswortET);

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
    }
}
