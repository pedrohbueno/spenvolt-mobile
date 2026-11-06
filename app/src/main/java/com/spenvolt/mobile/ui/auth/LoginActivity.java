package com.spenvolt.mobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.AuthApi;
import com.spenvolt.mobile.session.SessionManager;
import com.spenvolt.mobile.ui.common.Views;
import com.spenvolt.mobile.ui.home.HomeActivity;

/** Tela de login. É a primeira tela do app; se já houver sessão válida, pula direto para o Início. */
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
        AuthScreen.setupLink(this, linkRegister, "Não tem conta?", "Criar conta");
        linkRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String email = Views.text(etEmail);
        String password = etPassword.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Views.message(txtError, "Informe um e-mail válido.");
            return;
        }
        if (password.isEmpty()) {
            Views.message(txtError, "Informe sua senha.");
            return;
        }

        Views.message(txtError, null);
        Views.loading(btnLogin, true, "Entrando...", "Entrar");

        new AuthApi(this).login(email, password, new ApiCallback<String>() {
            @Override
            public void onSuccess(String token) {
                session.saveLogin(token);
                openHome();
            }

            @Override
            public void onError(String message) {
                Views.loading(btnLogin, false, "Entrando...", "Entrar");
                Views.message(txtError, "E-mail ou senha incorretos, ou algo deu errado. Tente novamente.");
            }
        });
    }

    private void openHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
