package com.walkitalki.preview;

import java.util.ArrayList;
import java.util.List;

public final class PreviewAutomationAudit {
    private static final List<String> REQUIRED_STATE_IDS = List.of(
        "permission-blocked",
        "scanning",
        "connecting",
        "ready-to-talk",
        "transmitting",
        "receiving",
        "busy",
        "bluetooth-blocked",
        "disconnected-over-budget"
    );
    private static final List<String> REQUIRED_HOOKS = List.of(
        "data-preview-schema=\"talk-screen-v1\"",
        "data-testid=\"talk-scenario-ready-to-talk\"",
        "data-ptt-state=\"CONNECTED\"",
        "data-permission-action=\"ALLOW\"",
        "data-bluetooth-state=\"BLUETOOTH_OFF\"",
        "data-connection-health=\"DISCONNECTED\"",
        "data-primary-enabled=\"true\"",
        "data-primary-enabled=\"false\"",
        "data-click-budget=\"over\""
    );
    private static final List<String> FORBIDDEN_MARKUP_SNIPPETS = List.of(
        "AA:BB:CC:DD:EE:FF",
        "Alice Personal Phone",
        "11, 12, 13"
    );

    private PreviewAutomationAudit() {
    }

    public static Report auditDefaultScenarios() {
        return audit(TalkScreenPreviewPage.renderDefaultScenarios());
    }

    public static Report audit(String html) {
        List<String> missingRequiredStates = missingRequiredStates(html);
        List<String> missingAutomationHooks = missingAutomationHooks(html);
        List<String> privacyViolations = privacyViolations(html);
        boolean clickBudgetCovered = html.contains("data-click-budget=\"over\"")
                && html.contains("Rollback if connect-to-talk needs more than 3 actions");
        return new Report(
            scenarioCount(html),
            missingRequiredStates,
            missingAutomationHooks,
            privacyViolations,
            clickBudgetCovered
        );
    }

    private static int scenarioCount(String html) {
        int count = 0;
        int index = 0;
        String marker = "data-testid=\"talk-scenario-";
        while ((index = html.indexOf(marker, index)) >= 0) {
            count++;
            index += marker.length();
        }
        return count;
    }

    private static List<String> missingRequiredStates(String html) {
        List<String> missing = new ArrayList<>();
        for (String stateId : REQUIRED_STATE_IDS) {
            if (!html.contains("id=\"" + stateId + "\"")) {
                missing.add(stateId);
            }
        }
        return missing;
    }

    private static List<String> missingAutomationHooks(String html) {
        List<String> missing = new ArrayList<>();
        for (String hook : REQUIRED_HOOKS) {
            if (!html.contains(hook)) {
                missing.add(hook);
            }
        }
        return missing;
    }

    private static List<String> privacyViolations(String html) {
        List<String> violations = new ArrayList<>();
        for (String forbiddenSnippet : FORBIDDEN_MARKUP_SNIPPETS) {
            if (html.contains(forbiddenSnippet)) {
                violations.add("forbidden-sensitive-markup");
            }
        }
        return violations;
    }

    public record Report(
        int scenarioCount,
        List<String> missingRequiredStates,
        List<String> missingAutomationHooks,
        List<String> privacyViolations,
        boolean clickBudgetCovered
    ) {
        public Report {
            missingRequiredStates = List.copyOf(missingRequiredStates);
            missingAutomationHooks = List.copyOf(missingAutomationHooks);
            privacyViolations = List.copyOf(privacyViolations);
        }

        public boolean passed() {
            return scenarioCount >= REQUIRED_STATE_IDS.size()
                    && missingRequiredStates.isEmpty()
                    && missingAutomationHooks.isEmpty()
                    && privacyViolations.isEmpty()
                    && clickBudgetCovered;
        }

        public String renderMarkdown() {
            StringBuilder markdown = new StringBuilder();
            markdown.append("# Talk screen preview automation audit\n");
            markdown.append("result=").append(passed() ? "PASS" : "FAIL").append('\n');
            markdown.append("scenarioCount=").append(scenarioCount).append('\n');
            markdown.append("requiredStates=").append(missingRequiredStates.isEmpty() ? "PASS" : "FAIL").append('\n');
            markdown.append("automationHooks=").append(missingAutomationHooks.isEmpty() ? "PASS" : "FAIL").append('\n');
            markdown.append("privacy=").append(privacyViolations.isEmpty() ? "PASS" : "FAIL").append('\n');
            markdown.append("clickBudget=").append(clickBudgetCovered ? "PASS" : "FAIL").append('\n');
            if (!missingRequiredStates.isEmpty()) {
                markdown.append("missingRequiredStates=").append(missingRequiredStates).append('\n');
            }
            if (!missingAutomationHooks.isEmpty()) {
                markdown.append("missingAutomationHooks=").append(missingAutomationHooks.size()).append('\n');
            }
            if (!privacyViolations.isEmpty()) {
                markdown.append("privacyViolations=").append(privacyViolations.size()).append('\n');
            }
            return markdown.toString();
        }
    }

}