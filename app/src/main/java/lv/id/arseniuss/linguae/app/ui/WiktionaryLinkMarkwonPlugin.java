package lv.id.arseniuss.linguae.app.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.LinkResolverDef;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.app.Configuration;
import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.Utilities;

public class WiktionaryLinkMarkwonPlugin extends AbstractMarkwonPlugin {
    private static final String WIKTIONARY_TERM_URL =
            "https://en.wiktionary.org/api/rest_v1/page/definition/";

    private final Context _context;

    public WiktionaryLinkMarkwonPlugin(Context context) {
        _context = context;
    }

    public static MarkwonPlugin create(Context context) {
        return new WiktionaryLinkMarkwonPlugin(context);
    }

    @NonNull
    private static String getWiktionaryResult(JsonArray langVariants, String word) {
        StringBuilder builder = new StringBuilder();

        if (!langVariants.isEmpty()) {

            builder.append("**Wiktionary:").append("**\n\n");

            for (JsonElement langVariantElement : langVariants) {
                JsonObject langVariantObject = langVariantElement.getAsJsonObject();

                String partOfSpeech = langVariantObject.get("partOfSpeech").getAsString();

                builder.append(partOfSpeech).append(":\n\n");

                JsonArray definitionsArray =
                        langVariantObject.get("definitions").getAsJsonArray();

                for (JsonElement definitionElement : definitionsArray) {
                    JsonObject definitionObject = definitionElement.getAsJsonObject();

                    String definition = definitionObject.get("definition").getAsString();

                    builder.append("- ").append(definition).append("\n");
                }

                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private void accept(View anchor, JsonElement jsonElement, Throwable throwable, String word) {
        if (throwable != null) {
            Toast.makeText(_context, throwable.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        try {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray langVariants =
                    jsonObject.get(Configuration.GetLanguageCode()).getAsJsonArray();

            String result = getWiktionaryResult(langVariants, word);

            View tooltip = LayoutInflater.from(_context)
                    .inflate(R.layout.tooltip_wiktionary, null);
            PopupWindow popupWindow =
                    new PopupWindow(tooltip, ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, true);

            TextView textView = tooltip.findViewById(R.id.text);
            if (textView != null) {
                BindingAdapters.SetMarkdown(textView, result);
            }

            int[] location = new int[2];

            anchor.getLocationOnScreen(location);

            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, location[0], location[1]);
        } catch (Exception ignored) {

        }
    }

    @Override
    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
        LinkResolverDef linkResolverDef = new LinkResolverDef();

        builder.linkResolver((view, link) -> {
            if (link.startsWith("wikt:")) {

                String word = link.substring("wikt:".length()).toLowerCase();

                showTooltip(view, word);
            } else {
                linkResolverDef.resolve(view, link);
            }
        });
    }

    private void showTooltip(View view, String word) {
        Disposable d = Utilities.FetchJson(WIKTIONARY_TERM_URL + word)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((jsonElement, throwable) -> accept(view, jsonElement, throwable, word));
    }
}
