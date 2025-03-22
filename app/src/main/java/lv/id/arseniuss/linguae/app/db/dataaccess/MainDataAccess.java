package lv.id.arseniuss.linguae.app.db.dataaccess;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lv.id.arseniuss.linguae.app.db.entities.ConfigEntity;
import lv.id.arseniuss.linguae.app.db.entities.SettingEntity;

@Dao
public abstract class MainDataAccess {
    @Query("SELECT * FROM setting")
    public abstract Single<List<SettingEntity>> GetSettings();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract Completable SaveSetting(List<SettingEntity> settings);

    @Query("SELECT * FROM config")
    public abstract Single<List<ConfigEntity>> GetConfig();
}
