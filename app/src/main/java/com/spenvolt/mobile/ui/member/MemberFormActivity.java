package com.spenvolt.mobile.ui.member;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.MemberApi;
import com.spenvolt.mobile.model.Member;
import com.spenvolt.mobile.model.Residence;
import com.spenvolt.mobile.ui.common.BaseActivity;
import com.spenvolt.mobile.ui.common.Views;

/** Adicionar membro (members/new/page.tsx do site). */
public class MemberFormActivity extends BaseActivity {

    public static final String EXTRA_RESIDENCE_ID = "residence_id";

    private EditText etName;
    private EditText etEmail;
    private EditText etPhotoUrl;
    private Spinner spinnerResidence;
    private TextView txtError;
    private MaterialButton btnSave;
    private boolean spinnerReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreen(R.layout.activity_member_form, NAV_NONE);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhotoUrl = findViewById(R.id.etPhotoUrl);
        spinnerResidence = findViewById(R.id.spinnerResidence);
        txtError = findViewById(R.id.txtError);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> save());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    /** Monta o seletor de residência uma única vez. */
    @Override
    protected void onResidencesLoaded(@Nullable Residence selected) {
        if (spinnerReady) return;
        if (residences.isEmpty()) {
            Views.message(txtError, "Cadastre uma residência antes de adicionar membros.");
            btnSave.setEnabled(false);
            return;
        }

        int wantedId = getIntent().getIntExtra(EXTRA_RESIDENCE_ID, selected == null ? -1 : selected.id);
        int position = 0;
        for (int i = 0; i < residences.size(); i++) {
            if (residences.get(i).id == wantedId) position = i;
        }

        ArrayAdapter<Residence> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, residences);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerResidence.setAdapter(adapter);
        spinnerResidence.setSelection(position, false);
        spinnerReady = true;
    }

    private void save() {
        String name = Views.text(etName);
        String email = Views.text(etEmail);
        Residence residence = (Residence) spinnerResidence.getSelectedItem();

        if (name.isEmpty()) {
            etName.setError("Informe o nome");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Informe um e-mail válido");
            return;
        }
        if (residence == null) {
            Views.message(txtError, "Escolha a residência.");
            return;
        }

        Member member = new Member();
        member.name = name;
        member.email = email;
        member.photoUrl = Views.textOrNull(etPhotoUrl);
        member.residenceId = residence.id;

        Views.message(txtError, null);
        Views.loading(btnSave, true, "Salvando...", "Salvar membro");

        new MemberApi(this).create(member, new ApiCallback<Member>() {
            @Override
            public void onSuccess(Member created) {
                toast("Membro adicionado.");
                finish();
            }

            @Override
            public void onError(String message) {
                Views.loading(btnSave, false, "Salvando...", "Salvar membro");
                Views.message(txtError, "Algo deu errado. Tente novamente.");
            }
        });
    }
}
