# Simprints ID — Knowledge Base

> This document provides the AI assistant with comprehensive knowledge about
> the Simprints ID application. It is loaded as the system prompt context.

## Overview

Simprints ID is a biometric identification app used by field workers in developing
countries. It enables organisations to register and identify beneficiaries using
fingerprint and/or face biometrics.

## Core Workflows

### 1. Enrolment
Registers a new beneficiary with biometric data.
- Steps: Consent → Biometric Capture (fingerprint/face/both) → Confirmation
- The calling app provides a GUID; Simprints stores biometrics linked to that GUID.
- If both modalities are enabled, fingerprint is captured first, then face.

### 2. Identification
Searches for a matching beneficiary among all registered records.
- Steps: Consent → Biometric Capture → Matching → Results
- Returns a ranked list of potential matches with confidence scores.
- The calling app decides the final match threshold.

### 3. Verification
Confirms a specific beneficiary's identity (1:1 match).
- Steps: Consent → Biometric Capture → Matching against stored record → Result
- Returns a confidence score for the specific individual.

### 4. Confirm Identity
Marks which candidate from an identification was selected.
- Called after the user picks the correct match from the results list.

### 5. Enrol Last Biometrics
Saves the biometrics captured during the last identification as a new record.
- Used when identification found no match and user wants to enrol the person.

## Common Problems & Troubleshooting

### Fingerprint Scanner Issues
- **Scanner not connecting**: Ensure Bluetooth is enabled. Move closer to the scanner. Try turning the scanner off and on.
- **Poor scan quality**: Clean the scanner surface. Ask the subject to clean and dry their fingers. Apply gentle, even pressure.
- **Scanner battery low**: Charge the scanner before field work. A full charge lasts approximately 8 hours of use.

### Face Capture Issues
- **Face not detected**: Ensure good lighting. Remove hats or sunglasses. Hold the phone at face level.
- **Low quality capture**: Avoid direct sunlight behind the subject. Ensure the face fills the oval guide.

### Connectivity Issues
- **Sync not completing**: Check Wi-Fi or mobile data connection. Sync requires internet.
- **Login failing**: Verify project credentials. Check internet connectivity. Contact your project administrator.

### General Issues
- **App crashing**: Restart the app. If problem persists, check that the app is updated to the latest version.
- **Consent screen appears**: Consent is required by project configuration. The subject must provide consent to proceed.

## Settings & Configuration

Settings are controlled by the project administrator and synced from the server.

### Biometric Modalities
- **Fingerprint only**: Only fingerprint capture is used.
- **Face only**: Only face capture is used.
- **Fingerprint and Face**: Both modalities are captured for higher accuracy.

### Scanner Types
- **Vero 2**: Simprints' custom fingerprint scanner. Connects via Bluetooth.
- **SecuGen**: Alternative USB-connected fingerprint scanner.

### Matching Thresholds
- Controlled server-side per project.
- Higher thresholds mean stricter matching (fewer false positives, more false negatives).
- Lower thresholds mean looser matching (more potential matches returned).

## FAQ

**Q: How many fingers should be scanned?**
A: The number is configured per project, typically 2-4 fingers. Follow the on-screen guidance.

**Q: What if the subject refuses consent?**
A: You cannot proceed without consent. Explain the purpose and try again, or skip this person.

**Q: Can I use the app without internet?**
A: Yes, biometric capture and local matching work offline. However, syncing data to the server requires internet.

**Q: What does "No match found" mean?**
A: The biometric search did not find a matching record. The person may not be enrolled yet.

**Q: How do I update the app?**
A: Updates are distributed through the Google Play Store or your organisation's MDM solution.
