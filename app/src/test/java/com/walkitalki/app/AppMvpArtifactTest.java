package com.walkitalki.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class AppMvpArtifactTest {
    @Test
    public void mvpDebugApkArtifactNameIsStableForManualTesting() {
        assertEquals("walkitalki-mvp-debug.apk", MvpDebugArtifact.apkFileName());
        assertEquals("artifacts/apk/walkitalki-mvp-debug.apk", MvpDebugArtifact.relativeApkPath());
    }
}
