# Roadmap

## Phase 0 — Foundation
- [x] Establish repository history and protected development branch workflow.
- [x] Define module boundaries and safety constraints.
- [x] Create Android app shell and diagnostic dashboard.
- [x] Add read-only RAM, storage and thermal snapshots.
- [x] Add separate Free Fire and Free Fire MAX package profiles.
- [x] Add unit tests and Android CI workflow.
- [x] Confirm CI passes and user validates Foundation 0.1.0 on Galaxy A06.

## Phase 1 — Device intelligence
- [x] Device capability inventory for OS-reported SoC, ABIs, low-RAM classification and display modes.
- [x] Bounded on-demand sampling; no polling service or background loop.
- [x] RAM pressure signal, storage, thermal status, power-save mode and battery percentage where available.
- [ ] Continuous thermal trend history with a documented sampling/battery budget.
- [ ] Device-specific compatibility profile review using captured SM-A065M values.

## Phase 2 — Session preparation
- [x] User-started session flow for Free Fire and Free Fire MAX.
- [x] Separate game edition and before/after system snapshots.
- [x] Bounded local history with a maximum of 20 completed records.
- [ ] Optional user-mediated Android settings shortcuts.
- [ ] Detect overlays and recording conditions where public APIs permit.

## Phase 3 — Evidence lab
- [x] Session markers and a repeatable before/after system snapshot protocol.
- [x] Pure Kotlin comparison logic and boundary tests.
- [ ] Frame-time/FPS integrations only through legitimate, available sources.
- [ ] Repeatable multi-run reports with medians and confidence.
- [ ] Thermal and memory time-series regression checks.

## Phase 4 — Optional advanced bridge
- [ ] Shizuku connection and permission-state module.
- [ ] Capability registry for shell commands by Android release and OEM.
- [ ] Reversible, allow-listed actions only.
- [ ] Per-action preview, confirmation, verification and rollback tests.

## Phase 5 — Adaptive systems
- [ ] Performance Director orchestration.
- [ ] Adaptive Resource Manager recommendations.
- [ ] Thermal Prediction from supported observations.
- [ ] Conflict detector and optimization scheduler.
- [ ] Controlled experiments before any automatic action.

A phase is not complete merely because its screen exists. It requires implementation, tests, compatibility evidence and documentation.
