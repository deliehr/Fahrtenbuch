package implementations;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public class SpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
    protected Object[] objects = null;

    public SpinnerOnItemSelectedListener(Object[] objects) {
        this.objects = objects;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        return;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        return;
    }
}