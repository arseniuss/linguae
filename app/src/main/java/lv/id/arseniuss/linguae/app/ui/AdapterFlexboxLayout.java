package lv.id.arseniuss.linguae.app.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;

import com.google.android.flexbox.FlexboxLayout;

import lv.id.arseniuss.linguae.app.R;

public class AdapterFlexboxLayout extends FlexboxLayout {
    private Adapter _adapter;
    private final DataSetObserver _dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            reloadChildViews();
        }
    };
    private int _itemLayoutResId;

    public AdapterFlexboxLayout(Context context) {
        super(context);
    }

    public AdapterFlexboxLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AdapterFlexboxLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AdapterLinearLayout);

        _itemLayoutResId = a.getResourceId(R.styleable.AdapterLinearLayout_itemLayout, 0);

        a.recycle();
    }

    public int getItemLayoutResId() {
        return _itemLayoutResId;
    }

    public Adapter getAdapter() {
        return _adapter;
    }

    public void setAdapter(Adapter adapter) {
        if (_adapter == adapter) return;
        _adapter = adapter;
        if (_adapter != null) _adapter.registerDataSetObserver(_dataSetObserver);
        reloadChildViews();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (_adapter != null) _adapter.unregisterDataSetObserver(_dataSetObserver);
    }

    private void reloadChildViews() {
        removeAllViews();

        if (_adapter == null) return;

        int count = _adapter.getCount();

        for (int position = 0; position < count; ++position) {
            View v = _adapter.getView(position, null, this);

            addView(v);
        }

        requestLayout();
    }
}
