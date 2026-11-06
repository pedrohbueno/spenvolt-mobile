package com.spenvolt.mobile.ui.residence;

import com.spenvolt.mobile.model.Room;
import com.spenvolt.mobile.model.RoomKind;

import java.util.List;

/**
 * Regras das vagas da planta (lib/floorplan.ts do site).
 * Cada tipo de seção tem um máximo por andar; cada seção ocupa um número (slot): Quarto 1, Quarto 2...
 */
final class FloorSlots {

    private FloorSlots() {
    }

    /** Quantas seções do tipo existem no andar. */
    static int count(RoomKind kind, List<Room> rooms) {
        int total = 0;
        for (Room r : rooms) {
            if (r.kind == kind) total++;
        }
        return total;
    }

    /** Quantas vagas ainda cabem do tipo. */
    static int free(RoomKind kind, List<Room> rooms) {
        return Math.max(kind.maxPerFloor - count(kind, rooms), 0);
    }

    static boolean isFree(RoomKind kind, int slot, List<Room> rooms) {
        if (slot < 1 || slot > kind.maxPerFloor) return false;
        for (Room r : rooms) {
            if (r.kind == kind && r.slot == slot) return false;
        }
        return true;
    }

    /** Primeiro número livre do tipo, ou -1 se não há vaga. */
    static int nextFree(RoomKind kind, List<Room> rooms) {
        for (int slot = 1; slot <= kind.maxPerFloor; slot++) {
            if (isFree(kind, slot, rooms)) return slot;
        }
        return -1;
    }

    /** "Quartos 2/8 · Banheiros 1/7 · ..." */
    static String summary(List<Room> rooms) {
        StringBuilder text = new StringBuilder();
        for (RoomKind kind : RoomKind.values()) {
            if (text.length() > 0) text.append(" · ");
            text.append(kind.plural).append(" ").append(count(kind, rooms)).append("/").append(kind.maxPerFloor);
        }
        return text.toString();
    }
}
