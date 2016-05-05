package in.codehex.shopit;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import in.codehex.shopit.util.AppController;
import in.codehex.shopit.util.Config;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    Toolbar toolbar;
    EditText editTag, editDistance;
    FloatingActionButton fab;
    Intent intent;
    Location location;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    SharedPreferences sharedPreferences, favorite;
    double lat, lng;
    String searchTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initObjects();
        prepareObjects();

        if (checkPlayServices())
            buildGoogleApiClient();

        createLocationRequest();

        if (!isGPSEnabled(getApplicationContext()))
            showAlertGPS();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (googleApiClient != null)
            if (!googleApiClient.isConnected())
                googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null)
            if (googleApiClient.isConnected())
                googleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient != null)
            if (googleApiClient.isConnected())
                stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleApiClient != null)
            if (googleApiClient.isConnected())
                startLocationUpdates();
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location loc) {
        location = loc;
        lat = location.getLatitude();
        lng = location.getLongitude();
        processShops();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_user_favorite) {
            intent = new Intent(getApplicationContext(), UserFavoriteActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialize the objects.
     */
    private void initObjects() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        editTag = (EditText) findViewById(R.id.search_tag);
        editDistance = (EditText) findViewById(R.id.distance);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        sharedPreferences = getSharedPreferences(Config.PREF, MODE_PRIVATE);
        favorite = getSharedPreferences(Config.FAVORITE, MODE_PRIVATE);
    }

    /**
     * Implement and manipulate the objects.
     */
    private void prepareObjects() {
        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTag = editTag.getText().toString();
                String mDist = editDistance.getText().toString();
                float distance = 0;
                try {
                    distance = Float.parseFloat(mDist);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("searchTag", searchTag);
                editor.putString("lat", String.valueOf(lat));
                editor.putString("lng", String.valueOf(lng));
                editor.putFloat("distance", distance);
                editor.apply();

                intent = new Intent(getApplicationContext(), ShopActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Initialize the google api client for location services.
     */
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Checks for the availability of google play services functionality.
     *
     * @return true if play services is enabled else false
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode,
                        Config.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * display an alert to notify the user that GPS has to be enabled
     */
    void showAlertGPS() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Enable GPS");
        alertDialog.setMessage("GPS service is not enabled." +
                " Do you want to go to location settings?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.show();
    }

    /**
     * @param context context of the MainActivity class
     * @return true if GPS is enabled else false
     */
    boolean isGPSEnabled(Context context) {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Stop receiving location updates.
     */
    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    private void processShops() {
        Map<String, ?> keys = favorite.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            String productName = entry.getValue().toString();
            getShopDetails(productName);
        }
    }

    /**
     * Fetch the shop details from the database for the given favorite product.
     *
     * @param productName the name of the product to be searched
     */
    private void getShopDetails(final String productName) {
        // volley string request to server with POST parameters
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // parsing json response data
                try {
                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);

                        String shopName = object.getString("shop_name");
                        double latitude = object.getDouble("latitude");
                        double longitude = object.getDouble("longitude");

                        processDistance(latitude, longitude, shopName, productName);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "Network error! Check your internet connection!",
                        Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to the register URL
                Map<String, String> params = new HashMap<>();
                params.put("tag", "favorite");
                params.put("product_name", productName);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

    /**
     * Determine the distance between the user and the shop.
     *
     * @param latitude    latitude of the shop
     * @param longitude   longitude of the shop
     * @param shopName    name of the shop
     * @param productName product in the shop
     */
    private void processDistance(double latitude, double longitude, final String shopName
            , final String productName) {
        Location source = new Location("source");
        source.setLatitude(lat);
        source.setLongitude(lng);

        Location destination = new Location("destination");
        destination.setLatitude(latitude);
        destination.setLongitude(longitude);

        float distance = source.distanceTo(destination);

        if (distance <= 1000) {
            showNotification(shopName, productName);
        }
    }

    /**
     * Show notification when any favorite product is available in nearby shop.
     *
     * @param shopName    the name of the shop
     * @param productName the product which is saved as a favorite
     */
    private void showNotification(String shopName, String productName) {
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, UserFavoriteActivity.class), 0);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] v = {500, 1000};

        Notification notification = new NotificationCompat.Builder(this)
                .setTicker(productName + " is in nearby shop!")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(shopName)
                .setContentText(productName + " is available in " + shopName)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setSound(uri)
                .setVibrate(v)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    /**
     * Start receiving location updates.
     */
    private void startLocationUpdates() {
        // marshmallow runtime location permission
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(googleApiClient, locationRequest, this);
        } else if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Config.PERMISSION_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Initializes and implements location request object.
     */
    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(Config.UPDATE_INTERVAL);
        locationRequest.setFastestInterval(Config.FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}
