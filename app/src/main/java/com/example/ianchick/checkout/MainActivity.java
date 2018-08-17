package com.example.ianchick.checkout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static ArrayList<Device> DEVICE_LIST = new ArrayList<>(Arrays.asList(
            new Device("Moto G (3rd gen)", "ZY22242CRK"),
            new Device("Samsung Galaxy S5", "RF1G6073JQY"),
            new Device("CAT S41", "S411747041352")
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView deviceListView = findViewById(R.id.device_list);
        final DeviceAdapter deviceAdapter = new DeviceAdapter(this, DEVICE_LIST);
        deviceListView.setAdapter(deviceAdapter);

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, int position, long id) {
                final Device device = deviceAdapter.getItem(position);
                view.setSelected(true);

                if (device.isCheckedOut) {
                    device.checkIn();
                    deviceAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Device Returned. Android Fam thanks you!", Toast.LENGTH_SHORT).show();

                } else {
                    showChangeLangDialog(device, deviceAdapter);
                }
            }
        });
    }

    public void showChangeLangDialog(final Device device, final DeviceAdapter deviceAdapter) {
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
                            deviceAdapter.notifyDataSetChanged();
                            inputUserDialog.dismiss();
                        }
                    }
                });
            }
        });
        inputUserDialog.show();
    }
}
