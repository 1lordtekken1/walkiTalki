# walkiTalki design system draft

Date: 2026-05-09.
Status: Draft for implementation after Milestone 0 test harness.

## 1. Product feel

walkiTalki should feel like a reliable field radio, not a generic chat app. The interface must communicate three things immediately:

1. **Can I talk now?**
2. **Who am I connected to?**
3. **What should I do if connection or permissions fail?**

The visual direction is calm, high-contrast, and operational: dark graphite surfaces, electric cyan connectivity accents, warm amber warnings, and a large tactile Push-To-Talk control.

## 2. UX principles

- One primary action per screen.
- Connection state is always visible.
- Permission and pairing problems always include the next user action.
- No hidden recording: microphone/transmit state must be obvious.
- Foreground-only MVP: no UI should imply background monitoring.
- Text labels must accompany icons for accessibility.
- Animation should reinforce state changes, not decorate.

## 3. Core screens

### 3.1 Onboarding / permissions

Purpose: explain why nearby-device and microphone permissions are needed.

Primary elements:

- app mark and short promise: “Talk nearby. No internet.”
- permission cards:
  - Nearby devices: find and connect to a nearby phone;
  - Microphone: transmit only while Push-To-Talk is active;
- primary CTA: “Enable required permissions”;
- secondary CTA: “View privacy details”.

Failure copy examples:

- Nearby devices denied: “We cannot find nearby phones without this permission.”
- Microphone denied: “You can connect and listen, but cannot transmit.”

### 3.2 Discovery

Purpose: find peers and make pairing state understandable.

States:

- Bluetooth off;
- missing permission;
- scanning;
- peer found;
- pairing required;
- pairing rejected;
- pairing timed out;
- connecting;
- connection failed.

Primary elements:

- state header with icon and short status;
- peer list with privacy-safe display names;
- “Scan again” action with bounded scan timer;
- troubleshooting footer.

### 3.3 Talk

Purpose: foreground 1-to-1 Push-To-Talk.

Primary elements:

- connected peer card;
- signal/session status row;
- large circular PTT button;
- receive/transmit indicator;
- diagnostics shortcut;
- disconnect action.

PTT states:

- idle: button label “Hold to talk”;
- transmitting: button label “Talking…” and strong cyan ring;
- receiving: button disabled or secondary, label “Receiving…”;
- busy: label “Peer is talking”;
- disconnected: button disabled, reconnect CTA visible.

### 3.4 Diagnostics

Purpose: support Bluetooth troubleshooting without leaking sensitive data.

Primary elements:

- permission state summary;
- transport state timeline;
- counters: frames sent/received, drops, underruns, reconnect attempts;
- redacted peer hash;
- “Copy diagnostics” button.

Never show or export:

- raw peer identifiers;
- MAC addresses;
- raw device names by default;
- audio payload bytes.

## 4. Design tokens

| Token | Value | Use |
| --- | --- | --- |
| `color.background` | `#0B1014` | app background |
| `color.surface` | `#121A21` | cards and panels |
| `color.surfaceRaised` | `#1A242D` | active controls |
| `color.primary` | `#37D5FF` | connected/transmit accent |
| `color.primaryPressed` | `#00A9D6` | pressed PTT state |
| `color.warning` | `#FFB020` | pairing/permission warnings |
| `color.danger` | `#FF5A6A` | disconnected/errors |
| `color.success` | `#38D996` | ready/connected success |
| `color.textPrimary` | `#F4FAFF` | primary text |
| `color.textSecondary` | `#9FB2C3` | supporting text |

Typography:

- screen title: 28sp, semibold;
- state title: 22sp, semibold;
- body: 16sp, regular;
- support text: 14sp, regular;
- PTT label: 20sp, bold.

Shape:

- cards: 20dp rounded corners;
- buttons: 999dp pill/circle where possible;
- chips: 999dp;
- screen padding: 20dp;
- component gap: 12dp / 16dp / 24dp scale.

## 5. Accessibility requirements

- PTT button minimum size: 160dp diameter.
- All state colors must have text/icon alternatives.
- Haptic feedback on PTT press/release when available.
- Optional “tap to lock talking” is a backlog feature, not MVP default.
- All error states need plain-language recovery actions.

## 6. Implementation guidance

When Compose is introduced, create UI tokens before screen logic:

- `WalkiTalkiTheme`;
- `WalkiTalkiColors`;
- `ConnectionStateBanner`;
- `PushToTalkButton`;
- `PermissionCard`;
- `DiagnosticsCounterRow`.

Compose UI must consume immutable state from ViewModels. It must not call Bluetooth, audio, permission, or diagnostics infrastructure directly.
