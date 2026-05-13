package com.walkitalki.core.ui;

public record TalkScreenState(
    String statusTitle,
    String primaryActionLabel,
    boolean pushToTalkEnabled,
    boolean diagnosticsVisible,
    boolean withinClickBudget,
    String acceptanceCriteria,
    String rollbackTrigger,
    String diagnosticsSignal
) {
}
