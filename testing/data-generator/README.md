# 📦 Debug-Only Biometric Data Generator

This module enables developers to insert **bulk biometric enrollment records** and **session events** into the local SID database for **testing and E2E setup**.  
**Debug builds only** — never ship in production.

---

## Table of Contents

- [Features](#features)
- [Usage](#usage)
  - [Biometric Records Generation](#biometric-records-generation)
  - [Session Event Generation](#session-event-generation)
- [Parameters](#parameters)
- [Face Template Image](#face-template-image)

---

## Features

- Bulk insert of biometric enrollment records (fingerprint, face)
- Bulk generation of session events (enrol, identify, verify, etc.)
- Designed for performance, E2E, and sync testing

---

## Usage

### Biometric Records Generation

Populate the database with many enrollment records for 1:1 and 1:N biometric matching tests.

**Command:**
```bash
adb shell am start \
  -a com.simprints.test.GENERATE_ENROLLMENT_RECORDS \
  --es EXTRA_PROJECT_ID "oPru9XTAI2hE2nDFD5vZ" \
  --es EXTRA_MODULE_ID "module-abc" \
  --es EXTRA_ATTENDANT_ID "user-xyz" \
  --ei EXTRA_NUM_RECORDS 5000 \
  --ei EXTRA_TEMPLATES_PER_FORMAT.RANK_ONE_3_1 2 \
  --ei EXTRA_TEMPLATES_PER_FORMAT.NEC_1_5 2 \
  --es EXTRA_FINGER_ORDER.NEC_1 "RIGHT_INDEX_FINGER,LEFT_THUMB" \
  --es EXTRA_FIRST_SUBJECT_ID "d9a6c3f7-a6c3-d9a6-c3f7-a6c3d9a6c3f7"
```
### Parameters

| Key                            | Description                                  |
| ------------------------------ |----------------------------------------------|
| `EXTRA_PROJECT_ID`             | Project ID to assign to generated records    |
| `EXTRA_MODULE_ID`              | Module ID for the records                    |
| `EXTRA_ATTENDANT_ID`           | User ID associated with the records          |
| `EXTRA_NUM_RECORDS`            | Number of enrollment records to insert       |
| `EXTRA_TEMPLATES_PER_FORMAT.*` | Number of templates per biometric format     |
| `EXTRA_FINGER_ORDER.*`         | Comma-separated finger order for each format |
| `EXTRA_FIRST_SUBJECT_ID`       | UUID of the first subject                    |

### Session Event Generation
Simulate real-world usage and server sync by generating session events.
**Command:**
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
### Parameters
| Key                          | Type   | Description                                                           |
|-----------------------------|--------|-----------------------------------------------------------------------|
| `EXTRA_PROJECT_ID`          | String | The ID of the project to associate with the generated session events. |
| `EXTRA_MODULE_ID`           | String | The module identifier                                                 |
| `EXTRA_ATTENDANT_ID`        | String | The ID of the field worker                                            |
| `EXTRA_ENROL_COUNT`         | Int    | Number of enrolment sessions generate.                                |
| `EXTRA_IDENTIFY_COUNT`      | Int    | Number of identification sessions  to generate.                       |
| `EXTRA_VERIFY_COUNT`        | Int    | Number of verification sessions to generate.                          |
| `EXTRA_CONFIRM_IDENTIFY_COUNT` | Int | Number of confirm-identification sessions to generate.                |
| `EXTRA_ENROL_LAST_COUNT`    | Int    | Number of last enrolment sessions  to generate .                      |

### Face Template Image
Face Template Image
A static face image is used for template generation:
📍<img src="./FACE-IMAGE.png" alt="Face image"></img>
