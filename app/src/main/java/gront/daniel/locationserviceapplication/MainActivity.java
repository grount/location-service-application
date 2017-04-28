package gront.daniel.locationserviceapplication;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ImageButton refreshCurrentLocationButton;
    private ImageButton refreshLngLatLocationButton;
    private TextView currentLocationInputTextView;
    private TextView lngTextView;
    private TextView latTextView;
    private TextView lngLatCurrentLocationTextView;
    private LocationManager service;
    private Criteria criteria;
    private Location mlocation;
    private boolean refreshCurrentLocationEnabled = false;
    private boolean refreshLngLatLocationEnabled = false;
    private TextView searchesTextView;
    private String searchesText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modifySearchesTextView();

        refreshCurrentLocationButton = (ImageButton) findViewById(R.id.currentLocationRefreshImageButton);
        currentLocationInputTextView = (TextView) findViewById(R.id.currentLocationInputTextView);
        refreshLngLatLocationButton = (ImageButton) findViewById(R.id.latLongCurrentLocationRefreshImageButton);
        lngLatCurrentLocationTextView = (TextView) findViewById(R.id.latLongCurrentLocationTextView);
        lngTextView = (TextView) findViewById(R.id.longInputTextView);
        latTextView = (TextView) findViewById(R.id.latInputTextView);
        searchesTextView = (TextView) findViewById(R.id.searchesInputTextView);

        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mlocation = location;
                Log.d("Location Changes", location.toString());
                String address = getAddress(location.getLatitude(), location.getLongitude());
                if (refreshCurrentLocationEnabled)
                {
                    currentLocationInputTextView.setText(address);
                }
                else if (refreshLngLatLocationEnabled)
                {
                    lngLatCurrentLocationTextView.setText(address);
                    latTextView.setText(String.valueOf(location.getLatitude()));
                    lngTextView.setText(String.valueOf(location.getLongitude()));
                }
                searchesText += currentLocationInputTextView.getText().toString() + "\n\n";
                searchesTextView.setText(searchesText);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Status Changed", String.valueOf(status));
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Provider Enabled", provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Provider Disabled", provider);
            }
        };


        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        service = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        refreshCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                refreshCurrentLocationEnabled = true;
                refreshLngLatLocationEnabled = false;

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            10);
                } else {
                    Looper looper = null;
                    service.requestSingleUpdate(criteria, locationListener, looper);
                }
            }
        });


        refreshLngLatLocationButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                refreshCurrentLocationEnabled = false;
                refreshLngLatLocationEnabled = true;

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            10);
                } else {
                    Looper looper = null;
                    service.requestSingleUpdate(criteria, locationListener, looper);
                }
            }
        });
    }



    public String getAddress(double lat, double lng) {
        String add;
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 10);
            Address obj = addresses.get(0);
            add = obj.getAddressLine(0);

            if (obj.getAdminArea() != null) {
                add = add + ", " + obj.getAdminArea();
            }
            else{
                String adminArea = obj.getAdminArea();
                int count = 1;
                while (adminArea == null && count < addresses.size()) {
                    adminArea = addresses.get(count).getAdminArea();
                    count++;
                }

                if (adminArea != null) {
                    add = add + ", " + adminArea;
                }
            }

            if (obj.getLocality() != null) {
                add = add + ", " + obj.getLocality();
            }
            else{
                String locality = obj.getLocality();
                int count = 1;
                while (locality == null && count < addresses.size()) {
                    locality = addresses.get(count).getAdminArea();
                    count++;
                }

                if (locality != null) {
                    add = add + ", " + locality;
                }
            }

            Log.v("IGA", "Address" + add);
        } catch (IOException e) {
            add = null;
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return add;
    }



    protected void modifySearchesTextView()
    {
        String amountToDisplay = "5";
        TextView searchesID = (TextView) findViewById(R.id.searchesTextView);
        searchesID.setText("Last " + amountToDisplay + " searched addresses:");
    }

    private void alertDialog()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage("Please enable your location service");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}



