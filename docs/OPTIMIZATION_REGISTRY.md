# Optimization registry

This registry is the source of truth for what the app actually does.

| ID | Capability | Current state | Permission tier | Writes device state? |
|---|---|---|---|---|
| DIAG-RAM-001 | Read system-wide available/total RAM | Implemented | Observe | No |
| DIAG-STORAGE-001 | Read data-partition free/total storage | Implemented | Observe | No |
| DIAG-THERMAL-001 | Read Android thermal status on supported APIs | Implemented | Observe | No |
| GAME-PROFILE-001 | Detect Free Fire package | Implemented | Observe | No |
| GAME-PROFILE-002 | Detect Free Fire MAX package | Implemented | Observe | No |
| GAME-LAUNCH-001 | Launch installed game normally | Implemented | User action | No |
| PERF-DIRECTOR-001 | Coordinate independent optimization engines | Planned | Recommend | No |
| RAM-ADAPT-001 | Adaptive resource action | Research required | TBD | TBD |
| THERMAL-PRED-001 | Thermal trend analysis | Planned | Observe/Recommend | No |
| SHIZUKU-BRIDGE-001 | Optional shell capability bridge | Not implemented | ADB/Shizuku | Not until allow-listed actions exist |

## Evidence labels
- Implemented: code exists and is covered by project tests.
- Measured: a repeatable test measured the outcome against a baseline.
- Experimental: prototype only; not enabled in stable automatic flow.
- Planned: not implemented.
- Unavailable: no valid data source or permission on the target platform.

Do not upgrade a registry status based on a successful command return alone.
