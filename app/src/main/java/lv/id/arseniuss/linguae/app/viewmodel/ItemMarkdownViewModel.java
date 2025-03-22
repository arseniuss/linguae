package lv.id.arseniuss.linguae.app.viewmodel;

import androidx.databinding.BaseObservable;

public class ItemMarkdownViewModel extends BaseObservable {

    public String Markdown;

    public ItemMarkdownViewModel(String markdown) {
        Markdown = markdown;
    }
}
