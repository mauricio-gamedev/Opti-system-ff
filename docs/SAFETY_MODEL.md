# Safety model

## Non-negotiable boundaries
- No root is required or assumed.
- The app does not inject into, hook, patch or edit either game's process or files.
- The app does not automate gameplay, modify game packets, manipulate memory or interfere with anti-cheat.
- No blanket process killing, generic RAM cleaner, hidden background service or persistent setting mutation.
- No claim of higher FPS, lower ping, lower touch latency or cooler operation without valid measurements.

## Change lifecycle for future tweaks
Every device-changing action must have:
1. A documented technical rationale and supported Android/device matrix.
2. Preconditions and permission checks.
3. A user-visible preview of the exact change.
4. The prior value saved when restoration is technically possible.
5. Explicit user confirmation.
6. Post-application verification.
7. A rollback path or a clear statement that rollback is unavailable.
8. A registry entry, tests, risk rating and change history.

Actions that cannot satisfy these controls remain recommendations or are excluded.

## Permission policy
The initial app requests no runtime permissions. Package visibility is limited to the two game identifiers through manifest queries. Shizuku integration is not part of this foundation and must remain optional if added later.

## Measurement integrity
- Android available RAM is system-wide headroom, not memory consumed by Free Fire.
- Thermal status is a platform signal, not direct skin temperature or a frame-rate measurement.
- Network tests against public endpoints do not represent the route to Garena game servers.
- Frame-time and FPS require a legitimate measurement source. Missing data must be labeled unavailable.
