package com.spenvolt.mobile.ui.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.util.Consumption;

public final class Views {

    private Views() {
    }

    public static String text(EditText field) {
        return field.getText().toString().trim();
    }

    public static String textOrNull(EditText field) {
        String value = text(field);
        return value.isEmpty() ? null : value;
    }

    public static void show(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public static void empty(View emptyView, boolean visible, String title, String text) {
        show(emptyView, visible);
        ((TextView) emptyView.findViewById(R.id.txtEmptyTitle)).setText(title);
        ((TextView) emptyView.findViewById(R.id.txtEmptyText)).setText(text);
    }

    public static void message(TextView view, String message) {
        view.setText(message);
        show(view, message != null);
    }

    public static void loading(MaterialButton button, boolean loading, String loadingText, String normalText) {
        button.setEnabled(!loading);
        button.setAlpha(loading ? 0.6f : 1f);
        button.setText(loading ? loadingText : normalText);
    }

    public static void statusBadge(TextView badge, Consumption.Status status) {
        Context context = badge.getContext();
        badge.setText(status.label);
        badge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, status.colorRes)));
        badge.setTextColor(ContextCompat.getColor(context, status.textColorRes));
    }

    public static TextView addChip(ViewGroup container, String label) {
        TextView chip = (TextView) LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_chip, container, false);
        chip.setText(label);
        container.addView(chip);
        return chip;
    }

    public static SpannableStringBuilder append(Context context, SpannableStringBuilder text,
                                                String part, int colorRes, boolean bold) {
        int start = text.length();
        text.append(part);
        if (colorRes != 0) {
            int color = ContextCompat.getColor(context, colorRes);
            text.setSpan(new ForegroundColorSpan(color), start, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (bold) {
            text.setSpan(new StyleSpan(Typeface.BOLD), start, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    public static void confirm(Context context, String title, String message, String confirmLabel, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(confirmLabel, (dialog, which) -> onConfirm.run())
                .show();
    }
}
