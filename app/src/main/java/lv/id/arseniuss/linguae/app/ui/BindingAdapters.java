package lv.id.arseniuss.linguae.app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lv.id.arseniuss.linguae.app.R;
import lv.id.arseniuss.linguae.app.Utilities;

public class BindingAdapters {
    private static Markwon _markwonNoLinks = null;
    private static Markwon _markwon = null;


    @BindingAdapter("items")
    public static <T extends BaseObservable>
    void SetAdapterLinearLayoutObservableItems(AdapterLinearLayout container, List<T> entries) {
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

    @BindingAdapter("items")
    public static <T extends BaseObservable>
    void SetAdapterFlexboxLayoutObservableItems(AdapterFlexboxLayout container,
                                                List<T> entries) {
        if (entries == null) return;

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

    @BindingAdapter("items")
    public static void SetAdapterLinearLayoutItems(AdapterLinearLayout container,
                                                   List<String> entries) {
        if (entries == null) return;

        Adapter adapter = container.getAdapter();

        if (!(adapter instanceof ArrayAdapter)) {
            Context context = container.getContext();

            adapter = new ArrayAdapter<String>(context, R.layout.item_bubble_string);
            container.setAdapter(adapter);
        }

        ArrayAdapter<String> stringArrayAdapter = (ArrayAdapter<String>) container.getAdapter();

        stringArrayAdapter.clear();
        stringArrayAdapter.addAll(entries);
        stringArrayAdapter.notifyDataSetChanged();
    }

    @BindingAdapter("items")
    public static void SetAdapterFlexboxLayoutItems(AdapterFlexboxLayout container,
                                                    List<String> entries) {
        if (entries == null) return;

        Adapter adapter = container.getAdapter();

        if (!(adapter instanceof ArrayAdapter)) {
            Context context = container.getContext();

            adapter = new ArrayAdapter<String>(context, R.layout.item_bubble_string);
            container.setAdapter(adapter);
        }

        ArrayAdapter<String> stringArrayAdapter = (ArrayAdapter<String>) container.getAdapter();

        stringArrayAdapter.clear();
        stringArrayAdapter.addAll(entries);
        stringArrayAdapter.notifyDataSetChanged();
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
    public static void SetSpinnerListener(Spinner spinner,
                                          final InverseBindingListener selectedItemChangedListener) {
        if (selectedItemChangedListener != null) {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position,
                                           long id) {
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
            Disposable d = Utilities.LoadImage(imageView.getContext(), urlString)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(imageView::setImageBitmap, Throwable::printStackTrace);
        }
    }

    @BindingAdapter("image")
    public static void SetImageViewImage(ImageView imageView, Bitmap bitmap) {
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

    @BindingAdapter("markdown.nolinks")
    public static void SetMarkdownNoLinks(TextView textView, String text) {
        if (_markwonNoLinks == null) {
            _markwonNoLinks = Markwon.builder(textView.getContext())
                    .usePlugin(TablePlugin.create(textView.getContext()))
                    .build();
        }

        if (text != null && !text.isEmpty()) {
            _markwonNoLinks.setMarkdown(textView, text);
            textView.setMovementMethod(null);
        } else {
            textView.setText("");
        }
    }

    @BindingAdapter("markdown")
    public static void SetMarkdown(TextView textView, String text) {
        if (_markwon == null) {
            _markwon = Markwon.builder(textView.getContext())
                    .usePlugin(TablePlugin.create(textView.getContext()))
                    .usePlugin(WiktionaryLinkMarkwonPlugin.create(textView.getContext()))
                    .build();
        }

        if (text != null && !text.isEmpty()) {
            _markwon.setMarkdown(textView, text);
        } else {
            textView.setText("");
        }
    }

    @BindingAdapter("divider")
    public static void SetRecycleViewSpacing(RecyclerView recyclerView, Drawable divider) {
        if (recyclerView == null) return;

        DividerItemDecoration decoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);

        decoration.setDrawable(divider);

        recyclerView.addItemDecoration(decoration);
    }
}
