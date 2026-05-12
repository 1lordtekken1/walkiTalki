package com.walkitalki.core.permissions;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public final class PermissionPolicy {
    private PermissionPolicy() {
    }

    public static Decision evaluate(PlatformVersion platform, PermissionGrantState grants, UseCase useCase) {
        EnumSet<AndroidPermission> required = requiredPermissions(platform, useCase);
        if (grants.containsAll(required)) {
            return new Decision(PermissionAction.ALLOW, Collections.unmodifiableSet(required));
        }

        if (required.contains(AndroidPermission.RECORD_AUDIO) && !grants.has(AndroidPermission.RECORD_AUDIO)) {
            return new Decision(PermissionAction.REQUEST_MICROPHONE, Collections.unmodifiableSet(required));
        }
        if (required.contains(AndroidPermission.ACCESS_FINE_LOCATION) && !grants.has(AndroidPermission.ACCESS_FINE_LOCATION)) {
            return new Decision(PermissionAction.REQUEST_LOCATION_FOR_LEGACY_DISCOVERY, Collections.unmodifiableSet(required));
        }
        return new Decision(PermissionAction.REQUEST_NEARBY_DEVICES, Collections.unmodifiableSet(required));
    }

    private static EnumSet<AndroidPermission> requiredPermissions(PlatformVersion platform, UseCase useCase) {
        EnumSet<AndroidPermission> required = EnumSet.noneOf(AndroidPermission.class);
        switch (useCase) {
            case SCAN_FOR_PEERS -> {
                if (platform.usesNearbyDevicesPermissions()) {
                    required.add(AndroidPermission.BLUETOOTH_SCAN);
                    required.add(AndroidPermission.BLUETOOTH_CONNECT);
                } else {
                    required.add(AndroidPermission.BLUETOOTH);
                    required.add(AndroidPermission.BLUETOOTH_ADMIN);
                    required.add(AndroidPermission.ACCESS_FINE_LOCATION);
                }
            }
            case CONNECT_TO_PEER -> {
                if (platform.usesNearbyDevicesPermissions()) {
                    required.add(AndroidPermission.BLUETOOTH_CONNECT);
                } else {
                    required.add(AndroidPermission.BLUETOOTH);
                }
            }
            case RECEIVE_AUDIO -> {
                if (platform.usesNearbyDevicesPermissions()) {
                    required.add(AndroidPermission.BLUETOOTH_CONNECT);
                } else {
                    required.add(AndroidPermission.BLUETOOTH);
                }
            }
            case TRANSMIT_AUDIO -> {
                if (platform.usesNearbyDevicesPermissions()) {
                    required.add(AndroidPermission.BLUETOOTH_CONNECT);
                } else {
                    required.add(AndroidPermission.BLUETOOTH);
                }
                required.add(AndroidPermission.RECORD_AUDIO);
            }
        }
        return required;
    }

    public record Decision(PermissionAction action, Set<AndroidPermission> requiredPermissions) {
    }
}
