package com.spenvolt.mobile.ui.economize;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.spenvolt.mobile.R;
import com.spenvolt.mobile.model.Device;
import com.spenvolt.mobile.model.DeviceOffer;
import com.spenvolt.mobile.ui.common.Views;
import com.spenvolt.mobile.util.Format;

import java.util.ArrayList;
import java.util.List;

/** Lista de ofertas das lojas (item_offer). */
public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.ViewHolder> {

    public interface Listener {
        void onCompare(DeviceOffer offer);

        void onOpenStore(DeviceOffer offer);
    }

    private final List<DeviceOffer> offers = new ArrayList<>();
    private final Listener listener;
    private Device mine;     // aparelho do usuário usado na comparação
    private double tariff;   // tarifa da residência selecionada

    public OfferAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setOffers(List<DeviceOffer> newOffers, @Nullable Device mine, double tariff) {
        offers.clear();
        offers.addAll(newOffers);
        this.mine = mine;
        this.tariff = tariff;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        DeviceOffer o = offers.get(position);

        h.txtInitial.setText(Format.initial(o.name));
        h.txtName.setText(o.name);
        h.txtSubtitle.setText(subtitle(o));

        int discount = o.discountPercent();
        Views.show(h.txtDiscount, discount > 0);
        h.txtDiscount.setText("-" + discount + "%");

        h.txtPrice.setText(o.price == null ? "Preço indisponível" : Format.brl(o.price));
        boolean showOriginal = discount > 0;
        Views.show(h.txtOriginalPrice, showOriginal);
        if (showOriginal) {
            h.txtOriginalPrice.setText(Format.brl(o.originalPrice));
            h.txtOriginalPrice.setPaintFlags(h.txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        h.txtSavings.setText(Savings.text(Savings.estimate(o, mine, tariff), mine));

        h.btnCompare.setOnClickListener(v -> listener.onCompare(o));
        h.btnStore.setOnClickListener(v -> listener.onOpenStore(o));
        Views.show(h.btnStore, o.deviceUrl != null);
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    /** "Consul · 110 W · FastShop" */
    private String subtitle(DeviceOffer o) {
        List<String> parts = new ArrayList<>();
        if (o.brand != null) parts.add(o.brand);
        if (o.power != null) parts.add(Format.num(o.power) + " W");
        if (o.source != null) parts.add(o.source);
        return android.text.TextUtils.join(" · ", parts);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtInitial, txtName, txtSubtitle, txtDiscount, txtPrice, txtOriginalPrice, txtSavings;
        final View btnCompare, btnStore;

        ViewHolder(View view) {
            super(view);
            txtInitial = view.findViewById(R.id.txtInitial);
            txtName = view.findViewById(R.id.txtName);
            txtSubtitle = view.findViewById(R.id.txtSubtitle);
            txtDiscount = view.findViewById(R.id.txtDiscount);
            txtPrice = view.findViewById(R.id.txtPrice);
            txtOriginalPrice = view.findViewById(R.id.txtOriginalPrice);
            txtSavings = view.findViewById(R.id.txtSavings);
            btnCompare = view.findViewById(R.id.btnCompare);
            btnStore = view.findViewById(R.id.btnStore);
        }
    }
}
