package com.spenvolt.mobile.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class Format {

    private static final Locale PT_BR = new Locale("pt", "BR");

    private Format() {
    }

    public static String brl(double value) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(value).replace(' ', ' ');
    }

    public static String num(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(PT_BR);
        format.setMaximumFractionDigits(0);
        return format.format(value);
    }

    public static String kwh(double value) {
        return num(value) + " kWh";
    }

    public static String area(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(PT_BR);
        format.setMinimumFractionDigits(1);
        format.setMaximumFractionDigits(1);
        return format.format(value) + " m²";
    }

    public static Double parseDecimal(String text) {
        String clean = text == null ? "" : text.trim().replace(',', '.');
        if (clean.isEmpty()) return null;
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String input(Double value) {
        if (value == null) return "";
        if (value == Math.rint(value)) return String.valueOf(value.longValue());
        return String.valueOf(value);
    }

    public static String dayMonth(String isoDate) {
        if (isoDate == null || isoDate.length() < 10) return "";
        return isoDate.substring(8, 10) + "/" + isoDate.substring(5, 7);
    }

    public static String initial(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        return name.trim().substring(0, 1).toUpperCase(PT_BR);
    }
}
