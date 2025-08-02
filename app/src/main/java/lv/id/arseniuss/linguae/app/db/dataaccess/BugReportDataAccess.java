package lv.id.arseniuss.linguae.app.db.dataaccess;

import androidx.room.Dao;
import androidx.room.Insert;

import io.reactivex.rxjava3.core.Completable;
import lv.id.arseniuss.linguae.app.db.entities.BugReportEntity;

@Dao
public abstract class BugReportDataAccess {
    @Insert
    public abstract Completable Save(BugReportEntity entity);
}
