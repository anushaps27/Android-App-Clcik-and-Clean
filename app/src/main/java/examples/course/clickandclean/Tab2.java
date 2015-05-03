package examples.course.clickandclean;


/**
 * Created by hp1 on 21-01-2015.
 */
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by hp1 on 21-01-2015.
 */
public class Tab2 extends Fragment {

    SparseArray<Group> groups = new SparseArray<Group>();

    ArrayList<serverInfo> infoItems = new ArrayList<serverInfo>();

    int no_updates;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.tab2,container,false);

        Parse.initialize(getActivity(), "2NUGGHhCoB6kQKMVTKfYwehHdg3NnKcrqXy2vdib", "IRERSYLJwVQFNxAW1SNqKQoVtROSknDvReDVA6M6");
        ParseObject.registerSubclass(serverInfo.class);
        //PushService.setDefaultPushCallback(getActivity(), MainScreen.class);
        //PushService.subscribe(getActivity(), "Everyone", MainScreen.class);
        //ParseInstallation.getCurrentInstallation().saveInBackground();


        processData(v);
        return v;
    }

    public void processData(View v2) {

        final View v = v2;

        ParseQuery<serverInfo> query = ParseQuery.getQuery("serverInfo");
        query.whereEqualTo("isCleaned", false);
        query.orderByAscending("timestamp");

        query.findInBackground(new FindCallback<serverInfo>() {
            public void done(List<serverInfo> list, ParseException e) {
                if (e == null && list.size()>0) {

                    Log.d("serverInfo", "Retrieved " + list.size() + " serverInfo");

                    // check for a valid email
                    if (list.size() != 0) {
                        for (int i = 0; i < list.size(); i++) {

                            infoItems.add(list.get(i));
                        }

                        HashMap<ParseGeoPoint, ArrayList<serverInfo>> collection = new HashMap<ParseGeoPoint, ArrayList<serverInfo>>();
                        collection = makeHash();

                        Set<ParseGeoPoint> keys = collection.keySet();
                        ParseGeoPoint[] arraykeys = new ParseGeoPoint[keys.size()];
                        arraykeys = keys.toArray(arraykeys);

                        for (int i = 0; i < keys.size(); i++) {
                            int num = collection.get(arraykeys[i]).size();
                            double cricLevel = avgCriticalLevel(collection.get(arraykeys[i]));
                            DecimalFormat d1 = new DecimalFormat("###.##");

                            cricLevel = Double.parseDouble(d1.format(cricLevel));

                            String rowString = "Number of Complaints: " + num + "\nAverage Critical Level: " + cricLevel;
                            Group group = new Group(rowString);
                            for (int j = 0; j < 1; j++) {
                                LocationAddress locationAddress = new LocationAddress();
                                locationAddress.getAddressFromLocation(i, arraykeys[i].getLatitude(), arraykeys[i].getLongitude(),
                                        getActivity().getApplicationContext(), new GeocoderHandler());
                            }
                            for (int j=0; j<num; j++) {
                                group.objectIds.add(collection.get(arraykeys[i]).get(j).getObjectId());
                                group.channels.add(collection.get(arraykeys[i]).get(j).getEmailId());
                            }

                            groups.append(i,group);

                        }

                        ExpandableListView listView = (ExpandableListView) v.findViewById(R.id.expandableListView);
                        final MyExpandableListAdapter adapter = new MyExpandableListAdapter(getActivity(), groups, true, false);
                        listView.setAdapter(adapter);

                        final Button checkButton = (Button) v.findViewById(R.id.clean_button);

                        checkButton.setOnClickListener(new View.OnClickListener() {

                            public void onClick(View v1) {

                                System.out.println(adapter.getmGroupCheckStates().toString());
                                HashMap<Integer, Boolean> checked = adapter.getmGroupCheckStates();

                                final Set<Integer> keys = checked.keySet();
                                Integer[] arrayKeys = new Integer[keys.size()];
                                arrayKeys = keys.toArray(arrayKeys);

                                for (int i = 0; i < keys.size(); i++) {
                                    boolean value = checked.get(arrayKeys[i]);
                                    if(value)
                                        no_updates+=groups.get(arrayKeys[i]).objectIds.size();
                                }
                                final int updates_check = no_updates;
                                no_updates=0;
                                for (int i = 0; i < keys.size(); i++) {
                                    final int k = i;
                                    boolean value = checked.get(arrayKeys[i]);
                                    if(value) {
                                        final Group group = groups.get(arrayKeys[i]);
                                        for (int j = 0; j < group.objectIds.size(); j++) {

                                            final int l = j;

                                            ParseQuery<serverInfo> query = ParseQuery.getQuery("serverInfo");

                                            // Retrieve the object by id
                                            query.getInBackground(group.objectIds.get(j), new GetCallback<serverInfo>() {

                                                public void done(serverInfo info, ParseException e) {
                                                if (e == null) {
                                                    // Now let's update it with some new data. In this case, only cheatMode and score
                                                    // will get sent to the Parse Cloud. playerName hasn't changed.
                                                    info.put("isCleaned", true);
                                                    System.out.println("INFO: "+info);
                                                    info.saveInBackground();

                                                    no_updates++;
                                                    System.out.println("Updates"+updates_check+no_updates);
                                                    if (no_updates == updates_check) {

                                                        for (int j = 0; j < group.channels.size(); j++) {
                                                            AsyncTask<String, Void, String> newPass = new SendEmailAsyncTask().execute(group.channels.get(j));
                                                        }

                                                        // Create an explicit Intent for starting the reloading
                                                        // Activity
                                                        Intent reloadIntent = new Intent(getActivity(),
                                                                DepartmentScreen.class);

                                                        // Use the Intent to start the reload Activity
                                                        startActivity(reloadIntent);
                                                    }
                                                }
                                                }
                                            });
                                        }
                                    }

                                }
                            }
                        });

                    } else {
                        Log.d("user", "Error: " + e.getMessage());
                    }
                }

            }
        });

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
            groups.get(group).children.add(locationAddress);
        }
    }

    HashMap<ParseGeoPoint, ArrayList<serverInfo>> makeHash() {

        HashMap<ParseGeoPoint, ArrayList<serverInfo>> collection = new HashMap<ParseGeoPoint, ArrayList<serverInfo>>();
        DecimalFormat df = new DecimalFormat("###.###");
        boolean found = false;


        for (int i = 0; i < (infoItems.size()); i++) {
            found=false;
            ParseGeoPoint key = new ParseGeoPoint();
            Set<ParseGeoPoint> keys = collection.keySet();
            ParseGeoPoint[] arraykeys = new ParseGeoPoint[keys.size()];
            arraykeys = keys.toArray(arraykeys);
            key.setLatitude(Double.parseDouble(df.format(infoItems.get(i).getGPSlocation().getLatitude())));
            key.setLongitude(Double.parseDouble(df.format(infoItems.get(i).getGPSlocation().getLongitude())));

            for (int j = 0; j < keys.size(); j++) {

                if((arraykeys[j].getLatitude() == key.getLatitude()) && (arraykeys[j].getLongitude() == key.getLongitude())) {
                    collection.get(arraykeys[j]).add(infoItems.get(i));
                    found = true;
                    break;
                }
            }
            if(!found) {
                ArrayList<serverInfo> serverVals = new ArrayList<serverInfo>();
                serverVals.add(infoItems.get(i));
                collection.put(key, serverVals);
            }

        }

        return collection;

    }

    private class SendEmailAsyncTask extends AsyncTask<String, Void, String> {
        SendEmailAsyncTask() {
        }


        @Override
        protected String doInBackground(String... username) {

            try {

                //System.out.println("USERNAME:" +username[0]);
                GMailSender sender = new GMailSender("department.garbage@gmail.com", "garbage123");
                sender.sendMail("Click and Clean: Thank You",
                        "Thank You for helping. Action has been taken. Please confirm.",
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