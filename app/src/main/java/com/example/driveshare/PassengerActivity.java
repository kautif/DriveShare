package com.example.driveshare;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Button requestRideBtn;
    private Button logoutBtn;

    private boolean rideInactive = true;
    private boolean rideReady = false;

    private Timer t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        logoutBtn = findViewById(R.id.passenger_logout_button);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        requestRideBtn = findViewById(R.id.request_ride_button);
        requestRideBtn.setOnClickListener(this);

//        Request to RideRequest class object in ParseServer
        ParseQuery<ParseObject> rideRequestQuery = ParseQuery.getQuery("RequestRide");
        rideRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        rideRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size() > 0 && e == null) {
                    rideInactive = false;
                    requestRideBtn.setText("Cancel Ride Request");

                    getDriverUpdates();

                }
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
//                Exits from prior activity
                        if (e == null) {
                            finish();
                        }
                    }
                });
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateCameraPassengerLoc(location);
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
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if (Build.VERSION.SDK_INT < 23) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(PassengerActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
                return;
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentPassengerLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLoc(currentPassengerLoc);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentPassengerLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLoc(currentPassengerLoc);
            }

        }


    }

    private void updateCameraPassengerLoc(Location pLocation) {

        if (rideReady == false) {
            LatLng passengerLoc = new LatLng(pLocation.getLatitude(), pLocation.getLongitude());
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLoc, 15));

            mMap.addMarker(new MarkerOptions().position(passengerLoc).title("Your current position"));
        }
    }

    //        Sends username and location to ParseServer under RequestRide class object
    @Override
    public void onClick(View view) {
        if (rideInactive) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentPassengerLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (currentPassengerLoc != null) {
                    ParseObject requestRide = new ParseObject("RequestRide");
                    requestRide.put("username", ParseUser.getCurrentUser().getUsername());
                    ParseGeoPoint passengerLoc = new ParseGeoPoint(currentPassengerLoc.getLatitude(), currentPassengerLoc.getLongitude());

                    requestRide.put("passengerLocation", passengerLoc);

                    requestRide.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(PassengerActivity.this, "Ride request sent", Toast.LENGTH_SHORT).show();
                                requestRideBtn.setText("Cancel Ride Request");
                                rideInactive = false;
                            }
                        }
                    });

                } else {
                    Toast.makeText(this, "Passenger location not found", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            ParseQuery<ParseObject> rideRequestQuery = ParseQuery.getQuery("RequestRide");
            rideRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
            rideRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (objects.size() > 0 && e == null) {
                        Toast.makeText(PassengerActivity.this, "RequestRide object accessed", Toast.LENGTH_SHORT).show();
                        rideInactive = true;
                        requestRideBtn.setText("Request a Ride");
                        for (ParseObject ride : objects) {
                            ride.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Toast.makeText(PassengerActivity.this, "Ride Request Removed", Toast.LENGTH_SHORT).show();
                                        rideReady = false;
                                        rideInactive = true;
                                        requestRideBtn.setText("Request Next Ride");
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    private void getDriverUpdates() {
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ParseQuery<ParseObject> rideRequestQuery = ParseQuery.getQuery("RequestRide");
                rideRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                rideRequestQuery.whereEqualTo("requestAccepted", true);
                rideRequestQuery.whereExists("assignedDriver");

                rideRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (objects.size() > 0 && e == null) {

                            rideReady = true;
                            for (final ParseObject object : objects) {

                                ParseQuery<ParseUser> driverQuery = ParseUser.getQuery();
                                driverQuery.whereEqualTo("username", object.getString("assignedDriver"));
                                driverQuery.findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> drivers, ParseException error) {
                                        if (drivers.size() > 0 && error == null) {
                                            for (ParseUser requestedDriver : drivers) {
                                                ParseGeoPoint requestedDriverLoc = requestedDriver.getParseGeoPoint("driverLocation");
                                                if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                                    Location passengerLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                                    ParseGeoPoint pLocGeoPoint = new ParseGeoPoint(passengerLoc.getLatitude(), passengerLoc.getLongitude());
                                                    double milesDistance = requestedDriverLoc.distanceInMilesTo(pLocGeoPoint);

                                                    if (milesDistance < 0.3) {
                                                        Toast.makeText(PassengerActivity.this, "Your driver has arrived", Toast.LENGTH_SHORT).show();
                                                        rideReady = false;
                                                        rideInactive = true;
                                                        requestRideBtn.setText("Request Next Ride");
                                                    }

                                                    float roundedDistance = Math.round(milesDistance * 10) / 10;
                                                    Toast.makeText(PassengerActivity.this, "The driver " + object.getString("assignedDriver") + " is" + roundedDistance + " miles away.", Toast.LENGTH_SHORT).show();

                                                    LatLng dLocation = new LatLng(requestedDriverLoc.getLatitude(), requestedDriverLoc.getLatitude());
                                                    LatLng pLocation = new LatLng(pLocGeoPoint.getLatitude(), passengerLoc.getLongitude());

                                                    LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
                                                    Marker dMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver Location"));
                                                    Marker pMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title("Passenger Location"));

                                                    ArrayList<Marker> markers = new ArrayList<>();
                                                    markers.add(dMarker);
                                                    markers.add(pMarker);

//        Preps bounds to be tied to the driver and passenger markers
                                                    for (Marker marker : markers) {
                                                        latLngBuilder.include(marker.getPosition());
                                                    }

                                                    LatLngBounds bounds = latLngBuilder.build();
                                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 80);
                                                    mMap.animateCamera(cameraUpdate);


                                                }
                                            }
                                        } else {
                                            rideReady = false;
                                        }
                                    }
                                });

                            }
                        }
                    }
                });
            }
        }, 0, 3000);
    }

}
