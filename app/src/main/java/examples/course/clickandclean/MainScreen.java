package examples.course.clickandclean;

import android.content.Intent;
import android.location.Location;
import android.media.session.MediaSessionManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.PushService;
import com.parse.SaveCallback;

import java.text.DecimalFormat;
import java.util.List;


public class MainScreen extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private EditText emailView;
    private  EditText passwordView;
    String email;

    ParseGeoPoint point = new ParseGeoPoint();

    private LocationRequest mLocationRequest;
    private static final String TAG = PhotoScreen.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        if(checkPlayServices())
            buildGoogleApiClient();

        Parse.initialize(this, "2NUGGHhCoB6kQKMVTKfYwehHdg3NnKcrqXy2vdib", "IRERSYLJwVQFNxAW1SNqKQoVtROSknDvReDVA6M6");
        ParseObject.registerSubclass(User.class);
        //PushService.setDefaultPushCallback(this, MainScreen.class);
        //ParseInstallation.getCurrentInstallation().saveInBackground();
        session = new SessionManager(getApplicationContext());


        emailView = (EditText) findViewById(R.id.email);
        passwordView = (EditText) findViewById(R.id.password);

        final Button loginButton = (Button) findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                isLoginValid();
            }
        });

        final TextView forgotPassword = (TextView) findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Create an explicit Intent for starting the RegisterScreen
                // Activity
                Intent forgotPasswordIntent = new Intent(MainScreen.this,
                        ForgotPasswordScreen.class);

                // Use the Intent to start the RegisterScreen Activity
                startActivity(forgotPasswordIntent);
            }
        });

        final TextView changePassword = (TextView) findViewById(R.id.change_password);
        changePassword.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Create an explicit Intent for starting the RegisterScreen
                // Activity
                Intent changePasswordIntent = new Intent(MainScreen.this,
                        ChangePasswordScreen.class);

                // Use the Intent to start the RegisterScreen Activity
                startActivity(changePasswordIntent);
            }
        });

        final TextView registerButton = (TextView) findViewById(R.id.register);
        registerButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Create an explicit Intent for starting the RegisterScreen
                // Activity
                Intent registerIntent = new Intent(MainScreen.this,
                        RegisterScreen.class);

                // Use the Intent to start the RegisterScreen Activity
                startActivity(registerIntent);
            }
        });
    }

    private void isLoginValid() {

        System.out.println("IN LOGIN");
        emailView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        email = emailView.getText().toString();
        final String password = passwordView.getText().toString();


        // Check for a non-empty email field
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
        }

        // check for sufficient password length
        else if(password.length() <= 5){
            passwordView.setError(getString(R.string.error_invalid_password));
        }

        // check for a valid email and password
        else {
            ParseQuery<User> query = ParseQuery.getQuery("User");
            query.whereEqualTo("email_id", email);

            query.findInBackground(new FindCallback<User>() {
                public void done(List<User> list, ParseException e) {
                    if (e == null) {

                        Log.d("user", "Retrieved " + list.size() + " users");

                        // check for a valid email
                        if (list.size() != 0) {
                            User u = new User();
                            u.name = list.get(0).getName();
                            u.email = list.get(0).getEmail();
                            u.password = list.get(0).getPassword();
                            //System.out.println(u.toString());

                            // check for a valid password
                            if (u.password.equals(password)) {

                                session.createLoginSession(email, password);

                                if(email.contains("@garbage.com")) {

                                    Toast.makeText(MainScreen.this, "Successfully Logged In", Toast.LENGTH_SHORT).show();



                                    // Create an explicit Intent for starting the DepartmentScreen Activity
                                    Intent departmentIntent = new Intent(MainScreen.this,
                                            DepartmentScreen.class);

                                    departmentIntent.putExtra("email", email);

                                    // Use the Intent to start the DepartmentScreen Activity
                                    startActivity(departmentIntent);
                                }

                                else {

                                    saveLocation();

                                    Toast.makeText(MainScreen.this, "Successfully Logged In", Toast.LENGTH_SHORT).show();

                                    ParseQuery<serverInfo> query = ParseQuery.getQuery("serverInfo");
                                    System.out.println("LAT:"+point.getLatitude()+" LONG:"+point.getLongitude());
                                    query.whereEqualTo("gpslocation", point);
                                    query.whereEqualTo("isConfirmed", false);
                                    query.orderByAscending("timestamp");

                                    query.findInBackground(new FindCallback<serverInfo>() {

                                        @Override
                                        public void done(List<serverInfo> result, ParseException e) {
                                            if (e == null) {

                                                System.out.println("SIZE:" + result.size());

                                                if (result.size() > 0) {

                                                    // Create an explicit Intent for starting the PhotoScreen Activity
                                                    Intent listscreenIntent = new Intent(MainScreen.this,
                                                            ListScreen.class);

                                                    listscreenIntent.putExtra("email", email);

                                                    // Use the Intent to start the PhotoScreen Activity
                                                    startActivity(listscreenIntent);
                                                } else {

                                                    // Create an explicit Intent for starting the PhotoScreen Activity
                                                    Intent photoIntent = new Intent(MainScreen.this,
                                                            PhotoScreen.class);

                                                    photoIntent.putExtra("email", email);

                                                    // Use the Intent to start the PhotoScreen Activity
                                                    startActivity(photoIntent);

                                                }
                                            }

                                        }
                                    });


                                }
                            } else {
                                passwordView.setError(getString(R.string.error_incorrect_password));
                            }
                        } else {
                            emailView.setError(getString(R.string.error_invalid_email));
                        }
                    } else {
                        Log.d("user", "Error: " + e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
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

    private void getGPSLocation() {

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            saveLocation();
        }
    }


    /**
     * Method to display the location on UI
     * */
    private void saveLocation() {


        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {

            System.out.println("Obtaining LOCATION");
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            DecimalFormat d1 = new DecimalFormat("###.###");

            point.setLatitude(Double.parseDouble(d1.format(latitude)));
            point.setLongitude(Double.parseDouble(d1.format(longitude)));

        } else {

            //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
            //Popup saying unable to obtain GPS location
            System.out.println("Cannot obtain LOCATION");
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
}
