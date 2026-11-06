package com.spenvolt.mobile.model;

/**
 * Tipos de seção da planta (igual a lib/house-plan.ts do site).
 * Cada tipo tem uma área média (para calcular a densidade de consumo)
 * e um máximo de seções por andar.
 */
public enum RoomKind {

    SALA("Sala", "Salas", 18, 3),
    COZINHA("Cozinha", "Cozinhas", 9, 2),
    QUARTO("Quarto", "Quartos", 10, 8),
    BANHEIRO("Banheiro", "Banheiros", 4.5, 7),
    OUTROS("Outros", "Outros", 8, 1);

    public final String label;
    public final String plural;
    public final double area;     // m²
    public final int maxPerFloor;

    RoomKind(String label, String plural, double area, int maxPerFloor) {
        this.label = label;
        this.plural = plural;
        this.area = area;
        this.maxPerFloor = maxPerFloor;
    }

    /** "Quarto 2" (ou só "Cozinha" quando o tipo só aceita uma). */
    public String defaultName(int slot) {
        return maxPerFloor > 1 ? label + " " + slot : label;
    }

    /** Converte o texto da API ("QUARTO") no enum; desconhecido vira OUTROS. */
    public static RoomKind from(String value) {
        try {
            return RoomKind.valueOf(value);
        } catch (Exception e) {
            return OUTROS;
        }
    }
}
