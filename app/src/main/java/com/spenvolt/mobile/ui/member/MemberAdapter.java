package com.spenvolt.mobile.ui.member;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.spenvolt.mobile.R;
import com.spenvolt.mobile.model.Member;
import com.spenvolt.mobile.model.Usage;
import com.spenvolt.mobile.util.Format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onMemberClick(Member member);

        void onAddClick();
    }

    private static final int TYPE_MEMBER = 0;
    private static final int TYPE_ADD = 1;

    private final List<Member> members = new ArrayList<>();
    private final Map<Integer, Usage> usage = new HashMap<>();
    private final Listener listener;
    private int selectedId = -1;

    public MemberAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<Member> newMembers, Map<Integer, Usage> newUsage) {
        members.clear();
        members.addAll(newMembers);
        usage.clear();
        usage.putAll(newUsage);
        notifyDataSetChanged();
    }

    public void setSelectedId(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return members.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == members.size() ? TYPE_ADD : TYPE_MEMBER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ADD) {
            View view = inflater.inflate(R.layout.item_member_add, parent, false);
            return new RecyclerView.ViewHolder(view) { };
        }
        return new MemberHolder(inflater.inflate(R.layout.item_member_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ADD) {
            holder.itemView.setOnClickListener(v -> listener.onAddClick());
            return;
        }

        Member m = members.get(position);
        Usage u = usage.containsKey(m.id) ? usage.get(m.id) : new Usage(0, 0);
        MemberHolder h = (MemberHolder) holder;

        h.txtAvatar.setText(Format.initial(m.name));
        h.txtName.setText(m.name);
        h.txtEmail.setText(m.email);
        h.txtKwh.setText(Format.kwh(u.kwh));
        h.txtCost.setText(Format.brl(u.cost) + " atribuído");
        h.itemView.setElevation(m.id == selectedId ? 10f : 2f);
        h.itemView.setOnClickListener(v -> listener.onMemberClick(m));
    }

    static class MemberHolder extends RecyclerView.ViewHolder {
        final TextView txtAvatar;
        final TextView txtName;
        final TextView txtEmail;
        final TextView txtKwh;
        final TextView txtCost;

        MemberHolder(View view) {
            super(view);
            txtAvatar = view.findViewById(R.id.txtAvatar);
            txtName = view.findViewById(R.id.txtName);
            txtEmail = view.findViewById(R.id.txtEmail);
            txtKwh = view.findViewById(R.id.txtKwh);
            txtCost = view.findViewById(R.id.txtCost);
        }
    }
}
