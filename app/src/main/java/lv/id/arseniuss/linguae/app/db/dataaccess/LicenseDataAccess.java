package lv.id.arseniuss.linguae.app.db.dataaccess;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import lv.id.arseniuss.linguae.app.db.entities.LicenseEntity;

@Dao
public abstract class LicenseDataAccess {

    @Query("SELECT * FROM license")
    public abstract Single<List<LicenseEntity>> GetLicenses();

}
