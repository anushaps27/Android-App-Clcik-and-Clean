package examples.course.clickandclean;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;


public class ChangePasswordScreen extends ActionBarActivity {

    private EditText emailView;
    private  EditText oldpasswordView;
    private  EditText newpasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password_screen);

        emailView = (EditText) findViewById(R.id.email);
        oldpasswordView = (EditText) findViewById(R.id.oldpassword);
        newpasswordView = (EditText) findViewById(R.id.newpassword);

        final TextView changeButton = (TextView) findViewById(R.id.change);
        changeButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                isChangeValid();
                // Create an explicit Intent for starting the RegisterScreen
                // Activity
                Intent loginIntent = new Intent(ChangePasswordScreen.this,
                        MainScreen.class);

                // Use the Intent to start the RegisterScreen Activity
                startActivity(loginIntent);
            }
        });
    }

    private void isChangeValid() {

        emailView.setError(null);
        oldpasswordView.setError(null);
        newpasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String email = emailView.getText().toString();
        final String oldpassword = oldpasswordView.getText().toString();
        final String newpassword = newpasswordView.getText().toString();

        // Check for a non-empty email field
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
        }

        else {
            ParseQuery<User> query = ParseQuery.getQuery("User");
            query.whereEqualTo("email_id", email);

            query.findInBackground(new FindCallback<User>() {
                public void done(List<User> list, ParseException e) {
                    if (e == null) {

                        Log.d("user", "Retrieved " + list.size() + " users");

                        // check for a valid email
                        if (list.size() != 0) {
                            //User u = new User();
                            //u.name = list.get(0).getName();
                            //u.email = list.get(0).getEmail();
                            //u.password = list.get(0).getPassword();
                            //System.out.println(u.toString());

                            // check for a valid password
                            if (list.get(0).getPassword().equals(oldpassword)) {

                                // check for sufficient password length
                                if(newpassword.length() <= 5){
                                    newpasswordView.setError(getString(R.string.error_invalid_password));
                                }

                                else {
                                    list.get(0).setPassword(newpassword);
                                    list.get(0).saveInBackground();

                                    Toast.makeText(ChangePasswordScreen.this, "Password has been changed", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                oldpasswordView.setError(getString(R.string.error_incorrect_password));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_change_password_screen, menu);
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
