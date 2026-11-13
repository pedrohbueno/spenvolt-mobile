package com.spenvolt.mobile.ui.auth;

import android.app.Activity;
import android.graphics.Color;
import android.text.Html;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.spenvolt.mobile.R;

final class AuthScreen {

    private AuthScreen() {
    }

    static void setupWindow(ComponentActivity activity) {
        EdgeToEdge.enable(activity,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
        ViewCompat.setOnApplyWindowInsetsListener(activity.findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    static void setupTagline(Activity activity) {
        TextView tagline = activity.findViewById(R.id.txtTagline);
        tagline.setText(Html.fromHtml("Energia sob <b><font color='#FFD43B'>controle</font></b>, casa em <b><font color='#72F45B'>harmonia</font></b>.", Html.FROM_HTML_MODE_LEGACY));
    }

    static void setupLink(TextView link, String question, String action) {
        link.setText(Html.fromHtml(question + " <b><font color='#15803D'>" + action + "</font></b>", Html.FROM_HTML_MODE_LEGACY));
    }
}
