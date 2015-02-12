package com.icitic.core.db.object;

import com.icitic.core.db.object.Name;

public class Person {

    private String name;

    private int device;

    private String deviceName;

    private String gender;

    public Person(String name, int device, String gender) {
        this.name = name;
        this.device = device;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDevice() {
        return device;
    }

    public void setDevice(int device) {
        this.device = device;
    }

    public String getDeviceName() {
        return deviceName;
    }

    @Name(type = "device")
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getGender() {
        return gender;
    }

    @Name(type = "CODE", subType = "01", src = "gender")
    public void setGender(String gender) {
        this.gender = gender;
    }

}
