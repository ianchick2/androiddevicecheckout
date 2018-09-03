package com.example.ianchick.checkout.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.example.ianchick.checkout.OnRecyclerViewItemClickListener;
import com.example.ianchick.checkout.adapters.ListDevicesAdapter;
import com.example.ianchick.checkout.R;
import com.example.ianchick.checkout.models.Device;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements OnRecyclerViewItemClickListener {

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private ListDevicesAdapter deviceAdapter;
    private ArrayList<Device> deviceList = new ArrayList<>();

    private RecyclerView deviceRecyclerView;

    private Chronometer lastUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastUpdated = findViewById(R.id.last_updated_status_bar);
        lastUpdated.start();
        lastUpdated.setFormat("Last updated %s ago");

        final SwipeRefreshLayout swipeLayout = findViewById(R.id.swipe_refresh_wrapper);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateDeviceList(deviceAdapter);
                swipeLayout.setRefreshing(false);
                lastUpdated.setBase(SystemClock.elapsedRealtime());
            }
        });

        db = FirebaseFirestore.getInstance();

        deviceRecyclerView = findViewById(R.id.device_recycler_view);
        deviceAdapter = new ListDevicesAdapter(deviceList);
        deviceAdapter.setOnRecyclerViewItemClickListener(this);
        updateDeviceList(deviceAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressLint("LogNotTimber")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_device:
                Log.v(TAG, "Add device");
                Intent intent = new Intent(this, AddDevice.class);
                startActivity(intent);
                return true;
//            case R.id.filter_list:
//                deviceAdapter.getFilter().filter("Filter");
//                return true;
//            case R.id.sort_list:
//                deviceAdapter.sortList();
//                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showCheckoutDialog(final Device device, final ListDevicesAdapter deviceAdapter) {
        final SharedPreferences sharedPref = this.getSharedPreferences("CheckoutPrefs", Context.MODE_PRIVATE);
        final Set<String> users = sharedPref.getStringSet("Users", new HashSet<String>());

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.input_user_name, null);
        final AlertDialog inputUserDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Checking out device")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(users));
        final AutoCompleteTextView inputName = dialogView.findViewById(R.id.input_user_name_edit);
        inputName.setAdapter(adapter);

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

                            users.add(inputName.getText().toString());
                            sharedPref.edit().putStringSet("Users", users).apply();

                            inputUserDialog.dismiss();
                        }
                    }
                });
            }
        });
        inputUserDialog.show();
    }

    private void showCheckInDialog(final Device device, final ListDevicesAdapter deviceAdapter) {
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.checkin_confirmation_dialog, null);
        final AlertDialog inputUserDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Are you sure you want to return this device? ")
                .setMessage(device.deviceName)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        inputUserDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = inputUserDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        device.checkIn();
                        updateDatabase(device);
                        deviceAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "Device Returned. Android Fam thanks you!", Toast.LENGTH_SHORT).show();
                        inputUserDialog.dismiss();
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
                    @SuppressLint("LogNotTimber")
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.v(TAG, "DeviceSnapshot added with ID: " + d.serialNumber);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("LogNotTimber")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v(TAG, "Error adding document", e);
                    }
                });
    }

    private void updateDeviceList(final ListDevicesAdapter deviceAdapter) {
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
                            Log.v(TAG, "DocumentSnapshot data: " + data);
                        } else {
                            Log.v(TAG, "No such document");
                        }
                    }
                    deviceRecyclerView.setAdapter(deviceAdapter);
                    deviceRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                } else {
                    Log.v(TAG, "failed query with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onItemClick(int position, View view) {
        Device device = deviceList.get(position);
        if (deviceList.get(position).isCheckedOut()) {
            showCheckInDialog(device, deviceAdapter);
        } else {
            showCheckoutDialog(device, deviceAdapter);
        }
    }
}
