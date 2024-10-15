package lv.id.arseniuss.linguae.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import androidx.databinding.BindingAdapter;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

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
        Disposable d = loadImage(urlString).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imageView::setImageBitmap, Throwable::printStackTrace);
    }

    private static Single<Bitmap> loadImage(String imageUrl) {
        return Single.fromCallable(() -> {
            URL url = new URL(imageUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        });
    }
}
