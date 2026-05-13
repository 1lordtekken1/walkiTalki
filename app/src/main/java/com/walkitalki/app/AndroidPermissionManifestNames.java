package com.walkitalki.app;

import android.Manifest;
import com.walkitalki.core.permissions.AndroidPermission;

public final class AndroidPermissionManifestNames {
    public static final String BLUETOOTH = Manifest.permission.BLUETOOTH;
    public static final String BLUETOOTH_ADMIN = Manifest.permission.BLUETOOTH_ADMIN;
    public static final String BLUETOOTH_SCAN = Manifest.permission.BLUETOOTH_SCAN;
    public static final String BLUETOOTH_ADVERTISE = Manifest.permission.BLUETOOTH_ADVERTISE;
    public static final String BLUETOOTH_CONNECT = Manifest.permission.BLUETOOTH_CONNECT;
    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;

    private AndroidPermissionManifestNames() {
    }

    public static String from(AndroidPermission permission) {
        return switch (permission) {
            case BLUETOOTH -> BLUETOOTH;
            case BLUETOOTH_ADMIN -> BLUETOOTH_ADMIN;
            case BLUETOOTH_SCAN -> BLUETOOTH_SCAN;
            case BLUETOOTH_ADVERTISE -> BLUETOOTH_ADVERTISE;
            case BLUETOOTH_CONNECT -> BLUETOOTH_CONNECT;
            case ACCESS_FINE_LOCATION -> ACCESS_FINE_LOCATION;
            case RECORD_AUDIO -> RECORD_AUDIO;
        };
    }
}
