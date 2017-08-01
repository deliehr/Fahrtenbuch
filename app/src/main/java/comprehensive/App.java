package comprehensive;


import android.content.Context;
import android.util.Log;

import java.io.InputStream;

public class App {
    /**
     * Reads a raw file byte by byte and return the content as a string.
     * @param context The corresponding activity / context
     * @param resourceId The raw ressource id as Integer
     * @return The content as string
     */
    public static String getStringContentFromRawFile(Context context, int resourceId) {
        // try to read the raw file
        try {
            StringBuilder stringBuilder = new StringBuilder();
            InputStream is = context.getResources().openRawResource(resourceId);
            //byte[] buffer = new byte[is.available()];
            byte[] buffer = new byte[1];
            while(is.read(buffer) != -1) {
                stringBuilder.append(new String(buffer, "ISO-8859-1"));
            }

            return stringBuilder.toString();
        } catch (Exception e) {
            Log.e("App", "Exception raw file: " + e.getMessage());
        }

        return "";
    }

    public static int convertBooleanToInt(boolean value) {
        if(value) {
            return 1;
        }

        return 0;
    }
}