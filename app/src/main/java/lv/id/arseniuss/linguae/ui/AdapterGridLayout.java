package lv.id.arseniuss.linguae.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.GridLayout;

public class AdapterGridLayout extends GridLayout {
    private Adapter _adapter;
    private final DataSetObserver _dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            reloadChildViews();
        }
    };

    public AdapterGridLayout(Context context) {
        super(context);
    }

    public AdapterGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdapterGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AdapterGridLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Adapter getAdapter() { return _adapter; }

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

            if (v != null) {
                final int columnCount = getColumnCount();

                if (columnCount != 0) {
                    GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(v.getLayoutParams());

                    layoutParams.columnSpec = GridLayout.spec(position % columnCount, GridLayout.FILL, 1f);
                    layoutParams.rowSpec = GridLayout.spec(position / columnCount, GridLayout.FILL, 1f);
                    layoutParams.setMargins(10, 10, 10, 10);

                    v.setLayoutParams(layoutParams);
                }

                addView(v);
            }
        }

        requestLayout();
    }
}
