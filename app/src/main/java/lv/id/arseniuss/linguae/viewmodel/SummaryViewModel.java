package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import androidx.lifecycle.AndroidViewModel;
import androidx.preference.PreferenceManager;

import lv.id.arseniuss.linguae.Configuration;
import lv.id.arseniuss.linguae.Constants;
import lv.id.arseniuss.linguae.db.LanguageDatabase;
import lv.id.arseniuss.linguae.db.dataaccess.SummaryDataAccess;

public class SummaryViewModel extends AndroidViewModel {

    private final SharedPreferences _sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication().getBaseContext());

    private final String _language = _sharedPreferences.getString(Constants.PreferenceLanguageKey, "");

    private final SummaryDataAccess _summaryDataAccess =
            LanguageDatabase.GetInstance(getApplication(), _language).GetSummaryDataAccess();

    private LoadListener _loadListener;

    public SummaryViewModel(Application app) {
        super(app);

        Configuration.AddConfigChangeLister(this::onConfigChanged);
    }

    private void onConfigChanged() {
        
    }

    public void Load(LoadListener listener) {

    }

    public interface LoadListener {
        void SetupLogo(Bitmap bitmap);
    }
}