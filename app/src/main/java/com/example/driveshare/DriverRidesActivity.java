package com.example.driveshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

public class DriverRidesActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button requestsBtn;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ListView requestListView;
    private ArrayList<String> requestList;
    private ArrayAdapter adapter;

    private ArrayList<Double> passengerLats;
    private ArrayList<Double> passengerLongs;

    private ArrayList<String> passengerRequestName;

    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_rides);
        requestListView = findViewById(R.id.requests_listView);
        requestsBtn = findViewById(R.id.update_requests_button);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        requestList = new ArrayList<>();
        passengerLats = new ArrayList<>();
        passengerLongs = new ArrayList<>();
        passengerRequestName = new ArrayList<>();

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, requestList);
        requestListView.setAdapter(adapter);

        requestsBtn.setOnClickListener(this);

//        requestList.clear();
        if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initLocationListener();
//            try {
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

        }

        requestListView.setOnItemClickListener(this);

    }

    private void updateRequestListView(Location dLocation) {

        if (dLocation != null) {

//            requestList.clear();

            dLocationToParseDB(dLocation);

            final ParseGeoPoint currentDriverLoc = new ParseGeoPoint(dLocation.getLatitude(), dLocation.getLongitude());

            ParseQuery<ParseObject> rideRequestQuery = ParseQuery.getQuery("RequestRide");
            rideRequestQuery.whereNear("passengerLocation", currentDriverLoc);
            rideRequestQuery.whereDoesNotExist("assignedDriver");


            rideRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {

                        if (objects.size() > 0) {
                            if (requestList.size() > 0) {
                                requestList.clear();
                            }

                            if (passengerLats.size() > 0) {
                                passengerLats.clear();
                            }

                            if (passengerLongs.size() > 0) {
                                passengerLongs.clear();
                            }

                            if (passengerRequestName.size() > 0) {
                                passengerRequestName.clear();
                            }

                            i = 0;
                            for (ParseObject requests : objects) {
                                ParseGeoPoint pLocation = (ParseGeoPoint) requests.get("passengerLocation");

//                            Checks miles of each request in relation to current driver location
                                Double milesToPassenger = currentDriverLoc.distanceInMilesTo(pLocation);
                                float roundedMiles = Math.round(milesToPassenger * 10) / 10;
//                            Miles to queried requeest in database
                                if (i < objects.size()) {
                                    requestList.add(roundedMiles + " miles to " + requests.get("username"));
                                    passengerLats.add(pLocation.getLatitude());
                                    passengerLongs.add(pLocation.getLongitude());
                                    passengerRequestName.add(requests.get("username").toString());
                                    i++;
                                }

                            }


                        } else {
                            Toast.makeText(DriverRidesActivity.this, "No Active Requests", Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initLocationListener();

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(DriverRidesActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentDriverLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentDriverLoc);
            }

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.driver_logout_item) {
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        finish();
                    }
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
//        locationListener = new LocationListener() {
//            @Override
//            public void onLocationChanged(Location location) {
//                updateRequestListView(location);
//            }
//
//            @Override
//            public void onStatusChanged(String s, int i, Bundle bundle) {
//
//            }
//
//            @Override
//            public void onProviderEnabled(String s) {
//
//            }
//
//            @Override
//            public void onProviderDisabled(String s) {
//
//            }
//        };

        if (Build.VERSION.SDK_INT < 23) {

//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location currentDriverLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestListView(currentDriverLoc);
        } else {
            if (ActivityCompat.checkSelfPermission(DriverRidesActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(DriverRidesActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
                return;
            } else {
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentDriverLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentDriverLoc);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

//        Toast.makeText(this, "ListView Item Clicked", Toast.LENGTH_SHORT).show();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (currentDriverLocation != null) {
                Intent intent = new Intent(this, LocationsMapActivity.class);
                intent.putExtra("dLat", currentDriverLocation.getLatitude());
                intent.putExtra("dLong", currentDriverLocation.getLongitude());
                intent.putExtra("pLat", passengerLats.get(position));
                intent.putExtra("pLong", passengerLongs.get(position));

                intent.putExtra("pUsername", passengerRequestName.get(position));

                startActivity(intent);
            }
        }
    }

    private void initLocationListener() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
    }

    private void dLocationToParseDB(Location location) {
        ParseUser driverUser = ParseUser.getCurrentUser();
        ParseGeoPoint driverLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        driverUser.put("driverLocation", driverLocation);
        driverUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(DriverRidesActivity.this, "Driver Location Saved", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
