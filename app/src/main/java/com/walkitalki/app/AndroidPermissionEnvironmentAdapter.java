package com.walkitalki.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import com.walkitalki.core.bluetooth.BluetoothEnvironmentState;
import com.walkitalki.core.permissions.AndroidPermission;
import com.walkitalki.core.permissions.PermissionAction;
import com.walkitalki.core.permissions.PermissionGrantState;
import com.walkitalki.core.permissions.PermissionPolicy;
import com.walkitalki.core.permissions.PlatformVersion;
import com.walkitalki.core.permissions.UseCase;
import java.util.EnumSet;

public final class AndroidPermissionEnvironmentAdapter {
    private final PlatformVersion platformVersion;
    private final PermissionChecker permissionChecker;
    private final boolean bluetoothEnabled;

    public AndroidPermissionEnvironmentAdapter(int androidApi, PermissionChecker permissionChecker, boolean bluetoothEnabled) {
        this.platformVersion = PlatformVersion.androidApi(androidApi);
        this.permissionChecker = permissionChecker;
        this.bluetoothEnabled = bluetoothEnabled;
    }

    public static AndroidPermissionEnvironmentAdapter fromContext(Context context, boolean bluetoothEnabled) {
        return new AndroidPermissionEnvironmentAdapter(
                Build.VERSION.SDK_INT,
                permissionName -> context.checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED,
                bluetoothEnabled);
    }

    public PermissionPolicy.Decision decisionFor(UseCase useCase) {
        return PermissionPolicy.evaluate(platformVersion, grantState(), useCase);
    }

    public BluetoothEnvironmentState bluetoothStateFor(UseCase useCase) {
        if (!bluetoothEnabled) {
            return BluetoothEnvironmentState.BLUETOOTH_OFF;
        }
        if (decisionFor(useCase).action() != PermissionAction.ALLOW) {
            return BluetoothEnvironmentState.MISSING_PERMISSION;
        }
        return BluetoothEnvironmentState.READY;
    }

    public String diagnosticsSignalFor(UseCase useCase) {
        if (!bluetoothEnabled) {
            return "bluetooth:off";
        }
        PermissionAction action = decisionFor(useCase).action();
        return switch (action) {
            case ALLOW -> "permission:ready";
            case REQUEST_MICROPHONE -> "permission:microphone_missing";
            case REQUEST_LOCATION_FOR_LEGACY_DISCOVERY -> "permission:legacy_location_missing";
            case REQUEST_NEARBY_DEVICES -> "permission:nearby_devices_missing";
        };
    }

    private PermissionGrantState grantState() {
        EnumSet<AndroidPermission> granted = EnumSet.noneOf(AndroidPermission.class);
        for (AndroidPermission permission : AndroidPermission.values()) {
            if (permissionChecker.isGranted(AndroidPermissionManifestNames.from(permission))) {
                granted.add(permission);
            }
        }
        return PermissionGrantState.granted(granted);
    }

    public interface PermissionChecker {
        boolean isGranted(String permissionName);
    }
}
