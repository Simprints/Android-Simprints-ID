package com.simprints.id.tools;


import android.view.View;
import android.widget.Button;

import java.util.List;

public class ViewHelper {

    public static void registerOnClickButtonListeners(Button button, final List<View.OnClickListener> onClickListeners) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (View.OnClickListener onClickListener : onClickListeners) {
                    onClickListener.onClick(v);
                }
            }
        });
    }
}
