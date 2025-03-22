package lv.id.arseniuss.linguae.app;

import android.app.Application;

public class LinguaeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Constants.Init(getBaseContext());
    }
}
