package implementations;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

public class ExtEditText extends AppCompatEditText {
    // region local fields
    private Object[] hiddenValues = null;
    // endregion

    // region init
    public ExtEditText(Object[] hiddenValues, Context context) {
        this(context, null);

        this.setHiddenValues(hiddenValues);
    }

    public ExtEditText(Context context) {
        this(context, null);
    }

    public ExtEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // endregion

    // region getters & setters
    public Object[] getHiddenValues() {
        return hiddenValues;
    }

    public void setHiddenValues(Object[] hiddenValues) {
        this.hiddenValues = hiddenValues;
    }
    // endregion
}