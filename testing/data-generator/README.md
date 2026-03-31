# Debug-only biometric data generator

This module lets developers insert bulk biometric enrollment records and session events into the local SID database for testing, sync checks, and E2E setup.

Use it in debug builds only. Do not ship it in production.

## Features

- Bulk insert of biometric enrollment records
- Bulk generation of session events (enrol, identify, verify, etc.)
- Designed for performance, sync, and E2E testing

## Usage

### Generate enrollment records

Populate the local database with enrollment records for 1:1 and 1:N matching tests.

The generator accepts nested extras either as real `Bundle` extras or as flattened keys like `EXTRA_TEMPLATES_PER_FORMAT.ISO_19794_2`. The `adb shell am start` examples below use the flattened form.

```bash
adb shell am start \
  -a com.simprints.test.GENERATE_ENROLLMENT_RECORDS \
  --es EXTRA_PROJECT_ID "oPru9XTAI2hE2nDFD5vZ" \
  --es EXTRA_MODULE_ID "module-abc" \
  --es EXTRA_ATTENDANT_ID "user-xyz" \
  --ei EXTRA_NUM_RECORDS 5000 \
  --ei EXTRA_TEMPLATES_PER_FORMAT.RANK_ONE_3_1 2 \
  --ei EXTRA_TEMPLATES_PER_FORMAT.NEC_1_5 2 \
  --es EXTRA_FINGER_ORDER.NEC_1_5 "RIGHT_INDEX_FINGER,LEFT_THUMB" \
  --es EXTRA_FIRST_SUBJECT_ID "d9a6c3f7-a6c3-d9a6-c3f7-a6c3d9a6c3f7" \
  --ei EXTRA_EXTERNAL_CREDENTIALS_PER_TYPE.GhanaIdCard 1 \
  --ei EXTRA_EXTERNAL_CREDENTIALS_PER_TYPE.QRCode 1
```

#### Parameters

| Key | Type | Description |
| --- | --- | --- |
| `EXTRA_PROJECT_ID` | String | Project ID to assign to generated records |
| `EXTRA_MODULE_ID` | String | Module ID for the generated records |
| `EXTRA_ATTENDANT_ID` | String | Attendant or user ID for the generated records |
| `EXTRA_NUM_RECORDS` | Int | Number of enrollment records to insert. Must be greater than `0` |
| `EXTRA_TEMPLATES_PER_FORMAT.*` | Int | Number of templates to generate for a specific biometric format |
| `EXTRA_FINGER_ORDER.*` | String | Comma-separated finger order for a fingerprint format. The suffix must match the corresponding template format key, for example `NEC_1_5` or `ISO_19794_2` |
| `EXTRA_FIRST_SUBJECT_ID` | String | Subject ID to use for the first generated record only. Remaining subject IDs are random |
| `EXTRA_EXTERNAL_CREDENTIALS_PER_TYPE.*` | Int | Number of external credentials to generate for a specific type |

Supported biometric format keys currently include `SIM_FACE_BASE_1`, `RANK_ONE_1_23`, `RANK_ONE_3_1`, `ISO_19794_2`, and `NEC_1_5`.

Supported external credential type keys currently include `GhanaIdCard`, `NHISCard`, and `QRCode`.

Note: The generator uses static example external IDs by default. The mapping used by the generator is:

- `ExternalCredentialType.GhanaIdCard` -> "GHA-12345789-0"
- `ExternalCredentialType.NHISCard` -> "12345678"
- `ExternalCredentialType.QRCode` -> "123456"

### Generate session events

Simulate real-world usage and server sync by generating session events.

```bash
adb shell am start \
  -a com.simprints.test.GENERATE_SESSION_EVENTS \
  --es EXTRA_PROJECT_ID "oPru9XTAI2hE2nDFD5vZ" \
  --es EXTRA_MODULE_ID "module-abc" \
  --es EXTRA_ATTENDANT_ID "user-xyz" \
  --ei EXTRA_ENROL_COUNT 2 \
  --ei EXTRA_IDENTIFY_COUNT 2 \
  --ei EXTRA_VERIFY_COUNT 2 \
  --ei EXTRA_CONFIRM_IDENTIFY_COUNT 2 \
  --ei EXTRA_ENROL_LAST_COUNT 2
```

#### Session event parameters

| Key | Type | Description |
| --- | --- | --- |
| `EXTRA_PROJECT_ID` | String | Project ID to associate with generated session events |
| `EXTRA_MODULE_ID` | String | Module identifier |
| `EXTRA_ATTENDANT_ID` | String | Field worker or attendant ID |
| `EXTRA_ENROL_COUNT` | Int | Number of enrolment sessions to generate |
| `EXTRA_IDENTIFY_COUNT` | Int | Number of identification sessions to generate |
| `EXTRA_VERIFY_COUNT` | Int | Number of verification sessions to generate |
| `EXTRA_CONFIRM_IDENTIFY_COUNT` | Int | Number of confirm-identification sessions to generate |
| `EXTRA_ENROL_LAST_COUNT` | Int | Number of enrol-last sessions to generate |

### Face template image

A static face image is used for face template generation:

<img src="./FACE-IMAGE.jpg" alt="Face image" />
