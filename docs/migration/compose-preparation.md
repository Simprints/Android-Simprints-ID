# Compose Preparation — Phase 2 Entry Checklist

One-time actions that must be done **before any module starts Phase 2** (Compose UI). The Phase 2
preparation agent (`migration-p2-1-planner`) refuses to plan a module until this checklist is complete.

Tick an item and link the PR when it is done.

## Build setup

- [ ] Add Compose, Navigation 3 and Compose test dependencies to the version catalog.
- [ ] Create a Compose convention plugin.
- [ ] Add the `COMPOSE_UI_ENABLED` flag to the feature module convention plugin (always `false` in release).
- [ ] Add a library `benchmark` build type that honours the flag, so Compose screens can be measured before cleanup.

## Lint

- [ ] Add Compose lint checks so they run as part of `lintDebug`.

## Shared Compose module

- [ ] Create the `:infra:compose-common` module.
- [ ] Implement `SimTheme` as specified in `compose-theme.md`.
- [ ] Add the `simComposeView` Fragment shell helper.
- [ ] Add the lifecycle-aware `ObserveEffects` helper for collecting ViewModel effects.
- [ ] Add test tag tooling and the naming convention from the design document (Appium-compatible resource IDs).
- [ ] Add the core components the dashboard pilot needs.
- [ ] Define window insets handling for Compose screens.
- [ ] Define how orientation-locked host Activities unlock when the flag is on.

## Testing

- [ ] Make Compose UI tests run in the CI unit test job.
- [ ] Set up reference screenshot capture.
- [ ] Add a frame timing (jank) benchmark and record it in the migration metrics.

## Metrics and docs

- [ ] Collect the `pre-compose` metrics snapshot before the first Compose dependency is merged.
- [ ] Update `compose-ui.md` and `compose-theme.md` with the outcomes of the items above.
