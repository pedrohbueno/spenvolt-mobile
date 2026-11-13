package com.spenvolt.mobile.ui.device;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.spenvolt.mobile.R;
import com.spenvolt.mobile.model.Device;
import com.spenvolt.mobile.util.Format;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    public interface OnDeviceClick {
        void onClick(Device device);
    }

    private final List<Device> devices = new ArrayList<>();
    private final OnDeviceClick onClick;

    public DeviceAdapter(OnDeviceClick onClick) {
        this.onClick = onClick;
    }

    public void setDevices(List<Device> newDevices) {
        devices.clear();
        devices.addAll(newDevices);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Device d = devices.get(position);

        holder.txtInitial.setText(Format.initial(d.displayName()));
        holder.txtName.setText(d.displayName());
        holder.txtSubtitle.setText(subtitle(d));
        holder.txtCost.setText(Format.brl(d.monthlyCost));
        holder.txtPower.setText(d.power == null ? "—" : Format.num(d.power) + " W");
        holder.txtKwh.setText(Format.num(d.monthlyKwh) + " kWh/mês");
        holder.itemView.setOnClickListener(v -> onClick.onClick(d));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    private String subtitle(Device d) {
        StringBuilder text = new StringBuilder();
        if (d.brand != null) text.append(d.brand);
        if (d.category != null) {
            if (text.length() > 0) text.append(" · ");
            text.append(d.category);
        }
        return text.length() == 0 ? "Sem categoria" : text.toString();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtInitial;
        final TextView txtName;
        final TextView txtSubtitle;
        final TextView txtCost;
        final TextView txtPower;
        final TextView txtKwh;

        ViewHolder(View view) {
            super(view);
            txtInitial = view.findViewById(R.id.txtInitial);
            txtName = view.findViewById(R.id.txtName);
            txtSubtitle = view.findViewById(R.id.txtSubtitle);
            txtCost = view.findViewById(R.id.txtCost);
            txtPower = view.findViewById(R.id.txtPower);
            txtKwh = view.findViewById(R.id.txtKwh);
        }
    }
}
