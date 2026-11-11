# Game Session Lab — test protocol

## What the current build records
A session stores:
- Selected edition (Free Fire or Free Fire MAX).
- Start and finish timestamps.
- Device-wide available/total RAM and Android's low-memory signal.
- Android thermal status.
- Power-save state and battery percentage when available.
- Display refresh rate and supported modes.
- SoC fields and supported ABIs when the OS exposes them.

Only the app's own private storage is used. The history is capped at 20 completed sessions.

## Repeatable baseline procedure
1. Choose one game edition and keep that edition for the entire comparison.
2. Keep the in-game graphics, FPS and other quality settings unchanged between runs.
3. Keep Android power-save state and screen refresh configuration consistent.
4. Use a similar battery range and avoid charging during thermal comparison runs.
5. Start a session from Opti System FF, play the same type/duration of match, then return and finish the session.
6. Repeat the same procedure before changing any device setting.
7. Change only one variable at a time, then repeat the same sessions.

Run Free Fire and Free Fire MAX as separate baselines. Do not compare a short FF session directly with a long FF MAX session.

## Interpretation
- A negative available-RAM delta means the system reported less available RAM at the end snapshot; it does not identify which process used it.
- A higher thermal severity means the Android thermal status was more severe at the end snapshot; it does not measure exact skin temperature.
- A changed power-save flag means the mode changed between snapshots.
- Display refresh rate is not in-game FPS.
- This release does not measure FPS, frame time, touch latency, game-process memory, CPU/GPU utilization or network latency to Garena servers.

The Session Lab is an evidence recorder, not a benchmark claim. Use identical conditions and multiple runs before drawing conclusions.
