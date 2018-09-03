package com.example.ianchick.checkout.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ianchick.checkout.R;
import com.example.ianchick.checkout.models.Device;

import java.util.ArrayList;

/**
 * Created by ianchick on 8/31/18
 */
public class ListDevicesAdapter extends RecyclerView.Adapter<ListDevicesAdapter.DeviceViewHolder> {

    private ArrayList<Device> devices;

    public ListDevicesAdapter(ArrayList<Device> source) {
        this.devices = source;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.device_list_item, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, final int position) {
        final Device device = devices.get(position);
        holder.deviceTitle.setText(device.deviceName);
        holder.deviceSerial.setText(device.serialNumber);
        holder.deviceUser.setText(device.getUserName());
        holder.deviceIsCheckedOut.setText(device.isCheckedOut().toString());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }



    class DeviceViewHolder extends RecyclerView.ViewHolder {

        TextView deviceTitle;
        TextView deviceSerial;
        TextView deviceIsCheckedOut;
        TextView deviceUser;
        ImageView deviceImageView;

        DeviceViewHolder(View deviceItemView) {
            super(deviceItemView);

            deviceTitle = deviceItemView.findViewById(R.id.device_item_title);
            deviceSerial = deviceItemView.findViewById(R.id.device_item_serial);
            deviceIsCheckedOut = deviceItemView.findViewById(R.id.device_item_checked_out);
            deviceUser = deviceItemView.findViewById(R.id.device_item_user);
            deviceImageView = deviceItemView.findViewById(R.id.device_item_image);

        }
    }
}
