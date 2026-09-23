# Face Capture Timing Report — Query Overview

## Purpose
Reconstructs end-to-end timing of a single face-capture session (consent →
capture → confirmation → biometric reference) from synced events in
`simprints-prod-eu.mnemosyne_live_data`, for manual/eyeball review of capture
performance per session.

Behavior described here is based on app version **2026.3.0** (fixed the
`attemptNb` recapture-increment bug — see gap #8). Older versions may differ.

## Event start/end time definitions (from the app user's perspective)

| Event | `startTime` = | `endTime` = | Used in query? |
|---|---|---|---|
| `ConsentEvent`  | Consent screen appears | User taps Accept/Decline | ✅ `consent_end_time` |
| `FaceFallbackCaptureEvent` | Capture screen appears (idle preview starting) | Camera first sees a usable face while still idle  | ✅ `proxy_screen_created_time`, `proxy_first_face_seen_time` |
| `FaceCaptureEvent`  | App starts processing one saved frame from this attempt. Most rows are a genuine capture frame; each attempt also always includes one extra, non-capture row for the idle-preview frame that was on screen before capture started ("fallback") | That frame finishes processing | ✅, fallback rows excluded (`first_capture_start_time`, `last_capture_end_time`, `total_attempts`, `attempt_numbers`) |
| `FaceCaptureBiometricsEvent` | Same moment as its `FaceCaptureEvent`, only for frames good enough to use | Single point in time | ✅, id-joined to non-fallback capture rows (`first_biometrics_time`) |
| `FaceCaptureConfirmationEvent`| Confirmation screen appears | User taps Confirm/Recapture/back | ✅ `confirmed_finish_time`, `recapture_count` |
| `BiometricReferenceCreationEvent` | Biometric record finished processing | Single point in time | ✅ `reference_created_time` |

## Data sources (tables used)
All joined on `labels.sessionId`, except `Session` (uses its own `id`).

| CTE | Table | What it provides |
|---|---|---|
| `session_meta` | `Session` | `sidVersion` (app version) |
| `consent` | `Session_Consent` | `endTimeUnixMs` |
| `fallback` | `Session_FaceFallbackCapture` | Proxy screen-created / first-face-seen times; fires once per attempt, not once per session (gap #3) |
| `capture_session_summary` | `Session_FaceCapture` | First/last capture timestamps, `attemptNb` values, total attempts |
| `biometrics` | `Session_FaceCaptureBiometrics` | Validity flag, not a distinct "extraction time" (see below) |
| `confirmation_final` | `Session_FaceCaptureConfirmation` | Confirm time (`result = CONTINUE`), recapture count |
| `biometric_reference` | `Session_BiometricReferenceCreation` | Reference-created time (`modality = 'FACE'`) |


No date range filter — the query scans all history for the project and
returns the 10 most recent sessions 
## Output columns
- **Session identifier**: `session_id`
- **Time summary** (chronological): `consent_end_time` → `proxy_screen_created_time` →
  `proxy_first_face_seen_time` → `first_capture_start_time` → `last_capture_end_time` →
  `first_biometrics_time` → `confirmed_finish_time` → `reference_created_time`,
  plus computed durations (`ms_screen_open_to_first_face`,
  `ms_capture_duration_ALL_ATTEMPTS`, `ms_first_capture_to_finish_ALL_ATTEMPTS`,
  `ms_finish_to_reference_created`, `ms_consent_end_to_reference_created`)
- **Metadata**: `project_id`, `app_version`
- **Attempts**: `total_attempts`, `attempt_numbers` (array), `recapture_count`,
  `attempt_nb_reliability` (flags app versions before **2026.3.0**, where a
  bug meant `attemptNb` wasn't incremented on recapture — gap #8)
- **Data quality**: `multi_flow_detected` (flags sessions with more than one
  completed biometric reference — gap #6)

## What each timestamp actually means
- **`proxy_screen_created_time` / `proxy_first_face_seen_time`**: from
  `Session_FaceFallbackCapture` — a genuine proxy, not a rough estimate.
  `proxy_first_face_seen_time` is always strictly **before** "Start capture"
  is tapped (once the tap fires, a valid frame goes straight into a capture,
  not a fallback update). Fires once per attempt, not once per session; the
  `fallback` CTE picks the earliest row, so these reflect the **first**
  attempt's screen, not necessarily the attempt that finished the session.
  Both can be `NULL` if no usable face was ever seen before the tap.
- **`first_capture_start_time` / `last_capture_end_time`**: from
  `Session_FaceCapture`, across all real attempts (including recaptures),
  fallback frames excluded (`isFallback = FALSE`). `ms_capture_duration_ALL_ATTEMPTS`
  is the diff between them — the closest proxy we have for "tap Start capture
  to capture processing done," since no synced event exists for the tap
  itself, but the first capture frame starts processing immediately once the
  tap fires. This spans **all** attempts if there were recaptures, not just
  one.
- **`first_biometrics_time`**: not an independent measurement — it's copied
  from the same capture frame's timestamp, so it equals
  `first_capture_start_time` whenever the earliest non-fallback frame was
  valid. Its real value is as a **validity indicator**: a capture row with no
  matching biometrics row (joined by `id`) failed the frame's quality check.
- **`confirmed_finish_time`**: `endTimeUnixMs` of `Session_FaceCaptureConfirmation`
  where `result = 'CONTINUE'` — when the user tapped **Confirm**, not when the
  screen first appeared.
- **`reference_created_time`**: when the final biometric reference was
  created; no end time exists in the event schema.

## Known issues / gaps
1. **No true "screen created" or "start capture clicked" event**.
2. **The `fallback` proxy can be missing entirely.** It's only created once a
   valid face is detected while idle. If "Start capture" is tapped before any
   usable face was seen, `proxy_screen_created_time` and
   `proxy_first_face_seen_time` are `NULL`, not just delayed.
3. **`fallback` fires once per face capture screen instance, not once per
   session.** Recapture re-enters a new instance of the screen, so a session
   with recaptures gets one fallback event per attempt. The CTE picks the
   earliest row, so the proxy timestamps reflect the **first** attempt only.
4. **Sessions that never start a capture attempt are excluded** (filtered on
   `first_capture_start_time IS NOT NULL`). Can't currently measure
   drop-off/failure rate before capture.
5. **Sessions that reach capture but never tap Confirm still appear as a
   row**, with `confirmed_finish_time` and everything downstream `NULL` (only
   set when `result = 'CONTINUE'`). Don't mistake this for a query bug.
6. **Multiple full capture flows in one session blend together.** Every CTE
   aggregates at `session_id` grain with `MIN`/`MAX`/`COUNT(DISTINCT
   attemptNb)`. A session with two capture→confirm→reference sequences can
   pair flow 1's start with flow 2's finish, inflating durations, and can
   merge identical `attemptNb` values across flows. **Measured (2026-09-22)**:
   for this report's target project, rare — 1 of 523 sessions (0.19%) had 2
   completed biometric references. Across **all projects** (988k+ sessions),
   however, it's meaningfully more common: 1.54% have 2 references, plus a
   long tail of 3 (14 sessions) and 4 (3 sessions). Rates likely vary a lot by
   project, so don't assume the 0.19% figure holds when repointing this query
   at a different `project_id`. `CONTINUE`-confirmation counts didn't
   independently surface the same multi-flow session in the single-project
   check, a minor discrepancy worth another look but not blocking. Given the
   low prevalence for this specific project, a full grain change to "flow"
   wasn't done; instead the query now flags affected rows via
   `multi_flow_detected` (`TRUE` when `reference_count > 1`) so they can be
   excluded/reviewed rather than silently blended — re-run the diagnostic
   (unfiltered, or for the new target project) before trusting duration
   columns elsewhere.
7. **No per-attempt breakdown in the final output.** `capture_attempts` is
   already row-level (one row per non-fallback frame), but the final
   `SELECT` only exposes the session-level aggregate.
8. **Pre-2026.3.0 `attemptNb` bug**: not incremented on recapture, so
   `total_attempts`/`attempt_numbers` can under-count on older app versions.
   `attempt_nb_reliability` flags this via a lexicographic string comparison
   (`app_version < '2026.3.0'`), which isn't a fully robust version compare.
9. **Single project, single modality** — hardcoded, not generalized.
10. **Manual/eyeball tool, not a dashboard** — one row per session, no
    avg/median/percentile aggregation.
11. **`*UnixMs` naming vs. actual type.** Named as if raw millisecond
    integers, but are genuinely `TIMESTAMP` columns — confirmed via
    `INFORMATION_SCHEMA.COLUMNS` on 2026-09-22 for every table used in this
    query (`Session`, `Session_Consent`, `Session_FaceFallbackCapture`,
    `Session_FaceCapture`, `Session_FaceCaptureBiometrics`,
    `Session_FaceCaptureConfirmation`, `Session_BiometricReferenceCreation`).
    Just a naming quirk inherited from the internal schema, not a live bug —
    no query change needed.


## Full query
> ⚠️ Before running: replace the hardcoded `project_id = 'z3zbWY612tEJwcFHBIwT'`
> with your target project, and adjust/remove `LIMIT 10` (it currently caps
> output to the 10 most recent sessions with no date bound, scanning full
> history — see "Data sources" above).

```sql
WITH session_meta AS (
  -- Flat schema: id IS the session ID, app version is sidVersion
  SELECT
    id AS session_id,
    sidVersion AS app_version
  FROM `simprints-prod-eu.mnemosyne_live_data.Session`
  WHERE projectId = "z3zbWY612tEJwcFHBIwT"
),

consent AS (
  SELECT
    labels.sessionId AS session_id,
    MAX(endTimeUnixMs) AS consent_end_time
  FROM `simprints-prod-eu.mnemosyne_live_data.Session_Consent`
  WHERE labels.projectId = "z3zbWY612tEJwcFHBIwT"
  GROUP BY session_id
),

fallback AS (
  -- Proxy for "screen created" (start) and "first usable face seen" (end).
  -- Uses ARRAY_AGG(... ORDER BY startTimeUnixMs LIMIT 1) instead of independent
  -- MIN(start)/MIN(end) so the two values always come from the *same* row
  -- (a session with recaptures has one fallback row per attempt - see gap #3).
  SELECT
    session_id,
    first_row.startTimeUnixMs AS fallback_start_time,
    first_row.endTimeUnixMs   AS fallback_end_time
  FROM (
    SELECT
      labels.sessionId AS session_id,
      ARRAY_AGG(
        STRUCT(startTimeUnixMs, endTimeUnixMs)
        ORDER BY startTimeUnixMs LIMIT 1
      )[OFFSET(0)] AS first_row
    FROM `simprints-prod-eu.mnemosyne_live_data.Session_FaceFallbackCapture`
    WHERE labels.projectId = "z3zbWY612tEJwcFHBIwT"
    GROUP BY session_id
  )
),

capture_attempts AS (
  -- Excludes fallback frames (isFallback = TRUE): these are sent alongside real
  -- attempts but originate from the idle preview, before the user's tap, and
  -- would otherwise contaminate MIN(startTimeUnixMs) below.
  -- COALESCE(..., FALSE) guards against any historical rows synced before
  -- isFallback existed, where the field would be NULL rather than FALSE -
  -- without it, `= FALSE` would silently drop those rows (and, because of the
  -- NOT NULL filter on first_capture_start_time downstream, drop the whole
  -- session from the report rather than surfacing it with missing data).
  SELECT
    labels.sessionId AS session_id,
    labels.projectId AS project_id,
    FaceCapture.id AS capture_id,
    FaceCapture.attemptNb AS attempt_nb,
    startTimeUnixMs,
    endTimeUnixMs,
  FROM `simprints-prod-eu.mnemosyne_live_data.Session_FaceCapture`
  WHERE COALESCE(FaceCapture.isFallback, FALSE) = FALSE
  -- Deliberately NOT filtering by labels.projectId here: this CTE gates
  -- whether a session can appear at all (via first_capture_start_time IS NOT
  -- NULL downstream), and labels.projectId on this table has been observed to
  -- be inconsistently populated across a session's own rows. Filtering here
  -- silently dropped at least one real session. The project filter is instead
  -- applied once, at the end, against cs.project_id (ANY_VALUE-aggregated
  -- across all of a session's rows) - more tolerant of a stray mislabeled row.
),

capture_session_summary AS (
  SELECT
    session_id,
    ANY_VALUE(project_id) AS project_id,
    MIN(startTimeUnixMs) AS first_capture_start_time,
    MAX(endTimeUnixMs)   AS last_capture_end_time,
    COUNT(DISTINCT attempt_nb) AS total_attempts,
    ARRAY_AGG(DISTINCT attempt_nb ORDER BY attempt_nb) AS attempt_numbers
  FROM capture_attempts
  GROUP BY session_id
),

biometrics AS (
  -- Joined on the payload id (FaceCaptureEvent and FaceCaptureBiometricsEvent
  -- for the same frame share the same `payloadId` - SimpleCaptureEventReporter
  -- passes faceDetection.id to both), not on timestamp. The id join is exact
  -- and 1:1; a timestamp join is only incidentally correct and would silently
  -- fan out if two frames ever shared a millisecond.
  --
  -- Note: FaceCaptureBiometricsPayload.createdAt is copied from the same
  -- frame's detectionStartTime, so first_biometrics_time is *not* a distinct
  -- "template extraction" timestamp - it equals first_capture_start_time
  -- whenever the earliest non-fallback frame was valid. Its real value is as
  -- a validity indicator (a capture row with no matching biometrics row
  -- failed the quality/validity check), not as an independent timing signal.
  SELECT
    ca.session_id,
    MIN(b.startTimeUnixMs) AS first_biometrics_time
  FROM `simprints-prod-eu.mnemosyne_live_data.Session_FaceCaptureBiometrics` b
  INNER JOIN capture_attempts ca
    ON b.labels.sessionId = ca.session_id
   AND b.FaceCaptureBiometrics.id = ca.capture_id
  GROUP BY ca.session_id
),

confirmation_final AS (
  SELECT
    labels.sessionId AS session_id,
    MAX(CASE WHEN FaceCaptureConfirmation.result = 'CONTINUE' THEN endTimeUnixMs END) AS finish_time,
    COUNTIF(FaceCaptureConfirmation.result = 'RECAPTURE') AS recapture_count
  FROM `simprints-prod-eu.mnemosyne_live_data.Session_FaceCaptureConfirmation`
  WHERE labels.projectId = "z3zbWY612tEJwcFHBIwT"
  GROUP BY session_id
),

biometric_reference AS (
  -- reference_count surfaces multi-flow sessions (gap #6): measured at ~0.2%
  -- of sessions for this project (1 in 523) - rare enough that a full
  -- session->flow grain redesign isn't justified, but MIN(startTimeUnixMs)
  -- alone would silently pick the first of several references without
  -- flagging that the row blends multiple completed flows.
  SELECT
    labels.sessionId AS session_id,
    MIN(startTimeUnixMs) AS reference_created_time,
    COUNT(*) AS reference_count
  FROM `simprints-prod-eu.mnemosyne_live_data.Session_BiometricReferenceCreation`
  WHERE BiometricReferenceCreation.modality = 'FACE'
    AND labels.projectId = "z3zbWY612tEJwcFHBIwT"
  GROUP BY session_id
)

SELECT
  -- Session identifier
  COALESCE(c.session_id, f.session_id, cs.session_id, b.session_id, cf.session_id, br.session_id, sm.session_id) AS session_id,

  -- ===== Time summary (chronological) =====
  c.consent_end_time,
  f.fallback_start_time AS proxy_screen_created_time,
  f.fallback_end_time   AS proxy_first_face_seen_time,
  TIMESTAMP_DIFF(f.fallback_end_time, f.fallback_start_time, MILLISECOND) AS ms_screen_open_to_first_face,
  cs.first_capture_start_time,
  cs.last_capture_end_time,
  -- Proxy for "tap Start capture" -> capture processing done, across ALL attempts
  -- (no synced event fires on the tap itself; the first capture frame starts
  -- processing immediately once the tap releases the auto-capture hold-off)
  TIMESTAMP_DIFF(cs.last_capture_end_time, cs.first_capture_start_time, MILLISECOND) AS ms_capture_duration_ALL_ATTEMPTS,
  b.first_biometrics_time,
  cf.finish_time AS confirmed_finish_time,
  TIMESTAMP_DIFF(cf.finish_time, cs.first_capture_start_time, MILLISECOND) AS ms_first_capture_to_finish_ALL_ATTEMPTS,
  br.reference_created_time,
  TIMESTAMP_DIFF(br.reference_created_time, cf.finish_time, MILLISECOND) AS ms_finish_to_reference_created,
  TIMESTAMP_DIFF(br.reference_created_time, c.consent_end_time, MILLISECOND) AS ms_consent_end_to_reference_created,

  -- ===== Metadata =====
  cs.project_id,
  sm.app_version,

  -- ===== Attempts =====
  cs.total_attempts,
  cs.attempt_numbers,
  cf.recapture_count,

  -- Known bug: before app version 2026.3.0, attemptNb was not incremented on recapture,
  -- so total_attempts can under-count real attempts for older app versions.
  CASE
    WHEN sm.app_version < '2026.3.0' AND cf.recapture_count >= cs.total_attempts
      THEN 'PRE-FIX (attemptNb unreliable)'
    ELSE 'OK'
  END AS attempt_nb_reliability,

  -- Gap #6: flags sessions where more than one biometric reference was
  -- created (multiple completed capture->confirm->reference flows blended
  -- into one row by this session-grain query). Rare (~0.2% measured) but
  -- when TRUE, every duration column downstream of first_capture_start_time
  -- should be treated as unreliable for that row.
  br.reference_count > 1 AS multi_flow_detected

FROM consent c
FULL OUTER JOIN fallback f ON c.session_id = f.session_id
FULL OUTER JOIN capture_session_summary cs ON COALESCE(c.session_id, f.session_id) = cs.session_id
FULL OUTER JOIN biometrics b ON COALESCE(c.session_id, f.session_id, cs.session_id) = b.session_id
FULL OUTER JOIN confirmation_final cf ON COALESCE(c.session_id, f.session_id, cs.session_id, b.session_id) = cf.session_id
FULL OUTER JOIN biometric_reference br ON COALESCE(c.session_id, f.session_id, cs.session_id, b.session_id, cf.session_id) = br.session_id
FULL OUTER JOIN session_meta sm ON COALESCE(c.session_id, f.session_id, cs.session_id, b.session_id, cf.session_id, br.session_id) = sm.session_id

WHERE cs.first_capture_start_time IS NOT NULL
  AND cs.project_id = "z3zbWY612tEJwcFHBIwT"

ORDER BY cs.first_capture_start_time DESC
LIMIT 10;
```
