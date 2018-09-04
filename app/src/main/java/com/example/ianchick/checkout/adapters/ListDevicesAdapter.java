package com.example.ianchick.checkout.adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ianchick.checkout.OnRecyclerViewItemClickListener;
import com.example.ianchick.checkout.R;
import com.example.ianchick.checkout.models.Device;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by ianchick on 8/31/18
 */
public class ListDevicesAdapter extends RecyclerView.Adapter<ListDevicesAdapter.DeviceViewHolder> {

    private ArrayList<Device> devices;
    private OnRecyclerViewItemClickListener onItemClickListener;

    public ListDevicesAdapter(ArrayList<Device> source) {
        this.devices = source;
    }

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
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

        if (!TextUtils.isEmpty(device.imageRef)) {
            setImage(device.imageRef, holder.deviceImageView);
        } else {
            holder.deviceImageView.setVisibility(View.GONE);
        }

        if (device.isCheckedOut()) {
            holder.parentLayout.setAlpha(0.5f);
        } else {
            holder.parentLayout.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return devices.size();
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
            }
        }).addOnFailureListener(new OnFailureListener() {
            @SuppressLint("LogNotTimber")
            @Override
            public void onFailure(@NonNull Exception exception) {
                imageView.setVisibility(View.GONE);
            }
        });
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {

        TextView deviceTitle;
        TextView deviceSerial;
        TextView deviceIsCheckedOut;
        TextView deviceUser;
        ImageView deviceImageView;

        RelativeLayout parentLayout;

        DeviceViewHolder(final View deviceItemView) {
            super(deviceItemView);

            deviceTitle = deviceItemView.findViewById(R.id.device_item_title);
            deviceSerial = deviceItemView.findViewById(R.id.device_item_serial);
            deviceIsCheckedOut = deviceItemView.findViewById(R.id.device_item_checked_out);
            deviceUser = deviceItemView.findViewById(R.id.device_item_user);
            deviceImageView = deviceItemView.findViewById(R.id.device_item_image);

            parentLayout = deviceItemView.findViewById(R.id.device_list_item_relative_layout);
            parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(getAdapterPosition(), deviceItemView);
                    }
                }
            });
        }
    }
}