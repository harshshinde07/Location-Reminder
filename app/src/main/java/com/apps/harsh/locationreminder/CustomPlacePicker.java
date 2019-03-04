package com.apps.harsh.locationreminder;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomPlacePicker extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 12;
    private static final float DEFAULT_ZOOM = 15;
    MapFragment mapFragment;
    private GoogleMap mMap;
    // PlaceAutocompleteFragment autocompleteFragment;
    Marker marker;
    GoogleApiClient mGoogleApiClient;
    private TrackGPS gps;
    LatLng mLatLng, dLatLng;
    LocationManager locationManager;
    int id = 1;
    double mLat, mLng;
    BottomBar bottomBar;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_place_picker);
        //sets volume controls to handle alarm volume
        this.setVolumeControlStream(AudioManager.STREAM_ALARM);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting Your Current Location...");

        //gps = new TrackGPS(this);

        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

       /* autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                marker.setPosition(place.getLatLng());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
            }
            @Override
            public void onError(Status status) {

            }
        });*/

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_location);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(CustomPlacePicker.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);

                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        FloatingActionButton myfab = (FloatingActionButton) findViewById(R.id.fab_my_loc);
        myfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    //progressDialog.show();
                    gps = new TrackGPS(CustomPlacePicker.this);


                    if (gps.canGetLocation()) {
                        //while (mLat == 0 && mLng == 0) {
                            mLng = gps.getLongitude();
                            mLat = gps.getLatitude();
                            dLatLng = new LatLng(0, 0);
                            mLatLng = new LatLng(mLat, mLng);
                        //}
                       // progressDialog.dismiss();
                        marker.setPosition(mLatLng);
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mLatLng, DEFAULT_ZOOM);
                        mMap.animateCamera(cameraUpdate);
                    }

                    //Updated Method for faster location updates
                    /*SingleShotLocationProvider.requestSingleUpdate(CustomPlacePicker.this,
                            new SingleShotLocationProvider.LocationCallback() {
                                @Override public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                                    Log.d("Location", "my location is " + location.toString());
                                    mLat = location.latitude;
                                    mLng = location.longitude;
                                    mLatLng = new LatLng(mLat,mLng);
                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mLatLng, DEFAULT_ZOOM);
                                    mMap.animateCamera(cameraUpdate);
                                }
                            });*/
                } else {
                    id++;
                    mGoogleApiClient = new GoogleApiClient
                            .Builder(CustomPlacePicker.this)
                            .enableAutoManage(CustomPlacePicker.this, id, CustomPlacePicker.this)
                            .addApi(LocationServices.API)
                            .addConnectionCallbacks(CustomPlacePicker.this)
                            .addOnConnectionFailedListener(CustomPlacePicker.this)
                            .build();

                    locationChecker(mGoogleApiClient, CustomPlacePicker.this);
                }
            }
        });
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                marker.setPosition(place.getLatLng());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), DEFAULT_ZOOM);
                mMap.animateCamera(cameraUpdate);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                //Log.e(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        mMap = map;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                LatLng lat = map.getCameraPosition().target;
                marker.setPosition(lat);
            }
        });
        checkAndSetLocation();
        Log.d("permission", "already have permission");
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_normal) {
                    // The tab with id R.id.tab_normal was selected,
                    // change your content accordingly.
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (tabId == R.id.tab_satellite) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (tabId == R.id.tab_terrain) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                } else if (tabId == R.id.tab_hybrid) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
            }
        });
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_normal) {
                    // The tab with id R.id.tab_normal was selected,
                    // change your content accordingly.
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (tabId == R.id.tab_satellite) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (tabId == R.id.tab_terrain) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                } else if (tabId == R.id.tab_hybrid) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
            }
        });
    }

    public void setLocation(View view) {            //on Button Click
        LatLng pos = marker.getPosition();
        Geocoder geocoder;
        List<Address> addresses = new ArrayList<>();
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        String address = "";
        if (!addresses.isEmpty()) {
            address = addresses.get(0).getAddressLine(0);
        }
        Intent intent = new Intent();
        intent.putExtra("latitude", pos.latitude);
        intent.putExtra("longitude", pos.longitude);
        intent.putExtra("address", address);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void checkAndSetLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, "Please Enable Your GPS", Toast.LENGTH_SHORT).show();
        } else {
            if (ActivityCompat.checkSelfPermission(CustomPlacePicker.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            LatLng pos = marker.getPosition();
            Geocoder geocoder;
            List<Address> addresses = new ArrayList<>();
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }
            String address = "";
            if (!addresses.isEmpty()) {
                address = addresses.get(0).getAddressLine(0);
            }
            Intent intent = new Intent();
            intent.putExtra("latitude", pos.latitude);
            intent.putExtra("longitude", pos.longitude);
            intent.putExtra("address", address);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void locationChecker(GoogleApiClient mGoogleApiClient, final Activity activity) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    activity, 1000);
                            //status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //startActivityForResult(new Intent(MapsActivity.this, CustomPlacePicker.class), REQUEST_CODE);
                }

            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
