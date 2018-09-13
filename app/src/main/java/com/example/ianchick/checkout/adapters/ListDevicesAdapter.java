package com.example.ianchick.checkout.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ianchick.checkout.OnRecyclerViewItemClickListener;
import com.example.ianchick.checkout.OnRecyclerViewItemLongClickListener;
import com.example.ianchick.checkout.R;
import com.example.ianchick.checkout.models.Device;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ianchick on 8/31/18
 */
public class ListDevicesAdapter extends RecyclerView.Adapter<ListDevicesAdapter.DeviceViewHolder> implements Filterable {

    private Context context;

    private ArrayList<Device> devices;
    private ArrayList<Device> devicesFiltered;
    private OnRecyclerViewItemClickListener onItemClickListener;
    private OnRecyclerViewItemLongClickListener onRecyclerViewItemLongClickListener;

    private List<String> AVAILABLE_STRINGS = Arrays.asList("available", "false", "free", "avail");
    private List<String> UNAVAILABLE_STRINGS = Arrays.asList("unavailable", "true", "checked", "out");

    public ListDevicesAdapter(Context context, ArrayList<Device> source) {
        this.devices = source;
        this.devicesFiltered = devices;
        this.context = context;
    }

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnRecyclerViewItemLongClickListener(OnRecyclerViewItemLongClickListener onRecyclerViewItemLongClickListener) {
        this.onRecyclerViewItemLongClickListener = onRecyclerViewItemLongClickListener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.device_list_item, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DeviceViewHolder holder, final int position) {
        final Device device = devicesFiltered.get(position);
        holder.deviceTitle.setText(device.deviceName);
        holder.deviceSerial.setText(device.serialNumber);
        holder.deviceUser.setText(device.getUserName());
        holder.deviceOs.setText(String.valueOf(device.os));
        holder.deviceType.setText(device.type);

        if (device.isCheckedOut()) {
            holder.deviceIsCheckedOut.setText("Device is currently checked out");
        } else {
            holder.deviceIsCheckedOut.setText("Available");
        }

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
        return devicesFiltered.size();
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
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                imageView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    devicesFiltered = devices;
                } else {
                    ArrayList<Device> filteredList = new ArrayList<>();
                    for (Device d : devices) {
                        if (d.deviceName.toLowerCase().contains(charString.toLowerCase())
                                || d.getUserName().toLowerCase().contains(charString.toLowerCase())
                                || d.serialNumber.toLowerCase().contains(charString.toLowerCase())
                                || (AVAILABLE_STRINGS.contains(charString.toLowerCase()) && !d.isCheckedOut())
                                || (UNAVAILABLE_STRINGS.contains(charString.toLowerCase()) && d.isCheckedOut()))

                        {
                            filteredList.add(d);
                        }
                    }
                    devicesFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = devicesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                devicesFiltered = (ArrayList<Device>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {

        TextView deviceTitle;
        TextView deviceSerial;
        TextView deviceIsCheckedOut;
        TextView deviceUser;
        TextView deviceOs;
        TextView deviceType;
        ImageView deviceImageView;

        RelativeLayout parentLayout;

        DeviceViewHolder(final View deviceItemView) {
            super(deviceItemView);

            deviceTitle = deviceItemView.findViewById(R.id.device_item_title);
            deviceSerial = deviceItemView.findViewById(R.id.device_item_serial);
            deviceIsCheckedOut = deviceItemView.findViewById(R.id.device_item_checked_out);
            deviceUser = deviceItemView.findViewById(R.id.device_item_user);
            deviceOs = deviceItemView.findViewById(R.id.device_item_os);
            deviceType = deviceItemView.findViewById(R.id.device_item_type);
            deviceImageView = deviceItemView.findViewById(R.id.device_item_image);

            parentLayout = deviceItemView.findViewById(R.id.device_list_item_relative_layout);
            parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(deviceSerial.getText().toString());
                    }
                }
            });

            parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (onRecyclerViewItemLongClickListener != null) {
                        onRecyclerViewItemLongClickListener.onItemLongClick(deviceSerial.getText().toString());
                        return true;
                    }
                    return false;
                }
            });

            deviceItemView.findViewById(R.id.device_item_open_info_layout_hitbox).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout infoLayout = deviceItemView.findViewById(R.id.device_item_info_layout);
                    if (infoLayout.getVisibility() == View.GONE) {
                        infoLayout.setVisibility(View.VISIBLE);
                        deviceItemView.findViewById(R.id.device_item_open_info).setBackground(context.getDrawable(R.drawable.ic_baseline_expand_less_24px));
                    } else {
                        infoLayout.setVisibility(View.GONE);
                        deviceItemView.findViewById(R.id.device_item_open_info).setBackground(context.getDrawable(R.drawable.ic_baseline_expand_more_24px));
                    }
                }
            });
        }
    }
}