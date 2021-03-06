package com.example.ianchick.checkout.models;

import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Created by ianchick on 8/16/18
 */
public class Device implements Comparable<Device> {
    public String deviceName;
    public String serialNumber;
    public String imageRef;
    private String userName;
    public int os;
    public String type;
    private boolean isCheckedOut;

    public Device(String deviceName, String serialNumber) {
        this.deviceName = deviceName;
        this.serialNumber = serialNumber;
        this.isCheckedOut = false;
    }

    public void checkOut(String userName) {
        this.isCheckedOut = true;
        setUserName(userName);
    }

    public void checkIn() {
        this.isCheckedOut = false;
        this.userName = "";
    }

    public void setCheckedOut(String checkedOut) {
        isCheckedOut = Boolean.valueOf(checkedOut);
    }

    public void setCheckedOut(boolean checkedOut) {
        isCheckedOut = checkedOut;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        if (TextUtils.isEmpty(userName)) {
            this.userName = "";
        } else {
            this.userName = userName;
        }
    }

    public Boolean isCheckedOut() {
        return this.isCheckedOut;
    }


    @Override
    public int compareTo(@NonNull Device device) {
        return this.deviceName.compareTo(device.deviceName);
    }
}
