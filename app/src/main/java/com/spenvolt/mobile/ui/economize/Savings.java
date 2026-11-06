package com.spenvolt.mobile.ui.economize;

import androidx.annotation.Nullable;

import com.spenvolt.mobile.model.Device;
import com.spenvolt.mobile.model.DeviceOffer;
import com.spenvolt.mobile.util.Consumption;
import com.spenvolt.mobile.util.Format;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Compara uma oferta com o aparelho que o usuário já tem.
 * A oferta é usada com as mesmas horas por dia e dias por mês do aparelho atual.
 */
final class Savings {

    final double offerKwh;     // kWh/mês da oferta
    final double offerCost;    // R$/mês da oferta
    final double savedCost;    // R$/mês economizados (pode ser <= 0)
    final int paybackMonths;   // meses para o preço se pagar (-1 = não se paga)

    private Savings(double offerKwh, double offerCost, double savedCost, int paybackMonths) {
        this.offerKwh = offerKwh;
        this.offerCost = offerCost;
        this.savedCost = savedCost;
        this.paybackMonths = paybackMonths;
    }

    /** Calcula a economia, ou devolve null se faltar dado (potência, horas...). */
    @Nullable
    static Savings estimate(DeviceOffer offer, @Nullable Device mine, double tariff) {
        if (mine == null || offer.power == null || mine.hoursPerDay == null || mine.daysPerMonth == null) {
            return null;
        }
        double offerKwh = Consumption.monthlyKwh(offer.power, mine.hoursPerDay, mine.daysPerMonth);
        double offerCost = offerKwh * tariff;
        double saved = mine.monthlyCost - offerCost;
        int payback = saved > 0 && offer.price != null ? (int) Math.ceil(offer.price / saved) : -1;
        return new Savings(offerKwh, offerCost, saved, payback);
    }

    /** "Economia estimada: R$ 21/mês · se paga em 14 meses" */
    static String text(@Nullable Savings savings, @Nullable Device mine) {
        if (mine == null) return "Cadastre um aparelho parecido para ver a economia estimada.";
        if (savings == null) return "Sem dados de potência para comparar com o seu " + mine.displayName() + ".";
        if (savings.savedCost <= 0) return "Não gasta menos que o seu " + mine.displayName() + ".";

        String text = "Economia estimada: " + Format.brl(savings.savedCost) + "/mês";
        if (savings.paybackMonths > 0) text += " · se paga em " + savings.paybackMonths + " meses";
        return text;
    }

    /**
     * Acha, entre os aparelhos do usuário, o que combina com a busca
     * (pelo nome ou categoria). Se houver vários, fica com o que mais consome.
     */
    @Nullable
    static Device findMine(String query, List<Device> devices) {
        String wanted = normalize(query);
        Device best = null;
        for (Device d : devices) {
            String name = normalize(d.displayName());
            String category = normalize(d.category == null ? "" : d.category);
            boolean matches = name.contains(wanted) || wanted.contains(name) || category.contains(wanted);
            if (matches && (best == null || d.monthlyKwh > best.monthlyKwh)) best = d;
        }
        return best;
    }

    /** "Ar-Condicionado" → "ar-condicionado" (sem acentos e em minúsculas). */
    private static String normalize(String text) {
        String noAccents = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return noAccents.toLowerCase(Locale.ROOT).trim();
    }
}
