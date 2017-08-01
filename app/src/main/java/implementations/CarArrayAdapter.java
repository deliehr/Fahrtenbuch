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

public class CarArrayAdapter extends ArrayAdapter<String> {
    // region local fields
    private Context context = null;
    private int layoutResourceId = 0;
    private List<Car> listCars = null;
    private List<Long> listCarDatabaseIds = new ArrayList<Long>();
    // endregion

    // region init
    public CarArrayAdapter(Context context, int layoutResourceId, CarList listCars) {
        super(context, layoutResourceId, listCars.getStringList());

        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.listCars = listCars;

        // database ids
        for(Car c:listCars) {
            this.listCarDatabaseIds.add(c.getDatabaseId());
        }
    }
    // endregion

    // region local methods
    public long getItemDatabaseId(int position) {
        return this.listCarDatabaseIds.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemRow = inflater.inflate(this.layoutResourceId, parent, false);

        TextView tvRow = (TextView) itemRow.findViewById(android.R.id.text1);
        tvRow.setText(String.format(Locale.GERMAN, "%s: %6.0f km", this.listCars.get(position).getModel(), this.listCars.get(position).getMileage()));

        return tvRow;
    }
    // endregion

    // region getters & setters

    public List<Car> getListCars() {
        return listCars;
    }

    // endregion
}