package de.wassersportclub.wcg;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class BlauesbandZeitnahmeGps extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blauesband_zeitnahme_gps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng Segelhafen = new LatLng(48.5246745, 7.8072005);
        mMap.addMarker(new MarkerOptions().position(Segelhafen).title(""));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Segelhafen, 15));
        Toast.makeText(BlauesbandZeitnahmeGps.this,"Diese Funktion ist noch nicht verfügbar", Toast.LENGTH_LONG).show();

    }

}

