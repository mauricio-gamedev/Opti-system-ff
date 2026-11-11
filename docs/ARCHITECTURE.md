# Architecture

## Product boundary
Opti System FF is an Android companion utility for device-side diagnostics, session preparation and explicitly authorized, reversible configuration actions. It supports separate Free Fire and Free Fire MAX profiles. It does not modify either game package.

## Modules
| Module | Responsibility | Android dependency |
|---|---|---|
| app | Compose UI, user flow and dependency assembly | Yes |
| core:model | Game identifiers, device snapshots and session contracts | No |
| core:engine | Pure diagnostic recommendations and session comparisons | No |
| core:diagnostics | On-demand, read-only device data collection | Yes |
| core:games | Scoped game detection and launch intents | Yes |
| core:session | Bounded local session persistence | Yes |

Add future engines as focused modules only when they have a concrete data source and testable output.

## Runtime flow
1. UI requests a fresh device snapshot.
2. Diagnostics reads public Android APIs and returns a typed snapshot.
3. Engine evaluates the snapshot and emits findings; it does not mutate device state.
4. When a user starts a game session, the app stores a start snapshot and opens the selected game normally.
5. When the user returns and finishes the session, the app captures an end snapshot, stores the completed record and calculates system-wide deltas.
6. UI presents findings, session history, data sources and limitations.

## Capability tiers
- Observe: read-only information from public Android APIs.
- Recommend: deterministic advice based on observed data.
- User-mediated: opens Android settings or an app for the user to act.
- ADB/Shizuku: optional shell-level operation, gated by explicit authorization and capability checks.
- Unavailable: inaccessible or unsupported data. The UI must show unavailable instead of fabricating a value.

## Device intelligence snapshot
- System-wide total/available RAM and the platform low-memory signal.
- Device low-RAM classification.
- Data-partition total/available storage.
- Thermal status reported by Android.
- Power-save state and battery capacity percentage when exposed.
- Current display refresh rate and supported display modes.
- SoC manufacturer/model and supported ABIs when the OS exposes them.

All collection is on demand. There is no polling loop or foreground service in this phase.

## Session storage
- SharedPreferences in app-private storage.
- One active session at a time.
- Up to 20 completed sessions retained locally.
- Each session stores only the selected game edition, timestamps and start/end device snapshots.
- No gameplay content, account data, network traffic or information from the game process is collected.

## Measurement boundaries
- Android available RAM and the low-memory signal describe the whole system, not Free Fire process memory.
- Display refresh rate is not the game's rendered FPS.
- A start/end comparison cannot attribute system changes exclusively to the game.
- Thermal status is a platform signal, not a precise skin-temperature measurement.
- The current Session Lab does not capture FPS or frame time.
