package examples.course.clickandclean;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;


public class ForgotPasswordScreen extends ActionBarActivity {

    private EditText emailView;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_screen);

        Parse.initialize(this, "2NUGGHhCoB6kQKMVTKfYwehHdg3NnKcrqXy2vdib", "IRERSYLJwVQFNxAW1SNqKQoVtROSknDvReDVA6M6");
        ParseObject.registerSubclass(User.class);

        emailView = (EditText) findViewById(R.id.email);

        final Button sendButton = (Button) findViewById(R.id.change);
        sendButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                isEmailValid();
            }
        });
    }

    private void isEmailValid() {

        emailView.setError(null);

        // Store values at the time of the login attempt.
        final String email = emailView.getText().toString();

        // Check for a non-empty email field
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
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


                            AsyncTask<String, Void, String> newPass = new SendEmailAsyncTask().execute(email);

                            Intent loginIntent = new Intent(ForgotPasswordScreen.this,
                                    MainScreen.class);

                            // Use the Intent to start the PhotoScreen Activity
                            startActivity(loginIntent);

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

    private class SendEmailAsyncTask extends AsyncTask<String, Void, String> {
        SendEmailAsyncTask() {
        }


        @Override
        protected String doInBackground(String... username) {

            String password = Long.toHexString(Double.doubleToLongBits(Math.random()));
            password = password.substring(1, 8);

            try {

                //System.out.println("USERNAME:" +username[0]);
                GMailSender sender = new GMailSender("department.garbage@gmail.com", "garbage123");
                sender.sendMail("Click and Clean: Forgot Password",
                        "Your password has been reset to "+password+".",
                        "Garbage Department",
                        username[0]);
                System.out.println("SENT EMAIL");

            } catch (Exception e) {
                Log.e("SendMail", e.getMessage(), e);
            }
            return "";
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_forgot_password_screen, menu);
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
}
