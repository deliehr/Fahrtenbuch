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

public class DatabaseTripArrayAdapter extends ArrayAdapter<String> {
    // region local fields
    private Context context = null;
    private int layoutResourceId = 0;
    private List<Long> listTripDatabaseIds = null;
    // endregion

    // region init
    public DatabaseTripArrayAdapter(Context context, int layoutResourceId, List<String> listTrips, List<Long> listTripDatabaseIds) {
        super(context, layoutResourceId, listTrips);

        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.listTripDatabaseIds = listTripDatabaseIds;
    }
    // endregion

    // region local methods
    public long getItemDatabaseId(int position) {
        return this.listTripDatabaseIds.get(position);
    }

    /*
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemRow = inflater.inflate(this.layoutResourceId, parent, false);

        TextView tvRow = (TextView) itemRow.findViewById(android.R.id.text1);
        tvRow.setText(String.format(Locale.GERMAN, "%s: %6.1f km", this.listCars.get(position).getModel(), this.listCars.get(position).getMileage()));

        return tvRow;
    }
    */
    // endregion

    // region getters & setters

    public List<Long> getListTripDatabaseIds() {
        return listTripDatabaseIds;
    }

    // endregion
}