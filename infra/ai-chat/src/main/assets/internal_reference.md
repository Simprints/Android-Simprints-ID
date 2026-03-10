# Simprints ID — Internal Reference (Diagnostic Use Only)

> **IMPORTANT**: This document contains internal technical details about how SID works
> under the hood. Use this information ONLY to diagnose and explain issues to users
> in simple, non-technical language. NEVER mention internal concepts like worker chains,
> ViewModels, state machines, navigation graphs, or code-level details to the user.
> Instead, translate your understanding into plain advice: "the app is trying to...",
> "this usually means...", "you should try...".

---

## Workflow Step Ordering

When a calling app triggers SID, a sequence of steps is built based on the workflow
type and project configuration. Steps execute in order — if one fails or is skipped,
it affects subsequent steps.

### Enrolment
1. Setup (device initialisation)
2. Age Selection — only if the project has age-restricted modalities AND no age was provided by the calling app
3. Consent — only if consent collection is enabled in the project
4. Fingerprint Capture — for each configured fingerprint modality
5. Face Capture — for each configured face modality
6. External Credential scan (QR/OCR) — only if Multi-Factor ID is configured
7. Fingerprint Matching (duplicate check) — only if duplicate biometric enrolment check is enabled
8. Face Matching (duplicate check) — only if duplicate biometric enrolment check is enabled

### Identification
1. Setup
2. ID Pool Validation — only if enabled in experimental config AND data source is Simprints
3. Age Selection (conditional, same as enrolment)
4. Consent (conditional)
5. Fingerprint Capture — only matching modalities (may differ from all configured modalities)
6. Face Capture — only matching modalities
7. External Credential scan (conditional)
8. Fingerprint Matching
9. Face Matching

### Verification
1. Setup
2. Age Selection (conditional)
3. Fetch Subject — retrieves the subject to verify against (skipped if data source is CommCare)
4. Consent (conditional)
5. Fingerprint Capture — only matching modalities
6. Face Capture — only matching modalities
7. Fingerprint Matching
8. Face Matching

### Confirm Identity
1. Confirm Identity — single step where user selects from identification results

### Enrol Last Biometric
1. Enrol Last Biometric — saves biometrics captured in a previous identification session

### Key Rules
- If no biometric SDK covers the subject's age group, the user is returned to the
  calling app without biometric capture.
- If age-restricted modalities have no capture steps for the given age, ALL modalities
  are used as a fallback.
- External credential scan is only added when there are biometric capture steps in the workflow.

---

## Login and Authentication Checks

When a workflow starts, SID performs these checks in strict order before any
biometric steps begin. Each check can produce a specific error.

1. **Device safety** — Is the device rooted? If yes → red error, workflow blocked.
2. **Sign-in status** — Is the user signed in to the correct project?
   - Not signed in → login screen shown
   - Signed in to a different project → yellow error ("different project ID")
   - Signed in correctly → proceed
3. **Login attempt** (if needed) — Checks Google Play Services, integrity service,
   Play Store version. Failures produce red errors.
4. **Project state** — Is the project active?
   - Running → proceed
   - Paused → yellow error ("project paused by administrator")
   - Ending → red error ("project ending")
   - Ended → forces re-login
5. **Post-login setup** — Stores credentials, schedules background sync, starts workflow.

If the user sees a login-related error, the checks above explain what failed and why.

---

## Sync Lifecycle

### What Triggers Sync
- **Automatically** after login (background sync starts immediately)
- **Periodically** in the background (configurable interval per project)
- **Manually** via the "Sync Now" button on the dashboard
- **After configuration changes** (e.g., module selection changed)
- **On internet connectivity change** (when connection becomes available)

### What Gets Synced
| Direction | Data | When |
|-----------|------|------|
| Upload | Biometric events and session data | Every sync cycle |
| Upload | Biometric images | Separate schedule (hourly default); can require Wi-Fi |
| Download | Enrolled subject records (templates) | Every sync cycle |
| Download | Project configuration | Separate schedule |
| Download | Device configuration | Separate schedule |

### Sync Errors and Recovery
| Error Type | What Happens | User Impact |
|------------|-------------|-------------|
| Network timeout / transient error | Automatic retry on next cycle | Temporary; resolves when connectivity improves |
| Backend maintenance | Sync stops until maintenance ends | "Backend maintenance" message; includes estimated time |
| Authentication expired | Sync stops; re-login required | User must log in again |
| Rate limiting (too many requests) | Sync stops for this cycle | Resolves on next sync cycle |
| Cloud integration error | Sync stops permanently for this cycle | May need Simprints support |

### Sync Constraints
- Event sync requires network connection (except CommCare co-sync which works without)
- Image upload may require unmetered (Wi-Fi) connection depending on project config
- Database migration (Realm → Room) pauses down-sync during migration and resumes after

---

## Error Alert Categories

Error screens have coloured backgrounds indicating the type of problem:

### Red Alerts — Serious / Device Issues
Cannot be resolved by the user in-app. Require device changes or Simprints support.
- Rooted device detected
- Project is ending
- Google Play Services missing, outdated, or Play Store app outdated
- Integrity service error
- OTA firmware update failed (needs scanner reset)
- Unexpected/unhandled error

### Yellow Alerts — Configuration / Input Issues
Usually caused by incorrect setup in the calling app or project configuration.
- Project ID, User ID, Module ID, Session ID, Verify ID, or Selected ID invalid
- Metadata in the intent is invalid
- Invalid workflow state (e.g., confirm identity after an enrolment)
- Different project ID than what user is signed into
- Project paused

### Grey Alerts — Data / Connectivity Issues
Often related to missing data or temporary connectivity problems.
- Subject not found online (for verification) — needs internet
- Subject not found offline (for verification) — needs sync
- Enrolment save failed
- Face license missing or invalid
- Backend maintenance
- Face configuration error (codes 000/001/002)

### Blue Alerts — Bluetooth / Scanner Issues
Related to Vero fingerprint scanner connectivity.
- Bluetooth not supported, not enabled, or no permission
- Scanner not paired, disconnected, off, or multiple scanners paired
- Low scanner battery
- NFC not enabled or NFC pairing failed

### All Alerts
- Show a "Close" button that returns to the calling app
- Include an error code that can be copied for support
- Are tracked in analytics for monitoring

---

## Configuration-Driven Behaviour

These project settings significantly affect what the user experiences:

### Modalities
- A project can use fingerprint, face, or both
- Each modality can have an age range restriction
- If dual modality is enabled, both captures happen in sequence

### Consent
- When enabled, a consent screen appears before biometric capture
- Separate consent text for enrolment vs identification/verification
- Refusing consent shows the exit form

### Matching
- Verification threshold is configurable per SDK — determines match/no-match decision
- Identification returns ranked candidates with confidence scores

### Sync
- Data destination: Simprints backend or calling app
- Sync frequency: periodic or on-demand
- Module-based partitioning: only selected modules' records sync to device

### Experimental Flags (set via Custom Configuration in Vulcan)
| Flag | Effect |
|------|--------|
| `faceAutoCaptureEnabled` | Automatically captures face without button press |
| `displayCameraFlashToggle` | Shows flash on/off toggle during face capture |
| `validateIdentificationPool` | Checks if ID pool is empty/synced before identification |
| `recordsDbMigrationFromRealmEnabled` | Enables Realm → Room database migration |

---

## Biometric Capture Behaviour

### Face Capture
- Shows camera preview with live feedback overlays
- **Detection states**: Too far, Too close, Face angled (yaw/roll), Poor quality, Valid
- **Manual mode** (default): User presses capture button when face is valid
- **Auto-capture mode**: Captures automatically after face stays valid for a configurable
  duration (default 3 seconds)
- Quality threshold is checked per SDK configuration
- If quality fallback is enabled and a fallback capture exists, quality check may be skipped

### Fingerprint Capture
- Connects to Vero scanner via Bluetooth
- Captures configured number of fingers in sequence
- **Scanner LED feedback**:
  - Green = good scan
  - Red = poor quality, try again
  - Orange = waiting/processing
- If scanner disconnects mid-capture, an error is shown
- If no finger is detected repeatedly, a limit is enforced before moving on
- Image saving strategy varies: always save, never save, only good scans, or only
  samples used in the final reference

### Common to Both
- Exit form shown if user presses back during capture
- Each capture records events for analytics and audit trail
- Results include quality scores, timestamps, and device identifiers

---

## Common Diagnostic Scenarios

### "Sync is not working"
- Check: Is the device connected to the internet?
- Check: Are the correct modules selected? (tokenized module IDs in context can be cross-checked)
- Check: Was the last sync recent? If not, suggest manual sync
- Check: Is the backend in maintenance? (grey alert would show)
- Check: Has authentication expired? (would need re-login)

### "Person not found"
- For verification: Is the subject enrolled? Check online vs offline (needs sync)
- For identification: Is the ID pool synced? Are the right modules selected?
- Has the subject's age bracket changed since enrolment? (SDK mismatch)

### "App shows error after opening from [calling app]"
- Yellow alert → likely invalid parameters from calling app (check request parameters in context)
- Red alert → device or project issue (check login check chain)
- Grey alert → connectivity or data issue

### "Capture quality is poor"
- Face: Check lighting, distance, face angle (relay detection state feedback)
- Fingerprint: Check finger cleanliness, pressure, scanner battery, scanner connection
- Check if quality threshold is appropriate for the population (configured per project)

### "App is slow / stuck"
- Check free storage (< 20% causes slow identification)
- Check battery level
- Check if database migration is in progress
- Check if large sync is happening in background
