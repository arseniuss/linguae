package lv.id.arseniuss.linguae.app.db.dataaccess;

import androidx.room.Dao;
import androidx.room.Insert;

import java.util.Collection;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lv.id.arseniuss.linguae.app.db.entities.SessionResultEntity;
import lv.id.arseniuss.linguae.app.db.entities.SessionResultWithTaskResults;
import lv.id.arseniuss.linguae.app.db.entities.TaskResultEntity;

@Dao
public abstract class SessionDataAccess {

    @Insert
    protected abstract Single<Long> InsertSessionResult(SessionResultEntity result);

    @Insert
    protected abstract Completable InsertTaskResults(Collection<TaskResultEntity> taskResultList);

    public Completable SaveResult(SessionResultWithTaskResults result) {
        return InsertSessionResult(result.SessionResult).flatMapCompletable(id -> InsertTaskResults(
                result.TaskResults.stream()
                        .peek(taskResult -> taskResult.SessionId = id)
                        .collect(Collectors.toList())));
    }
}
