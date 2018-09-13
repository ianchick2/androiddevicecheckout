package com.example.ianchick.checkout.activities;

import android.app.SearchManager;
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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.example.ianchick.checkout.BuildConfig;
import com.example.ianchick.checkout.OnRecyclerViewItemClickListener;
import com.example.ianchick.checkout.OnRecyclerViewItemLongClickListener;
import com.example.ianchick.checkout.R;
import com.example.ianchick.checkout.adapters.ListDevicesAdapter;
import com.example.ianchick.checkout.models.Device;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements OnRecyclerViewItemClickListener, OnRecyclerViewItemLongClickListener {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListDevicesAdapter deviceAdapter;
    private ArrayList<Device> deviceList = new ArrayList<>();
    private RecyclerView deviceRecyclerView;
    private Chronometer lastUpdated;

    private SearchView searchView;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        auth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Timber.d("signInAnonymously:success");
                            FirebaseUser user = auth.getCurrentUser();
                        } else {
                            Timber.w(task.getException(), "signInAnonymously:failure");
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        lastUpdated = findViewById(R.id.last_updated_status_bar);
        lastUpdated.start();
        lastUpdated.setFormat("Last updated %s ago");

        final SwipeRefreshLayout swipeLayout = findViewById(R.id.swipe_refresh_wrapper);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                deviceList = new ArrayList<>();
                updateDeviceList();
                swipeLayout.setRefreshing(false);
                lastUpdated.setBase(SystemClock.elapsedRealtime());
            }
        });

        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        deviceRecyclerView = findViewById(R.id.device_recycler_view);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(deviceRecyclerView.getContext(), LinearLayoutManager.VERTICAL);
        deviceRecyclerView.addItemDecoration(dividerItemDecoration);

        deviceAdapter = new ListDevicesAdapter(this, deviceList);
        deviceAdapter.setOnRecyclerViewItemClickListener(this);
        deviceRecyclerView.setAdapter(deviceAdapter);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        updateDeviceList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                deviceAdapter.getFilter().filter(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                deviceAdapter.getFilter().filter(s);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_device:
                Timber.d("Add device");
                Intent intent = new Intent(this, EditDeviceActivity.class);
                startActivity(intent);
                return true;
            case R.id.search:
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    private void showCheckoutDialog(final Device device) {
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

    private void showCheckInDialog(final Device device) {
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
        device.put("type", d.type);
        device.put("isCheckedOut", d.isCheckedOut());
        device.put("os", d.os);

        db.collection("devices").document(d.serialNumber)
                .set(device)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Timber.d("DeviceSnapshot added with ID: %s", d.serialNumber);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.d(e, "Error adding document");
                    }
                });
    }

    private void updateDeviceList() {
        deviceAdapter = new ListDevicesAdapter(this, deviceList);
        deviceAdapter.setOnRecyclerViewItemClickListener(this);
        deviceAdapter.setOnRecyclerViewItemLongClickListener(this);
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
                                if (data.get("os") != null) {
                                    device.os = Integer.parseInt(data.get("os").toString());
                                }
                                if (data.get("type") != null) {
                                    device.type = data.get("type").toString();
                                }
                                deviceList.add(device);
                            }
                            Timber.d("DocumentSnapshot data: %s", data);
                        } else {
                            Timber.d("No such document");
                        }
                    }
                    Collections.sort(deviceList);
                    deviceRecyclerView.setAdapter(deviceAdapter);
                } else {
                    Timber.v(task.getException(), "failed query with ");
                }
            }
        });
    }

    @Override
    public void onItemClick(String id) {
        Device device = null;
        for (Device d : deviceList) {
            if (d.serialNumber.equals(id)) {
                device = d;
            }
        }
        if (device != null) {
            if (device.isCheckedOut()) {
                showCheckInDialog(device);
            } else {
                showCheckoutDialog(device);
            }
        }
    }

    @Override
    public void onItemLongClick(String id) {
        Intent intent = new Intent(this, EditDeviceActivity.class);
        Device device = null;
        for (Device d : deviceList) {
            if (d.serialNumber.equals(id)) {
                device = d;
            }
        }
        if (device != null) {
            intent.putExtra("deviceTitle", device.deviceName);
            intent.putExtra("deviceSerial", device.serialNumber);
            intent.putExtra("deviceImageRef", device.imageRef);
            intent.putExtra("deviceType", device.type);
            intent.putExtra("deviceOs", device.os);
            intent.putExtra("isCheckedOut", device.isCheckedOut());
            intent.putExtra("deviceUser", device.getUserName());
        }
        startActivity(intent);
    }
}
