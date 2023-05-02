package com.robocat.android.rc.gatt;

import java.util.HashMap;

public class BluetoothAttributes {

    private static HashMap<String, String> attributes = new HashMap<>();

    public static final String serviceUUID = "75cececa-bf0a-11ed-a712-f7b97125a3fb";
    public static final String handshakeUUID = "26e77f82-c090-11ed-bd7b-0752602678c8";
    public static final String networkIdUUID = "36960d20-c090-11ed-8d57-9b0594765e6b";
    public static final String networkSecretUUID = "303bc6ce-c090-11ed-9eee-d346786b4c24";
    public static final String leftMotorControlUUID = "c092";
    public static final String rightMotorControlUUID = "11ed";

    static {
        attributes.put(serviceUUID, "Jerry Service");
        attributes.put(handshakeUUID, "Handshake");
        attributes.put(networkIdUUID, "Network Id");
        attributes.put(networkSecretUUID, "Network Secret");
        attributes.put(leftMotorControlUUID, "Left Motor Control");
        attributes.put(rightMotorControlUUID, "Right Motor Control");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return (null == name) ? defaultName : name;
    }
}
