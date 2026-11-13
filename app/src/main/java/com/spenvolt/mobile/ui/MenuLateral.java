package com.spenvolt.mobile.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
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
import com.spenvolt.mobile.ui.home.HomeActivity;
import com.spenvolt.mobile.ui.member.MembersActivity;
import com.spenvolt.mobile.ui.profile.ProfileActivity;
import com.spenvolt.mobile.ui.residence.ResidenceSettingsActivity;

import java.util.List;

public class MenuLateral {

    public static boolean montar(AppCompatActivity activity, int layout, int itemAtivo) {
        SessionManager session = new SessionManager(activity);
        if (!session.isLoggedIn()) {
            sair(activity, session);
            return false;
        }

        EdgeToEdge.enable(activity,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));

        activity.setContentView(R.layout.activity_base);
        FrameLayout conteudo = activity.findViewById(R.id.screenContent);
        activity.getLayoutInflater().inflate(layout, conteudo, true);

        DrawerLayout drawer = activity.findViewById(R.id.drawerLayout);
        View toolbar = activity.findViewById(R.id.appToolbar);
        View painel = activity.findViewById(R.id.drawerPanel);
        ViewCompat.setOnApplyWindowInsetsListener(drawer, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(bars.left, bars.top, bars.right, 0);
            conteudo.setPadding(bars.left, 0, bars.right, bars.bottom);
            painel.setPadding(0, bars.top, 0, bars.bottom);
            return insets;
        });

        String inicial = session.getName().isEmpty() ? "?" : session.getName().substring(0, 1).toUpperCase();
        ((TextView) activity.findViewById(R.id.btnProfile)).setText(inicial);
        ((TextView) activity.findViewById(R.id.drawerAvatar)).setText(inicial);

        if (itemAtivo != 0) {
            TextView item = activity.findViewById(itemAtivo);
            item.setTextColor(activity.getColor(R.color.yellow));
            item.setTypeface(item.getTypeface(), Typeface.BOLD);
            item.setBackgroundResource(R.drawable.bg_drawer_item_active);
        }

        activity.findViewById(R.id.btnMenu).setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        activity.findViewById(R.id.toolbarBrand).setOnClickListener(v -> abrir(activity, HomeActivity.class));
        activity.findViewById(R.id.navHome).setOnClickListener(v -> abrir(activity, HomeActivity.class));
        activity.findViewById(R.id.navDevices).setOnClickListener(v -> abrir(activity, DevicesActivity.class));
        activity.findViewById(R.id.navMembers).setOnClickListener(v -> abrir(activity, MembersActivity.class));
        activity.findViewById(R.id.btnProfile).setOnClickListener(v -> activity.startActivity(new Intent(activity, ProfileActivity.class)));
        activity.findViewById(R.id.navProfile).setOnClickListener(v -> activity.startActivity(new Intent(activity, ProfileActivity.class)));
        activity.findViewById(R.id.navLogout).setOnClickListener(v -> sair(activity, session));
        activity.findViewById(R.id.navSettings).setOnClickListener(v -> {
            if (session.getSelectedResidenceId() == -1) {
                Toast.makeText(activity, "Cadastre uma residência primeiro.", Toast.LENGTH_SHORT).show();
                return;
            }
            activity.startActivity(new Intent(activity, ResidenceSettingsActivity.class));
        });

        carregarResidencias(activity, session);
        return true;
    }

    private static void abrir(AppCompatActivity activity, Class<?> tela) {
        if (tela.isInstance(activity)) {
            ((DrawerLayout) activity.findViewById(R.id.drawerLayout)).closeDrawers();
            return;
        }
        Intent intent = new Intent(activity, tela);
        if (tela == HomeActivity.class) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        activity.startActivity(intent);
        if (!(activity instanceof HomeActivity)) {
            activity.finish();
        }
    }

    private static void sair(AppCompatActivity activity, SessionManager session) {
        session.clear();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private static void carregarResidencias(AppCompatActivity activity, SessionManager session) {
        new ResidenceApi(activity).findAll(new ApiCallback<List<Residence>>() {
            @Override
            public void onSuccess(List<Residence> residencias) {
                int posicao = -1;
                for (int i = 0; i < residencias.size(); i++) {
                    if (residencias.get(i).id == session.getSelectedResidenceId()) {
                        posicao = i;
                    }
                }

                if (posicao == -1 && !residencias.isEmpty()) {
                    session.setSelectedResidenceId(residencias.get(0).id);
                    activity.recreate();
                    return;
                }

                Spinner spinner = activity.findViewById(R.id.spinnerResidence);
                ArrayAdapter<Residence> adapter = new ArrayAdapter<>(activity, R.layout.item_spinner_drawer, residencias);
                adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
                spinner.setAdapter(adapter);
                if (posicao != -1) {
                    spinner.setSelection(posicao, false);
                }

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        int escolhida = residencias.get(position).id;
                        if (escolhida != session.getSelectedResidenceId()) {
                            session.setSelectedResidenceId(escolhida);
                            activity.recreate();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onError(String message) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
