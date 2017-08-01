package implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriverList extends ArrayList<Driver> {
    public List<String> getStringList() {
        List<String> list = new ArrayList<String>();

        for(int i=0;i < this.size();i++) {
            list.add(String.format(Locale.GERMAN, "%s %s", this.get(i).getFirstName(), this.get(i).getLastName()));
        }

        return list;
    }
}
