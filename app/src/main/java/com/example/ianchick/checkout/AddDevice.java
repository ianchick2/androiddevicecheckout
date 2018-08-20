package com.example.ianchick.checkout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddDevice extends AppCompatActivity {

    private static final String TAG = "AddDevice";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        setTitle("Add a new device");

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_device_save:
                String deviceName = ((EditText) findViewById(R.id.add_device_title)).getText().toString();
                String serial = ((EditText) findViewById(R.id.add_device_serial)).getText().toString();
                String imageRef = ((EditText) findViewById(R.id.add_device_imageref)).getText().toString();

                if(!TextUtils.isEmpty(deviceName)) {
                    if (!TextUtils.isEmpty(serial)) {
                        Device device = new Device(deviceName, serial);
                        device.imageRef = imageRef;
                        updateDatabase(device);
                        finish();
                    } else {
                        ((TextView) findViewById(R.id.add_device_error_message)).setText("Please enter a serial number for the device");
                    }
                } else {
                    ((TextView) findViewById(R.id.add_device_error_message)).setText("Please enter a device name");
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

        db.collection("devices").document(d.serialNumber)
                .set(device)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.v(TAG, "DeviceSnapshot added with ID: " + d.serialNumber);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v(TAG, "Error adding document", e);
                    }
                });
    }
}
