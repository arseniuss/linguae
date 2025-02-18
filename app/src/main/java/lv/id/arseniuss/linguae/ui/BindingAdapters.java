package lv.id.arseniuss.linguae.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewAnimator;
import android.widget.ViewSwitcher;

import androidx.databinding.BaseObservable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.lifecycle.LifecycleOwner;

import java.io.InputStream;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.Utilities;

public class BindingAdapters {

    @BindingAdapter("items")
    public static <T extends BaseObservable>
    void SetAdapterLinearLayoutItems(AdapterLinearLayout container, List<T> entries) {
        if (entries == null || container.getItemLayoutResId() == 0) return;

        Adapter adapter = container.getAdapter();

        if (!(adapter instanceof MyAdapter)) {
            Context context = container.getContext();
            LifecycleOwner lifecycleOwner = null;

            if (context instanceof LifecycleOwner) {
                lifecycleOwner = (LifecycleOwner) context;
            }

            adapter = new MyAdapter<T>(container.getContext(), lifecycleOwner,
                    container.getItemLayoutResId());
            container.setAdapter(adapter);
        }


        MyAdapter<T> myAdapter = (MyAdapter<T>) container.getAdapter();

        myAdapter.clear();
        myAdapter.addAll(entries);
        myAdapter.notifyDataSetChanged();
    }

    @BindingAdapter("android:items")
    public static void SetSpinnerItems(Spinner spinner, List<String> entries) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();

        if (adapter == null) {

            adapter = new ArrayAdapter<>(spinner.getContext(),
                    android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

        assert entries != null;

        adapter.clear();
        adapter.addAll(entries);
        adapter.notifyDataSetChanged();
    }

    @InverseBindingAdapter(attribute = "android:selectedItem")
    public static String GetSpinnerItem(Spinner spinner) {
        return (String) spinner.getSelectedItem();
    }

    @BindingAdapter("android:selectedItemAttrChanged")
    public static void SetSpinnerListener(Spinner spinner, final InverseBindingListener selectedItemChangedListener) {
        if (selectedItemChangedListener != null) {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedItemChangedListener.onChange();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    @BindingAdapter("android:selectedItem")
    public static void SetSpinnerItem(Spinner spinner, String value) {
        if (spinner.getAdapter() != null)
            for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
                Object item = spinner.getAdapter().getItem(i);

                if (item == value) {
                    spinner.setSelection(i);
                    break;
                }
            }
    }

    @BindingAdapter("android:switchIndex")
    public static void SetSwitchIndex(ViewSwitcher viewSwitcher, int index) {
        if (index < viewSwitcher.getChildCount()) {
            viewSwitcher.setDisplayedChild(index);
        }
    }

    @BindingAdapter("android:switchIndex")
    public static void SetSwitchIndex(ViewAnimator viewAnimator, int index) {
        if (index < viewAnimator.getChildCount()) {
            viewAnimator.setDisplayedChild(index);
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
