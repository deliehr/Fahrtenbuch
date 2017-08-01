package implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CarList extends ArrayList<Car> {
    public List<String> getStringList() {
        List<String> list = new ArrayList<String>();

        for(int i=0;i < this.size();i++) {
            list.add(String.format(Locale.GERMAN, "%s: %6.1f km", this.get(i).getModel(), this.get(i).getMileage()));
        }

        return list;
    }
}
