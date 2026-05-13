package com.walkitalki.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class AppTalkViewModelTest {
    @Test
    public void primaryActionAndPttIntentsDriveUiStateWithoutFrameworkCalls() {
        AppTalkViewModel viewModel = AppTalkViewModel.createMvpDemoModel();

        assertEquals("SCAN", viewModel.state().buttonLabel());
        assertEquals("ui_idle", viewModel.state().diagnosticsSignal());

        viewModel.onIntent(AppTalkUiIntent.PRIMARY_ACTION);
        assertEquals("...", viewModel.state().buttonLabel());
        assertEquals("ui_scanning", viewModel.state().diagnosticsSignal());

        viewModel.onIntent(AppTalkUiIntent.PRIMARY_ACTION);
        assertEquals("PTT", viewModel.state().buttonLabel());
        assertTrue(viewModel.state().pushToTalkEnabled());

        viewModel.onIntent(AppTalkUiIntent.PUSH_TO_TALK_DOWN);
        assertEquals("TALK", viewModel.state().buttonLabel());
        assertTrue(viewModel.state().transmitting());

        viewModel.onIntent(AppTalkUiIntent.PUSH_TO_TALK_UP);
        assertEquals("PTT", viewModel.state().buttonLabel());
        assertFalse(viewModel.state().transmitting());
    }

    @Test
    public void pttDownIsIgnoredUntilPresenterEnablesPushToTalk() {
        AppTalkViewModel viewModel = AppTalkViewModel.createMvpDemoModel();

        viewModel.onIntent(AppTalkUiIntent.PUSH_TO_TALK_DOWN);

        assertEquals("SCAN", viewModel.state().buttonLabel());
        assertFalse(viewModel.state().transmitting());
        assertEquals("ui_idle", viewModel.state().diagnosticsSignal());
    }

    @Test
    public void supportReportRemainsRedactedAfterUiIntents() {
        AppTalkViewModel viewModel = AppTalkViewModel.createMvpDemoModel();

        viewModel.onIntent(AppTalkUiIntent.PRIMARY_ACTION);
        viewModel.onIntent(AppTalkUiIntent.PRIMARY_ACTION);
        viewModel.onIntent(AppTalkUiIntent.PUSH_TO_TALK_DOWN);
        viewModel.onIntent(AppTalkUiIntent.PUSH_TO_TALK_UP);

        String report = viewModel.supportReport().renderMarkdown().toLowerCase();
        assertTrue(report.contains("ui_transmitting"));
        assertFalse(report.contains("mac"));
        assertFalse(report.contains("raw peer"));
        assertFalse(report.contains("audio payload"));
    }

    @Test
    public void lifecycleHooksRecordCoarseSignalsWithoutDuplicatingStart() {
        AppTalkViewModel viewModel = AppTalkViewModel.createMvpDemoModel();

        viewModel.onStart();
        viewModel.onStart();
        viewModel.onIntent(AppTalkUiIntent.PRIMARY_ACTION);
        viewModel.onStop();

        String report = viewModel.supportReport().renderMarkdown();
        assertEquals(1, countOccurrences(report, "ui_lifecycle:start:ui_idle"));
        assertEquals(1, countOccurrences(report, "ui_lifecycle:stop:ui_scanning"));
        assertFalse(report.toLowerCase().contains("mac"));
        assertFalse(report.toLowerCase().contains("audio payload"));
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int index = 0;
        while ((index = haystack.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }
}
