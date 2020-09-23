package fructuoso.speechtotext;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Long.valueOf;

public class Road extends AppCompatActivity implements LocationListener{

    private TextView editArival;

    // Location Variables
    private LocationManager locationManager;
    private final static int DISTANCE_UPDATES = 1;
    private final static int TIME_UPDATES = 5;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private boolean LocationAvailable;

    String locationAddresslat;
    String locationAddresslong;

    TextView arrivLat;
    TextView arriveLong;
    TextView departLat;
    TextView departLong;
    TextView resDis;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road);

        LocationAvailable = false;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
        } else {
            requestPermission();
        }

        final String destination_voix = getIntent().getStringExtra("voix_destination");

        editArival = (TextView) findViewById(R.id.editArrivee);
        Button btnSearch = (Button) findViewById(R.id.btnSearch);
        arrivLat = (TextView) findViewById(R.id.latitudeArText);
        arriveLong = (TextView) findViewById(R.id.longitudeArText);
        resDis = (TextView)findViewById(R.id.Distance);

        editArival.setText(destination_voix);
        GeocodingLocation.getAddressFromLocation(destination_voix, getApplicationContext(), new GeocoderHandler());

        btnSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                if("".equals(departLat.getText().toString().trim())) {
                    Toast.makeText(Road.this, "Merci d'attendre votre localisation", Toast.LENGTH_SHORT).show();
                }
                else {
                        //On calcul la distance
                        double t1 = Math.sin(Math.toRadians(Double.parseDouble(departLat.getText().toString()))) * Math.sin(Math.toRadians(Double.parseDouble(arrivLat.getText().toString())));
                        double t2 = Math.cos(Math.toRadians(Double.parseDouble(departLat.getText().toString()))) * Math.cos(Math.toRadians(Double.parseDouble(arrivLat.getText().toString())));
                        double t3 = Math.cos(Math.toRadians(Double.parseDouble(departLong.getText().toString())) - Math.toRadians(Double.parseDouble(arriveLong.getText().toString())));
                        double t4 = t2 * t3;
                        double t5 = t1 + t4;
                        double rad_dist = Math.atan(-t5/Math.sqrt(-t5 * t5 +1)) + 2 * Math.atan(1);
                        double final_rad_dist= (rad_dist*3437.74677 * 1.1508)*1.6093470878864446;
                        double km_arrondi = Math.round(final_rad_dist*100.0)/100.0;
                        resDis.setText(String.valueOf(km_arrondi)+ " kms");

                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        departLat = (TextView) findViewById(R.id.latitudeDeText);
        departLong = (TextView) findViewById(R.id.longitudeDeText);
        departLat.setText(String.valueOf(location.getLatitude()));
        departLong.setText(String.valueOf(location.getLongitude()));
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (checkPermission()) {
            locationManager.removeUpdates(this);
        } else {
            requestPermission();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (checkPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
        } else {
            requestPermission();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){
            LocationAvailable = true;
            return true;
        } else {
            LocationAvailable = false;
            return false;
        }
    }

    private void requestPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(this, "Please enable GPS data to access all features.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (checkPermission()) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
                    } else {
                        requestPermission();
                    }
                } else {

                    Toast.makeText(this, "Permission Not Granted.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {

            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddresslat = bundle.getString("lat");
                    locationAddresslong= bundle.getString("long");
                    break;
                default:
                    locationAddresslat = null;
                    locationAddresslong = null;
            }

            arrivLat.setText(locationAddresslat);
            arriveLong.setText(locationAddresslong);
        }
    }

}
