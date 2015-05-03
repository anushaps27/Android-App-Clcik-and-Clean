package examples.course.clickandclean;


/**
 * Created by anusha on 27/4/15.
 */
import android.app.Activity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class MyExpandableListScreenAdapter extends BaseExpandableListAdapter {

    private final SparseArray<Group> groups;
    public LayoutInflater inflater;
    public Activity activity;
    //private boolean check;

    //private HashMap<Integer, Boolean> mGroupCheckStates;

    /*public HashMap<Integer, Boolean> getmGroupCheckStates() {
        return mGroupCheckStates;
    }*/

    /*public void setmGroupCheckStates(HashMap<Integer, Boolean> mGroupCheckStates) {
        this.mGroupCheckStates = mGroupCheckStates;
    }*/

    public MyExpandableListScreenAdapter(Activity act, SparseArray<Group> groups) {
        activity = act;
        this.groups = groups;
        inflater = act.getLayoutInflater();
        // this.check=check;

        // mGroupCheckStates = new HashMap<Integer, Boolean>();
        // for(int i = 0; i<groups.size(); i++) {
        //     mGroupCheckStates.put(i, false);
        // }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).children.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String children = (String) getChild(groupPosition, childPosition);
        TextView text = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_details2, null);
        }

        text = (TextView) convertView.findViewById(R.id.textView1);

        text.setText(children);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, children,
                        Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).children.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        final int mGroupPosition = groupPosition;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_group, null);
        }
        Group group = (Group) getGroup(groupPosition);

        CheckedTextView item = (CheckedTextView) convertView.findViewById(R.id.checkedTextView);

        item.setText(group.string);
        item.setChecked(isExpanded);

        //mGroupCheckStates.put(mGroupPosition, ((CheckBox) v).isChecked());





        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    /*@Override
    public  HashMap<Integer, Boolean> getChecked(Vi) {
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

        boolean value = checkBox.isChecked();
        mGroupCheckStates.put(mGroupPosition, value);
    }*/
}