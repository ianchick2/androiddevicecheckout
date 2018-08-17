package com.example.ianchick.checkout;

import android.text.TextUtils;

/**
 * Created by ianchick on 8/16/18
 */
public class Device {
    public String deviceName;
    public String serialNumber;
    public String userName;
    public boolean isCheckedOut;
    public int imageId;

    public Device(String deviceName, String serialNumber) {
        this.deviceName = deviceName;
        this.serialNumber = serialNumber;
        this.isCheckedOut = false;
    }

    public void checkOut(String userName) {
        this.isCheckedOut = true;
        if (TextUtils.isEmpty(userName)) {
            this.userName = "";
        } else {
            this.userName = userName;
        }
    }

    public void checkIn() {
        this.isCheckedOut = false;
        this.userName = "";
    }
}
