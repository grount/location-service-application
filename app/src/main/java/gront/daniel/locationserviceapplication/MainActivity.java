package gront.daniel.locationserviceapplication;

import android.Manifest;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    // UI buttons and textView
    private ImageButton refreshCurrentLocationButton;
    private ImageButton refreshLngLatLocationButton;
    private TextView currentLocationInputTextView;
    private TextView lngTextView;
    private TextView latTextView;
    private TextView lngLatCurrentLocationTextView;
    private TextView searchesTextView;

    private String[] searchesTextArray = new String[5]; // Maintain text array in order to insert new items.
    private int insertIndex = 0; // Maintain insert index to the array.
    private double currentLatitude;
    private double currentLongitude;

    private static final String SAVE = "MySaveFile";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 500;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private GoogleApiClient mGoogleApiClient; // Provides the entry point to Google Play services.
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;

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

        loadFromFile();
        checkPlayServices();
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();

        refreshCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String currentLocation = getCurrentLocation();

                if (currentLocation != null) {
                    currentLocationInputTextView.setText(currentLocation);
                    locationUpdates();
                }
            }
        });

        refreshLngLatLocationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String currentLocation = getCurrentLocation();

                if (currentLocation != null) {
                    lngLatCurrentLocationTextView.setText(currentLocation);
                    lngTextView.setText(String.valueOf(currentLongitude));
                    latTextView.setText(String.valueOf(currentLatitude));
                    manageSearchesString(lngLatCurrentLocationTextView.getText().toString());
                    locationUpdates();
                }
            }
        });
    }

    // sets current latitude and longtitude
    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveIntoFile();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    protected void locationUpdates() {
        // Check permissions first.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    10);
        } else { // If permissions is granted request location update.
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);

            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    10);
        } else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient); // Get last location to speed up process.

            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    // Convert longtitude and latitude to an address
    public String getAddress(double lat, double lng) {
        String add;
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 10);
            Address obj = addresses.get(0);
            add = obj.getAddressLine(0);

            if (obj.getAdminArea() != null) {
                add = add + ", " + obj.getAdminArea();
            } else {
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
            } else {
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
            add = "";
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return add;
    }

    // Initialize Google API client
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    // Initialize location settings request.
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    private void modifySearchesTextView() {
        String amountToDisplay = "5";
        TextView searchesID = (TextView) findViewById(R.id.searchesTextView);
        searchesID.setText("Last " + amountToDisplay + " searched addresses:");
    }


    // Manages the array of searches in order to limit to 5 searches and insert to start afterwards.
    private void manageSearchesString(String stringToAdd) {
        String text = stringToAdd + "\n\n";
        String finalText = "";

        searchesTextArray[insertIndex] = text;
        insertIndex = (insertIndex + 1) % 5;

        for (int i = 0; i < 5; i++) {
            if (searchesTextArray[i] != null) {
                finalText += searchesTextArray[i];
            }
        }

        searchesTextView.setText(finalText);
    }


    // Saves the relevant files in shared preferences
    private void saveIntoFile() {
        SharedPreferences save = getSharedPreferences(SAVE, MODE_PRIVATE);
        SharedPreferences.Editor editor = save.edit();
        editor.putString("savedSearchesTextView", searchesTextView.getText().toString());
        editor.putInt("insertIndex", insertIndex);
        editor.apply();
    }

    // Loads the relevant files in shard preferences
    private void loadFromFile() {
        SharedPreferences load = getSharedPreferences(SAVE, MODE_PRIVATE);
        String text = load.getString("savedSearchesTextView", "");
        searchesTextView.setText(text);
        insertIndex = load.getInt("insertIndex", 0);
        inLoadGetStringArray(text);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 2404)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void inLoadGetStringArray(String text) {
        int j = 0;
        int count = 0;
        StringBuilder tempText = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                count++;
            }

            tempText.append(text.charAt(i));

            if (count == 2) {
                count = 0;
                searchesTextArray[j] = tempText.toString();
                tempText = new StringBuilder();
                j++;
            }
        }
    }

    private String getCurrentLocation() {
        String currentLocation = null;

        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    10);
        } else {
            Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mCurrentLocation != null) {
                currentLatitude = mCurrentLocation.getLatitude();
                currentLongitude = mCurrentLocation.getLongitude();
                currentLocation = getAddress(currentLatitude, currentLongitude);
            }
        }

        return currentLocation;
    }
}