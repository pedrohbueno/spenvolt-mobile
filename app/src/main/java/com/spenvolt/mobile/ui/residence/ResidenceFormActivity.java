package com.spenvolt.mobile.ui.residence;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.ResidenceApi;
import com.spenvolt.mobile.model.Residence;
import com.spenvolt.mobile.session.SessionManager;
import com.spenvolt.mobile.ui.MenuLateral;

public class ResidenceFormActivity extends AppCompatActivity {

    private SessionManager session;

    private ResidenceFields fields;
    private TextView txtError;
    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!MenuLateral.montar(this, R.layout.activity_residence_form, 0)) return;

        fields = new ResidenceFields(this);
        txtError = findViewById(R.id.txtError);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> save());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private void save() {
        Residence residence = new Residence();
        String error = fields.readInto(residence);
        if (error != null) {
            txtError.setText(error);
            txtError.setVisibility(View.VISIBLE);
            return;
        }

        txtError.setVisibility(View.GONE);
        btnSave.setEnabled(false);
        btnSave.setText("Criando...");

        new ResidenceApi(this).create(residence, new ApiCallback<Residence>() {
            @Override
            public void onSuccess(Residence created) {
                session.setSelectedResidenceId(created.id);
                Toast.makeText(ResidenceFormActivity.this, "Residência criada.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                btnSave.setEnabled(true);
                btnSave.setText("Criar residência");
                txtError.setText("Algo deu errado. Tente novamente.");
                txtError.setVisibility(View.VISIBLE);
            }
        });
    }
}
