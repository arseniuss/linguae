package lv.id.arseniuss.linguae.app.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class UIHelper {
    public static void ShowFullscreen(View view) {
        if (!(view instanceof ImageView)) return;

        Context context = view.getContext();
        ImageView sourceImageView = (ImageView) view;
        Drawable drawable = sourceImageView.getDrawable();

        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setCancelable(true);

        ImageView fullscreenImage = new ImageView(context);
        fullscreenImage.setImageDrawable(drawable);
        fullscreenImage.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        fullscreenImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        fullscreenImage.setOnClickListener(v -> dialog.dismiss());

        dialog.getWindow().setWindowAnimations(android.R.style.Animation_Activity);

        dialog.getWindow().setDimAmount(0.9f);

        dialog.setContentView(fullscreenImage);
        dialog.show();
    }
}
