package lv.id.arseniuss.linguae.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.databinding.BindingAdapter;

import java.io.InputStream;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.Utilities;

public class BindingAdapters {

    @BindingAdapter("android:switchIndex")
    public static void SetSwitchIndex(ViewSwitcher viewSwitcher, int index) {
        if (index < viewSwitcher.getChildCount()) {
            viewSwitcher.setDisplayedChild(index);
        }
    }

    @BindingAdapter("android:selected")
    public static void SetSelected(View linearLayout, boolean selected) {
        linearLayout.setSelected(selected);
    }

    @BindingAdapter("imageUrl")
    public static void SetImageByUrl(ImageView imageView, String urlString) {
        if (urlString != null && !urlString.isEmpty()) {
            Disposable d = loadImage(imageView.getContext(), urlString).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(imageView::setImageBitmap, Throwable::printStackTrace);
        }
    }

    @BindingAdapter("markdown")
    public static void SetMarkdown(TextView textView, String text) {
        final Markwon markwon =
                Markwon.builder(textView.getContext())
                        .usePlugin(TablePlugin.create(textView.getContext()))
                        .build();

        if (text != null && !text.isEmpty()) {
            markwon.setMarkdown(textView, text);
        } else {
            textView.setText("");
        }
    }

    private static Single<Bitmap> loadImage(Context context, String imageUrl) {
        return Single.fromCallable(() -> {
            InputStream input = Utilities.GetInputStream(context, imageUrl);

            return BitmapFactory.decodeStream(input);
        });
    }
}
