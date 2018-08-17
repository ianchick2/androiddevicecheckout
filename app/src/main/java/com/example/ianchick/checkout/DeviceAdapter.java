package com.example.ianchick.checkout;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ianchick on 8/16/18
 */
public class DeviceAdapter extends ArrayAdapter<Device> {

    private ArrayList<Device> devices;
    private Context context;

    public DeviceAdapter(@NonNull Context context, ArrayList<Device> devices) {
        super(context, R.layout.device_list_item, devices);
        this.devices = devices;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Device device = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_list_item, parent, false);
        }
        TextView deviceTitle = convertView.findViewById(R.id.device_item_title);
        TextView deviceSerial = convertView.findViewById(R.id.device_item_serial);
        TextView deviceIsCheckedOut = convertView.findViewById(R.id.device_item_checked_out);
        TextView deviceUser = convertView.findViewById(R.id.device_item_user);
        ImageView deviceImage = convertView.findViewById(R.id.device_item_image);

        deviceTitle.setText(device.deviceName);
        deviceSerial.setText(device.serialNumber);
        deviceUser.setText(device.getUserName());


        int resourceId = context.getResources().getIdentifier(device.serialNumber.toLowerCase(), "drawable", context.getPackageName());
        if (resourceId == 0) {
            deviceImage.setVisibility(View.GONE);
        } else {
            deviceImage.setImageResource(resourceId);
        }

        if (device.isCheckedOut()) {
            deviceIsCheckedOut.setText("Checked Out");
            convertView.setAlpha(0.5f);
            deviceIsCheckedOut.setTextColor(Color.parseColor("#ffcc0000"));
        } else {
            deviceIsCheckedOut.setText("Available");
            convertView.setAlpha(1.0f);
            deviceIsCheckedOut.setTextColor(Color.parseColor("#ff669900"));
        }

        return convertView;
    }

    public Device getItem(int position) {
        return devices.get(position);
    }
}
