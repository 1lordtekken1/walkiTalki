package com.walkitalki.core.simulation;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class SimulationScenarioReportRenderer {
    private SimulationScenarioReportRenderer() {
    }

    public static String renderMarkdown() {
        List<SimulationScenario> scenarios = SimulationScenarioCatalog.all();
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Simulation scenario catalog\n\n");
        markdown.append("Total scenarios: ").append(scenarios.size()).append("\n\n");
        appendCategorySummary(markdown, scenarios);
        appendScenarioTable(markdown, scenarios);
        return markdown.toString();
    }

    private static void appendCategorySummary(StringBuilder markdown, List<SimulationScenario> scenarios) {
        Map<String, Integer> counts = new TreeMap<>();
        for (SimulationScenario scenario : scenarios) {
            counts.merge(scenario.category(), 1, Integer::sum);
        }

        markdown.append("## Category summary\n\n");
        markdown.append("| Category | Scenarios |\n");
        markdown.append("|---|---:|\n");
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            markdown
                .append("| ").append(entry.getKey())
                .append(" | ").append(entry.getValue())
                .append(" |\n");
        }
        markdown.append('\n');
    }

    private static void appendScenarioTable(StringBuilder markdown, List<SimulationScenario> scenarios) {
        markdown.append("## Scenarios\n\n");
        markdown.append("| # | ID | Stakeholder | Category | Situation | Expected UX | Final state | User message | Diagnostics signal |\n");
        markdown.append("|---:|---|---|---|---|---|---|---|---|\n");
        for (int index = 0; index < scenarios.size(); index++) {
            SimulationScenario scenario = scenarios.get(index);
            markdown
                .append("| ").append(index + 1)
                .append(" | `").append(escape(scenario.id())).append('`')
                .append(" | ").append(escape(scenario.stakeholder()))
                .append(" | ").append(escape(scenario.category()))
                .append(" | ").append(escape(scenario.situation()))
                .append(" | ").append(escape(scenario.expectedUx()))
                .append(" | ").append(scenario.result().state())
                .append(" | ").append(escape(scenario.result().userMessage()))
                .append(" | `").append(escape(scenario.diagnosticsSignal())).append('`')
                .append(" |\n");
        }
    }

    private static String escape(String value) {
        return value
            .replace("|", "\\|")
            .replace("\n", " ");
    }
}
