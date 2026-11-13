package com.spenvolt.mobile.ui.residence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.MemberApi;
import com.spenvolt.mobile.api.ResidenceApi;
import com.spenvolt.mobile.model.Member;
import com.spenvolt.mobile.model.Residence;
import com.spenvolt.mobile.session.SessionManager;
import com.spenvolt.mobile.ui.MenuLateral;
import com.spenvolt.mobile.ui.home.HomeActivity;
import com.spenvolt.mobile.util.Format;

import java.util.List;

public class ResidenceSettingsActivity extends AppCompatActivity {

    private SessionManager session;

    private int residenceId;
    private Residence residence;

    private ResidenceFields fields;
    private TextView txtStatus;
    private MaterialButton btnSave;
    private LinearLayout membersContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!MenuLateral.montar(this, R.layout.activity_residence_settings, R.id.navSettings)) return;

        residenceId = session.getSelectedResidenceId();
        fields = new ResidenceFields(this);
        txtStatus = findViewById(R.id.txtStatus);
        btnSave = findViewById(R.id.btnSave);
        membersContainer = findViewById(R.id.membersContainer);

        btnSave.setOnClickListener(v -> save());
        findViewById(R.id.btnDelete).setOnClickListener(v -> confirmDelete());

        loadResidence();
        loadMembers();
    }

    private void loadResidence() {
        new ResidenceApi(this).findById(residenceId, new ApiCallback<Residence>() {
            @Override
            public void onSuccess(Residence result) {
                residence = result;
                ((TextView) findViewById(R.id.txtResidenceName)).setText(result.name);
                fields.fill(result);
            }

            @Override
            public void onError(String message) {
                showStatus("Residência não encontrada ou API indisponível.", false);
            }
        });
    }

    private void save() {
        if (residence == null) return;
        String error = fields.readInto(residence);
        if (error != null) {
            showStatus(error, false);
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Salvando...");
        new ResidenceApi(this).update(residence, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                btnSave.setEnabled(true);
                btnSave.setText("Salvar alterações");
                ((TextView) findViewById(R.id.txtResidenceName)).setText(residence.name);
                showStatus("Alterações salvas.", true);
            }

            @Override
            public void onError(String message) {
                btnSave.setEnabled(true);
                btnSave.setText("Salvar alterações");
                showStatus("Não foi possível salvar. Tente novamente.", false);
            }
        });
    }

    private void showStatus(String message, boolean success) {
        txtStatus.setTextColor(ContextCompat.getColor(this, success ? R.color.green_text : R.color.red_600));
        txtStatus.setText(message);
        txtStatus.setVisibility(message == null ? View.GONE : View.VISIBLE);
    }

    private void loadMembers() {
        new MemberApi(this).findByResidence(residenceId, new ApiCallback<List<Member>>() {
            @Override
            public void onSuccess(List<Member> members) {
                renderMembers(members);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ResidenceSettingsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderMembers(List<Member> members) {
        membersContainer.removeAllViews();
        findViewById(R.id.txtMembersEmpty).setVisibility(members.isEmpty() ? View.VISIBLE : View.GONE);

        for (Member m : members) {
            View row = getLayoutInflater().inflate(R.layout.item_settings_member, membersContainer, false);
            ((TextView) row.findViewById(R.id.txtAvatar)).setText(Format.initial(m.name));
            ((TextView) row.findViewById(R.id.txtName)).setText(m.name);
            row.findViewById(R.id.btnRemove).setOnClickListener(v -> confirmRemoveMember(m));
            membersContainer.addView(row);
        }
    }

    private void confirmRemoveMember(Member member) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remover membro")
                .setMessage("Remover " + member.name + " da residência?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Remover", (dialog, which) ->
                new MemberApi(this).delete(member.id, new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        loadMembers();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ResidenceSettingsActivity.this, "Não foi possível remover o membro. Tente novamente.", Toast.LENGTH_SHORT).show();
                    }
                }))
                .show();
    }

    private void confirmDelete() {
        if (residence == null) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle("Excluir residência")
                .setMessage("Excluir \"" + residence.name + "\"? Esta ação não pode ser desfeita.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) ->
                        new ResidenceApi(this).delete(residenceId, new ApiCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                session.setSelectedResidenceId(-1);
                                Toast.makeText(ResidenceSettingsActivity.this, "Residência excluída.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ResidenceSettingsActivity.this, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(ResidenceSettingsActivity.this, "Não foi possível excluir a residência. Tente novamente.", Toast.LENGTH_SHORT).show();
                            }
                        }))
                .show();
    }
}
