package com.walkitalki.core.permissions;

public record PlatformVersion(int androidApi) {
    private static final int ANDROID_12_API = 31;

    public PlatformVersion {
        if (androidApi < 1) {
            throw new IllegalArgumentException("androidApi must be positive");
        }
    }

    public static PlatformVersion androidApi(int androidApi) {
        return new PlatformVersion(androidApi);
    }

    public boolean usesNearbyDevicesPermissions() {
        return androidApi >= ANDROID_12_API;
    }
}
