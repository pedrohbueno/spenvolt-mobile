package com.spenvolt.mobile.util;

import com.spenvolt.mobile.R;
import com.spenvolt.mobile.model.Device;
import com.spenvolt.mobile.model.Residence;
import com.spenvolt.mobile.model.Room;
import com.spenvolt.mobile.model.Usage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Consumption {

    private Consumption() {
    }

    public enum Status {
        OK("Tranquilo", R.color.status_ok, R.color.white),
        WARNING("Em alerta", R.color.status_warning, R.color.dark_blue),
        EXCEEDED("Limite estourado", R.color.status_exceeded, R.color.white),
        NO_LIMIT("Sem limite", R.color.status_none, R.color.dark_blue);

        public final String label;
        public final int colorRes;
        public final int textColorRes;

        Status(String label, int colorRes, int textColorRes) {
            this.label = label;
            this.colorRes = colorRes;
            this.textColorRes = textColorRes;
        }
    }

    public static double usagePercent(double used, double limit) {
        if (limit <= 0) return 0;
        return used / limit * 100;
    }

    public static Status status(double used, double limit, Integer alertAt) {
        if (limit <= 0) return Status.NO_LIMIT;
        double percent = usagePercent(used, limit);
        if (percent >= 100) return Status.EXCEEDED;
        if (percent >= (alertAt == null ? 80 : alertAt)) return Status.WARNING;
        return Status.OK;
    }

    public static Status status(Residence r) {
        return status(r.monthlyKwh, r.kwhLimit(), r.alertThresholdPercent);
    }

    public static double monthlyKwh(double powerW, double hoursPerDay, double daysPerMonth) {
        return powerW * hoursPerDay * daysPerMonth / 1000;
    }

    public static Usage sum(List<Device> devices) {
        Usage total = new Usage(0, 0);
        for (Device d : devices) {
            total.add(d.monthlyKwh, d.monthlyCost);
        }
        return total;
    }

    public static Usage deviceShare(Device device) {
        int users = Math.max(device.memberIds.size(), 1);
        return new Usage(device.monthlyKwh / users, device.monthlyCost / users);
    }

    public static Map<Integer, Usage> usageByMember(List<Device> devices) {
        Map<Integer, Usage> totals = new HashMap<>();
        for (Device device : devices) {
            Usage share = deviceShare(device);
            for (int memberId : device.memberIds) {
                Usage current = totals.get(memberId);
                if (current == null) {
                    current = new Usage(0, 0);
                    totals.put(memberId, current);
                }
                current.add(share.kwh, share.cost);
            }
        }
        return totals;
    }

    public enum Density {
        LOW(R.color.room_low, R.color.status_ok),
        WARNING(R.color.room_warning, R.color.status_warning),
        HIGH(R.color.room_high, R.color.status_exceeded);

        public final int fillRes;
        public final int strokeRes;

        Density(int fillRes, int strokeRes) {
            this.fillRes = fillRes;
            this.strokeRes = strokeRes;
        }
    }

    public static Density density(double kwh, double area) {
        double perM2 = kwh / area;
        if (perM2 >= 12) return Density.HIGH;
        if (perM2 >= 6) return Density.WARNING;
        return Density.LOW;
    }

    public static Usage roomUsage(Room room, List<Device> devices) {
        Usage usage = new Usage(0, 0);
        for (Device d : devices) {
            if (room.deviceIds.contains(d.id)) {
                usage.add(d.monthlyKwh, d.monthlyCost);
            }
        }
        return usage;
    }
}
