package com.spenvolt.mobile.ui.residence;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.model.Room;
import com.spenvolt.mobile.model.RoomKind;
import com.spenvolt.mobile.util.Format;

import java.util.List;

/** Diálogo "Adicionar seção" (porta de AddRoomDialog.tsx do site). */
final class AddRoomDialog {

    /** Recebe a escolha do usuário quando ele confirma. */
    interface Listener {
        void onConfirm(RoomKind kind, int slot, String name);
    }

    private final List<Room> rooms;          // seções do andar atual
    private final RoomKind clickedKind;      // vaga tocada na planta (pode ser null)
    private final int clickedSlot;

    private RoomKind kind;
    private RadioGroup radioKinds;
    private TextView txtSlots;
    private TextView txtNameLabel;
    private EditText etName;
    private TextView txtPreview;
    private AlertDialog dialog;

    private AddRoomDialog(List<Room> rooms, @Nullable RoomKind clickedKind, int clickedSlot) {
        this.rooms = rooms;
        this.clickedKind = clickedKind;
        this.clickedSlot = clickedSlot;
    }

    /** Abre o diálogo. clickedKind/clickedSlot vêm da vaga livre tocada (ou null/-1). */
    static void show(Context context, List<Room> rooms, @Nullable RoomKind clickedKind, int clickedSlot, Listener listener) {
        new AddRoomDialog(rooms, clickedKind, clickedSlot).open(context, listener);
    }

    private void open(Context context, Listener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_room, null);
        radioKinds = view.findViewById(R.id.radioKinds);
        txtSlots = view.findViewById(R.id.txtSlots);
        txtNameLabel = view.findViewById(R.id.txtNameLabel);
        etName = view.findViewById(R.id.etName);
        txtPreview = view.findViewById(R.id.txtPreview);

        // Tipo inicial: o da vaga tocada, ou o primeiro que ainda tem vaga.
        if (clickedKind != null && FloorSlots.free(clickedKind, rooms) > 0) {
            kind = clickedKind;
        } else {
            for (RoomKind k : RoomKind.values()) {
                if (FloorSlots.free(k, rooms) > 0) {
                    kind = k;
                    break;
                }
            }
        }

        // Um RadioButton por tipo; tipos sem vaga ficam desabilitados.
        for (RoomKind k : RoomKind.values()) {
            int free = FloorSlots.free(k, rooms);
            RadioButton radio = new RadioButton(context);
            radio.setId(View.generateViewId());
            radio.setText(k.label + "  ·  " + (free == 0 ? "sem vagas" : free + (free == 1 ? " livre" : " livres")));
            radio.setEnabled(free > 0);
            radio.setChecked(k == kind);
            radio.setOnClickListener(v -> {
                kind = k;
                refresh();
            });
            radioKinds.addView(radio);
        }

        etName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) { refresh(); }
        });

        dialog = new MaterialAlertDialogBuilder(context)
                .setTitle("Adicionar seção")
                .setView(view)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Adicionar", null) // o clique é tratado abaixo para validar antes de fechar
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int slot = slot();
            if (kind == null || slot == -1 || !canConfirm()) return;
            listener.onConfirm(kind, slot, finalName(slot));
            dialog.dismiss();
        });
        refresh();
    }

    /** Número que a nova seção vai ocupar. */
    private int slot() {
        if (kind == null) return -1;
        if (kind == clickedKind && FloorSlots.isFree(kind, clickedSlot, rooms)) return clickedSlot;
        return FloorSlots.nextFree(kind, rooms);
    }

    /** "Outros" precisa de nome (ex.: Escritório). */
    private boolean canConfirm() {
        return kind != RoomKind.OUTROS || !etName.getText().toString().trim().isEmpty();
    }

    private String finalName(int slot) {
        String typed = etName.getText().toString().trim();
        return typed.isEmpty() ? kind.defaultName(slot) : typed;
    }

    /** Atualiza os textos de vagas e a prévia sempre que algo muda. */
    private void refresh() {
        if (kind == null) {
            txtSlots.setText("Todos os slots deste andar estão ocupados.");
            etName.setEnabled(false);
        } else {
            int free = FloorSlots.free(kind, rooms);
            txtSlots.setText("Slots livres de " + kind.plural.toLowerCase() + ": " + free + " de " + kind.maxPerFloor
                    + ". Depois de adicionar, restarão " + Math.max(free - 1, 0) + ".");

            boolean needsName = kind == RoomKind.OUTROS;
            txtNameLabel.setText(needsName ? "Nome *" : "Nome (opcional)");
            int slot = slot();
            etName.setHint(needsName ? "Ex.: Escritório, Lavanderia" : (slot > 0 ? kind.defaultName(slot) : ""));
            txtPreview.setText(slot > 0 ? "Será adicionado: " + finalName(slot) + " · " + Format.area(kind.area) : "");
        }

        if (dialog != null) {
            Button confirm = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (confirm != null) confirm.setEnabled(kind != null && canConfirm());
        }
    }
}
