package com.example.ianchick.checkout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private DeviceAdapter deviceAdapter;
    private ListView deviceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SwipeRefreshLayout swipeLayout = findViewById(R.id.swipe_refresh_wrapper);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateDeviceList();
                swipeLayout.setRefreshing(false);
            }
        });

        db = FirebaseFirestore.getInstance();

        updateDeviceList();

        deviceListView = findViewById(R.id.device_list);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, int position, long id) {
                final Device device = deviceAdapter.getItem(position);
                view.setSelected(true);

                if (device.isCheckedOut()) {
                    device.checkIn();
                    updateDatabase(device);
                    deviceAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Device Returned. Android Fam thanks you!", Toast.LENGTH_SHORT).show();

                } else {
                    showCheckoutDialog(device, deviceAdapter);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_device:
                Log.v(TAG, "Add device");
                Intent intent = new Intent(this, AddDevice.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showCheckoutDialog(final Device device, final DeviceAdapter deviceAdapter) {
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.input_user_name, null);
        final AlertDialog inputUserDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Checking out device")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        final EditText inputName = dialogView.findViewById(R.id.input_user_name_edit);

        inputUserDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = inputUserDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (inputName.getText().toString().isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
                        } else {
                            device.checkOut(inputName.getText().toString());
                            updateDatabase(device);
                            deviceAdapter.notifyDataSetChanged();
                            inputUserDialog.dismiss();
                        }
                    }
                });
            }
        });
        inputUserDialog.show();
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

    private void updateDeviceList() {
        final ArrayList<Device> deviceList = new ArrayList<>();
        db.collection("devices").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    QuerySnapshot query = (QuerySnapshot) task.getResult();
                    List<DocumentSnapshot> documents = query.getDocuments();
                    for (DocumentSnapshot document : documents) {
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();
                            if (data.get("title") != null & data.get("serial") != null) {
                                Device device = new Device(data.get("title").toString(), data.get("serial").toString());
                                if (data.get("user") != null) {
                                    device.setUserName(data.get("user").toString());
                                } else {
                                    device.setUserName("");
                                }
                                if (data.get("isCheckedOut") != null) {
                                    device.setCheckedOut(data.get("isCheckedOut").toString());
                                } else {
                                    device.setCheckedOut(false);
                                }
                                if (data.get("imageRef") != null) {
                                    device.imageRef = data.get("imageRef").toString();
                                }
                                deviceList.add(device);
                            }

                            deviceAdapter = new DeviceAdapter(getApplicationContext(), deviceList);
                            deviceListView.setAdapter(deviceAdapter);

                            Log.d(TAG, "DocumentSnapshot data: " + data);
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    }
                } else {
                    Log.d(TAG, "failed query with ", task.getException());
                }
            }
        });
    }
}
