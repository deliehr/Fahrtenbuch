package implementations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriverArrayAdapter extends ArrayAdapter<String> {
    // region local fields
    private Context context = null;
    private int layoutResourceId = 0;
    private List<Driver> listDrivers = null;
    private List<Long> listDriverDatabaseIds = new ArrayList<Long>();
    // endregion

    // region init
    public DriverArrayAdapter(Context context, int layoutResourceId, DriverList listDrivers) {
        super(context, layoutResourceId, listDrivers.getStringList());

        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.listDrivers = listDrivers;

        // database ids
        for(Driver d:listDrivers) {
            this.listDriverDatabaseIds.add(d.getDatabaseId());
        }
    }
    // endregion

    // region local methods
    public long getItemDatabaseId(int position) {
        return this.listDriverDatabaseIds.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemRow = inflater.inflate(this.layoutResourceId, parent, false);

        TextView tvRow = (TextView) itemRow.findViewById(android.R.id.text1);
        tvRow.setText(String.format(Locale.GERMAN, "%s %s", this.listDrivers.get(position).getFirstName(), this.listDrivers.get(position).getLastName()));

        return tvRow;
    }
    // endregion

    // region getters & setters

    public List<Driver> getListDrivers() {
        return listDrivers;
    }

    // endregion
}