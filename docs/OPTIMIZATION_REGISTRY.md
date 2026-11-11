# Optimization registry

This registry is the source of truth for what the app actually does.

| ID | Capability | Current state | Permission tier | Writes device state? |
|---|---|---|---|---|
| DIAG-RAM-001 | Read system-wide available/total RAM | Implemented | Observe | No |
| DIAG-RAM-002 | Read Android low-memory signal and threshold | Implemented | Observe | No |
| DIAG-STORAGE-001 | Read data-partition free/total storage | Implemented | Observe | No |
| DIAG-THERMAL-001 | Read Android thermal status on supported APIs | Implemented | Observe | No |
| DIAG-POWER-001 | Read power-save mode and battery capacity when reported | Implemented | Observe | No |
| DIAG-DISPLAY-001 | Read active refresh rate and supported display modes | Implemented | Observe | No |
| DIAG-SOC-001 | Read SoC identity and supported ABIs when exposed by Android | Implemented | Observe | No |
| GAME-PROFILE-001 | Detect Free Fire package | Implemented | Observe | No |
| GAME-PROFILE-002 | Detect Free Fire MAX package | Implemented | Observe | No |
| GAME-LAUNCH-001 | Launch installed game normally | Implemented | User action | No |
| SESSION-CAPTURE-001 | Capture start/end system snapshots for a user-started game session | Implemented | User action | App-private data only |
| SESSION-HISTORY-001 | Retain up to 20 completed session records locally | Implemented | App-private storage | App-private data only |
| SESSION-COMPARE-001 | Compare system RAM/thermal/power snapshots | Implemented | Observe/Derived | No |
| PERF-DIRECTOR-001 | Coordinate independent optimization engines | Planned | Recommend | No |
| RAM-ADAPT-001 | Adaptive resource action | Research required | TBD | TBD |
| THERMAL-PRED-001 | Thermal trend analysis | Planned | Observe/Recommend | No |
| SHIZUKU-BRIDGE-001 | Optional shell capability bridge | Not implemented | ADB/Shizuku | Not until allow-listed actions exist |

## Evidence labels
- Implemented: code exists and is covered by the current project's tests.
- Measured: a repeatable test has measured the outcome against a baseline.
- Experimental: prototype only; not enabled in stable automatic flow.
- Planned: not implemented.
- Unavailable: no valid data source or permission on the target platform.

Do not upgrade a registry status based on a successful command return alone. Session deltas are system-wide observations, not proof that the game caused a change.
