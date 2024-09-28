package lv.id.arseniuss.linguae.db.dataaccess;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import lv.id.arseniuss.linguae.db.entities.Config;

@Dao
public abstract class SettingDataAccess {
    @Query("SELECT * from config")
    public abstract Single<List<Config>> GetTaskConfig();
}
