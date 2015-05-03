package examples.course.clickandclean;

import android.content.Intent;
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
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
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


public class ConfirmPhotoScreen extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    ImageView image;
    Button confirm;
    int status;
    ArrayList<String> to_send_email = new ArrayList<>();
    ArrayList<String> objectids = new ArrayList<>();
    String email;
    int no_updates=0;
    SessionManager session;

    private serverInfo server = new serverInfo();


    private LocationRequest mLocationRequest;
    private static final String TAG = PhotoScreen.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    ParseGeoPoint point = new ParseGeoPoint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_photo_screen);

        if(checkPlayServices())
            buildGoogleApiClient();

        Parse.initialize(this, "2NUGGHhCoB6kQKMVTKfYwehHdg3NnKcrqXy2vdib", "IRERSYLJwVQFNxAW1SNqKQoVtROSknDvReDVA6M6");
        ParseObject.registerSubclass(serverInfo.class);

        server = new serverInfo();

        image = (ImageView) findViewById(R.id.imageView);
        //logout = (Button) findViewById(R.id.button2);
        confirm = (Button) findViewById(R.id.ok_button);

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
        getGPSLocation();

        System.out.println("LATITUDE" + point.getLatitude());
        System.out.println("LONGITUDE"+point.getLatitude());

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ParseQuery<serverInfo> query = ParseQuery.getQuery("serverInfo");
                query.whereEqualTo("gpslocation", point);
                //query.whereEqualTo("IsCleaned", true);
                query.orderByAscending("timestamp");

                query.findInBackground(new FindCallback<serverInfo>() {

                    public void done(List<serverInfo> list, com.parse.ParseException e) {

                        if (e == null && list.size()>0) {

                            Log.d("serverInfo", "Retrieved " + list.size() + " serverInfo");

                            ParseQuery<serverInfo> query2 = ParseQuery.getQuery("serverInfo");
                            query2.whereEqualTo("gpslocation", point);
                            query2.whereEqualTo("isCleaned", true);
                            //query.orderByAscending("timestamp");

                            query2.findInBackground(new FindCallback<serverInfo>() {

                                public void done(List<serverInfo> list2, com.parse.ParseException e) {

                                    Log.d("serverInfo LIST2", "Retrieved " + list2.size() + " serverInfo");

                                    if (e == null && list2.size()>0) {

                                        final int updates_check = list2.size();

                                        for (int i = 0; i < list2.size(); i++) {
                                            //list2.get(i).setIsConfirmedVal(true);
                                            //list2.get(i).saveInBackground();
                                            //objectids.add(list2.get(i).getObjectId());
                                            to_send_email.add(list2.get(i).getEmailId());


                                            ParseQuery<serverInfo> query = ParseQuery.getQuery("serverInfo");

                                            // Retrieve the object by id
                                            query.getInBackground(list2.get(i).getObjectId(), new GetCallback<serverInfo>() {

                                                public void done(serverInfo info, com.parse.ParseException e) {
                                                    if (e == null) {
                                                        // Now let's update it with some new data. In this case, only cheatMode and score
                                                        // will get sent to the Parse Cloud. playerName hasn't changed.
                                                        info.put("isConfirmed", true);
                                                        //System.out.println("INFO: " + info);
                                                        info.saveInBackground();

                                                        no_updates++;
                                                        //System.out.println("Updates" + updates_check + no_updates);
                                                        if (no_updates == updates_check) {

                                                            for (int j = 0; j < to_send_email.size(); j++) {
                                                                AsyncTask<String, Void, String> newPass = new SendEmailAsyncTask().execute(to_send_email.get(j));
                                                            }

                                                            status=1;

                                                            //server = new serverInfo();
                                                            //server.setphotoInfo(true);
                                                            server.setcriticalLevel(0);
                                                            server.setEmailId(email);
                                                            server.setIsCleanedVal(true);
                                                            server.setIsConfirmedVal(true);
                                                            //convert to actual GMT time, not taking phone time
                                                            Date date = new Date();
                                                            //DateFormat df = new SimpleDateFormat("MMM d, yyyy HH:mm:ss");
                                                            DateFormat df = DateFormat.getDateTimeInstance();

                                                            df.setTimeZone(TimeZone.getTimeZone("gmt"));
                                                            String s = df.format(Calendar.getInstance().getTime());

                                                            try {
                                                                date = df.parse(s);
                                                                server.settimestamp(date);
                                                            } catch (ParseException e1) {
                                                                e1.printStackTrace();
                                                            }
                                                            server.setGPSlocation(point);
                                                            server.saveInBackground();

                                                            Toast.makeText(ConfirmPhotoScreen.this, "Complaints successfully closed", Toast.LENGTH_SHORT).show();


                                                            if(email.contains("@garbage.com")) {
                                                                // Create an explicit Intent for starting the ThankYouScreen Activity
                                                                Intent departmentIntent = new Intent(ConfirmPhotoScreen.this,
                                                                        DepartmentScreen.class);

                                                                departmentIntent.putExtra("email", email);

                                                                // Use the Intent to start the PhotoScreen Activity
                                                                startActivity(departmentIntent);
                                                            }
                                                            else {
                                                                // Create an explicit Intent for starting the ThankYouScreen Activity
                                                                Intent tyIntent = new Intent(ConfirmPhotoScreen.this,
                                                                        ThankYouScreen.class);

                                                                // Use the Intent to start the PhotoScreen Activity
                                                                startActivity(tyIntent);
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                    else {
                                        status=3;
                                        Toast.makeText(ConfirmPhotoScreen.this, "Can't confirm! No action taken yet", Toast.LENGTH_SHORT).show();
                                        // Create an explicit Intent for starting the ThankYouScreen Activity
                                        Intent departmentIntent = new Intent(ConfirmPhotoScreen.this,
                                                DepartmentScreen.class);

                                        departmentIntent.putExtra("email", email);

                                        // Use the Intent to start the PhotoScreen Activity
                                        startActivity(departmentIntent);
                                    }
                                }
                            });
                        }
                        else {
                            Log.d("user", "Error: " + e.getMessage());
                            status = 2;
                            Toast.makeText(ConfirmPhotoScreen.this, "No complaints for your location", Toast.LENGTH_SHORT).show();
                            // Create an explicit Intent for starting the ThankYouScreen Activity
                            Intent departmentIntent = new Intent(ConfirmPhotoScreen.this,
                                    DepartmentScreen.class);

                            // Use the Intent to start the PhotoScreen Activity
                            startActivity(departmentIntent);
                        }
                    }
                });
            }
        });
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

        boolean result;
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            DecimalFormat df = new DecimalFormat("###.###");

            //ParseGeoPoint location = new ParseGeoPoint();
            point.setLatitude(Double.parseDouble(df.format(latitude)));
            point.setLongitude(Double.parseDouble(df.format(longitude)));

        } else {

            //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
            //Popup saying unable to obtain GPS location
            //Toast.makeText(ConfirmPhotoScreen.this, "Cannot obtain current location", Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_confirm_photo_screen, menu);
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
        }        session = new SessionManager(getApplicationContext());
        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            finish();
            session.logoutUser();
        }



        return super.onOptionsItemSelected(item);
    }

    private class SendEmailAsyncTask extends AsyncTask<String, Void, String> {
        SendEmailAsyncTask() {
        }


        @Override
        protected String doInBackground(String... username) {

            try {

                //System.out.println("USERNAME:" +username[0]);
                GMailSender sender = new GMailSender("department.garbage@gmail.com", "garbage123");
                sender.sendMail("Click and Clean: Confirmation",
                        "Thank You for helping. Action has been taken and confirmed. Complaint successfully closed by "+email+"!",
                        "department.garbage@gmail.com",
                        username[0]);
                System.out.println("SENT EMAIL");

            } catch (Exception e) {
                Log.e("SendMail", e.getMessage(), e);
            }
            return "";
        }
    }
}
