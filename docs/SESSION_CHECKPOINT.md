# Session checkpoint

## Current state
- Repository: mauricio-gamedev/Opti-system-ff
- Foundation branch: feat/engine-foundation-v1
- Current work branch: feat/device-intelligence-session-lab
- Product: Opti System FF / FF Performance Engine
- Reference target: Samsung Galaxy A06 SM-A065M, Android 16 (API 36)
- App ID: io.github.astromg01.optisystemff
- App version in this branch: 0.2.0 (versionCode 2)

## Foundation validated
- User confirmed Foundation 0.1.0 installs and opens on the Galaxy A06.
- Android CI completed successfully for Foundation 0.1.0 after retrying a transient Gradle distribution network timeout.
- Foundation includes Kotlin/Compose, separate FF/FF MAX package profiles, read-only RAM/storage/thermal diagnostics and unit tests.

## 0.2.0 implementation in this branch
- Extended the read-only snapshot with Android's system low-memory signal/threshold, low-RAM classification, power-save mode, battery percent, current/supported display refresh rates, SoC fields and ABIs.
- Added core:session using app-private SharedPreferences.
- Added explicit start/finish session flow for both games.
- Added bounded local history (20 completed sessions).
- Added pure SessionComparisonEngine and unit tests.
- Added UI for device intelligence, preflight findings, active session and recent comparisons.

## Explicitly not implemented
- No Shizuku integration or shell tweaks.
- No device-setting writes.
- No continuous background sampling or foreground service.
- No actual in-game FPS/frame-time capture.
- No game-process CPU/GPU/RAM inspection.
- No touch-latency measurement.
- No network route measurement to Garena servers.
- No automatic background-process management.

## Session Lab interpretation
Start/end values are system-wide snapshots. Deltas cannot be attributed exclusively to the game. An unavailable thermal reading produces an unknown thermal delta, not an invented improvement or regression. Display refresh rate is not the game's FPS.

## Next actions
1. Run GitHub Actions for this branch and fix any compile/test failures.
2. Install 0.2.0 on SM-A065M and verify device-reported SoC/refresh-rate values.
3. Record separate repeatable sessions for Free Fire and Free Fire MAX.
4. Review the captured A06 evidence before selecting any device-changing tweak.
5. Keep Shizuku/ADB changes isolated behind capability checks, explicit consent and rollback.
