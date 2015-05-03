package examples.course.clickandclean;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;


public class RegisterScreen extends ActionBarActivity implements OnClickListener {

    private EditText emailView;
    private EditText passwordView;
    private EditText nameView;
    private EditText confirmPasswordView;

    Button registerButton, insidePopupButton;
    LinearLayout layoutOfPopup;
    PopupWindow popupMessage;
    TextView popupText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        Parse.initialize(this, "2NUGGHhCoB6kQKMVTKfYwehHdg3NnKcrqXy2vdib", "IRERSYLJwVQFNxAW1SNqKQoVtROSknDvReDVA6M6");
        ParseObject.registerSubclass(User.class);

        nameView = (EditText) findViewById(R.id.name);
        emailView = (EditText) findViewById(R.id.email);
        passwordView = (EditText) findViewById(R.id.password);
        confirmPasswordView = (EditText) findViewById(R.id.confirm_password);

        registerButton = (Button) findViewById(R.id.register);
        registerButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //attemptLogin();
                checkValidRegistration();
            }
        });
    }


   private void checkValidRegistration(){

       nameView.setError(null);
       emailView.setError(null);
       passwordView.setError(null);
       confirmPasswordView.setError(null);


       // Store values at the time of the login attempt.
       final String name = nameView.getText().toString();
       final String email = emailView.getText().toString();
       final String password = passwordView.getText().toString();
       final String confirmPassword = confirmPasswordView.getText().toString();


       // Check for a non-empty name field
       if (TextUtils.isEmpty(name)) {
           nameView.setError(getString(R.string.error_field_required));
       }

       // Check for a non-empty email field
       else if (TextUtils.isEmpty(email)) {
           emailView.setError(getString(R.string.error_field_required));
       }

       // check for sufficient password length
       else if(password.length() <= 5){
           passwordView.setError(getString(R.string.error_invalid_password));
       }

       // check for invalid confirm password
       else if(!password.equals(confirmPassword)){
           confirmPasswordView.setError(getString(R.string.error_invalid_confirm_password));
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
                           emailView.setError(getString(R.string.error_existing_email));
                       }
                       else{
                           User new_user = new User();
                           new_user.setName(name);
                           new_user.setEmail(email);
                           new_user.setPassword(password);
                           new_user.saveInBackground();

                           init();
                           popupInit();

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
        getMenuInflater().inflate(R.menu.menu_register_screen, menu);
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



    public void init() {
        popupText = new TextView(this);
        insidePopupButton = new Button(this);
        layoutOfPopup = new LinearLayout(this);
        insidePopupButton.setText(R.string.ok);
        popupText.setText(R.string.popuptext);
        popupText.setPadding(0, 0, 0, 20);
        layoutOfPopup.setOrientation(LinearLayout.HORIZONTAL);
        layoutOfPopup.addView(popupText);
        layoutOfPopup.addView(insidePopupButton);
    }


    private void popupInit() {
        popupMessage = new PopupWindow(layoutOfPopup, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        popupMessage.setContentView(layoutOfPopup);
        popupMessage.showAsDropDown(registerButton, 0, 0);
        insidePopupButton.setOnClickListener(this);

    }

    public void onClick(View v) {
        if (v.getId() == R.id.register) {
            popupMessage.showAsDropDown(registerButton, 0, 0);
        }
        else {
            popupMessage.dismiss();

            final String email = emailView.getText().toString();

            if(email.contains("@garbage.com")) {

                Toast.makeText(RegisterScreen.this, "Successfully Registered", Toast.LENGTH_SHORT).show();

                // Create an explicit Intent for starting the DepartmentScreen Activity
                Intent departmentIntent = new Intent(RegisterScreen.this,
                        DepartmentScreen.class);

                // Use the Intent to start the DepartmentScreen Activity
                startActivity(departmentIntent);
            }
            else {

                Toast.makeText(RegisterScreen.this, "Successfully Registered", Toast.LENGTH_SHORT).show();

                // Create an explicit Intent for starting the PhotoScreen Activity
                Intent PhotoIntent = new Intent(RegisterScreen.this,
                        PhotoScreen.class);
                PhotoIntent.putExtra("email", email);

                // Use the Intent to start the PhotoScreen Activity
                startActivity(PhotoIntent);
            }
        }
    }
}