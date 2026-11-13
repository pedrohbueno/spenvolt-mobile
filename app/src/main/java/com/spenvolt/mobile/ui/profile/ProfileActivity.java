package com.spenvolt.mobile.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.UserApi;
import com.spenvolt.mobile.ui.auth.LoginActivity;
import com.spenvolt.mobile.ui.auth.RegisterActivity;
import com.spenvolt.mobile.ui.common.BaseActivity;
import com.spenvolt.mobile.ui.common.Views;
import com.spenvolt.mobile.util.Format;

public class ProfileActivity extends BaseActivity {

    private EditText etCurrent;
    private EditText etNew;
    private EditText etConfirm;
    private TextView txtPasswordError;
    private MaterialButton btnSavePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreen(R.layout.activity_profile, NAV_NONE);

        String name = session.getName();
        ((TextView) findViewById(R.id.txtAvatar)).setText(Format.initial(name));
        ((TextView) findViewById(R.id.txtName)).setText(name.isEmpty() ? "Meu perfil" : name);
        ((TextView) findViewById(R.id.txtEmail)).setText(session.getEmail());

        etCurrent = findViewById(R.id.etCurrent);
        etNew = findViewById(R.id.etNew);
        etConfirm = findViewById(R.id.etConfirm);
        txtPasswordError = findViewById(R.id.txtPasswordError);
        btnSavePassword = findViewById(R.id.btnSavePassword);

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showPasswordForm(true));
        findViewById(R.id.btnCancelPassword).setOnClickListener(v -> showPasswordForm(false));
        btnSavePassword.setOnClickListener(v -> changePassword());
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    private void showPasswordForm(boolean visible) {
        Views.show(findViewById(R.id.passwordForm), visible);
        Views.show(findViewById(R.id.btnChangePassword), !visible);
        Views.show(findViewById(R.id.txtStatus), false);
        Views.message(txtPasswordError, null);
        etCurrent.setText("");
        etNew.setText("");
        etConfirm.setText("");
    }

    private void changePassword() {
        String current = etCurrent.getText().toString();
        String next = etNew.getText().toString();

        if (current.isEmpty()) {
            Views.message(txtPasswordError, "Informe a senha atual.");
            return;
        }
        if (next.length() < RegisterActivity.MIN_PASSWORD) {
            Views.message(txtPasswordError, "A nova senha deve ter ao menos " + RegisterActivity.MIN_PASSWORD + " caracteres.");
            return;
        }
        if (!next.equals(etConfirm.getText().toString())) {
            Views.message(txtPasswordError, "A confirmação não confere com a nova senha.");
            return;
        }

        Views.message(txtPasswordError, null);
        Views.loading(btnSavePassword, true, "Salvando...", "Salvar nova senha");

        new UserApi(this).changePassword(current, next, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Views.loading(btnSavePassword, false, "Salvando...", "Salvar nova senha");
                showPasswordForm(false);
                Views.show(findViewById(R.id.txtStatus), true);
            }

            @Override
            public void onError(String message) {
                Views.loading(btnSavePassword, false, "Salvando...", "Salvar nova senha");
                Views.message(txtPasswordError, "Não foi possível trocar a senha. Confira a senha atual.");
            }
        });
    }

    private void logout() {
        session.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
