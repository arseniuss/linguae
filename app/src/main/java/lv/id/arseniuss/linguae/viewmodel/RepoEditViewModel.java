package lv.id.arseniuss.linguae.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lv.id.arseniuss.linguae.data.ItemLanguageRepo;

public class RepoEditViewModel extends AndroidViewModel {
    private final MutableLiveData<List<EditRepoViewModel>> _repos = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> _selected = new MutableLiveData<>(-1);
    Gson gson = new Gson();
    Type listType = new TypeToken<List<ItemLanguageRepo>>() { }.getType();

    public RepoEditViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<List<EditRepoViewModel>> Data() {
        return _repos;
    }

    public LiveData<Integer> Selected() { return _selected; }

    public void Add(EditRepoViewModel model) {
        List<EditRepoViewModel> repos = _repos.getValue();

        if (repos == null) repos = new ArrayList<>();
        repos.add(model);

        _repos.setValue(repos);
    }

    public void SetData(String json) {
        List<ItemLanguageRepo> data = gson.fromJson(json, listType);

        if (data == null) data = new ArrayList<>();

        _repos.setValue(data.stream().map(EditRepoViewModel::new).collect(Collectors.toList()));
    }

    public String GetData() {
        List<ItemLanguageRepo> data = Objects.requireNonNull(_repos.getValue())
                .stream()
                .map(r -> new ItemLanguageRepo(r.Name().getValue(), r.Location().getValue()))
                .collect(Collectors.toList());

        return gson.toJson(data);
    }

    public static class EditRepoViewModel extends BaseObservable {

        private final MutableLiveData<String> _name = new MutableLiveData<>("");
        private final MutableLiveData<String> _location = new MutableLiveData<>("");

        public EditRepoViewModel() {

        }

        public EditRepoViewModel(ItemLanguageRepo repo) {
            _name.setValue(repo.Name);
            _location.setValue(repo.Location);
        }

        public MutableLiveData<String> Name() { return _name; }

        public MutableLiveData<String> Location() { return _location; }
    }
}
