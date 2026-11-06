package com.spenvolt.mobile.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Formatação de números no padrão brasileiro (igual a lib/format.ts do site). */
public final class Format {

    private static final Locale PT_BR = new Locale("pt", "BR");

    private Format() {
    }

    /** 1031.5 → "R$ 1.031,50" */
    public static String brl(double value) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(value).replace(' ', ' ');
    }

    /** 1270.4 → "1.270" */
    public static String num(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(PT_BR);
        format.setMaximumFractionDigits(0);
        return format.format(value);
    }

    /** 1270.4 → "1.270 kWh" */
    public static String kwh(double value) {
        return num(value) + " kWh";
    }

    /** 18 → "18,0 m²" */
    public static String area(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(PT_BR);
        format.setMinimumFractionDigits(1);
        format.setMaximumFractionDigits(1);
        return format.format(value) + " m²";
    }

    /** Texto da API para número decimal: aceita vírgula ("0,82"). Vazio → null. */
    public static Double parseDecimal(String text) {
        String clean = text == null ? "" : text.trim().replace(',', '.');
        if (clean.isEmpty()) return null;
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Número para preencher um campo: 150.0 → "150", 0.8126 → "0.8126". Null → "". */
    public static String input(Double value) {
        if (value == null) return "";
        if (value == Math.rint(value)) return String.valueOf(value.longValue());
        return String.valueOf(value);
    }

    /** "2026-10-27T14:30:00" → "27/10" */
    public static String dayMonth(String isoDate) {
        if (isoDate == null || isoDate.length() < 10) return "";
        return isoDate.substring(8, 10) + "/" + isoDate.substring(5, 7);
    }

    /** Primeira letra em maiúscula, para os avatares ("pedro" → "P"). */
    public static String initial(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        return name.trim().substring(0, 1).toUpperCase(PT_BR);
    }
}
