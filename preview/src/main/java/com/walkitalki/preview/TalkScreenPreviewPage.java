package com.walkitalki.preview;

import com.walkitalki.core.bluetooth.BluetoothEnvironmentState;
import com.walkitalki.core.connection.ConnectionHealth;
import com.walkitalki.core.domain.PttState;
import com.walkitalki.core.permissions.PermissionAction;
import com.walkitalki.core.ui.TalkScreenPresenter;
import com.walkitalki.core.ui.TalkScreenState;

import java.util.List;

public final class TalkScreenPreviewPage {
    private TalkScreenPreviewPage() {
    }

    public static String renderDefaultScenarios() {
        return render(List.of(
            scenarioFor(
                "permission-blocked",
                PttState.IDLE,
                PermissionAction.REQUEST_NEARBY_DEVICES,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.STABLE,
                1,
                3
            ),
            scenarioFor(
                "scanning",
                PttState.SCANNING,
                PermissionAction.ALLOW,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.STABLE,
                1,
                3
            ),
            scenarioFor(
                "connecting",
                PttState.CONNECTING,
                PermissionAction.ALLOW,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.STABLE,
                2,
                3
            ),
            scenarioFor(
                "ready-to-talk",
                PttState.CONNECTED,
                PermissionAction.ALLOW,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.STABLE,
                2,
                3
            ),
            scenarioFor(
                "transmitting",
                PttState.TRANSMITTING,
                PermissionAction.ALLOW,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.STABLE,
                3,
                3
            ),
            scenarioFor(
                "receiving",
                PttState.RECEIVING,
                PermissionAction.ALLOW,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.STABLE,
                2,
                3
            ),
            scenarioFor(
                "busy",
                PttState.BUSY,
                PermissionAction.ALLOW,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.STABLE,
                3,
                3
            ),
            scenarioFor(
                "bluetooth-blocked",
                PttState.IDLE,
                PermissionAction.ALLOW,
                BluetoothEnvironmentState.BLUETOOTH_OFF,
                ConnectionHealth.STABLE,
                1,
                3
            ),
            scenarioFor(
                "disconnected-over-budget",
                PttState.DISCONNECTED,
                PermissionAction.ALLOW,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.DISCONNECTED,
                4,
                3
            )
        ));
    }

    public static String render(List<Scenario> scenarios) {
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"utf-8\">\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        html.append("  <title>walkiTalki Talk Screen Preview</title>\n");
        html.append("  <style>");
        html.append("body{font-family:system-ui,sans-serif;background:#101820;color:#f7fbff;margin:0;padding:24px;}");
        html.append("main{display:grid;gap:16px;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));}");
        html.append("article{border:1px solid #395166;border-radius:20px;padding:20px;background:#172633;box-shadow:0 12px 30px #0004;}");
        html.append("article[data-click-budget='over']{border-color:#ffb84d;}");
        html.append("button{border:0;border-radius:999px;padding:12px 18px;font-weight:700;background:#65d6ad;color:#052018;}");
        html.append("button[disabled]{background:#657585;color:#dce5ec;}");
        html.append("small{display:block;color:#a9bccb;margin-top:12px;}");
        html.append("  </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("  <h1>walkiTalki talk screen preview</h1>\n");
        html.append("  <p>Static browser surface rendered from framework-free core state.</p>\n");
        html.append("  <main data-preview-schema=\"talk-screen-v1\">\n");
        for (Scenario scenario : scenarios) {
            appendScenario(html, scenario);
        }
        html.append("  </main>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        return html.toString();
    }

    public static Scenario scenario(String id, TalkScreenState state) {
        return new Scenario(id, state, null, null, null, null);
    }

    public static Scenario scenarioFor(
        String id,
        PttState pttState,
        PermissionAction permissionAction,
        BluetoothEnvironmentState bluetoothState,
        ConnectionHealth connectionHealth,
        int userActions,
        int clickBudget
    ) {
        TalkScreenState state = TalkScreenPresenter.render(
            pttState,
            permissionAction,
            bluetoothState,
            connectionHealth,
            userActions,
            clickBudget
        );
        return new Scenario(id, state, pttState, permissionAction, bluetoothState, connectionHealth);
    }

    private static void appendScenario(StringBuilder html, Scenario scenario) {
        TalkScreenState state = scenario.state();
        String clickBudget = state.withinClickBudget() ? "within" : "over";
        html.append("    <article id=\"").append(escape(scenario.id())).append("\"");
        html.append(" data-testid=\"talk-scenario-").append(escape(scenario.id())).append("\"");
        appendOptionalDataAttribute(html, "ptt-state", scenario.pttState());
        appendOptionalDataAttribute(html, "permission-action", scenario.permissionAction());
        appendOptionalDataAttribute(html, "bluetooth-state", scenario.bluetoothState());
        appendOptionalDataAttribute(html, "connection-health", scenario.connectionHealth());
        html.append(" data-primary-enabled=\"").append(state.pushToTalkEnabled()).append("\"");
        html.append(" data-click-budget=\"").append(clickBudget).append("\"");
        html.append(" data-diagnostics=\"").append(escape(state.diagnosticsSignal())).append("\">\n");
        html.append("      <h2>").append(escape(state.statusTitle())).append("</h2>\n");
        html.append("      <button type=\"button\" aria-label=\"").append(escape(state.primaryActionLabel())).append("\"");
        if (!state.pushToTalkEnabled()) {
            html.append(" disabled");
        }
        html.append(">").append(escape(state.primaryActionLabel())).append("</button>\n");
        html.append("      <small>").append(escape(state.acceptanceCriteria())).append("</small>\n");
        if (state.diagnosticsVisible()) {
            html.append("      <small>Diagnostics: ").append(escape(state.diagnosticsSignal())).append("</small>\n");
        }
        if (!state.withinClickBudget()) {
            html.append("      <small>").append(escape(state.rollbackTrigger())).append("</small>\n");
        }
        html.append("    </article>\n");
    }

    private static void appendOptionalDataAttribute(StringBuilder html, String name, Object value) {
        if (value != null) {
            html.append(" data-").append(name).append("=\"").append(escape(value.toString())).append("\"");
        }
    }

    private static String escape(String value) {
        return value
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }

    public record Scenario(
        String id,
        TalkScreenState state,
        PttState pttState,
        PermissionAction permissionAction,
        BluetoothEnvironmentState bluetoothState,
        ConnectionHealth connectionHealth
    ) {
    }
}
