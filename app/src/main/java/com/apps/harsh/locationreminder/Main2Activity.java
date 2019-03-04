package com.apps.harsh.locationreminder;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Database

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 12;
    static AlarmDatabase alarmDatabase;
    final Context context = Main2Activity.this;
    final private int REQUEST_CODE = 1;
    double lati, lang;
    RecyclerView mRecyclerView;
    private GeoAlarmAdapter mAdapter;
    static public ArrayList<GeoAlarm> mAlarms;
    TextView tvEmpty;
    ImageView ivEmpty;
    ViewStub viewStub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvEmpty = (TextView) findViewById(R.id.textViewEmpty);
        ivEmpty = (ImageView) findViewById(R.id.imageViewEmpty);

        viewStub = (ViewStub) findViewById(R.id.emptyView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        //sets volume controls to handle alarm volume
        this.setVolumeControlStream(AudioManager.STREAM_ALARM);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                  //      .setAction("Action", null).show();
            }
        });*/



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Button used to set the alarm
        final FloatingActionButton setAlarm = (FloatingActionButton) findViewById(R.id.set_alarm);
        //On click listener for setting the alarm button
        setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
            }
        });
        mAlarms = new ArrayList<GeoAlarm>();


        /*RecyclerViewEmptySupport list =
                (RecyclerViewEmptySupport) findViewById(R.id.alarm_list);
        list.setLayoutManager(new LinearLayoutManager(context));
        list.setEmptyView(findViewById(R.id.emptyView));*/


        //Implements RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.alarm_list);
        mRecyclerView.setHasFixedSize(true);
        //mRecyclerView.setEmptyView(findViewById(R.id.emptyView));
        final LinearLayoutManager layoutManager;
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);//sets the RecyclerView as Vertical
        mRecyclerView.setLayoutManager(layoutManager);
        //list.setLayoutManager(layoutManager);
        mAdapter = new GeoAlarmAdapter(mAlarms);
        //list.setAdapter(mAdapter);
        mRecyclerView.setAdapter(mAdapter);




        //Adds horizontal bar after each item
/*
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        //mRecyclerView.addItemDecoration(new MyDividerItemDecoration(this));
*/
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 0 ||dy<0 && setAlarm.isShown())
                {
                    setAlarm.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    setAlarm.show();
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        // adding database
        alarmDatabase = new AlarmDatabase(getApplicationContext());
        //shows all of the alarms present in the database
        showAlarms();
        emptyViewDisplay();
        /* if(mAlarms.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            ivEmpty.setVisibility(View.VISIBLE);
        }
        else {
            mRecyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            ivEmpty.setVisibility(View.GONE);
        } */
        stopService(new Intent(this, GeoService.class));
        startService(new Intent(this, GeoService.class));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (isTaskRoot()) {
            /*android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            System.exit(0);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            android.support.v7.app.AlertDialog alert = builder.create();
            alert.show();*/
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Are you sure you want to exit?")
                    //.setContentText("Won't be able to redo this action!")
                    .setConfirmText("Yes!")
                    .setCancelText("No!")
                    .showCancelButton(true)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            System.exit(0);
                            sDialog.dismissWithAnimation();

                        }
                    })
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.cancel();
                        }
                    })
                    .show();

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_share_app) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Your body here";
            String shareSub = "Your subject here";
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share using"));
        } else if (id == R.id.nav_settings) {
            //startActivity(new Intent(Main2Activity.this, SettingsActivity.class));
        } else if (id == R.id.nav_feedback) {
            Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "");
            intent.putExtra(Intent.EXTRA_TEXT, "");
            intent.setData(Uri.parse("mailto:developer.locationalarmsystem@gmail.com")); // or just "mailto:" for blank
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Use to set info for new alarm
    public void setAlarm(String name, LocationCoordiante location, boolean vibrate,
                         String ringtone, String ringtoneName, int range, String message) {
        GeoAlarm geoAlarm = new GeoAlarm(name, location, vibrate, ringtone, ringtoneName, range, message);
        geoAlarm.setStatus(true);
        alarmDatabase.insertData(geoAlarm);
        geoAlarm.setmId(alarmDatabase.getId());
        mAlarms.add(geoAlarm);
        mAdapter.addItem(mAdapter.getItemCount());
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        if(mAdapter.getItemCount() == 0)
        {
            emptyViewDisplay();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAdapter.getItemCount() == 0)
        {
            emptyViewDisplay();
        }

        ((GeoAlarmAdapter) mAdapter).setOnItemClickListener(
                new GeoAlarmAdapter.MyClickListener() {
                    @Override
                    public void onItemClick(final int position, View v) {
                        //On click event for row items

                        //Adds Haptic Feedback
                        View view = findViewById(R.id.drawer_layout);
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        //Creates the dialog for configuring the new alarm
                        LayoutInflater li = LayoutInflater.from(context);
                        View promptsView = li.inflate(R.layout.location_alarm_dialog, null);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                        // set prompts.xml to alertdialog builder
                        alertDialogBuilder.setView(promptsView);

                        final EditText userInput = (EditText) promptsView.findViewById(R.id.alarm_name_input);
                        TextView locationShow = (TextView) promptsView.findViewById(R.id.location_coordinates);
                        final Spinner ringtoneSelect = (Spinner) promptsView.findViewById(R.id.ringtone);
                        final CheckBox vibration = (CheckBox) promptsView.findViewById(R.id.vibration);
                        vibration.setChecked(mAlarms.get(position).getVibration());
                        locationShow.setText(mAlarms.get(position).getLocationCoordinate());
                        userInput.setText(mAlarms.get(position).getName());
                        final EditText range = (EditText) promptsView.findViewById(R.id.range);
                        range.setText("" + mAlarms.get(position).getRadius());
                        final EditText message = (EditText) promptsView.findViewById(R.id.message);
                        message.setText("" + mAlarms.get(position).getMessage());

                        //Use to retreive ringtones from the phone
                        final Map<String, String> ringtones = new HashMap<>();
                        RingtoneManager manager = new RingtoneManager(Main2Activity.this);
                        manager.setType(RingtoneManager.TYPE_ALARM);
                        Cursor cursor = manager.getCursor();
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()) {
                            ringtones.put(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX),
                                    manager.getRingtoneUri(cursor.getPosition()).toString());
                            cursor.moveToNext();
                        }

                        //Extracts the names of the ringtones
                        final ArrayList<String> ringtoneNames = new ArrayList<String>();
                        for (Map.Entry<String, String> entry : ringtones.entrySet()) {
                            ringtoneNames.add(entry.getKey());
                        }
                        //Puts the values in the ringtone spinner
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(Main2Activity.this,
                                android.R.layout.simple_spinner_item, ringtoneNames);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        ringtoneSelect.setAdapter(dataAdapter);


                        // set dialog message
                        alertDialogBuilder
                                .setCancelable(true)
                                .setPositiveButton("SAVE",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //Sets the alarm. Code needs to be entered
                                            }
                                        })
                                .setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                        // create alert dialog
                        final AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (range.getText().toString().equals("")) {
                                    Toast.makeText(Main2Activity.this, "Please enter the range", Toast.LENGTH_SHORT).show();
                                } else if (Integer.parseInt(range.getText().toString()) < 50) {
                                    Toast.makeText(Main2Activity.this, "Range must be greater than or equal to 50", Toast.LENGTH_SHORT).show();
                                } else {
                                    mAlarms.get(position).setName(userInput.getText().toString());
                                    mAlarms.get(position).setRingtone(ringtoneSelect.getSelectedItem().toString(),
                                            ringtones.get(ringtoneSelect.getSelectedItem()));
                                    mAlarms.get(position).setVibration(vibration.isChecked());
                                    mAlarms.get(position).setRadius(Integer.parseInt(range.getText().toString()));
                                    mAlarms.get(position).setMessage("" + message.getText());
                                    mAdapter.refreshItem(position);
                                    alertDialog.dismiss();
                                    emptyViewDisplay();
                                }
                            }
                        });

                    }
                }


        );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Intent", "" + resultCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                // coordinates for destination

                lati = data.getDoubleExtra("latitude", 0);
                lang = data.getDoubleExtra("longitude", 0);

                //Creates the dialog for configuring the new alarm
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.location_alarm_dialog, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView.findViewById(R.id.alarm_name_input);
                TextView locationShow = (TextView) promptsView.findViewById(R.id.location_coordinates);
                final EditText range = (EditText) promptsView.findViewById(R.id.range);
                final Spinner ringtoneSelect = (Spinner) promptsView.findViewById(R.id.ringtone);
                final CheckBox vibration = (CheckBox) promptsView.findViewById(R.id.vibration);
                final EditText message = (EditText) promptsView.findViewById(R.id.message);
                userInput.setText(data.getStringExtra("address"));


//Use to retreive ringtones from the phone
                final Map<String, String> ringtones = new HashMap<>();
                RingtoneManager manager = new RingtoneManager(Main2Activity.this);
                manager.setType(RingtoneManager.TYPE_ALARM);
                Cursor cursor = manager.getCursor();
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    ringtones.put(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX),
                            manager.getRingtoneUri(cursor.getPosition()).toString());
                    cursor.moveToNext();
                }


//Extracts the names of the ringtones
                final ArrayList<String> ringtoneNames = new ArrayList<String>();
                for (Map.Entry<String, String> entry : ringtones.entrySet()) {
                    ringtoneNames.add(entry.getKey());
                }
                //Puts the values in the ringtone spinner
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, ringtoneNames);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                ringtoneSelect.setAdapter(dataAdapter);


                // set dialog box
                alertDialogBuilder
                        .setCancelable(true)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //Sets the alarm. Code needs to be entered

                                    }

                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog

                final AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (range.getText().toString().equals("")) {
                            Toast.makeText(Main2Activity.this, "Please enter the range", Toast.LENGTH_SHORT).show();
                        } else if (Integer.parseInt(range.getText().toString()) < 50) {
                            Toast.makeText(Main2Activity.this, "Range must be greater than or equal to 50", Toast.LENGTH_SHORT).show();
                        } else {
                            setAlarm(userInput.getText().toString(),
                                    new LocationCoordiante(lati, lang), vibration.isChecked(),
                                    ringtones.get(ringtoneSelect.getSelectedItem()), ringtoneSelect.getSelectedItem().toString(),
                                    Integer.parseInt(range.getText().toString()), "" + message.getText());
                            //stopService(new Intent(context,GeoService.class));
                            Intent intent = new Intent(context, GeoService.class);
                            startService(intent);
                            alertDialog.dismiss();
                        }
                    }
                });
                locationShow.setText("" + lati + ", " + lang);
                userInput.setText(data.getStringExtra("address"));
            }
        }
    }


    // loading old alarms
    private void showAlarms() {

        ArrayList<GeoAlarm> geoAlarms = alarmDatabase.getAllData();

        if (geoAlarms == null) {

        } else {
            for (GeoAlarm geoAlarm1 : geoAlarms) {
                mAlarms.add(geoAlarm1);
                mAdapter.addItem(mAdapter.getItemCount());
                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                if(mAdapter.getItemCount() == 0)
                {
                    emptyViewDisplay();
                }
            }
        }
    }

    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Main2Activity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(Main2Activity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(Main2Activity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
            return;
        }
        startActivityForResult(new Intent(Main2Activity.this, CustomPlacePicker.class), REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(Main2Activity.this, CustomPlacePicker.class), REQUEST_CODE);
                }

            }
        }
    }

    public  void emptyViewDisplay() {
        int count = mAdapter.getItemCount();
        if (count == 0) {
            mRecyclerView.setVisibility(View.GONE);
            viewStub.setVisibility(View.VISIBLE);
        }
        else {
            mRecyclerView.setVisibility(View.VISIBLE);
            viewStub.setVisibility(View.GONE);
        }
    }
}
