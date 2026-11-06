package com.spenvolt.mobile.ui.residence;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.ResidenceApi;
import com.spenvolt.mobile.model.Residence;
import com.spenvolt.mobile.ui.common.BaseActivity;
import com.spenvolt.mobile.ui.common.Views;

/** Criar residência (residences/new/page.tsx do site). */
public class ResidenceFormActivity extends BaseActivity {

    private ResidenceFields fields;
    private TextView txtError;
    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreen(R.layout.activity_residence_form, NAV_NONE);

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
            Views.message(txtError, error);
            return;
        }

        Views.message(txtError, null);
        Views.loading(btnSave, true, "Criando...", "Criar residência");

        new ResidenceApi(this).create(residence, new ApiCallback<Residence>() {
            @Override
            public void onSuccess(Residence created) {
                session.setSelectedResidenceId(created.id); // a nova casa vira a selecionada
                toast("Residência criada.");
                finish(); // o Início recarrega a lista ao voltar
            }

            @Override
            public void onError(String message) {
                Views.loading(btnSave, false, "Criando...", "Criar residência");
                Views.message(txtError, "Algo deu errado. Tente novamente.");
            }
        });
    }
}
