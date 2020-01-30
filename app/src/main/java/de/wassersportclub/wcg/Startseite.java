package de.wassersportclub.wcg;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class Startseite extends AppCompatActivity {

    TextView willkommenstextTV;
    Button logoutBTN, stegbelegungBTN, verwaltungBTN, blauesbandBTN, regattaBTN;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    List<Benutzer> benutzerliste = new ArrayList<>();


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

        benutzerliste.add(new Benutzer("Tom", 50));
        benutzerliste.add(new Benutzer("Max", 1));
        ranglisteErstellen();

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

    public void ranglisteErstellen(){
        List<String> name = new ArrayList<>();
        List<Integer> punkte = new ArrayList<>();

        for (Benutzer a : benutzerliste) {
            name.add(a.getName());
            punkte.add(a.getInt());
        }

        ListView list = findViewById(R.id.rangliste);
        list.setAdapter(new MyListAdapterRang(Startseite.this, R.layout.rangliste_zeile, name, punkte));
    }
}

class MyListAdapterRang extends ArrayAdapter<String> {

    int layout;
    List<String> object;
    List<Integer> punktzahl;

    public MyListAdapterRang(@NonNull Context context, int resource, @NonNull List<String> objects, List<Integer> punkte) {
        super(context, resource, objects);
        layout = resource;
        object = objects;
        punktzahl = punkte;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder mainViewHolder;
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);
            final ViewHolderRang viewHolder = new ViewHolderRang();

            viewHolder.name = convertView.findViewById(R.id.ranglisteNameTV);
            viewHolder.name.setText(object.get(position));

            viewHolder.rang = convertView.findViewById(R.id.ranglisteRangTV);
            viewHolder.rang.setText("Rang");

            viewHolder.punktzahl = convertView.findViewById(R.id.ranglistePunkteTV);
            viewHolder.punktzahl.setText("100");


            convertView.setTag(viewHolder);
        }
        else{
            mainViewHolder = (ViewHolder) convertView.getTag();
            mainViewHolder.name.setText(getItem(position));
        }

        return convertView;
    }

}
class ViewHolderRang{
    TextView rang;
    TextView name;
    TextView punktzahl;

}
class Benutzer implements Comparable<Benutzer>{
    private String name;
    private int punktzahl;

    public Benutzer(String m, int i) {
        this.name = m;
        this.punktzahl = i;
    }
    public String getName() {
        return this.name;
    }

    public int getInt() {
        return this.punktzahl;
    }

    @Override
    public int compareTo(Benutzer benutzer) {
        return this.name.compareTo(benutzer.getName());
    }
}

