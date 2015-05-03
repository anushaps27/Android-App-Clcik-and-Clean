package examples.course.clickandclean;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;


public class ListScreen extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    SparseArray<Group> groups = new SparseArray<Group>();

    ArrayList<serverInfo> infoItems = new ArrayList<>();

    ParseGeoPoint point = new ParseGeoPoint();

    SessionManager session;

    final Activity cur_act = this;

    private LocationRequest mLocationRequest;
    private static final String TAG = PhotoScreen.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    Button register;
    Button confirm;
    String email;

    boolean status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_screen);

        email = getIntent().getStringExtra("email");

        if(checkPlayServices())
            buildGoogleApiClient();

        Parse.initialize(this, "2NUGGHhCoB6kQKMVTKfYwehHdg3NnKcrqXy2vdib", "IRERSYLJwVQFNxAW1SNqKQoVtROSknDvReDVA6M6");
        ParseObject.registerSubclass(serverInfo.class);


        register = (Button) findViewById(R.id.register_button);
        confirm = (Button) findViewById(R.id.confirm_button);

        register.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Create an explicit Intent for starting the PhotoScreen
                // Activity
                Intent photoIntent = new Intent(ListScreen.this,
                        PhotoScreen.class);

                photoIntent.putExtra("email",email);

                // Use the Intent to start the PhotoScreen Activity
                startActivity(photoIntent);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if(status) {
                    // Create an explicit Intent for starting the RegisterScreen
                    // Activity
                    Intent confirmIntent = new Intent(ListScreen.this,
                            ConfirmPhotoScreen.class);

                    confirmIntent.putExtra("email", email);

                    // Use the Intent to start the RegisterScreen Activity
                    startActivity(confirmIntent);
                }
                else {
                    Toast.makeText(ListScreen.this, "Can't confirm! No action taken yet", Toast.LENGTH_SHORT).show();
                }
            }
        });



        //final MyExpandableListAdapter adapter = new MyExpandableListAdapter(this, groups, true);
        //listView.setAdapter(adapter);


    }

    public void listData(){

        //ParseGeoPoint point = new ParseGeoPoint();
        //while(!mGoogleApiClient.isConnected()) {
          //  System.out.println("STUCK");
            getGPSLocation();
        //}

        ParseQuery<serverInfo> query = ParseQuery.getQuery("serverInfo");
        query.whereEqualTo("gpslocation", point);
        query.whereEqualTo("isConfirmed", false);
        query.orderByDescending("timestamp");

        query.findInBackground(new FindCallback<serverInfo>() {
            public void done(List<serverInfo> list, com.parse.ParseException e) {
                if (e == null && list.size()>0) {

                    Log.d("serverInfo", "Retrieved " + list.size() + " serverInfo");

                    // check for a valid email
                    if (list.size() != 0) {
                        for (int i = 0; i < list.size(); i++) {

                            infoItems.add(list.get(i));
                        }

                        // Group 0
                        LocationAddress locationAddress = new LocationAddress();
                        locationAddress.getAddressFromLocation(0, infoItems.get(0).getGPSlocation().getLatitude(), infoItems.get(0).getGPSlocation().getLongitude(),
                                getApplicationContext(), new GeocoderHandler());
                        Group group = new Group("Current GPS Location");
                        groups.append(0,group);


                        // Group 1
                        Group group2 = new Group("Complaints");
                        int num = infoItems.size();
                        double cricLevel = avgCriticalLevel(infoItems);
                        DecimalFormat d1 = new DecimalFormat("###.##");

                        cricLevel = Double.parseDouble(d1.format(cricLevel));

                        String rowString = "Number of Complaints: " + num + "\nAverage Critical Level: " + cricLevel;
                        group2.children.add(rowString);
                        groups.append(1,group2);


                        // Group 2
                        Group group3 = new Group("Confirm ?");
                        ArrayList<serverInfo> waitItems = waitingForConfirmation(infoItems);
                        if (waitItems.size()==0){
                            group3.children.add("No action has been taken from Department still");
                            status=false;
                        }
                        else{
                            group3.children.add("Action has been taken on the given complaints on "+ waitItems.get(0).getDate().toString()+" Waiting for Confirmation");
                            status=true;
                        }


                        groups.append(2,group3);ExpandableListView listView = (ExpandableListView) findViewById(R.id.expandableListView3);
                        MyExpandableListScreenAdapter adapter = new MyExpandableListScreenAdapter(cur_act,groups);
                        listView.setAdapter(adapter);

                    } else {
                        Log.d("user", "Error: " + e.getMessage());
                    }
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_screen, menu);
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

        session = new SessionManager(getApplicationContext());
        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            finish();
            session.logoutUser();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();
    }


    public ArrayList<serverInfo> waitingForConfirmation(ArrayList<serverInfo> items){
        ArrayList<serverInfo> waitservItems = new ArrayList<>();

        for(int i=0;i<items.size();i++)
        {
            if((!items.get(i).getIsConfirmedVal()) && (items.get(i).getIsCleanedVal())){
                waitservItems.add(items.get(i));
            }
        }

        return waitservItems;
    }

    public double avgCriticalLevel(ArrayList<serverInfo> servVal){
        double sum = 0.0;
        for(int i=0;i<servVal.size();i++) {
            sum += (servVal.get(i).getcriticalLevel().doubleValue());
        }

        return  sum / servVal.size();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            int group;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    group = bundle.getInt("group");
                    break;
                default:
                    locationAddress = null;
                    group = -1;
            }
            groups.get(0).children.add(locationAddress);
        }
    }

    private void getGPSLocation() {

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            //buildGoogleApiClient();
            saveLocation();
        }
    }


    /**
     * Method to display the location on UI
     * */
    private void saveLocation() {


        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            DecimalFormat d1 = new DecimalFormat("###.###");

            point.setLatitude(Double.parseDouble(d1.format(latitude)));
            point.setLongitude(Double.parseDouble(d1.format(longitude)));


        } else {

            //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
            //Popup saying unable to obtain GPS location
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        System.out.println("IN ON CONNECTED");

        // Once connected with google api, get the location
        saveLocation();
        listData();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

}