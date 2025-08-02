package lv.id.arseniuss.linguae.app.viewmodel;

import androidx.databinding.BaseObservable;

import lv.id.arseniuss.linguae.app.Utilities;

public class ItemMarkdownViewModel extends BaseObservable {

    public String Markdown;
    public String Word;

    public ItemMarkdownViewModel(String markdown) {
        Markdown = markdown;
        Word = Utilities.ExtractLinkTitles(markdown);
    }
}
