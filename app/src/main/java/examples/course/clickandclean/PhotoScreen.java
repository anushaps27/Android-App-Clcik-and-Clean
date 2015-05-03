package examples.course.clickandclean;

import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class PhotoScreen extends ActionBarActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    ImageView image;
    Button critical;
    TextView criticalVal;
    Button logout;
    String email;
    SessionManager session;
    int no_updates = 0;
    boolean status= false;

    private static final String TAG = PhotoScreen.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    private serverInfo server;
    // boolean flag to toggle periodic location updates
    //private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    ParseGeoPoint point = new ParseGeoPoint();

    // Location updates intervals in sec
    /*private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters*/

    // UI elements
    //private TextView lblLocation;
    //private Button btnShowLocation, btnStartLocationUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_screen);

        if(checkPlayServices())
            buildGoogleApiClient();

        Parse.initialize(this, "2NUGGHhCoB6kQKMVTKfYwehHdg3NnKcrqXy2vdib", "IRERSYLJwVQFNxAW1SNqKQoVtROSknDvReDVA6M6");
        ParseObject.registerSubclass(serverInfo.class);

        session = new SessionManager(getApplicationContext());

        server = new serverInfo();

        image = (ImageView) findViewById(R.id.imageView);
        //logout = (Button) findViewById(R.id.button2);
        critical = (Button) findViewById(R.id.ok_button);
        criticalVal = (TextView) findViewById(R.id.critical_value);

        email = getIntent().getStringExtra("email");

        //lblLocation = (TextView) findViewById(R.id.location_text);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 0);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bp = (Bitmap) data.getExtras().get("data");
        image.setImageBitmap(bp);

        server.setphotoInfo(true);
        //getGPSLocation();
    }

    private void updates(){
        critical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String val = criticalVal.getText().toString();
                final Number critical_value = Integer.parseInt(val);
                server.setcriticalLevel(critical_value);


                server.setGPSlocation(point);
                server.setIsCleanedVal(false);
                server.setIsConfirmedVal(false);
                server.setEmailId(email);


                //convert to actual GMT time, not taking phone time
                Date date = new Date();
                //DateFormat df = new SimpleDateFormat("MMM d, yyyy HH:mm:ss");
                DateFormat df = DateFormat.getDateTimeInstance();

                df.setTimeZone(TimeZone.getTimeZone("gmt"));
                String s = df.format(Calendar.getInstance().getTime());

                try {
                    date = df.parse(s);
                    server.settimestamp(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                server.saveInBackground();
                //write query to update all the cleaned but not confirmed to not cleaned

                ParseQuery<serverInfo> query2 = ParseQuery.getQuery("serverInfo");
                query2.whereEqualTo("gpslocation", point);
                query2.whereEqualTo("isCleaned", true);
                query2.whereEqualTo("isCconfirmed", false);
                //query.orderByAscending("timestamp");

                query2.findInBackground(new FindCallback<serverInfo>() {

                    public void done(List<serverInfo> list2, com.parse.ParseException e) {

                        Log.d("serverInfo LIST2", "Retrieved " + list2.size() + " serverInfo");

                        if (e == null && list2.size()>0) {


                            for (int i = 0; i < list2.size(); i++) {
                                //list2.get(i).setIsConfirmedVal(true);
                                //list2.get(i).saveInBackground();
                                //objectids.add(list2.get(i).getObjectId());
                                //to_send_email.add(list2.get(i).getEmailId());


                                ParseQuery<serverInfo> query = ParseQuery.getQuery("serverInfo");

                                // Retrieve the object by id
                                query.getInBackground(list2.get(i).getObjectId(), new GetCallback<serverInfo>() {

                                    public void done(serverInfo info, com.parse.ParseException e) {
                                        if (e == null) {
                                            // Now let's update it with some new data. In this case, only cheatMode and score
                                            // will get sent to the Parse Cloud. playerName hasn't changed.
                                            info.put("isCleaned", false);
                                            //System.out.println("INFO: " + info);
                                            info.saveInBackground();
                                        }
                                    }
                                });
                            }
                        }
                    }
                });

                if(!status) {
                    // Create an explicit Intent for starting the ThankYouScreen Activity
                    Intent photoIntent = new Intent(PhotoScreen.this,
                            ThankYouScreen.class);

                    photoIntent.putExtra("email", email);

                    // Use the Intent to start the PhotoScreen Activity
                    startActivity(photoIntent);
                }
            }
        });
    }

    private void getGPSLocation() {

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
        //    buildGoogleApiClient();
            saveLocation();
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

    /**
     * Method to display the location on UI
     * */
    private void saveLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {

            System.out.println("Obtaining LOCATION  -PHOTO");

            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();


            //lblLocation.setText(latitude + ", " + longitude);
            //System.out.println("Latitude :"+latitude+" Longitude : "+longitude);
            //Add to the database
            DecimalFormat d1 = new DecimalFormat("###.###");

//            ParseGeoPoint point = new ParseGeoPoint();

            point.setLatitude(Double.parseDouble(d1.format(latitude)));
            point.setLongitude(Double.parseDouble(d1.format(longitude)));
        }

        else {

            //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
            //Popup saying unable to obtain GPS location
            System.out.println("Cannot obtain LOCATION - PHOTO SCREEN");
            //Toast.makeText(PhotoScreen.this, "Cannot obtain current location", Toast.LENGTH_SHORT).show();
        }
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

        // Once connected with google api, get the location
        saveLocation();
        updates();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_screen, menu);
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
            session.logoutUser();
        }

        return super.onOptionsItemSelected(item);
    }
}
