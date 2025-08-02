package lv.id.arseniuss.linguae.app;

import android.app.Application;
import android.os.DeadObjectException;
import android.util.Log;

public class LinguaeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Constants.Init(getBaseContext());

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof DeadObjectException) {
                Log.i("DEBUG", "DeadObjectException");


            }
        });
    }
}
