package com.spenvolt.mobile.ui.common;

import android.view.View;
import android.widget.AdapterView;

/**
 * Listener de Spinner que só avisa a posição escolhida.
 * Uso: spinner.setOnItemSelectedListener(new SimpleSelection(position -> { ... }));
 */
public class SimpleSelection implements AdapterView.OnItemSelectedListener {

    public interface OnSelected {
        void onSelected(int position);
    }

    private final OnSelected onSelected;

    public SimpleSelection(OnSelected onSelected) {
        this.onSelected = onSelected;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        onSelected.onSelected(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
