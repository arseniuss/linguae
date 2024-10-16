package lv.id.arseniuss.linguae.db.dataaccess;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lv.id.arseniuss.linguae.db.entities.Setting;

@Dao
public abstract class SettingDataAccess {
    @Query("SELECT * FROM setting")
    public abstract Single<List<Setting>> GetSettings();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract Completable SaveSetting(List<Setting> settings);
}
