package com.example.ianchick.checkout.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ianchick.checkout.R;
import com.example.ianchick.checkout.models.Device;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class EditDeviceActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_device);
        setTitle("Add a new device");

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_device_save:
                String deviceName = ((EditText) findViewById(R.id.edit_device_title)).getText().toString();
                String serial = ((EditText) findViewById(R.id.edit_device_serial)).getText().toString();
                String imageRef = ((EditText) findViewById(R.id.edit_device_imageref)).getText().toString();
                int os = Integer.valueOf(((EditText) findViewById(R.id.edit_device_os_version)).getText().toString());

                if (!TextUtils.isEmpty(deviceName)) {
                    if (!TextUtils.isEmpty(serial)) {
                        Device device = new Device(deviceName, serial);
                        device.os = os;
                        device.imageRef = imageRef;
                        updateDatabase(device);
                        finish();
                    } else {
                        ((TextView) findViewById(R.id.edit_device_error_message)).setText("Please enter a serial number for the device");
                    }
                } else {
                    ((TextView) findViewById(R.id.edit_device_error_message)).setText("Please enter a device name");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateDatabase(final Device d) {
        Map<String, Object> device = new HashMap<>();
        device.put("title", d.deviceName);
        device.put("serial", d.serialNumber);
        device.put("user", d.getUserName());
        device.put("imageRef", d.imageRef);
        device.put("isCheckedOut", d.isCheckedOut());
        device.put("os", d.os);

        db.collection("devices").document(d.serialNumber)
                .set(device)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Timber.v("DeviceSnapshot added with ID: %s", d.serialNumber);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.v(e, "Error adding document");
                    }
                });
    }
}