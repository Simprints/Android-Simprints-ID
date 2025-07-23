# 📦 Debug-Only Biometric Data Generator

This module allows developers to insert **bulk biometric enrollment records** directly into the local SID database for **developer testing**, **performance testing**, and **E2E test setup**. It is only available in **debug builds** and should never be shipped in production.

---

## 🚀 How to Use

Use the following ADB command to trigger bulk record generation via an explicit `Intent`. This will insert fingerprint and face templates using pre-configured logic.

### ✅ Example Command

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

---

## 🧠 Parameters

| Key                            | Description                                  |
| ------------------------------ |----------------------------------------------|
| `EXTRA_PROJECT_ID`             | Project ID to assign to generated records    |
| `EXTRA_MODULE_ID`              | Module ID for the records                    |
| `EXTRA_ATTENDANT_ID`           | User ID associated with the records          |
| `EXTRA_NUM_RECORDS`            | Number of enrollment records to insert       |
| `EXTRA_TEMPLATES_PER_FORMAT.*` | Number of templates per biometric format     |
| `EXTRA_FINGER_ORDER.*`         | Comma-separated finger order for each format |
| `EXTRA_FIRST_SUBJECT_ID`       | UUID of the first subject                    |

---

## 🖼️ Face Template Image

This generator uses a static face image to create biometric templates.
📍![Face image](./FACE-IMAGE.png)



---
