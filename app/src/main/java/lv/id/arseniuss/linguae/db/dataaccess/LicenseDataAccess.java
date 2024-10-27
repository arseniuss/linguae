package lv.id.arseniuss.linguae.db.dataaccess;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import lv.id.arseniuss.linguae.db.entities.License;

@Dao
public abstract class LicenseDataAccess {

    @Query("SELECT * FROM license")
    public abstract Single<List<License>> GetLicenses();

}
