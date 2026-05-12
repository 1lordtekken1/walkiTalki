package com.walkitalki.app;

public final class MvpDebugArtifact {
    private MvpDebugArtifact() {
    }

    public static String apkFileName() {
        return "walkitalki-mvp-debug.apk";
    }

    public static String relativeApkPath() {
        return "artifacts/apk/" + apkFileName();
    }
}
