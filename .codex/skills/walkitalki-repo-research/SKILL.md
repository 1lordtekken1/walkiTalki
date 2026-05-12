---
name: walkitalki-repo-research
description: Use when researching existing walkie-talkie, Bluetooth, BLE mesh, Wi-Fi Direct, Nearby Connections, PTT, or Android audio repositories to inform walkiTalki decisions without unsafe code copying.
---

# walkiTalki repository research

## Workflow

1. Search current public sources and official Android documentation before relying on memory.
2. Prefer primary sources: repository README/docs, official Android docs, license files, release notes.
3. Record transport, audio approach, Android age, license, tests, UX pattern, privacy/security posture, and maintenance status.
4. Separate ideas to borrow from code to copy. Do not copy code until license compatibility is confirmed.
5. Convert useful findings into ADRs, tests, or backlog items.
6. Treat old projects as risk evidence and pattern references, not dependency candidates.

## Required output fields

For each repository/product:

- URL
- transport
- audio/PTT behavior
- offline/server dependency
- best practices to borrow
- risks and anti-patterns
- license caution
- concrete action for walkiTalki

## Red flags

- GPL/AGPL code copied into a differently licensed app without decision.
- Deprecated Android APIs or old permission models copied forward.
- BLE selected for realtime audio without benchmark.
- Emergency/disaster claims added before field validation.
- Server-based SDK added to no-server MVP.
