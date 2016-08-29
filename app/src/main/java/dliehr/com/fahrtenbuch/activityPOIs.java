package dliehr.com.fahrtenbuch;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class activityPOIs extends AppCompatActivity {
    // app
    private static final String TAG = activityStart.class.getSimpleName();

    // context
    private static Context mContext = null;

    public void btnClickRemovePoi(View view) {

    }

    private static int mSelectedPosition = -1;
    private static View mPopView = null;
    public void fillListView() {
        // get pois, add formated result to listview
        List<PointOfInterest> points = PointOfInterest.getPoints(mContext);
        List valueList = new ArrayList<String>();

        for(PointOfInterest poi : points) {
            //valueList.add(poi.getLocality());
            valueList.add(poi.getFormattedResult());
        }

        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, valueList);
        ((ListView) findViewById(R.id.listViewPois)).setAdapter(adapter);

        // listener
        ListView listPois = (ListView) findViewById(R.id.listViewPois);
        listPois.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedPosition = position;

                // show popup windows
                mPopView = getLayoutInflater().inflate(R.layout.popup_poi, null);
                PopupWindow p = new PopupWindow(mPopView, 500, ViewGroup.LayoutParams.WRAP_CONTENT);
                p.showAtLocation(view, Gravity.CENTER, 0, 0);

                // set text
                TextView tvPoi = (TextView) p.getContentView().findViewById(R.id.tvPoiToDelete);
                List<PointOfInterest> points = PointOfInterest.getPoints(mContext);

                try {
                    tvPoi.setText(points.get(mSelectedPosition).getFormattedResult());
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    public void btnClickDeletePoi(View view) {
        if(mSelectedPosition >= 0) {
            PointOfInterest.removePoi(mSelectedPosition);
        } else {
            Log.d(TAG, "no selected item");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_pois);

        // context
        mContext = this;

        // fill listview with elements
        this.fillListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemActivityStart:
                Intent intentMain = new Intent(this, activityStart.class);
                startActivity(intentMain);

                return true;

            case R.id.menuItemActivityGps:
                Intent intentGps = new Intent(this, activityGps.class);
                startActivity(intentGps);

                return true;

            case R.id.menuItemActivityDatabase:
                Intent intentDb = new Intent(this, activityDatabase.class);
                startActivity(intentDb);

                return true;

            case R.id.menuItemActivityConfig:
                Intent intentConfig = new Intent(this, activityConfig.class);
                startActivity(intentConfig);

                return true;

            case R.id.menuItemActivityPOI:
                Intent intentPoi = new Intent(this, activityPOIs.class);
                startActivity(intentPoi);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
