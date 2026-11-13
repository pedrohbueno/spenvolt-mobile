package com.spenvolt.mobile.model;

public enum RoomKind {

    SALA("Sala", "Salas", 18, 3),
    COZINHA("Cozinha", "Cozinhas", 9, 2),
    QUARTO("Quarto", "Quartos", 10, 8),
    BANHEIRO("Banheiro", "Banheiros", 4.5, 7),
    OUTROS("Outros", "Outros", 8, 1);

    public final String label;
    public final String plural;
    public final double area;
    public final int maxPerFloor;

    RoomKind(String label, String plural, double area, int maxPerFloor) {
        this.label = label;
        this.plural = plural;
        this.area = area;
        this.maxPerFloor = maxPerFloor;
    }

    public String defaultName(int slot) {
        return maxPerFloor > 1 ? label + " " + slot : label;
    }

    public static RoomKind from(String value) {
        try {
            return RoomKind.valueOf(value);
        } catch (Exception e) {
            return OUTROS;
        }
    }
}
