# Roadmap

## Phase 0 — Foundation (current)
- [x] Establish repository history and a feature branch.
- [x] Define module boundaries and safety constraints.
- [x] Create Android app shell and diagnostic dashboard.
- [x] Add read-only RAM, storage and thermal snapshots.
- [x] Add separate Free Fire and Free Fire MAX package profiles.
- [x] Add unit tests and Android CI workflow.
- [ ] Confirm the first GitHub Actions run is green.

## Phase 1 — Device intelligence
- [ ] Device capability inventory (SoC family, display mode, RAM class, API level and OEM limits).
- [ ] Bounded sampling policy with battery-overhead checks.
- [ ] Thermal trend history and lifecycle-safe monitoring.
- [ ] Storage and memory trend diagnostics.
- [ ] Device-specific compatibility profiles, starting with Galaxy A06 SM-A065M.

## Phase 2 — Session preparation
- [ ] Game launch preparation checklist.
- [ ] Optional user-mediated settings shortcuts.
- [ ] Detect overlays and recording conditions where public APIs permit.
- [ ] Separate profile persistence for each game edition.

## Phase 3 — Evidence lab
- [ ] Session markers and repeatable benchmark protocol.
- [ ] Frame-time/FPS integrations only through legitimate sources.
- [ ] Before/after reports with confidence and measurement limitations.
- [ ] Thermal and memory regression checks.

## Phase 4 — Optional advanced bridge
- [ ] Shizuku connection and permission-state module.
- [ ] Capability registry for shell commands by Android release and OEM.
- [ ] Reversible, allow-listed actions only.
- [ ] Per-action preview, confirmation, verification and rollback tests.

## Phase 5 — Adaptive systems
- [ ] Performance Director orchestration.
- [ ] Adaptive Resource Manager recommendations.
- [ ] Thermal Prediction from observed trends.
- [ ] Conflict detector and optimization scheduler.
- [ ] Controlled experiments before any automatic action.

A phase is not complete merely because its screen exists. It requires implementation, tests, compatibility evidence and documentation.
