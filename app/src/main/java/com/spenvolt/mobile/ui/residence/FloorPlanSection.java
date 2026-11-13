package com.spenvolt.mobile.ui.residence;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.spenvolt.mobile.R;
import com.spenvolt.mobile.api.ApiCallback;
import com.spenvolt.mobile.api.FloorApi;
import com.spenvolt.mobile.model.Device;
import com.spenvolt.mobile.model.Floor;
import com.spenvolt.mobile.model.Room;
import com.spenvolt.mobile.model.RoomKind;
import com.spenvolt.mobile.model.Usage;
import com.spenvolt.mobile.ui.common.Views;
import com.spenvolt.mobile.util.Consumption;
import com.spenvolt.mobile.util.Format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class FloorPlanSection {

    private final Context context;
    private final FloorApi api;
    private final int residenceId;
    private final LayoutInflater inflater;

    private final List<Floor> floors = new ArrayList<>();
    private List<Device> devices = new ArrayList<>();
    private int floorId = -1;
    private int selectedRoomId = -1;
    private boolean loading = true;

    private final TextView txtRoomCounts;
    private final LinearLayout floorChips;
    private final TextView txtPlanError;
    private final TextView txtPlanStatus;
    private final LinearLayout roomsGrid;
    private final TextView txtPlanHint;
    private final View roomPanel;
    private final EditText etRoomName;
    private final TextView txtRoomInfo;
    private final TextView txtRoomKwh;
    private final TextView txtRoomCost;
    private final LinearLayout roomDevicesContainer;
    private final Spinner spinnerAddDevice;

    FloorPlanSection(Context context, View root, int residenceId) {
        this.context = context;
        this.api = new FloorApi(context);
        this.residenceId = residenceId;
        this.inflater = LayoutInflater.from(context);

        txtRoomCounts = root.findViewById(R.id.txtRoomCounts);
        floorChips = root.findViewById(R.id.floorChips);
        txtPlanError = root.findViewById(R.id.txtPlanError);
        txtPlanStatus = root.findViewById(R.id.txtPlanStatus);
        roomsGrid = root.findViewById(R.id.roomsGrid);
        txtPlanHint = root.findViewById(R.id.txtPlanHint);
        roomPanel = root.findViewById(R.id.roomPanel);
        etRoomName = root.findViewById(R.id.etRoomName);
        txtRoomInfo = root.findViewById(R.id.txtRoomInfo);
        txtRoomKwh = root.findViewById(R.id.txtRoomKwh);
        txtRoomCost = root.findViewById(R.id.txtRoomCost);
        roomDevicesContainer = root.findViewById(R.id.roomDevicesContainer);
        spinnerAddDevice = root.findViewById(R.id.spinnerAddDevice);

        root.findViewById(R.id.btnAddRoom).setOnClickListener(v -> openAddRoom(null, -1));
        root.findViewById(R.id.btnRemoveRoom).setOnClickListener(v -> confirmRemoveRoom());

        etRoomName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                etRoomName.clearFocus();
                renameSelectedRoom();
            }
            return false;
        });
        etRoomName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) renameSelectedRoom();
        });
    }

    void load() {
        api.findFloors(residenceId, new ApiCallback<List<Floor>>() {
            @Override
            public void onSuccess(List<Floor> result) {
                loading = false;
                floors.clear();
                floors.addAll(result);
                if (findFloor(floorId) == null) {
                    floorId = floors.isEmpty() ? -1 : floors.get(0).id;
                }
                showError(null);
                render();
            }

            @Override
            public void onError(String message) {
                loading = false;
                showError("Não foi possível carregar a planta. Tente novamente.");
                render();
            }
        });
    }

    void setDevices(List<Device> devices) {
        this.devices = devices;
        render();
    }

    private Floor findFloor(int id) {
        for (Floor f : floors) {
            if (f.id == id) return f;
        }
        return null;
    }

    private List<Room> currentRooms() {
        Floor floor = findFloor(floorId);
        return floor == null ? new ArrayList<>() : floor.rooms;
    }

    private Room selectedRoom() {
        for (Room r : currentRooms()) {
            if (r.id == selectedRoomId) return r;
        }
        return null;
    }

    private double houseKwh() {
        double total = 0;
        for (Device d : devices) total += d.monthlyKwh;
        return total;
    }

    private void render() {
        if (loading) {
            Views.message(txtPlanStatus, "Carregando planta…");
        } else if (floors.isEmpty()) {
            Views.message(txtPlanStatus, "Toque em \"+ Andar\" para começar a montar a planta.");
        } else {
            Views.message(txtPlanStatus, null);
        }
        renderFloors();
        txtRoomCounts.setText(FloorSlots.summary(currentRooms()));
        renderGrid();
        renderPanel();
    }

    private void renderFloors() {
        floorChips.removeAllViews();
        for (Floor floor : floors) {
            TextView chip = Views.addChip(floorChips, floor.name);
            chip.setSelected(floor.id == floorId);
            chip.setOnClickListener(v -> {
                floorId = floor.id;
                selectedRoomId = -1;
                render();
            });
        }

        TextView addFloor = Views.addChip(floorChips, "+ Andar");
        addFloor.setBackgroundResource(R.drawable.bg_chip_dashed);
        addFloor.setTextColor(ContextCompat.getColor(context, R.color.gray_500));
        addFloor.setOnClickListener(v -> addFloor());
    }

    private void renderGrid() {
        roomsGrid.removeAllViews();
        if (loading || floorId == -1) return;

        List<Room> rooms = currentRooms();
        List<View> tiles = new ArrayList<>();

        for (RoomKind kind : RoomKind.values()) {
            List<Room> ofKind = new ArrayList<>();
            for (Room r : rooms) {
                if (r.kind == kind) ofKind.add(r);
            }
            Collections.sort(ofKind, (a, b) -> Integer.compare(a.slot, b.slot));

            for (Room room : ofKind) {
                tiles.add(roomTile(room));
            }
            int freeSlot = FloorSlots.nextFree(kind, rooms);
            if (freeSlot != -1) {
                tiles.add(freeTile(kind, freeSlot));
            }
        }

        LinearLayout row = null;
        for (int i = 0; i < tiles.size(); i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                roomsGrid.addView(row);
            }
            row.addView(tiles.get(i));
        }
        if (tiles.size() % 2 == 1 && row != null) {
            View spacer = new View(context);
            row.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1f));
        }
    }

    private View roomTile(Room room) {
        View tile = inflater.inflate(R.layout.item_room_tile, roomsGrid, false);
        Usage usage = Consumption.roomUsage(room, devices);
        Consumption.Density density = Consumption.density(usage.kwh, room.kind.area);
        boolean selected = room.id == selectedRoomId;

        GradientDrawable background = (GradientDrawable) tile.getBackground().mutate();
        background.setColor(ContextCompat.getColor(context, density.fillRes));
        int strokeColor = ContextCompat.getColor(context, selected ? R.color.blue_end : density.strokeRes);
        background.setStroke(dp(selected ? 3 : 2), strokeColor);

        ((TextView) tile.findViewById(R.id.txtName)).setText(room.name);
        ((TextView) tile.findViewById(R.id.txtArea)).setText(Format.area(room.kind.area));
        ((TextView) tile.findViewById(R.id.txtKwh)).setText(Format.kwh(usage.kwh));

        tile.setOnClickListener(v -> {
            selectedRoomId = selected ? -1 : room.id;
            render();
        });
        return tile;
    }

    private View freeTile(RoomKind kind, int slot) {
        View tile = inflater.inflate(R.layout.item_room_free, roomsGrid, false);
        ((TextView) tile.findViewById(R.id.txtName)).setText(kind.defaultName(slot));
        tile.setOnClickListener(v -> openAddRoom(kind, slot));
        return tile;
    }

    private void renderPanel() {
        Room room = selectedRoom();
        Views.show(roomPanel, room != null);
        Views.show(txtPlanHint, room == null && !loading && floorId != -1);
        if (room == null) return;

        Usage usage = Consumption.roomUsage(room, devices);
        double house = houseKwh();
        long share = house > 0 ? Math.round(usage.kwh / house * 100) : 0;

        if (!etRoomName.hasFocus()) etRoomName.setText(room.name);
        txtRoomInfo.setText(room.kind.label + " · " + Format.area(room.kind.area));
        txtRoomKwh.setText(Format.kwh(usage.kwh));
        txtRoomCost.setText(Format.brl(usage.cost) + " no mês · " + share + "% da casa");

        List<Device> inRoom = new ArrayList<>();
        List<Device> available = new ArrayList<>();
        for (Device d : devices) {
            if (room.deviceIds.contains(d.id)) inRoom.add(d);
            else available.add(d);
        }
        Collections.sort(inRoom, (a, b) -> Double.compare(b.monthlyKwh, a.monthlyKwh));

        roomDevicesContainer.removeAllViews();
        if (inRoom.isEmpty()) {
            TextView empty = new TextView(context);
            empty.setText("Nenhum dispositivo nesta seção.");
            empty.setTextColor(ContextCompat.getColor(context, R.color.white_70));
            roomDevicesContainer.addView(empty);
        }
        for (Device d : inRoom) {
            View row = inflater.inflate(R.layout.item_room_device, roomDevicesContainer, false);
            ((TextView) row.findViewById(R.id.txtName)).setText(d.displayName());
            ((TextView) row.findViewById(R.id.txtKwh)).setText(Format.kwh(d.monthlyKwh));
            row.findViewById(R.id.btnRemove).setOnClickListener(v -> unassignDevice(room, d.id));
            roomDevicesContainer.addView(row);
        }

        renderAddDeviceSpinner(room, available);
    }

    private void renderAddDeviceSpinner(Room room, List<Device> available) {
        Views.show(spinnerAddDevice, !available.isEmpty());

        List<String> labels = new ArrayList<>();
        labels.add("+ Dispositivo");
        for (Device d : available) {
            boolean elsewhere = isInAnyRoom(d.id);
            labels.add(d.displayName() + (elsewhere ? " (mover)" : ""));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.item_spinner, labels);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerAddDevice.setOnItemSelectedListener(null);
        spinnerAddDevice.setAdapter(adapter);
        spinnerAddDevice.setSelection(0, false);
        spinnerAddDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return;
                assignDevice(room, available.get(position - 1).id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private boolean isInAnyRoom(int deviceId) {
        for (Floor f : floors) {
            for (Room r : f.rooms) {
                if (r.deviceIds.contains(deviceId)) return true;
            }
        }
        return false;
    }

    private void addFloor() {
        api.createFloor(residenceId, new ApiCallback<Floor>() {
            @Override
            public void onSuccess(Floor floor) {
                floors.add(floor);
                floorId = floor.id;
                selectedRoomId = -1;
                showError(null);
                render();
            }

            @Override
            public void onError(String message) {
                showError("Não foi possível adicionar o andar.");
            }
        });
    }

    private void openAddRoom(RoomKind kind, int slot) {
        if (floorId == -1) {
            showError("Adicione um andar primeiro.");
            return;
        }
        AddRoomDialog.show(context, currentRooms(), kind, slot, this::addRoom);
    }

    private void addRoom(RoomKind kind, int slot, String name) {
        int targetFloor = floorId;
        api.createRoom(targetFloor, kind, slot, name, new ApiCallback<Room>() {
            @Override
            public void onSuccess(Room room) {
                Floor floor = findFloor(targetFloor);
                if (floor != null) floor.rooms.add(room);
                selectedRoomId = room.id;
                showError(null);
                render();
            }

            @Override
            public void onError(String message) {
                showError("Não foi possível adicionar a seção.");
            }
        });
    }

    private void renameSelectedRoom() {
        Room room = selectedRoom();
        if (room == null) return;
        String name = etRoomName.getText().toString().trim();
        if (name.isEmpty() || name.equals(room.name)) {
            etRoomName.setText(room.name);
            return;
        }

        api.renameRoom(room.id, name, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                room.name = name;
                render();
            }

            @Override
            public void onError(String message) {
                showError("Não foi possível renomear a seção.");
            }
        });
    }

    private void confirmRemoveRoom() {
        Room room = selectedRoom();
        if (room == null) return;
        Views.confirm(context, "Remover seção", "Remover \"" + room.name + "\" da planta?", "Remover", () ->
                api.deleteRoom(room.id, new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        currentRooms().remove(room);
                        selectedRoomId = -1;
                        render();
                    }

                    @Override
                    public void onError(String message) {
                        showError("Não foi possível remover a seção.");
                    }
                }));
    }

    private void assignDevice(Room room, int deviceId) {
        api.assignDevice(room.id, deviceId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                for (Floor f : floors) {
                    for (Room r : f.rooms) {
                        r.deviceIds.remove(Integer.valueOf(deviceId));
                    }
                }
                room.deviceIds.add(deviceId);
                render();
            }

            @Override
            public void onError(String message) {
                showError("Não foi possível adicionar o dispositivo.");
                render();
            }
        });
    }

    private void unassignDevice(Room room, int deviceId) {
        api.unassignDevice(room.id, deviceId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                room.deviceIds.remove(Integer.valueOf(deviceId));
                render();
            }

            @Override
            public void onError(String message) {
                showError("Não foi possível remover o dispositivo.");
            }
        });
    }

    private void showError(String message) {
        Views.message(txtPlanError, message);
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
