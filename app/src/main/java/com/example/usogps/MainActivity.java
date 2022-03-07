package com.example.usogps;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    Button bntObtenerUbicacion,BntUbicacion, bntcompartir;
    TextView Latitud, Longitud, Direccion;
    public static final int CODIGO_UBICACION = 100;

    GoogleMap miMapa;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       mapFragment = (SupportMapFragment) getSupportFragmentManager()
               .findFragmentById(R.id.miMapa);

        bntObtenerUbicacion = findViewById(R.id.bntcordenada);
        Latitud = findViewById(R.id.textlatitud);
        Longitud = findViewById(R.id.textlongitud);
        Direccion = findViewById(R.id.textdirecion);

        bntcompartir = findViewById(R.id.bntCompartir);
        

        
        

        bntcompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setPackage("com.whatsapp");
                intent.putExtra(Intent.EXTRA_TEXT,
                        "Hola, te adjunto mi ubicación: https://maps.google.com/?q="+Latitud+Longitud);
                startActivity(intent);


            }
        });

        bntObtenerUbicacion.setOnClickListener(view -> onClick(view));
        mapFragment.getMapAsync(this);
    }

    public void onMapReady (GoogleMap googleMap){
        miMapa = googleMap;
        LatLng lugar;

        lugar = new LatLng (Double.parseDouble(Latitud.getText().toString()), Double.parseDouble(Longitud.getText().toString()));
        miMapa.clear();

        miMapa.addMarker(new MarkerOptions().position (lugar).title("Justo donde estoy!"));

        if(ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){ miMapa.setMyLocationEnabled(true);

        }

        miMapa.moveCamera (CameraUpdateFactory.newLatLng (lugar));
    }

    public void ObtenerUbicacion() {
        verificarPermisosUbicacion();
    }

    public void verificarPermisosUbicacion() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,}, 100);
        } else {
            iniciarUbicaccion();
        }

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CODIGO_UBICACION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarUbicaccion();
                return;

            }
        }
    }

    public void iniciarUbicaccion() {
        LocationManager objGestorUbicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion ubicardor = new Localizacion();
        ubicardor.setMainActivity(this);
        final boolean gpsEnabled = objGestorUbicacion.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingIntent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
            startActivity(settingIntent);
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,}, CODIGO_UBICACION);
            return;
        }
        objGestorUbicacion.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                0, 0, (LocationListener) ubicardor);
        objGestorUbicacion.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 0, (LocationListener) ubicardor);
        Toast.makeText(MainActivity.this, "Localizacion Inicializada", Toast.LENGTH_SHORT).show();
        Latitud.setText("");
        Longitud.setText("");
        Direccion.setText("");


    }

    private void onClick(View view) {
        ObtenerUbicacion();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


    public class Localizacion implements LocationListener {
        MainActivity mainActivity;

        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;


        }

        @Override
        public void onLocationChanged(Location loc) {
            Latitud.setText(String.valueOf(loc.getLatitude()));
            Longitud.setText(String.valueOf(loc.getLongitude()));
            this.mainActivity.obtenerDireccion(loc);
            mapFragment.getMapAsync(this.mainActivity);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i("Estatus GPS", "GPS Activado");
        }

        @Override
        public void onStatusChanged(String prvider, int status, Bundle estras) {
            switch (status) {
                case LocationProvider
                        .AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider
                        .OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider
                        .TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }

    public void obtenerDireccion (Location ubicacion){
        if (ubicacion.getAltitude() !=  0.0 && ubicacion.getAltitude() != 0.0){
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        ubicacion.getLatitude(), ubicacion.getLatitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    Direccion.setText(DirCalle.getAddressLine(0));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

