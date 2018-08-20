package com.example.ianchick.checkout;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    public Device getItem(int position) {
        return devices.get(position);
    }

    private void setImage(String filename, final ImageView imageView) {
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://androiddevicecheckout.appspot.com");
        StorageReference storageRef = storage.getReference().child("images");
        StorageReference imageRef = storageRef.child(filename + ".jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
                Log.d("findme", "Successfully found image");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                imageView.setVisibility(View.GONE);
                Log.d("findme", "Failed to find image because " + exception);
            }
        });
    }
}
