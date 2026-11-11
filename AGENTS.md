# Repository working rules

## Before changing code
1. Read this file and docs/SESSION_CHECKPOINT.md.
2. Inspect affected modules, tests and GitHub Actions.
3. Confirm Android API availability, package visibility, permissions and Shizuku requirements before proposing a capability.

## Git workflow
- Never develop directly on main.
- Create a focused branch for each implementation step.
- Keep changes reviewable.
- Do not merge a pull request or change branch protection without explicit authorization.
- Report exact tests and outcomes. Never claim a build passed without evidence.

## Architecture
- Keep domain models and decision logic free of Android dependencies.
- Keep Android framework access inside core diagnostics, core games or a future dedicated integration module.
- The app module composes features; it must not become the home of system-level logic.
- Every new engine needs a concrete input, output, supported API range, permission tier, failure mode and tests.
- Separate observation, recommendation and device mutation. A diagnostic is not an optimization.

## Safety and integrity
- No root requirement.
- No game-process injection, memory editing, game-file modification, packet manipulation, anti-cheat interference or gameplay automation.
- Never force-stop arbitrary apps or kill background processes as generic RAM cleanup.
- No hidden writes. Future setting changes must be explicit, reversible where possible, validated before and after, and documented.
- Shizuku/ADB is optional and does not grant unrestricted access.
- Prefer targeted package visibility over broad QUERY_ALL_PACKAGES.
- Do not claim FPS, ping, touch latency, CPU/GPU load or frame-time gains without valid measurement.

## Quality gates
- Add unit tests for pure decision logic.
- Add tests for package mapping and boundary conditions.
- Prefer official Android APIs and stable dependencies.
- Keep the app usable when optional APIs or permissions are unavailable.
- Update the optimization registry and session checkpoint when behavior changes.
