package implementations;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

public class BackgroundService extends IntentService {
    // region fields
    private final String TAG = "BackgroundService";
    private static String field = "";
    // endregion

    // region init
    public BackgroundService() {
        super("");
    }

    public BackgroundService(String name) {
        super(name);
    }
    // endregion

    // region intent service
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean runThread = true;
                int i = 0;

                while(runThread) {
                    // wait
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Log.e("BackgroundService", ie.getMessage());
                    }

                    // do
                    i++;

                    // set
                    BackgroundService.setField(String.valueOf(i));

                    // log
                    Log.i("BackgroundService", "i: " + String.valueOf(i));
                }
            }
        });
        thread.start();
    }
    // endregion

    // region getters & setters
    public static String getField() {
        synchronized (BackgroundService.field) {
            return BackgroundService.field;
        }
    }

    private static void setField(String fieldValue) {
        synchronized (BackgroundService.field) {
            BackgroundService.field = fieldValue;
        }
    }
    // endregion
}