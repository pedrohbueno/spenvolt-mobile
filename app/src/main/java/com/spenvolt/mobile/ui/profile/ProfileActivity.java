package com.spenvolt.mobile.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.UserApi;
import com.spenvolt.mobile.session.SessionManager;
import com.spenvolt.mobile.ui.MenuLateral;
import com.spenvolt.mobile.ui.auth.LoginActivity;
import com.spenvolt.mobile.ui.auth.RegisterActivity;
import com.spenvolt.mobile.util.Format;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager session;

    private EditText etCurrent;
    private EditText etNew;
    private EditText etConfirm;
    private TextView txtPasswordError;
    private MaterialButton btnSavePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!MenuLateral.montar(this, R.layout.activity_profile, 0)) return;

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
        findViewById(R.id.passwordForm).setVisibility(visible ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnChangePassword).setVisibility(!visible ? View.VISIBLE : View.GONE);
        findViewById(R.id.txtStatus).setVisibility(View.GONE);
        txtPasswordError.setVisibility(View.GONE);
        etCurrent.setText("");
        etNew.setText("");
        etConfirm.setText("");
    }

    private void changePassword() {
        String current = etCurrent.getText().toString();
        String next = etNew.getText().toString();

        if (current.isEmpty()) {
            txtPasswordError.setText("Informe a senha atual.");
            txtPasswordError.setVisibility(View.VISIBLE);
            return;
        }
        if (next.length() < RegisterActivity.MIN_PASSWORD) {
            txtPasswordError.setText("A nova senha deve ter ao menos " + RegisterActivity.MIN_PASSWORD + " caracteres.");
            txtPasswordError.setVisibility(View.VISIBLE);
            return;
        }
        if (!next.equals(etConfirm.getText().toString())) {
            txtPasswordError.setText("A confirmação não confere com a nova senha.");
            txtPasswordError.setVisibility(View.VISIBLE);
            return;
        }

        txtPasswordError.setVisibility(View.GONE);
        btnSavePassword.setEnabled(false);
        btnSavePassword.setText("Salvando...");

        new UserApi(this).changePassword(current, next, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                btnSavePassword.setEnabled(true);
                btnSavePassword.setText("Salvar nova senha");
                showPasswordForm(false);
                findViewById(R.id.txtStatus).setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                btnSavePassword.setEnabled(true);
                btnSavePassword.setText("Salvar nova senha");
                txtPasswordError.setText("Não foi possível trocar a senha. Confira a senha atual.");
                txtPasswordError.setVisibility(View.VISIBLE);
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
