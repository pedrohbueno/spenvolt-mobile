package com.spenvolt.mobile.ui.auth;

import android.app.Activity;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.spenvolt.mobile.R;
import com.spenvolt.mobile.ui.common.Views;

/** Partes em comum entre as telas de login e de cadastro. */
final class AuthScreen {

    private AuthScreen() {
    }

    /** Fundo azul-escuro de ponta a ponta, respeitando as barras do Android. */
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

    /** "Energia sob controle, casa em harmonia." com as palavras coloridas. */
    static void setupTagline(Activity activity) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        Views.append(activity, text, "Energia sob ", 0, false);
        Views.append(activity, text, "controle", R.color.yellow, true);
        Views.append(activity, text, ", casa em ", 0, false);
        Views.append(activity, text, "harmonia", R.color.green_button, true);
        Views.append(activity, text, ".", 0, false);
        TextView tagline = activity.findViewById(R.id.txtTagline);
        tagline.setText(text);
    }

    /** "Não tem conta? Criar conta" com a parte final em verde. */
    static void setupLink(Activity activity, TextView link, String question, String action) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        Views.append(activity, text, question + " ", 0, false);
        Views.append(activity, text, action, R.color.green_text, true);
        link.setText(text);
    }
}
