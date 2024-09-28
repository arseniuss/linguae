package lv.id.arseniuss.linguae.viewmodel;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import java.util.Objects;

import lv.id.arseniuss.linguae.data.ItemLanguageRepo;

public class LanguageRepoItemViewModel extends BaseObservable {

    private final ItemLanguageRepo _repo;

    public LanguageRepoItemViewModel() {
        _repo = new ItemLanguageRepo();
    }

    public LanguageRepoItemViewModel(ItemLanguageRepo repo) {
        _repo = repo;
    }

    @Bindable("Name")
    public String getName() {
        return _repo.Name;
    }

    @Bindable("Name")
    public void setName(String name) {
        if (!Objects.equals(_repo.Name, name)) {
            _repo.Name = name;
            notifyPropertyChanged(BR.name);
        }
    }

    public ItemLanguageRepo getModel() { return _repo; }

    @Bindable("Location")
    public String getLocation() {
        return _repo.Location;
    }

    @Bindable("Location")
    public void setLocation(String location) {
        if (!Objects.equals(_repo.Location, location)) {
            _repo.Location = location;
            notifyPropertyChanged(BR.location);
        }
    }
}
