package com.example.ianchick.checkout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ianchick.checkout.models.Device;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by ianchick on 8/16/18
 */
public class DeviceAdapter extends ArrayAdapter<Device> {

    private static final String TAG = "DeviceAdapter";
    private static final String LIST_FILTER_ALL = "LIST_FILTER_ALL";
    private static final String LIST_FILTER_AVAILABLE = "LIST_FILTER_AVAILABLE";
    private static final String LIST_FILTER_UNAVAILABLE = "LIST_FILTER_UNAVAILABLE";
    private ArrayList<Device> allDevices;
    private ArrayList<Device> shownDevices;
    private String filterMode;
    private boolean isSortedAZ;

    public DeviceAdapter(@NonNull Context context, ArrayList<Device> devices) {
        super(context, R.layout.device_list_item, devices);
        this.allDevices = devices;
        this.shownDevices = devices;
        filterMode = "";
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

        ImageView deviceImageView = convertView.findViewById(R.id.device_item_image);

        deviceTitle.setText(device.deviceName);
        deviceSerial.setText(device.serialNumber);
        deviceUser.setText(device.getUserName());

        if (!TextUtils.isEmpty(device.imageRef)) {
            setImage(device.imageRef, deviceImageView);
        } else {
            deviceImageView.setVisibility(View.GONE);
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

    public void sortList() {
        if (!isSortedAZ) {
            Collections.sort(this.shownDevices, new DeviceNameComparator());
            isSortedAZ = true;
        } else {
            Collections.reverse(this.shownDevices);
            isSortedAZ = false;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return shownDevices != null ? shownDevices.size() : 0;
    }

    public Device getItem(int position) {
        return shownDevices.get(position);
    }

    private void setImage(String filename, final ImageView imageView) {
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://androiddevicecheckout.appspot.com");
        StorageReference storageRef = storage.getReference().child("images");
        StorageReference imageRef = storageRef.child(filename + ".jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @SuppressLint("LogNotTimber")
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
                Log.v(TAG, "Successfully found image");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @SuppressLint("LogNotTimber")
            @Override
            public void onFailure(@NonNull Exception exception) {
                imageView.setVisibility(View.GONE);
                Log.v(TAG, "Failed to find image because " + exception);
            }
        });
    }

    @Override
    public Filter getFilter() {
        return new DeviceFilter();
    }

    public class DeviceNameComparator implements Comparator<Device> {
        public int compare(Device left, Device right) {
            return left.deviceName.compareTo(right.deviceName);
        }
    }

    private class DeviceFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            shownDevices = allDevices;
            FilterResults results = new FilterResults();
            ArrayList<Device> filterList = new ArrayList<>();
            switch (filterMode) {
                case LIST_FILTER_ALL:
                    results.count = shownDevices.size();
                    results.values = shownDevices;
                    filterMode = LIST_FILTER_AVAILABLE;
                    break;
                case LIST_FILTER_UNAVAILABLE:
                    for (Device device : shownDevices) {
                        if (device.isCheckedOut()) {
                            filterList.add(device);
                        }
                    }
                    results.count = filterList.size();
                    results.values = filterList;
                    filterMode = LIST_FILTER_ALL;
                    break;
                case LIST_FILTER_AVAILABLE:
                    for (Device device : shownDevices) {
                        if (!device.isCheckedOut()) {
                            filterList.add(device);
                        }
                    }
                    results.count = filterList.size();
                    results.values = filterList;
                    filterMode = LIST_FILTER_UNAVAILABLE;
                    break;
                default:
                    for (Device device : shownDevices) {
                        if (!device.isCheckedOut()) {
                            filterList.add(device);
                        }
                    }
                    results.count = filterList.size();
                    results.values = filterList;
                    filterMode = LIST_FILTER_UNAVAILABLE;
                    break;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            shownDevices = (ArrayList<Device>) results.values;
            notifyDataSetChanged();
        }
    }
}
