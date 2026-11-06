package com.spenvolt.mobile.ui.common;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.ResidenceApi;
import com.spenvolt.mobile.model.Residence;
import com.spenvolt.mobile.session.SessionManager;
import com.spenvolt.mobile.ui.auth.LoginActivity;
import com.spenvolt.mobile.ui.device.DevicesActivity;
import com.spenvolt.mobile.ui.economize.EconomizeActivity;
import com.spenvolt.mobile.ui.home.HomeActivity;
import com.spenvolt.mobile.ui.member.MembersActivity;
import com.spenvolt.mobile.ui.profile.ProfileActivity;
import com.spenvolt.mobile.ui.residence.ResidenceSettingsActivity;
import com.spenvolt.mobile.util.Format;

import java.util.ArrayList;
import java.util.List;

/**
 * Base de todas as telas internas do app (as que ficam depois do login).
 *
 * Ela monta a barra do topo e o menu lateral e carrega as residências do usuário.
 * A tela filha só precisa:
 *   1. chamar setScreen(R.layout.activity_x, NAV_X) no onCreate;
 *   2. (opcional) sobrescrever onResidencesLoaded / onResidenceChanged para carregar seus dados.
 */
public abstract class BaseActivity extends AppCompatActivity {

    // Qual item do menu fica destacado
    protected static final int NAV_NONE = 0;
    protected static final int NAV_HOME = 1;
    protected static final int NAV_DEVICES = 2;
    protected static final int NAV_MEMBERS = 3;
    protected static final int NAV_ECONOMIZE = 4;
    protected static final int NAV_SETTINGS = 5;

    protected SessionManager session;

    /** Residências do usuário (atualizadas sempre que a tela volta a aparecer). */
    protected final List<Residence> residences = new ArrayList<>();

    private DrawerLayout drawerLayout;
    private Spinner spinnerResidence;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);

        // Sem sessão válida não dá para ficar em uma tela interna.
        if (!session.isLoggedIn()) {
            goToLogin();
        }
    }

    /** Monta a tela: barra do topo + layout da tela + menu lateral. */
    protected void setScreen(@LayoutRes int layoutId, int navItem) {
        // Barra de status escura (ícones claros) e barra de navegação clara.
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));

        setContentView(R.layout.activity_base);
        FrameLayout content = findViewById(R.id.screenContent);
        getLayoutInflater().inflate(layoutId, content, true);

        drawerLayout = findViewById(R.id.drawerLayout);
        applyInsets(content);
        setupToolbar();
        setupDrawer(navItem);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (drawerLayout != null && session.isLoggedIn()) {
            loadResidences();
        }
    }

    // ======================= Para as telas filhas =======================

    /** Chamado quando as residências chegam da API (toda vez que a tela volta a aparecer). */
    protected void onResidencesLoaded(@Nullable Residence selected) {
    }

    /** Chamado quando o usuário troca a residência no menu lateral. */
    protected void onResidenceChanged(Residence selected) {
    }

    /** Residência escolhida no menu (ou null se o usuário ainda não tem nenhuma). */
    @Nullable
    protected Residence selectedResidence() {
        int id = session.getSelectedResidenceId();
        for (Residence r : residences) {
            if (r.id == id) return r;
        }
        return residences.isEmpty() ? null : residences.get(0);
    }

    /** Mensagem rápida na parte de baixo da tela. */
    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // ======================= Barra do topo =======================

    private void setupToolbar() {
        findViewById(R.id.btnMenu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        findViewById(R.id.toolbarBrand).setOnClickListener(v -> openTopLevel(HomeActivity.class));

        TextView btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setText(Format.initial(session.getName()));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    /** Afasta o conteúdo da barra de status e da barra de navegação do Android. */
    private void applyInsets(View content) {
        View toolbar = findViewById(R.id.appToolbar);
        View drawerPanel = findViewById(R.id.drawerPanel);
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(bars.left, bars.top, bars.right, 0);
            content.setPadding(bars.left, 0, bars.right, bars.bottom);
            drawerPanel.setPadding(0, bars.top, 0, bars.bottom);
            return insets;
        });
    }

    // ======================= Menu lateral =======================

    private void setupDrawer(int navItem) {
        highlight(R.id.navHome, navItem == NAV_HOME);
        highlight(R.id.navDevices, navItem == NAV_DEVICES);
        highlight(R.id.navMembers, navItem == NAV_MEMBERS);
        highlight(R.id.navEconomize, navItem == NAV_ECONOMIZE);
        highlight(R.id.navSettings, navItem == NAV_SETTINGS);

        findViewById(R.id.navHome).setOnClickListener(v -> openTopLevel(HomeActivity.class));
        findViewById(R.id.navDevices).setOnClickListener(v -> openTopLevel(DevicesActivity.class));
        findViewById(R.id.navMembers).setOnClickListener(v -> openTopLevel(MembersActivity.class));
        findViewById(R.id.navEconomize).setOnClickListener(v -> openTopLevel(EconomizeActivity.class));
        findViewById(R.id.navSettings).setOnClickListener(v -> openSettings());

        TextView avatar = findViewById(R.id.drawerAvatar);
        avatar.setText(Format.initial(session.getName()));
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            startActivity(new Intent(this, ProfileActivity.class));
        });
        findViewById(R.id.navLogout).setOnClickListener(v -> logout());

        spinnerResidence = findViewById(R.id.spinnerResidence);
    }

    /** Item ativo: texto amarelo em negrito e faixa amarela à esquerda. */
    private void highlight(int viewId, boolean active) {
        if (!active) return;
        TextView item = findViewById(viewId);
        item.setTextColor(ContextCompat.getColor(this, R.color.yellow));
        item.setTypeface(item.getTypeface(), android.graphics.Typeface.BOLD);
        item.setBackgroundResource(R.drawable.bg_drawer_item_active);
    }

    /**
     * Abre uma tela principal do menu.
     * O Início fica sempre na base da pilha: voltar de qualquer tela principal leva a ele.
     */
    private void openTopLevel(Class<?> screen) {
        drawerLayout.closeDrawers();
        if (screen.isInstance(this)) return; // já está nela

        Intent intent = new Intent(this, screen);
        if (screen == HomeActivity.class) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        startActivity(intent);
        if (!(this instanceof HomeActivity)) {
            finish();
        }
    }

    private void openSettings() {
        Residence residence = selectedResidence();
        if (residence == null) {
            toast("Cadastre uma residência primeiro.");
            return;
        }
        drawerLayout.closeDrawers();
        Intent intent = new Intent(this, ResidenceSettingsActivity.class);
        intent.putExtra(ResidenceSettingsActivity.EXTRA_RESIDENCE_ID, residence.id);
        startActivity(intent);
    }

    private void logout() {
        session.clear();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ======================= Residências =======================

    private void loadResidences() {
        new ResidenceApi(this).findAll(new ApiCallback<List<Residence>>() {
            @Override
            public void onSuccess(List<Residence> result) {
                residences.clear();
                residences.addAll(result);

                Residence selected = selectedResidence();
                session.setSelectedResidenceId(selected == null ? -1 : selected.id);
                fillResidenceSpinner(selected);
                onResidencesLoaded(selected);
            }

            @Override
            public void onError(String message) {
                toast(message);
                onResidencesLoaded(null);
            }
        });
    }

    private void fillResidenceSpinner(@Nullable Residence selected) {
        ArrayAdapter<Residence> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_drawer, residences);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerResidence.setOnItemSelectedListener(null);
        spinnerResidence.setAdapter(adapter);
        if (selected != null) {
            spinnerResidence.setSelection(residences.indexOf(selected), false);
        }

        spinnerResidence.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Residence chosen = residences.get(position);
                if (chosen.id == session.getSelectedResidenceId()) return; // nada mudou
                session.setSelectedResidenceId(chosen.id);
                drawerLayout.closeDrawers();
                onResidenceChanged(chosen);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
