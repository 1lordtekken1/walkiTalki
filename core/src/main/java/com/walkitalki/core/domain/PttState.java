package com.walkitalki.core.domain;

public enum PttState {
    IDLE,
    SCANNING,
    PAIRING_REQUIRED,
    PAIRING_REJECTED,
    PAIRING_TIMED_OUT,
    CONNECTING,
    CONNECTED,
    TRANSMITTING,
    RECEIVING,
    BUSY,
    DISCONNECTED
}
