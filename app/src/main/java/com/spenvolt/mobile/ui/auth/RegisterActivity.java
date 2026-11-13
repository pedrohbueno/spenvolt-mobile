package com.spenvolt.mobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.AuthApi;
import com.spenvolt.mobile.session.SessionManager;
import com.spenvolt.mobile.ui.home.HomeActivity;

public class RegisterActivity extends AppCompatActivity {

    public static final int MIN_PASSWORD = 6;

    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private TextView txtError;
    private MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        AuthScreen.setupWindow(this);
        AuthScreen.setupTagline(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        txtError = findViewById(R.id.txtError);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> register());

        TextView linkLogin = findViewById(R.id.linkLogin);
        AuthScreen.setupLink(linkLogin, "Já tem conta?", "Entrar");
        linkLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (name.isEmpty()) {
            etName.setError("Informe seu nome");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Informe um e-mail válido");
            return;
        }
        if (password.length() < MIN_PASSWORD) {
            etPassword.setError("A senha precisa ter ao menos " + MIN_PASSWORD + " caracteres");
            return;
        }

        txtError.setVisibility(View.GONE);
        btnRegister.setEnabled(false);
        btnRegister.setText("Cadastrando...");

        AuthApi auth = new AuthApi(this);
        auth.register(name, email, password, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loginAfterRegister(auth, email, password);
            }

            @Override
            public void onError(String message) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Cadastrar");
                txtError.setText("Algo deu errado. Esse e-mail já pode estar cadastrado.");
                txtError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loginAfterRegister(AuthApi auth, String email, String password) {
        auth.login(email, password, new ApiCallback<String>() {
            @Override
            public void onSuccess(String token) {
                new SessionManager(RegisterActivity.this).saveLogin(token);
                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                finish();
            }
        });
    }
}
