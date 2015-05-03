package examples.course.clickandclean;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.parse.ParsePush;


public class ThankYouScreen extends ActionBarActivity {

    Button logout;
    String email;

    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you_screen);

        email = getIntent().getStringExtra("email");
        /*logout = (Button) findViewById(R.id.button);

        //session = new SessionManager(getApplicationContext());

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //ParsePush.unsubscribeInBackground(email);
                // Create an explicit Intent for starting the LoginScreen
                // Activity
                Intent loginIntent = new Intent(ThankYouScreen.this,
                        MainScreen.class);

                // Use the Intent to start the LoginScreen Activity
                startActivity(loginIntent);
            }
        });*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_thank_you_screen, menu);
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
}
