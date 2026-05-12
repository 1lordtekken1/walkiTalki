# ADR 0012: Framework boundary guard for core and preview

Date: 2026-05-11.
Status: Accepted.

## Context

The project depends on keeping `:core` and `:preview` framework-free until Android, Bluetooth, and audio adapters are introduced behind explicit seams. As the codebase grows, accidental imports of Android framework classes or direct references to `BluetoothSocket`, `AudioRecord`, or `AudioTrack` would undermine the TDD-first architecture.

## Decision

Add an executable architecture guard to the JVM harness. The guard scans protected production source roots:

- `core/src/main/java`
- `preview/src/main/java`

It fails the core test runner if those roots contain forbidden framework references such as `import android.`, `import androidx.`, `BluetoothSocket`, `AudioRecord`, or `AudioTrack`.

## Acceptance criteria

- `./gradlew :core:test --no-daemon` fails if framework references leak into protected core/preview production sources.
- The guard checks exactly the production roots that must stay framework-free before Android module gates.
- Android framework references remain allowed in future Android modules only, not in `:core` or `:preview`.

## Rollback / pivot trigger

Redesign this guard if future source layout changes make protected roots inaccurate, or if Android adapters need a dedicated module that should be explicitly excluded from framework-free checks.

## Diagnostics signal

Failures list the relative file path and forbidden pattern so reviewers can quickly identify the boundary violation.

## Consequences

Positive:

- Framework-free boundaries become executable instead of relying only on review discipline.
- Future Compose/Bluetooth/audio adapters have a clear place to live outside `:core` and `:preview`.

Negative:

- The guard is intentionally simple string matching and may need refinement if false positives appear.
