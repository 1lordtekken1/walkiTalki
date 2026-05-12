package com.walkitalki.core.permissions;

import java.util.EnumSet;
import java.util.Set;

public final class PermissionGrantState {
    private final EnumSet<AndroidPermission> granted;

    private PermissionGrantState(EnumSet<AndroidPermission> granted) {
        this.granted = granted.clone();
    }

    public static PermissionGrantState none() {
        return new PermissionGrantState(EnumSet.noneOf(AndroidPermission.class));
    }

    public static PermissionGrantState granted(AndroidPermission first, AndroidPermission... rest) {
        EnumSet<AndroidPermission> permissions = EnumSet.of(first, rest);
        return new PermissionGrantState(permissions);
    }

    public static PermissionGrantState granted(Set<AndroidPermission> permissions) {
        EnumSet<AndroidPermission> copy = permissions.isEmpty()
            ? EnumSet.noneOf(AndroidPermission.class)
            : EnumSet.copyOf(permissions);
        return new PermissionGrantState(copy);
    }

    public boolean has(AndroidPermission permission) {
        return granted.contains(permission);
    }

    public boolean containsAll(Set<AndroidPermission> permissions) {
        return granted.containsAll(permissions);
    }
}
