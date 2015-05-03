package examples.course.clickandclean;

/**
 * Created by anusha on 27/4/15.
 */
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import examples.course.clickandclean.R;

/**
 * Created by hp1 on 21-01-2015.
 */
public class Tab3 extends Fragment {

    SparseArray<Group> groups = new SparseArray<Group>();

    ArrayList<serverInfo> infoItems = new ArrayList<serverInfo>();
    String email;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab3, container,false);

        Parse.initialize(getActivity(), "2NUGGHhCoB6kQKMVTKfYwehHdg3NnKcrqXy2vdib", "IRERSYLJwVQFNxAW1SNqKQoVtROSknDvReDVA6M6");
        ParseObject.registerSubclass(serverInfo.class);

        email = getActivity().getIntent().getStringExtra("email");

        showData(v);

        return v;
    }

    public void showData(View v2) {

        final View v = v2;

        ParseQuery<serverInfo> query = ParseQuery.getQuery("serverInfo");
        query.whereEqualTo("isCleaned", true);
        query.whereEqualTo("isConfirmed", false);
        query.orderByAscending("timestamp");

        query.findInBackground(new FindCallback<serverInfo>() {
            public void done(List<serverInfo> list, ParseException e) {
                if (e == null && list.size() > 0) {

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
                            for (int j = 0; j < num; j++) {
                                group.objectIds.add(collection.get(arraykeys[i]).get(j).getObjectId());
                            }
                            groups.append(i, group);
                            System.out.println(groups.toString());

                        }

                        ExpandableListView listView = (ExpandableListView) v.findViewById(R.id.expandableListView2);
                        final MyExpandableListAdapter adapter = new MyExpandableListAdapter(getActivity(), groups, false, true);
                        listView.setAdapter(adapter);

                        final Button checkButton = (Button) v.findViewById(R.id.button);

                        checkButton.setOnClickListener(new View.OnClickListener() {

                            public void onClick(View v1) {

                                // Create an explicit Intent for starting the confirm
                                // Activity
                                Intent confirmIntent = new Intent(getActivity(),
                                        ConfirmPhotoScreen.class);

                                confirmIntent.putExtra("email", email);

                                // Use the Intent to start the reload Activity
                                startActivity(confirmIntent);
                            }
                        });

                    } else {
                        Log.d("user", "Error: " + e.getMessage());
                    }
                }

            }
        });
    }

    public double avgCriticalLevel(ArrayList<serverInfo> servVal) {
        double sum = 0.0;
        for (int i = 0; i < servVal.size(); i++) {
            sum += (servVal.get(i).getcriticalLevel().doubleValue());
        }

        return sum / servVal.size();
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
}