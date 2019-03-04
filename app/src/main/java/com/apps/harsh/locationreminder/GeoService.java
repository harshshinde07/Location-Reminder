package com.apps.harsh.locationreminder;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class GeoService extends Service {

    LocationListener locationListener;
    LocationManager locationManager;
    private ArrayList<GeoAlarm> geoAlarms;
    private boolean shouldStop;
    NotificationManager notificationManager;
    Notification notification;
    public GeoService() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //onStartCommand(intent,flags,startId);

    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        loadAlarms();

        shouldStop = true;
        if(geoAlarms !=null && !geoAlarms.isEmpty()){
            for(GeoAlarm geoAlarm:geoAlarms){
                if(geoAlarm.getStatus()){
                    shouldStop = false;
                    break;
                }
            }
        }
        if(shouldStop){
            stopSelf();
        }

        Intent intent23 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplication(),11,intent23,0);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = new Notification.Builder(GeoService.this)
                .setSmallIcon(R.drawable.ic_header)
                .setContentTitle("GPS is Disabled")
                .setContentText("Please Enable your GPS")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(true)
                .build();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

                if (geoAlarms != null) {
                    if (!geoAlarms.isEmpty()) {
                        for (int i=0;i<geoAlarms.size();i++) {
                            GeoAlarm geoAlarm = geoAlarms.get(i);
                            if (geoAlarm.getStatus()){
                                System.out.println(calculateDis(geoAlarm.getLatLang(), loc) + "");
                                System.out.println(geoAlarm.getLatLang() + "");
                                if (calculateDis(geoAlarm.getLatLang(), loc) < geoAlarm.getRadius()) {
                                    playAlarm(i);
                                    geoAlarms.remove(geoAlarm);
                                    stopSelf();
                                    break;
                                }
                            }
                        }
                    }
                    else {
                        stopSelf();
                    }
                }
                else {
                    stopSelf();
                }


            }

            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            public void onProviderEnabled(String provider) {
                Log.d("Provider","Enabled");
                notificationManager.cancel(1);
            }

            public void onProviderDisabled(String provider) {
                Log.d("Provider","Disabled");
                /*Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplication(),11,intent,0);
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Notification notification = new Notification.Builder(GeoService.this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("GPS is Disabled")
                        .setContentText("On GPS")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();*/
                notificationManager.notify(1,notification);
            }
        };

// Register the listener with the Location Manager to receive location updates

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            locationManager.removeUpdates(locationListener);
        }
    }

    // distance formula from current location
    private double calculateDis(LatLng destiny, LatLng myLoc) {
        double R = 6371e3; // metres
        double φ1 = Math.PI * destiny.latitude / 180;
        double φ2 = Math.PI * myLoc.latitude / 180;
        double Δφ = φ2 - φ1;
        double Δλ = Math.PI * (destiny.longitude - myLoc.longitude) / 180;
        double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double d = R * c;
        return d;
    }

    public void loadAlarms() {
        AlarmDatabase alarmDatabase = new AlarmDatabase(getApplicationContext());
       geoAlarms = alarmDatabase.getAllData();
    }

    public void playAlarm(int pos) {
        Intent intent1 = new Intent(getApplicationContext(),AlarmScreenActivity.class);
        intent1.putExtra("geoAlarm",geoAlarms.get(pos));
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent1);
    }

}
