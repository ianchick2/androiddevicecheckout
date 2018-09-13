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
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class EditDeviceActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String user;
    private String deviceTitle;
    private int deviceOs;
    private String deviceSerial;
    private String deviceImageRef;
    private String deviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_device);
        setTitle("Edit a new device");

        deviceTitle = getIntent().getStringExtra("deviceTitle");
        deviceSerial = getIntent().getStringExtra("deviceSerial");
        deviceOs = getIntent().getIntExtra("deviceOs", 0);
        deviceImageRef = getIntent().getStringExtra("deviceImageRef");
        deviceType = getIntent().getStringExtra("deviceType");
        user = getIntent().getStringExtra("deviceUser");

        ((EditText) findViewById(R.id.edit_device_title)).setText(deviceTitle);
        ((EditText) findViewById(R.id.edit_device_serial)).setText(deviceSerial);
        ((EditText) findViewById(R.id.edit_device_os_version)).setText(String.valueOf(deviceOs));
        ((EditText) findViewById(R.id.edit_device_imageref)).setText(deviceImageRef);
        ((EditText) findViewById(R.id.edit_device_device_type)).setText(deviceType);

        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        deviceTitle = ((EditText) findViewById(R.id.edit_device_title)).getText().toString();
        deviceSerial = ((EditText) findViewById(R.id.edit_device_serial)).getText().toString();
        String os = ((EditText) findViewById(R.id.edit_device_os_version)).getText().toString();
        deviceOs = Integer.valueOf(os);
        deviceImageRef = ((EditText) findViewById(R.id.edit_device_imageref)).getText().toString();
        deviceType = ((EditText) findViewById(R.id.edit_device_device_type)).getText().toString();

        switch (item.getItemId()) {
            case R.id.edit_device_save:

                if (!TextUtils.isEmpty(deviceTitle)) {
                    if (!TextUtils.isEmpty(deviceSerial)) {
                        Device device = new Device(deviceTitle, deviceSerial);
                        device.os = deviceOs;
                        device.imageRef = deviceImageRef;
                        device.setUserName(user);
                        if (!TextUtils.isEmpty(user)) {
                            device.setCheckedOut(true);
                        }
                        updateDatabase(device);
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
                        Timber.d("DeviceSnapshot added with ID: %s", d.serialNumber);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.d(e, "Error adding document");
                    }
                });
    }
}
