package com.spenvolt.mobile.ui.member;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.MemberApi;
import com.spenvolt.mobile.api.ResidenceApi;
import com.spenvolt.mobile.model.Member;
import com.spenvolt.mobile.model.Residence;
import com.spenvolt.mobile.session.SessionManager;
import com.spenvolt.mobile.ui.MenuLateral;

import java.util.ArrayList;
import java.util.List;

public class MemberFormActivity extends AppCompatActivity {

    private final List<Residence> residences = new ArrayList<>();
    private SessionManager session;

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
        session = new SessionManager(this);
        if (!MenuLateral.montar(this, R.layout.activity_member_form, 0)) return;

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhotoUrl = findViewById(R.id.etPhotoUrl);
        spinnerResidence = findViewById(R.id.spinnerResidence);
        txtError = findViewById(R.id.txtError);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> save());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    
        new ResidenceApi(this).findAll(new ApiCallback<List<Residence>>() {
            @Override
            public void onSuccess(List<Residence> result) {
                residences.addAll(result);
                fillResidences();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MemberFormActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillResidences() {
        if (spinnerReady) return;
        if (residences.isEmpty()) {
            txtError.setText("Cadastre uma residência antes de adicionar membros.");
            txtError.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
            return;
        }

        int wantedId = getIntent().getIntExtra(EXTRA_RESIDENCE_ID, session.getSelectedResidenceId());
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
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
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
            txtError.setText("Escolha a residência.");
            txtError.setVisibility(View.VISIBLE);
            return;
        }

        Member member = new Member();
        member.name = name;
        member.email = email;
        member.photoUrl = (etPhotoUrl.getText().toString().trim().isEmpty() ? null : etPhotoUrl.getText().toString().trim());
        member.residenceId = residence.id;

        txtError.setVisibility(View.GONE);
        btnSave.setEnabled(false);
        btnSave.setText("Salvando...");

        new MemberApi(this).create(member, new ApiCallback<Member>() {
            @Override
            public void onSuccess(Member created) {
                Toast.makeText(MemberFormActivity.this, "Membro adicionado.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                btnSave.setEnabled(true);
                btnSave.setText("Salvar membro");
                txtError.setText("Algo deu errado. Tente novamente.");
                txtError.setVisibility(View.VISIBLE);
            }
        });
    }
}
