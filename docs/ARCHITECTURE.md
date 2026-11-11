# Architecture

## Product boundary
Opti System FF is an Android companion utility for device-side diagnostics, session preparation and explicitly authorized, reversible configuration actions. It supports separate Free Fire and Free Fire MAX profiles. It does not modify either game package.

## Modules
| Module | Responsibility | Android dependency |
|---|---|---|
| app | Compose UI, user flow and dependency assembly | Yes |
| core:model | Game identifiers, device snapshot and diagnostic contracts | No |
| core:engine | Pure decision logic and recommendations | No |
| core:diagnostics | Read-only device data collection through Android APIs | Yes |
| core:games | Scoped game detection and launch intents | Yes |

Add future engines as focused modules only when they have a concrete data source and testable output.

## Runtime flow
1. UI requests a fresh device snapshot.
2. Diagnostics reads public Android APIs and returns a typed snapshot.
3. Engine evaluates the snapshot and emits findings; it does not mutate device state.
4. UI presents findings with data source and limitations.
5. Future action executors may apply only separately approved, supported actions and must verify the result.

## Capability tiers
- Observe: read-only information from public Android APIs.
- Recommend: deterministic advice based on observed data.
- User-mediated: opens Android settings or an app for the user to act.
- ADB/Shizuku: optional shell-level operation, gated by explicit authorization and capability checks.
- Unavailable: inaccessible or unsupported data. The UI must show unavailable instead of fabricating a value.

## Initial game profiles
- Free Fire: com.dts.freefireth
- Free Fire MAX: com.dts.freefiremax

Package identifiers are centralized in core:model and package visibility is limited to these two entries.
