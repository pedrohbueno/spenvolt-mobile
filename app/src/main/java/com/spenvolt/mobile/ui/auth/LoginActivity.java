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

public class LoginActivity extends AppCompatActivity {

    private SessionManager session;
    private EditText etEmail;
    private EditText etPassword;
    private TextView txtError;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);

        if (session.isLoggedIn()) {
            openHome();
            return;
        }

        setContentView(R.layout.activity_login);
        AuthScreen.setupWindow(this);
        AuthScreen.setupTagline(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        txtError = findViewById(R.id.txtError);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> login());

        TextView linkRegister = findViewById(R.id.linkRegister);
        AuthScreen.setupLink(linkRegister, "Não tem conta?", "Criar conta");
        linkRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtError.setText("Informe um e-mail válido.");
            txtError.setVisibility(View.VISIBLE);
            return;
        }
        if (password.isEmpty()) {
            txtError.setText("Informe sua senha.");
            txtError.setVisibility(View.VISIBLE);
            return;
        }

        txtError.setVisibility(View.GONE);
        btnLogin.setEnabled(false);
        btnLogin.setText("Entrando...");

        new AuthApi(this).login(email, password, new ApiCallback<String>() {
            @Override
            public void onSuccess(String token) {
                session.saveLogin(token);
                openHome();
            }

            @Override
            public void onError(String message) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");
                txtError.setText("E-mail ou senha incorretos, ou algo deu errado. Tente novamente.");
                txtError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void openHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
