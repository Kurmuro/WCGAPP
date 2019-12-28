package de.wassersportclub.wcg;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
                /*if(data.getCount() == 0){
                    Toast.makeText(TeilnehmerVerwaltung.this, "Keine Teilnehmer Vorhanden", Toast.LENGTH_LONG).show();
                    return;
                }

                List<String> users = new ArrayList<String>();
                String[] userlist = new String[users.size()];

                List<Integer> numbers = new ArrayList<Integer>();

                while(data.moveToNext()){
                    StringBuffer buffer = new StringBuffer();
                    numbers.add(data.getInt(0));
                    buffer.append("Vorname: " + data.getString(1) + "\n");
                    buffer.append("Nachname: " + data.getString(2) + "\n");
                    buffer.append("Boottyp: " + data.getString(3) + "\n");
                    buffer.append("Yardstick: " + data.getInt(4) + "\n");

                    users.add( buffer.toString());

                }

                userlist = users.toArray(userlist);
                displayDeleteView("Teilnehmer auswählen:", userlist);

                 */
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

    public void displayDeleteView(String title, String[] userlist){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setItems(userlist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int deleteRow = 1;
                if(deleteRow > 0){
                    Toast.makeText(TeilnehmerVerwaltung.this, "Erfolgreich gelöscht", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(TeilnehmerVerwaltung.this, "Irgendwas ist schief gegangen", Toast.LENGTH_LONG).show();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
