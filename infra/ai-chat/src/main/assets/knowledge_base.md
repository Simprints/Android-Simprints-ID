# Simprints ID — Knowledge Base

> This document provides the AI assistant with comprehensive knowledge about
> the Simprints ID (SID) application. Use this information to answer user
> questions accurately. If you are unsure about something, say so rather than
> guessing. Always provide actionable, step-by-step guidance when possible.

---

## About Simprints ID

Simprints ID (SID) is an Android biometric identification app used by field workers
in developing countries. It enables organisations to register and identify
beneficiaries using fingerprint and/or face biometrics.

SID is not a standalone application — it requires a **calling app** (e.g., ODK, DHIS2,
Comm Care) to initiate biometric workflows via Android Intents. The calling app provides
a project ID and the type of workflow to perform.

SID can be downloaded from the Google Play Store:
https://play.google.com/store/apps/details?id=com.simprints.id

---

## Overview & Getting Started

### Using Simprints ID (SID)

Simprints ID (SID) is the Android app that hosts the Simprints biometric workflows.

### Accessing SID

SID (Production) can be downloaded via the Google Play Store:

https://play.google.com/store/apps/details?id=com.simprints.id

### Dependencies

While SID can be downloaded publicly, there are several dependencies to access its features:

- Calling app
- Deployed SID backend, configured with a:

SID backend is not currently available open-source (however there are plans for it be).

### Flow Diagram

SID is not a standalone application; it requires a calling app to open each biometric workflow.

The following diagram visualises the relationship between SID and calling apps, and the sequence in which SID functions are accessed:

[Internal diagram]

## Functions

- Calling apps
- Login
- Consent
- Workflows
- Biometric Capture
- Exit Form
- Sync
- Settings
- Troubleshooting
- Device Size Optimisation & Landscape Support
- Verification decision threshold
- Managing multiple age groups
- Identification pool validation
- Scanning Audio Cues
- Vero Scan Feedback (Fingerprint)
- Biometric Database Migration: Realm to Room FAQ 🚀
- Face Capture - Device Flashlight
- Multi-factor Identification

---

### Calling apps

SID is designed to be used alongside a calling app. This is typically a platform which collects and manages medical records.

#### Responsibilities

- Calling app -captures and stores medical records.
- SID -biometrically authenticates patients when they access health services.

#### User Scenarios

| Calling app | SID |
| --- | --- |
| Create medical records for a new beneficiary | Enrollthe beneficiary's biometric data |
| Access a beneficiary’s medical records | Verifythat the beneficiary is present |
| Check if a beneficiary has medical records | Identifythe beneficiary |

#### Supported Platforms

Simprints has integrations with the following platforms:

- SurveyCTO
- Comm Care(Doesn’t support Enrolment+ and Identification+)
- OpenSRP
- DHIS2(via a Simprints maintained fork:DHIS2 (Product))

---

### Workflows

Workflows enable operators to enrol, identify and verify biometric users.

### User flow

- Workflows are started via a calling app; they cannot be started in any other way.
- Each workflowcaptures biometrics; these are the only screens shown in SID during a workflow and they are shared across all workflows.
- At the end of the workflow, SID returns data to the calling app.

### Workflows

- Enrolment
- Identification
- Verification

---

### Device requirements

|  | Face | Fingerprint (Minimum)As of July 2020 | Fingerprint (Recommended)As of December 2019 |
| --- | --- | --- | --- |
| Android Version | 8.0 (Oreo) | 6.0 (Marshmallow) | 9.0 (Pie) |
| Android API | 26 | 23 | 28 |
| Processor | 1.6GHz Octa-core | 1.4 GHz Quad Core | 2.1 GHz Quad Core or better |
| RAM | 3GB, 4GB if available | 1GB | 2 GB+ |
| Storage | 32GB | 300MB | 1GB+ |
| Camera | 16MP camera with autofocus |  |  |
| Bluetooth |  | 2.0 | 4.0 |

Huawei Phones produced May 2019 onwards are not recommended (e.g. Huawei P40, Mate 30 series, Mate Xs and Honor 30 series).

Due to Google’s revocation of Huawei’s Android license, these devices do not support Google Play Store, which prohibits the installation and update of Simprints ID via Play Store.

Pro tip: When setting up devices with SID, you’ll want toensure that the device always has at least 20% of storage spacefree. This is to ensure SID works fast and does not take forever to load (if you see the buffering icon of doom during an identification flow, it could be because there isn’t enough free space for our algorithm to do it’s thing!). Why is this so? Well, when running identifications, SID ideally runs through matches in a cluster. Without sufficient space, it will run through matches one by one instead.

---

## Authentication & Setup

### Login

The first time SID is opened, it will be logged out and must be configured to a project.
### Accessing the login form

1. Open your Calling App
2. Configure your Calling App for use with Simprints. Ensure that the Simprints project ID matches your Simprints project.
3. Send a request from the Calling App to access one of the Simprints Workflows. If this is your first time accessing SID, it will be an Enrolment.
4. SID will open the login form. Log in using your Project ID and Project Key.
---

### Device configuration sync

Simprints ID Release 2025.1.0 introduces an option to update device and project configuration on demand from the settings screen without waiting for the usual sync cycle. This feature is useful when experimenting with or troubleshooting project configurations.

To sync configuration, open the Simprints ID application from the icon, press on three dots in the upper corner and press settings.
---

### Settings Password

This article is a user guide intended for Project Managers and field workers

[Screenshot: (blue star)]

Simprints ID Release2023.1.0introduces a new Settings Passwordfunctionality, allowing project managers to lock in-app configuration changes. By creating a Settings Password, project managers can prevent unnecessary configuration changes and log-outs in the field.

### Creating a Settings Password

A settings password can be created, changed, and removed in Vulcan:

1. Navigate to your Project > Configurations > General > Settings Password
2. Add/edit or remove a password
3. Click ‘SAVE’
The password can only contain numbers and be 4-8 characters long

To remove your project’s Settings Password, simply clear the password field in Vulcan (leaving only the ‘Settings password’ prompt) and click ‘SAVE’.
Users must either log out and log back to Simprints ID, or wait for a least an hour for Settings Password changes made in Vulcan to take effect on their device. Offline devices will need to come back online before registering new Vulcan changes.

### Using the Settings Password

For Simprints ID users, a password will be required to access the following:

#### Modules

When navigating to the ‘Select modules’ screen, users will be prompted to tap the screen and enter their password before making module changes (module settings are only visible when enabled, and can be accessed from the Dashboard or via (⋮> Settings > Sync Information > Select Modules).
#### Language

Users will be required to enter the password before updating Simprints ID language setting (Dashboard > ⋮ > Settings > Language)
#### Logging out

Users will be required to enter the password before logging out (Dashboard > ⋮ > Settings > About > Log Out). In addition to restricting which users can log out of Simprints ID, this feature also prevents unintentional logouts.
### Frequently Asked Questions

- Can Simprints ID users change their own settings password?No, the password can only be set in Vulcan.
- Does each user have a different password?No, the same settings password is used across all project devices
- What happens if a user forgets their password, or a password is compromised?The password can be retrieved or edited by the project administrator.

### Related articles

- Page:General configuration

---

## Core Workflows

### Consent

Once an intent has been given to use one of the Workflows, beneficiaries may be asked for consent to process their data.

Consent is not always required. It is configurable per project, depending on the project controller.

### General Consent
General consent is captured for beneficiaries 18+ years old. This is the default option.

Consent text is configurable per project. It may vary based on:

- Biometric Modality (e.g. Face or Fingerprint).
- Project Type (e.g. R&D vs. Service Delivery).
- Other project-specific requirements.

### Parental Consent
Parents must provide proxy consent for beneficiaries 17 years and under.

Consent text is phrased differently to account for this relationship.

### Accepting or Declining Consent

Beneficiaries may decide whether to consent or not:

- If the beneficiary consents, the workflow progresses to the next step (typically Biometric Capture).
- If the beneficiary does not consent, they will be pushed to the Exit Form.

### Privacy Notice
The Privacy Notice gives users more information about how their data is processed. It is accessible via the hyperlink on the consent screen.

Users can tap the arrow at the top left corner or use the native Android back button to return to the consent screen.

---

### Enrolment

During enrollment, an individual’s biometric information is captured and stored in the database for future use in Verificationor Identification.

### User flow

Enrollment is started via a calling app.

Depending on the modality configured for a project, the screens displayed in SID will either be:

- Face Capture
- Fingerprint Capture

### Enrollment+

Enrollment+ includes an additional Identificationat the point of biometric capture.

This spots beneficiaries already in the system before their details are added again, which can help reduce duplicate records.

If Simprints identifies a potential duplicate, Simprints sends back those records to the frontline worker who can either:

1. Confirm this is a duplicate enrolment
2. Restrict enrollment (as there is a very high chance of duplication)

### Inputs

These values must be sent by the calling app:

- project Id
- user Id
- module Id

### Return Values

These values are returned to the calling app:

#### Standard

- GUID(globally unique identifier) for the newly enrolled user

#### Enrollment+

- List of Identifications

---

### Identification

During Identification, an individual’s biometric information is captured and compared against all enrolled users (Enrolment).

### User flow

Identification is started via a calling app.

Depending on the modality configured for a project, the screens displayed in SID will either be:

- Face Capture
- Fingerprint Capture

When the user completes the biometrics capture, identification begins, and a list of potential matches will be returned to the calling app for user adjudication.

The number of returned matches is configurable per project.

### Identification+

Identification+ adds an option to automatically enrol a unsuccessfully identified individual without recapturing the biometrics.

Identification+ is configurable per project.

### Inputs

These values must be sent by the calling app:

- project Id
- user Id
- module Id

### Return Values

These values are returned to the calling app:

- A list of potential matches. Each match includes:GU ID confidenceconfidence Band

---

### Verification

During Verification, an individual’s biometric information is captured and compared against a single enrolled user (Enrolment).

### User flow

Verification is started via a calling app.

Depending on the modality configured for a project, the screens displayed in SID will either be:

- Face Capture
- Fingerprint Capture

When the user completes the biometrics capture, Verification begins, and a decision will be returned to the calling app.

### Inputs

These values must be sent by the calling app:

- project Id
- user Id
- module Id
- verify Guid

### Return Values

These values are returned to the calling app:

- A single match record, including:guidconfidenceconfidence Band

---

### Biometric Capture

Biometric capture is the process of capturing biometrics, for the purposes of completing abiometric workflow.

### Accessing biometric capture

- Biometric Capture is accessed by calling a biometric workflow from a calling app.
- Biometric Capture cannot be accessed in any other way.

### Biometric Capture Flows

- Face Capture
- Fingerprint Capture
- Dual Modality Support

---

## Fingerprint Capture

### Fingerprint Capture

When one of the Workflows is opened, and Fingerprint is the configured modality, the Fingerprint Capture screens will be displayed.

### Scanning a finger
#### Identify finger

The finger to be scanned is indicated by the graphic and text (left, right, thumb or index finger). Users must select and scan the correct finger; Vero scanners can't detect which finger is placed.

Projects can be configured to hide the graphic, in which case the text will appear on its own.

#### Scan

Once users have placed their finger on the sensor, the scan can be activated either by:

- Tapping the “SCAN” button in SID
- Pressing the scan button on the Vero scanner

A loading bar will appear on the screen; the user’s finger must be kept steady while this is displayed.

The following LEDs on the Vero scanner will also indicate the scan process:

- Vero 1.0: The red LED on the image sensor will flash as it is activated, and the middle smile LED will turn red whilst the scan is in progress.
- Vero 2.0:The green LED on the image sensor will flash as it is activated.

If the “CANCEL” button is pressed the scan for that finger will stop. Scans of any previous fingers will be retained.

#### Good scan

If the image meets the Capture Qualitythreshold, SID will inform users it has been a good scan.

- The breadcrumb will turn green
- The button text will change to “GOOD SCAN”
- The Vero scanner smile LEDs will turn green and reset.

If additional fingerprints are required, SID will prompt for thenext fingerprint.

If no additional fingerprints are required, SID will move to theconfirm screen.

### Scanning additional fingers
If a project is configured for multiple finger scans, thesteps to scan a fingerare repeated (as many times as configured).

The differences between fingers are:

- The graphic will change to indicate the new finger to be scanned
- The breadcrumbs (dots below the image) will increment by one

### Confirming the scan
Once all fingers have been successfully scanned, users will be asked for confirmation to close the workflow.

If any fingers were skipped, a cross would appear against those instead of a tick.

- If users select “RESTART”, they will return to Step 1 to re-take all the fingerprints.
- If users select “CONFIRM,” they will return to the calling app to continue their journey. SID will return different data to the calling app depending on the intent (Enroll, Identify, Verify).

## Unhappy Paths

### Missing finger
If a user is missing a finger, it can be skipped by tapping the “Missing Finger” link.

- The “Let’s Try Another Finger!”overlay will appear
- The graphic will change and indicate the new finger to be scanned.
- An additional breadcrumb will be added, and the breadcrumb for the previous scan will be coloured red.

Missing finger functionality can be useful when capturing a fingerprint is challenging (for example, the process is distressing a child).

### No finger detected
If the image sensor does not detect a finger, SID will vibrate and inform users that no finger is detected.

Users can trigger a new scan by either pressing the scan button on the Vero or by tapping the “RE-SCAN” button.

Users may end up in a loop with “no finger detected” if SID cannot detect a finger. Users can select “missing finger” to move to the next step in the workflow.

No finger detected doesn't count as a bad scan.

### Rescan finger
If users need to re-scan a finger (for example, if they took the right index finger instead of the left), they can either swipe left across the image or tap the relevant breadcrumb.

Once the finger to be re-scanned is selected, press on the “GOOD SCAN” button and confirm the scanning by pressing on the “RE-SCAN?” button or by pressing the scan button on the Vero once more.

---

### Vero Scan Feedback (Fingerprint)

Simprints ID 2024.2.1 introduces enhanced visual scan feedbackto aid attendants in taking scans without having to refer to their mobile device. This is particularly useful in busy clinic environments where attendants might need to concentrate on encouraging a child to have their fingers scanned.

#### :vero: How do Vero visual cues work?

Visual scan feedback is designed to allow an attendant to understand both the status of the Vero and the outcome of each scan by using the LEDs positioned in an arc below the Vero’s scanning window. The table below shows the four ways Vero LEDs light up to indicate scanning status.

|  | All 5 LEDs turn offduring scanninguntil the scan ends. | Basic mode (<2024.2.1) |
| --- | --- | --- |
|  | After a scan, the LEDs willturn greenfor 3 seconds toindicate success. | Basic mode (<2024.2.1) |
|  | If the scan isunsuccessful, the LEDs willturn redfor 3 seconds indicating another attempt is required | Basic mode (<2024.2.1) |
|  | When idle(before, after, or between scans), the Vero’s LEDs flash white to indicate that the scanner is ready. | Enhanced Visual Scan Feedback (>2024.2.1) |

Examples of each feedback scenario are shown in the videos below

scan idle 2.mov
scan sccess 2.mov
scan fail 2.mov

Visual scan feedback can be used with both Sim Matcher and NEC SDKs

#### How to configure visual scan feedback

Vero visual cues can beconfiguredin the configuration box for each fingerprint SDK.

[Screenshot: Screenshot 2024-11-07 at 06.50.19.png]

Visual scan feedback is one of 3 options for configuring (in addition to basic and live feedback which shows how well the subject’s finger is positioned on the device).These features cannot be used in combination.

OTA update required

If an attendant’s scanner has not previously been configured to use visual cues, they may need an over-the-air software update. This should happen automatically as part of the scanning process and will only be required once.

---

### Scanning Audio Cues

Simprints ID 2024.2.1 introduces scanning audio cues. This a simple, feature to simplify the scanning process in busy environments by sounding a short beep after each scan.

#### How do Audio Cues Work?

The feature itself is very simple. During a fingerprint workflow, a brief beep will be playedby the user’s mobile devicewhen the scan is complete and it is time to move to the next finger.

This feature allows an attendant to manage a multiple-fingerprint workflow without necessarily needing to refer back to their mobile device after each finger.

The video to the right shows audio cues in action.

[Screenshot: (blue star)]

IMG_7148.mov

#### Configuring Audio Cues

Scanning audio cues are configured on a device-by-device basis, and are switched off by default. They can be switched on by opening Simprints ID and navigating to settings, where they will see a toggle controlling audio cues.

Attendants should ensure that the volume is turned upon their mobile deviceif they want to use this feature.
---

### [Experimental] Auto-capture

Face auto-capture is an experimental feature that enables automatic triggering of face capture at the moment when a qualifying image appears in the viewfinder.

Useful especially in cases when the subject moves quickly; in other words, for face capture of little kids.

#### How to enable

The feature is enabled when these conditions all apply:

- SID version is at least 2025.1.0
- Auto-capture is turned on in Vulcan for the project - see Custom configuration#Experimental-features. In short: in Configurations → Custom, have the JSON string of {"face Auto Capture Enabled": true} (or have "face Auto Capture Enabled": truebesides other, already existing, custom flags).
- Auto-capture is enabled in SID settings. Enabled by default.

Make sure that the configuration of SID is updated if you enabled auto-capture in Vulcan recently.

#### How to use

Auto-capture is mostly like the regular capture here Face Capture. There are 2 differences:

1. The viewfinder starts with the green “Start capture“ button. This allows the user to get ready and to aim the camera towards the intended person first. Press the button when ready.
2. Once the face in the viewfinder is well-positioned and lit, the capture starts, progresses, and finishes by itself. The captured image, the “Recapture” and “Tap to continue“ buttons are shown afterwards like usual.

To quickly switch to regular (manual) capture or back to auto-capture if needed: open Simprints ID (SID) → press 3-dot Menu button →press Settings→ toggle Auto-capture.

#### Configuration

- Duration of auto-capture imaging progress is by default 3 seconds but can be configured in Vulcan (see face Auto Capture Imaging Duration Millis in Custom configuration#Experimental-features.
- Sensitivityof auto-capture triggering can be adjusted by changing the Quality Thresholdvalue in Vulcan.Note: this affects the regular capture and quality of image data; shouldn’t be normally done.

---

## Face Capture

### Face Capture

When one of the Workflows is opened, and Face is the configured modality, the Face Capture screens will be displayed.

### Onboarding Screen
The first step is the onboarding screen, which provides simple instructions on good face capture. Users can tap anywhere to continue to the next screen.

The onboarding screen may be skipped on repeated use of face capture - see more details in Instructions screen auto-skipping.

### Preparation
After the onboarding screen, the device camera attempts to locate a face. This screen displays different messages to help the user capture a good image:

| Message | Guidance for user | Next Step |
| --- | --- | --- |
| No Face Detected | Make sure the face fills the circle | The user should ensure that the camera is pointed at the user and the face is enclosed in the circle. |
| Too Far | Move closer | The user should move closer to the subject, or ask the subject to move closer |
| Too Close | Move Back | The user should move further away from the subject, or ask the subject to step back or sit back in their chair |
| Unclear Image | Try adjusting the lighting or repositioning the face for a clearer photo | The user should ensure that the subject’s face is lit up as much as possible and as evenly as the conditions allow. |

The message will change as the user moves the camera:

### Capture
When conditions are met for a good face capture, the button will turn green and will change to say ‘Ready to capture’. The user should hold the device steady while clicking the green button.

The app will capture a burst of images when the button is pressed. The number of images captured is defined in Remote Config.

When the images have been captured, the screen changes to the blue “Confirmation Screen”.One image will be shown. The image shown is the ‘best’ image (according to a Capture Quality).

### Next Steps

Pressing “Tap to continue” continues the flow, bringing the user to the next modality or back to the calling app, according to the modality selection in Remote Config.

The user can also press ‘recapture’ if they wish to re-run the capture process for this person. If the user presses ‘recapture’, the current images are discarded, and the capture flow begins again.

---

### Face Capture - Device Flashlight

If you are using a device with a built in flashlight during aface capture, from SimprintsID 2025.3onwards, you can enable the device flashlight during capture to get the best possible capture of your subject (particularly useful in low-light conditions).

Note, constant flashlight use can drain a device’s battery, SID users should be advised to use it only when necessary.

In order to use this feature, your project should first be configured with the followingcustom configuration:

```
{"display Camera Flash Toggle": True}
```

Having enabled this feature, SID users can toggle the flashlight on and off via a new button on the face capture screen:
Please note that the UI above will only display when SID detects that a device has a camera flashlight. If it does not, the UI will not appear, even if the project is configured to enable camera flashlights.

---

### Instructions screen auto-skipping

Since SID version 2025.2.0, the Instructions (Preparation) screen of the face Capture flow is skipped when face capture is used repeatedly after the first time SID was newly installed or its data cleared.

This eliminates a step for most of the time the user follows a face capture flow.

If the user wants to see the instructions again, there’s a button on the Capture screen for that.

In summary:

- The Instructions screen: is shown the first time, and skipped every time afterwards - to the Capture screen
- The face Capture screen: contains an Instructions button at the bottom, that on click navigates to the Instructions screen

Face capture flow, the first time:
Face capture flow, the 2nd time and later:
---

## Multi-Modality & Advanced Biometrics

### Dual Modality Support

This article is a user guide intended for Project Managers and attendants

[Screenshot: (blue star)]

Simprints ID Release2023.1.0introduced Dual Modality Support.This allows Simprints ID users to use both fingerprintandface modalities to enrol, verify and identify service users, allowing for greater flexibility and accuracy in the verification and identification of biometric data.

If Dual Modality is enabled for your project, by default.

- Both modalities are captured duringenrolment
- Both modalities are captured forverificationand service usersare verified if either returns a match
- Both modalities are captured foridentification,the modality with the highest match score being used to identify the service user (more information).

From Simprints ID 2024.2.1 onwards it is possible to use only one modality for verification and identification. See the Matching Modality section below for more details.

### Enabling Dual Modality in Vulcan

Selecting multiple modalities for your project is very similar to the previous process of switching between modalities.

1. Navigate to ‘Project > Configurations > General > Modalities’
2. Select either or both modalities as required
3. Click ‘SAVE’
Users must either log out and log back to Simprints ID or wait for a least an hour for modality changes made in Vulcan to take effect on their device. Offline devices will need to come back online before registering new Vulcan changes.

Please also ensure you have requested provision of the correct Face SDK Licenses.Details here

### Using Dual Modality (Simprints ID)

Capturing biometrics when both modalities are enabled is very similar to existing processes. When Simprints ID is launched from your calling app, users will always be asked to register fingerprint biometrics first, followed by face:
Having successfully completed the process, Simprints ID will return the results to your calling app in exactly the same way as when using a single modality; showing a successful enrolment, verifying the selected service user or returning a list of possible identifications.

### Matching Modality

From Simprints ID 2024.2.1 onwards, when multiple modalities are selected for a project. Both modalities must always be used for enrolment, however, it is possible to use only one modality when doing a 1:1 or 1:N workflow.

This feature (intended to balance the technical benefits of using both modalities with the operational burden of taking multiple captures), is controlled at a project level and can becontrolled in Vulcanalongside the selection of modalities.

[Screenshot: Screenshot 2024-11-06 at 16.09.33.png]

### How are Dual Modality results prioritised?

In the longer term, Dual Modality opens up options for greater biometric accuracy by combining results from both modalities. At this point, if a project is capturing both modalities during 1:1 and 1:N workflows, the logic is used to return results to the calling app:

Verification:

1. A match is returned if either fingerprintorface modalities return a match.

Identification:

1. A list of match scores is generated for each modality.
2. The match scores are normalised (Face is multiplied by 100).
3. The list with the highest top match score is returned. If both lists have an equally strong match, fingerprint is preferred over face.

### Frequently Asked Questions

- Is consent given for each modality?No, consent for Dual Modality biometrics is still only requested once, at the beginning of the process.
- Do I have to use both modalities when Dual Modality is enabled?You must capture both modalities when enrolling a subject, but you can use the matching modality control to limit modality capture during 1:1 or 1:N.
- Can an existing project be switched to Dual Modality?Whilst it is technically possible to migrate a single modality project to Dual Modality (by selecting both in Vulcan), this is not advised as (for now):You will not be able to retrospectively attach modality results to older records If you switch to Dual Modality, only results with both modalities will be down-synced to your device Existing single-modality records stored on the phone can still be used for comparison. Both modalities would be captured but only the historical modality matched.
- Can a Dual Modality project revert to a single modality?There is less risk of ongoing synchronisation issues reverting back to a single modality, but it is still not recommended. Please raise a ticket with the Panda Help Centerif this is something you are considering.
- Will Dual Modality impact performance?Identification may take longer when using Dual Modality biometrics. You should use otherconfiguration optionsto minimise the dataset on each device.
- Do ID+ & Enrol+ work with Dual Modality?Yes, these features will work the same as with single modality projects

- Page:Fingerprint configuration
- Page:Using Simprints ID (SID)
- Page:Face configuration
- Page:Back-end 101: intro

---

### Multi-factor Identification

Simprints ID 2025.4 Introduces a major new feature designed to enhance users' experience and boost accuracy; Multi-factor Identification (MFID). Until now, identity management in Simprints ID has relied solely on biometrics as a mechanism for finding an individual registered in our database.

MFID supercharges search by allowing the registration of an ‘external credential’ alongside biometrics.

An external credential is simply a unique alphanumeric string of numbers associated with an individual, but defined outside of Simprints ID (distinct from simprints-generated GU ID s)

Whilst external credentials are defined outside of Simprints ID, this latest release allows users to detect and register external credentials within the app. The nature and delivery mechanism for external credentials are intentionally flexible to allow for expansion of the feature in the future, but there are currently two main ways that we can register and search:

QR codes

Independently generated QR codes encoding an alphanumeric string can be registered and searched by Simprints ID. Use cases might include labels attached to a booklet or even a QR code generated on a users smartphone.

ID Documents

Simprints ID uses Optical Character Recognition (OCR) to identify and extract pre-defined (unique) fields in ID documentation.

ID cards are supported on a case-by-case basis, and this feature launches with support for Ghana-based NHIS and Ghana Cards.

capture nhis.mp4

### :fingerprint: MFID Complements Biometrics

MFID is designed to enhance and complement biometrics rather than replace. Projects and users are not forced to use MFID if a credential is not available, but where it is, biometrics will still continue to be captured alongside the external credential. The feature is designed this way for two main reasons.

Coverage may not be universal.Not all users will have an ID card/QR code with them (or even at all). In this case they are able to skip the credential capture and fall back to our standard biometric workflows.

Biometric verificationremains a key Simprints product offering. Using credentials and biometrics together provides re-assurance for our users and partners that we’re correctly identifying individuals and reflecting the uniqueness of a given intervention.

When individuals do have an external credential, MFID supercharges the search workflow, quickly pinpointing and biometrically verifying an individual.

### New Features for Data Collectors

Registering an external alongside biometrics allows data collection platforms to enhance users' workflows in two specific ways:

👤 In the scenario that we’ve successfully matched an external credential alongside an individuals biometrics, we can be almost certain that we’ve found the correct record. Simprints ID lets the data collection platform know this so that they cannavigate straight to a users recordrather than displaying a list of results and forcing the user to adjudicate.

📝 When a credential has been scanned,Simprints ID returns the alphanumeric string(e.g. ID card number), so that it can be automatically used by the data collection platform (for example, ID card number could be populated on a users record). In the future, this may be extended to capturing and transferring users' demographic information (e.g. name, DoB).

Since MFID takes advantage of our existing 1:N callback structure, data collection platforms do not need to adapt workflows in order to use it. For example, in the case that we match an external credential and biometric, Simprints ID will return a 1:N callback with only one result.

Following our initial field-level testing in December 2025 (following which we may adjust our app to app API), our Simprints for Developers documentation will be updated with full integration documentation. In the meantime, if you want to explore enhanced data collection features, we’ll be happy tohelp.

### Flexible by design

We understand that the ID landscape may change over time and MFID has been designed flexibly as a result. In the future:

- Projects will be able tosupport new types of credential(e.g. a new ID card format)
- Users may be able tomanage an individuals credentials, including registering multiple credentials, or de-activating deprecated formats.
- Given MFID at its core only relies on an alphanumeric string, it may also be possible to expand our MFID feature withnovel capture mechanisms, for example NFC scanning (where tablets are enabled with NFC and NFC is embedded in ID documents/labels).

### Using MFID

Armed with an overview of MFID, let’s explore how to configure and use MFID:

Configuring Multi-factor Identification
Using Multi-factor Identification

🔜 Coming Soon, updated Simprints for developers documentation with full integration details.

https://simprints.gitbook.io/docs/development/simprints-for-developers

---

### Configuring Multi-factor Identification

### Enabling MFID

Configuring Multi-factor Identification (MFID) is straightforward. Project managers simply need to enable the feature in the new MFID tab in Vulcan.

[Screenshot: Screenshot 2025-11-07 at 11.28.13.png]

Having enabled MFID, users can now select the type of credential they would like to support in their project:

[Screenshot: Screenshot 2025-11-07 at 11.28.29.png]

### Enhanced OCR Configuration

For projects using OCR to capture credentials from pre-supported ID documents, it is possible to adjust the resolution of the camera feed as well as the number of images captured during the process.

- "ocr Captures"- sets how many images to run OCR on. Default is 3, range is 1-10
- "ocr High Res"- if false, the camer captures heavily-compressed images (faster, but less accurate). Default is true.

The process for implementing these adjustments as well as the specific JSON structure is defined on thecustom configurationpage.

### Configuration Limitations

There are currently some specific limitations projects wishing to use MFID should consider:

1. Whilst projects configured with fingerprint/face separately are supported,dual modality projectswhich use both in combination are not currently supported.
2. Only pre-defined ID documentation types are supported (currently Ghana Card and Ghana NHIS card). Projects wishing to support additional document types shouldcontact P&E
3. It is not currently possible for projects to save MFID OCR capture images (as is possible with biometric capture). This may be considered in future versions of this product.
4. MFID does not currently support Co Syncing credentials.

---

### Using Multi-factor Identification

Havingconfigured a projectto use MFID, users will be able to register and search with external credentials alongside biometrics. Before diving into each worflow, let’s refresh ourselves on the types of external credential currently supported:

| Name | Image | Description |
| --- | --- | --- |
| :qr:QR Code |  | Generic QR code embedding an alphanumeric string.Length: 6 characters exactly Content: alpha-numeric |
| :identification_card:NHIS Card (Ghana) |  | Ghana’s National Health Insurance Scheme(NHIS) card. |
| :identification_card:Ghana Card |  | Ghana’s National ID card |

A user can perform a number of workflows using any of the credentials above (as configured in their project). When a project is configured to use MFID, only enrolment (including enrol+) and identification pathways will require users to scan external credentials (verification workflow does not include a credential scan). Key MFID workflows are detailed below:

#### :identification_card: Credential Capture

For all enrolment and identification callouts where MFID is configured for a project, a user will be first asked to capture biometricsbefore being asked to capture the configured credential.

- They will initially be presented with a screen to select the credential or skip credential scan
- They will then be presented with the UI for credential captureQR code scannerOCR ID card scanner
- The result of the scan will then be presented to the user. This could be one of:No credential found in our database Credential matches one in our database
- Users can then either proceed with their workflow, or re-capture the credential.

qrtest.mp4

#### ⏭️ Skip Credential Scan

If a user is not able to scan an individuals credential, it is possible to skip credential scan. Users can do so by pressing 'Skip document scan' on the credential scanning page.

Having skipped credential scan, they will be asked to provide a reason for skipping credential scan before resuming their workflow.

skip credential then 1toN.mp4

#### ➕ Enrolment

If a user undertakes an enrolment pathway for a project configured with MFID, they will be promoted to capture biometrics followed by a credential.

- If neither the credential nor biometrics match any on record, the individual will be enrolled directly and the user returned to the data collector
- If either the credential, biometrics or both match, an identification response (enrol+) will be returned to the data collection platform so the user can choose an alternative indvidual, or choose to enrol anyway.
- If there is a credential match whilst enrolling, this will also automatically be flagged in SID during the scanning process.

direct_enrol.mp4

#### 🔎 Identification

If a user undertakes an identification pathway for a project configured with MFID, they will be prompted to capture biometrics followed by a credential.

- If both the credential and biometrics match, a 1:N containing a single record and a flag telling the data collector that this is the correct person will be returned
- If either the credential or the biometrics match, but the other does not, a standard 1:N response will be returned containing credential matches, and/or biometric matches so that the user can adjudicate

Callouts can be confirmed as per our standard identification workflow following ID workflow for an MFID project.

successful_identification.mp4

#### 🔎 ID+ Enrolment

If a user wishes to enrol the biometric and credential scanned during an ID (identification+) for an MFID project, they can do so using the exact same enrol+ callout they would use in a non-MFID project.

Doing so will result in both the biometric and credential being enrolled.

enrol last bio 1toN.mp4

#### Attaching credentials to biometric enrolments

In undertaking an identification workflow, the user may be scanning the credential of someone who has already enrolled their biometrics.

In this case, SID will flag that there is no credential match, and return to the data collector with a list of possible biometric matches.

When the user confirms a given match, they will return to SID and be asked whether they want to attach the scanned credential.

Doing so will update their enrolment with the scanned credential.

add via 1-N.mp4

#### Map of all MFID pathways

The whiteboard embedded below shows all possible MFID pathways in SID following enrolment or identification callouts.

[Internal diagram]

---

### Managing multiple age groups

This article is a user guide intended for Project Managers & attendants.

[Screenshot: (blue star)]

#### Overview

Historically, the biometric modalities used in Simprints’ projects have had no age restrictions. Whilst projects may have been defined for specific age groups (e.g. Gavi Toddler, Operation Sight), it has been down to project design to restrict enrolment of non-appropriate age groups.

Most biometric SDKs do not cover a full range of ages (particularly in the case of young children). For this reason, and having taken on more complex projects (e.g. Gavi Ghana) which need to span a broader range of ages, Simprints has built functionality to manage multiple biometric SDKs, according to their age range, within a single project.

This feature allows a project to:

- Select multiple SDKs in a project configuration (including two from the same modality)
- Configure operational age ranges for each modality
- Limit biometric enrolment, verification and identification to those age ranges
- Accept an age parameter from the data collector (for enrolment and verification) or prompt the user to give their age category (identification)

#### Configuring a project for multiple age groups

When configuring a project in Vulcan, you will have the option to select 1-3 biometric SDKs (Rank One for face, NEC & Sim Matcher for finger). You may now select 1, 2 or 3 SDKs for use in the project 🎉

[Screenshot: Screenshot 2024-08-08 at 20.52.25.png]

[Screenshot: Screenshot 2024-08-08 at 20.52.12.png]

Having selected your SDKs, you can configure the age ranges you would like these SDKs to cover in the individual configuration dialogues by enabling ‘Allowed Age Range’. If you choose not to enable this, the SDK will apply to all age ranges.

[Screenshot: Screenshot 2024-08-08 at 20.56.34.png]

[Screenshot: Screenshot 2024-08-08 at 20.57.47.png]

[Screenshot: Screenshot 2024-08-08 at 20.59.52.png]

You may choose any age range for each configuration, however:

- Vulcan has pre-defined ranges, and will alert you if you deviate from these. You may deviate, however…
- …it is important to take advice from your project well labelled dataset (WLDS) process to inform the age range for each SDK.

Within a single modality (e.g. fingerprint in this case) you cannot currently overlap age ranges.

#### :vero: Upgrading your Vero firmware

If your project uses fingerprint, it is essential to have the latest version of Vero’s firmware. Verosmanufacturedpost August 2024 will have this firmware automatically installed, however if you are using older Veros, you should enable over the air (OTA) updates for your project, which will automatically install the correct version. This will be done via each fingerprint configuration dialogue:

[Screenshot: Screenshot 2024-08-08 at 21.11.05.png]

When setting up a project, please submit a Simprints Support ticket to find out how the fields above should be populated (as this may change over time)

#### Using a project with multiple age ranges

Having configured your project to use age ranges and ensured, you can start using this feature 💪

From an attendant’s perspective, this functionality has been designed to be as unobtrusive as possible, they will be guided through capture for the appropriate SDKs depending on their age. They will only be asked for their age grouping if the data collection platform fails to send an age to SID (for enrolment and verification) or for identification (where age cannot be pre-determined).

Where there is no age submitted to SID, a new screen will prompt the user to submit their age grouping:

[Screenshot: Screenshot 2024-07-02 at 14.15.02-20240702-131509.png]

👈 An example age prompt.In this particular scenario (let’s assume it’s an identification) two SDKs have been configured:

NEC Fingerprint:6 months → 5 years

Sim Matcher Fingerprint:5 years+

This means that:

- If 0 → 6 months is selected, the attendant will be returned straight to the data collector without capturing biometrics (since no SDK is configured for this age)
- If 6 months → 5 years is selected, the subject's fingerprints will be captured and compared using the NEC SDK
- If 5 years+ the subject’s fingerprints will be captured and compared using the Sim Matcher SDK.

As shown above, ifnobiometric SDK is configured for the subject’s age, the will be returned to the data collector (with an appropriate message - see below) without capturing or comparing the subject’s biometrics.

Let’s examine two more examples:

Example 1: One SDK, enrolment

- A project is configured only with one SDK (Rank One Face)The SDK has an age range of 6m +
- The data collector is enrolling a subject, but is passing the subject’s age (5m) in the intentA 3 month old is returned to the data collector

Example 2: Three SDKs, verification

- A project has been configured with three SDKs:NEC (fingerprint): 6m → 5y Sim Matcher (fingerprint): 5y +Rank One (face): 10y+
- The data collector is verifying a subject and passing their age in the intent:A 3-year-old subject (enrolled 6 months ago) is verified using NECA 7-year-old subject (enrolled 6 months ago) is verified using only Sim MatcherA 13-year-old subject (enrolled 6 months ago) is verified using Sim Matcher and Rank One

A subject enrolled within a specific age bracket may have moved into another age bracket when being identified or verified. SID does not currently handle transitions (scheduled for late 2024).

In this scenario (which is much more unlikely in the early stages of a project)

- When verifying: SID will return an error indicating that the subject cannot be verified.
- When identifying: The pool being searched will not include the biometric enrolled using a different SDK and the subject’s existing GUID will not be returned.

#### :binary: Data collector integrations

Implementing these new features is dependent on changes to a project’s data collection platform. All changes required for data collection platforms are included in the latest version of Lib Simprints:

https://github.com/Simprints/Lib Simprints

And are documented in the integration section of Simprints public documentation:

https://simprints.gitbook.io/docs/development/simprints-for-developers/integrating-with-simprints

Of particular interest will be:

- Including a subject’s age parameter:https://simprints.gitbook.io/docs/development/simprints-for-developers/integrating-with-simprints
- Handling errors:https://simprints.gitbook.io/docs/development/simprints-for-developers/integrating-with-simprints/handling-errors

---

## Matching & Results

### Verification decision threshold

This article is a user guide intended for Project Managers & those configuring Simprints projects.

[Screenshot: (blue star)]

#### Overview

Historically, when a verification has been triggered by a data collector, Simprints ID (SID) has returned a comparison score (on a scale determined by the SDK), but no decision as to the verification success, which has been left to the calling platform to determine.

ForSID 2024.2.0and higher, a verification threshold can be configured for each SDK enabled in a Simprints project. In doing so, an indication of the verification’s success will be determined.

- A positive verification decision will be returned if the score is equal to or above the defined threshold (a match)
- A negative verification decision (non- will be returned if the score is below the defined threshold (a non-match)

#### Enabling verification threshold

A verification threshold checkbox is available in theconfigurationdialogue forfaceandfingerSDKs in Vulcan.

[Screenshot: Screenshot 2024-08-08 at 17.47.52.png]

This option will beturned off by defaultfor new and existing projects & the threshold will be set at a default value of 55 when enabled. Projects should adjust this value according to Well Labelled Dataset results.

A verification threshold is configured separately for each active project SDK. It is technically possible to only configure a threshold for a subset of the total active SDKs in a multi-modality project. However, this is strongly discouraged as this may lead to a verification decision not being returned in some cases.

#### :binary: Data collector integration

These changes are backwards compatible with existing data collector integrations and should not impact existing verification integrations. For data collection platforms wishing to process the new verification decision, details are available in the verification section of our public facing integration documentation:

https://simprints.gitbook.io/docs/development/simprints-for-developers/integrating-with-simprints/verification#verification-judgement

Frequently asked questions

[Screenshot: (blue star)]

Will this feature break my existing verification integration if I turn it on?Nothing should break with existing integrations, but if you turn on this feature for a project and do not update your integration it is advised to test your application still handles verification returns correctly

What happens if I only turn verification decision on for one SDK?If you are using a project with multiple SDKs and only turn on verification for one SDK, the decision may not be returned depending on which SDK scored higher. For this reason, you should configure the threshold for each SDK you are actively using.

---

### Identification pool validation

Please note, this feature is currently only available to projects viacustom configurationin Vulcan. Please raise a PHC ticket if you have any queries about setting this up.

#### Overview

When performing a biometric identification, the pool you are validating against (ie.project, module or user partition) may be empty (containing no enrolment records) or not even synchronised to the device.

For version2024.2.0onwards, SID will now flag when this is the case & may prompt the user to sync records or modules if this has not been done recently.

#### Validation Scenarios

There are several different scenarios where a user might find themselves attempting to identify against an empty or non-existent pool. These are summarised in the flow chart below:
The above chart effectively shows three fundamental reasons that records may not be on the device.

Partition is not synced:When partitioning via user or module; if an identification request is made for a pool which is not synced to the device, SID will alert the user.

[Screenshot: (blue star)]
No records on the device:Having determined that the requested partition is synced to the device, SID will inform the user that whilst the pool they are identifying against is synced, it contains no records.

[Screenshot: (blue star)]
Recent synchronisation window Both of the above scenarios may be caused by the fact that the device hasn’t recently synced. If this is the case, SID will prompt the user to synchronise before repeating the checks on a newly synchronised device.

[Screenshot: (blue star)]
In all cases, the user will be able to continue to proceed with capture having been warned that the pool is empty.

#### Customising a ‘recent synchronisation’

In the above scenarios, SID will check whether a device has been synced in the last 24 hours (and prompt the user to sync if last synced outside of this timeframe). This is the default timescale for this check, but the timescale can be customised on a project-by-project basis:

[Screenshot: Screenshot 2024-09-04 at 17.26.12.png]

See the Vulcan configuration guidancefor more information on customising the max age.

---

## Data Synchronisation

### Sync

Sync is the sharing of data between SID and a server.

In the humanitarian context, SID is frequently used in environments with low/no internet connectivity. Sync was designed with unreliable internet in mind and to take advantage of internet connectivity whenever possible.

### Motivation

- Share biometric templates between different devices running SID
- Backup templates securely to a server
- Receive SID usage analytics

### Sync Functionality

- Sync status
- Manual Data Synchronisation
- Sync Configuration
- Device configuration sync

---

### Sync status

The sync status card provides information on sync progress and enables users to trigger sync manually. It is visible on the SID dashboard whenlogged in.
### Error States

#### No internet connection
#### Select Modules
#### Sync Incomplete

Sync failed for an unknown reason. It can be triggered again with the Try Againbutton.
#### Sync failed

Sync failed due to a cloud error. It can’t be triggered again, users should contact the Simprints team for support.
---

### Sync Configuration

The behaviour of sync is configurable per project in the backend.

### Destination

Specifies where SID should sync data, either:

- Simprints
- Calling App

### Data Types

Specifies which types of data should be synced:

| Type | Subtype | Upload | Download |
| --- | --- | --- | --- |
| Biometrics | Templates | ✅ | ✅ |
| Images* | ✅ | ❌ |  |
| Analytics | Events | ✅ | ❌ |

Biometric Images have additional requirements:

- Biometric modality is either Face or Fingerprint (Vero 2.0 only).
- Images are synced hourly in the background; users can’t trigger sync manually.
- Images cannot be synced to calling apps.

### Frequency

Specifies when sync should occur.

- Regardless of Frequency, sync is always attempted On Internet connectivity changing and becoming available After a Biometric Capture After the Module is changed
- If Frequencyis set to Periodically Every hour
- If Frequencyis set to Periodically and sessions start Every hour Each new session

#### Connection Attempts

Each time sync is attempted:

- SID will attempt to connect every 15 seconds for 5 minutes (60 times).
- At each 15-second interval, SID attempts to connect 5 times.

Therefore, at each trigger, SID will attempt to connect up to 300 times (60 * 5) before failing and waiting for the next sync trigger.

---

### Sync Information

The Sync information screen provides a breakdown of records on a device.

It can be accessed via Dashboard>Settings>Sync Information
### Definitions

| Metric | Definition | Special Requirements |
| --- | --- | --- |
| Total records on device | Number of enrolment records currently on the device |  |
| Records to upload | Number of unsynced enrolment records to upload | Only for sync to Simprints. If thedestinationis set to Calling App, the number will not decrease. |
| Images to upload | Number of unsynced biometric images to upload | Only available in specific conditions, seeconfiguration. |
| Records to download | Number of records from the cloud to be downloaded. | Only displayed when internet connectivity is available. |
| Records to delete | Number of records marked for deletion on the device. | Only displayed when internet connectivity is available. |

### Modules

Modules are only shown when projects areconfiguredto sync by module.
| Selected modules | Number of records in the modules currently selected |
| --- | --- |
| Total records | Number of total records on the device across all modules |

#### Changing Modules

1. Tap the Select Modulesbutton
2. Confirm changes via the confirmation dialog
3. Sync will automatically be triggered

### Refresh Button

The refresh button attempts to retrieve the latest Sync Information. It does nottrigger a manual sync.
---

### Manual Data Synchronisation

This article is a user guide intended for Project Managers and field workers

[Screenshot: (blue star)]

Simprints ID Release2023.1.0introduces Manual Data Synchronisation.Prior to this release, synchronisation (of some or all records) occurred periodically in the background and on session start (depending on yoursynchronisation configuration settings).

For some users in the field, internet connectivity can be sporadic, meaning waiting in a specific location for synchronisation to take place. The ability to manually synchronise records between BFSID and Simprints ID allows those users to update their devices when most convenient for them.
### Using Manual Synchronisation

Manual synchronisation requires no extra configuration in Vulcan and is automatically available to users There are two ways to trigger a manual synchronisation:

Simprints ID Dashboard: When Simprints ID is launched, the dashboard shows a ‘Sync status’ box. When the application is aware that there are records to synchronise, a new ‘SYNC NOW’ button will appear in this box. Pressing this button will synchronise your Simprints ID app with BFSID.

Following synchronisation, this button will disappear, and only re-appear when records are available to synchronise (for example, following offline record collection).
If image upload is enabled for the project, it is already scheduled to run periodically. However, starting fromSID 2025.3.0, Users can also trigger image sync manually from the Sync Info screen.
Simprints ID Settings Menu:Manual synchronisation is available at all times (whether the app detects records to synchronise or not) via the settings menu (Dashboard⋮> Settings > Sync Information > Select Modules). You will also see the details of the records ready to synchronise on this page.

Please note that in versions before SID 2025.3.0, therefreshfunction (accessible via the refresh icon at the top right of this page) only updated the synchronization record statistics . It didnottrigger a manual synchronisation. This button was completely removed in SID 2025.3.0.
### Related articles

- Page:Synchronization configuration

---

## User Interface & Accessibility

### Device Size Optimisation & Landscape Support

This article is a user guide intended for Project Managers and field workers using Simprints ID

[Screenshot: (blue star)]

#### Overview

Simprints ID (SID) has historically been designed specifically for use with mobile phones in portrait mode. Now, in 2024, projects are increasingly using larger-screened tablets, and will often initiate Simprints workflows in landscape mode.

To account for this,SID 2024.2.0and higher will now:

- Scale user interface (UI) to account for larger screens
- Present UI optimised for landscape view
- Initiate workflows in landscape mode
- Switch from portrait to landscape mid-workflow

Size optimisation and landscape support is only currently supported for biometric workflows. Accessing SID, it’s dashboard and menus directly is only available in portrait mode for now

#### Example screens
---

### Exit Form

The exit form is presented when a user:

- Doesn't provide Consent
- Leaves one of the biometric Workflowsbefore it has been completed
#### Actions

To confirm exit and return to the calling app:

- Select a reason
- Write some text in the Additional Informationbox
- Tap Submit

Both a reason and additional information are required for the submit button to activate

To cancel exit and return to the previous screen:

- Tap Capture Biometics

---

## Troubleshooting & Errors

### Alert Screens on SID (Error Colors)

In Simprints ID the error/alert screens usually have a colored background to differentiate between the set of problems that the user is facing. We currently have four types of error screens.

On Clio (our internal analytics monitoring platform), you will find an Alerts tab (in the Project Dashboards). In this tab, you can track the errors/alert types that have occurred in SID within that project. This page expands and explains those alert types.

## Blue error screens

Mostly related to Bluetooth/scanner problems. Current screens include:

| Alert Name | Description |
| --- | --- |
| BLUETOOTH_NOT_SUPPORTED | Bluetooth is not supported on the device. |
| LOW_BATTERY | Scanner battery is low |
| DISCONNECTED | Scanner is off |
| MULTIPLE_PAIRED_SCANNERS | More than one scanner is paired to the phone |
| NOT_PAIRED | Scanner is not paired |
| BLUETOOTH_NOT_ENABLED | Bluetooth not enabled in the settings |
| BLUETOOTH_NO_PERMISSION | The Bluetooth permission is not enabled |
| NFC_NOT_ENABLED | NFC is not enabled |
| NFC_PAIR | The scanner has failed to pair through NFC |
| SERIAL_ENTRY_PAIR | The serial entry pair has been displayed |

## Yellow error screens

Related to configuration errors. Current screens include:

| Alert Name | Description |
| --- | --- |
| DIFFERENT_PROJECT_ID | The project id in the calling app differs from the one used for logging in. |
| DIFFERENT_USER_ID | The user id in the calling app differs from the one used for logging in. |
| INVALID_METADATA | The metadata in the intent is invalid |
| INVALID_MODULE_ID | The module id in the intent is invalid |
| INVALID_PROJECT_ID | The project id in the intent is invalid |
| INVALID_SELECTED_ID | The selected id in the intent is invalid |
| INVALID_SESSION_ID | The session id in the intent is invalid |
| INVALID_USER_ID | The user id in the intent is invalid |
| INVALID_VERIFY_ID | The verify id in the intent is invalid |
| INVALID_STATE_FOR_INTENT_ACTION | Invalid state for intent action, for example a confirmation after an enrolment. |
| PROJECT_PAUSED | The project is currently in paused |

## Grey error screens

Related to data, authentication, or connectivity errors. Current screens include:

| Alert Name | Description |
| --- | --- |
| GUID_NOT_FOUND_ONLINE | The GUID is not found online for the verification“The person selected for verification is not in the database“ |
| GUID_NOT_FOUND_OFFLINE | The GUID is not found offline for the verification“Person not found, please make sure you are connected to the internet and try again” |
| ENROLMENT_LAST_BIOMETRICS_FAILED | Fail to save enrolment record from the current session. |
| FACE_LICENSE_MISSING | The device doesn’t have a face license |
| FACE_LICENSE_INVALID | The device has an invalid face license |
| BACKEND_MAINTENANCE_ERROR | The backend is in maintenance(This alert will only be triggered during the face configuration (i.e. fetching the license) and not log in) |
| Face configuration error | Error code 000means that there was an unexpected error with the server.Error code 001means the authenticated user/device is not authorized to get a license attributed to the specified project, whether it is because the project does not exist, is ended, or because the device is compromised.Error code 002means the license project quota is exceeded. |

Note: The following is from old documentation and probably does not exist anymore, keeping here for posterity and just in case:

- unverified API key
- Safety Net API down, or API key issue - “Please try again in 5-10 minutes” [Depracated and replaced by integrity services API]

## Red error screens

Related to errors that are unexpected and not handled by Simprints ID. One such example is attempting to log in to a production build of SID with a development or staging project ID.

| Alert Name | Description |
| --- | --- |
| UNEXPECTED_ERROR | An unexpected error happens that we have not planned for.Crashes: e.g., an error from the backend or an issue that’s not expected |
| SAFETYNET_ERROR | An error happens when contacting safety net during the login |
| OTA_RECOVERY | This is OTA has failed and we need a reset |
| OTA_FAILED | This is OTA has failed. |
| INTEGRITY_SERVICE_ERROR | An error happens when contacting the integrity service during the login |
| GOOGLE_PLAY_SERVICES_OUTDATED | Google play service is outdated |
| MISSING_GOOGLE_PLAY_SERVICES | Google play service is missing from the device |
| MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP | Google play store app is outdated or missing |

---

### Advanced troubleshooting screen

As of version 2025.1.0, SID includes a hidden screen for advanced troubleshooting.

## Accessing the troubleshooting screen

Some of the information on the screen could be considered internal, so access to the screen must be limited to Simprints employees.

Having the "setting password “ set up in Vulcan is highly recommended.
If not logged in, press the Simprints logo on the “login request” pageten times.
If logged in, pressfive timeson the “Sync & Search Configuration” field in the “About” screen. When prompted, enter the settings password.

## Screen contents

| Tab | Details |
| --- | --- |
|  | The "Overview”tab contains the following information:IDs that could help find the exact device and user in the Big Query Information about the project configuration currently synchronised on the device (ID, time the configuration has been saved in Vulcan and how long ago it was synced)List of licenses available on the specific device Current network conditions as seen by the device (might not be accurate in reality due to some hidden factors).Information about the paired Vero scanner (scanner must be turned on to provide this information).The “Check connection” button sends a ping network request to verify that this specific device can reach Simprint's backend. This is relevant when the device is connected to some modem/router, which does not have access to the internet or specifically Google’s services.The “Export logs” button will initiate the creation of a zip archive with log messages collected over time. Then, the default email client will send the archive to the pre-defined support email address. Zip archive will be protected with the settings password from the project configuration or the project id if the settings password is not set.Log export is only available on devices with Android 8 and above.All of the information on the screen can be highlighted and copied by long pressing any text. |
|  | The “Intents” tab contains a list of all callouts processed by SID in thelast 2 weeks.Each card shows the following information:Session ID for the specific callout (might be the same if multiple follow-up calls were made)Timestamp of the callout The exact intent and caller’s package name The summary of the callout result Contents of the card can be copied to the clipboard by pressing the icon in top left corner Pressing the “Details” button in the bottom left corner will open the full list of the events that were tracked in the specific session (second screenshot).Each event card shows the following information:The exact event key/name Timestamp of start (and end where appropriate) of the tracked event Event IDContents of the event payload (sanitised to avoid showing any sensitive information)Contents of the card can be copied to the clipboard by pressing the icon in top left corner Events are available locally only until they are uploaded to the BFSID. |
|  | The “Network” tab contains a list of all network requests done by SID in thelast 2 weeks.Each card shows the following information:The exact endpoint of the request Timestamp of the request Response code or error Contents of the card can be copied to the clipboard by pressing the icon in top left corner |
|  | The “Workers” tab contains a list of all available information about the background worker execution in SID.Each card shows the following information:The name of the background worker Status of the execution attempt - either Enqued for future work or Succeeded/Failed for past work Approximate time of the next enqueued run (actual time depends on the system)Execution result data for past work Contents of the card can be copied to the clipboard by pressing the icon in top left corner |

---

## Data Migration

### Biometric Database Migration: Realm to Room FAQ 🚀

Welcome to our FAQ guide on the upcoming database migration within the SID 2025.2.0. To ensure continued stability, performance, and support, we are transitioning our biometric record storage from Realm to Room, a Google-supported library. This document answers key questions about this important update.

Q1: Why are we changing the database from Realm?

A: Realm is no longer supported by its developers, meaning it won’t receive future updates or security patches. We're migrating to Room, a database library recommended and maintained by Google. This transition not only ensures long-term stability and performance for Simprints ID but also unblocks us from upgrading critical tools like Kotlin—allowing us to stay current with the latest features and security improvements.

Q2: When will this migration happen?

A: The initial phase of the migration will be part of theSID 2025.2.0 release.In this version, both Realm and Room databases will coexist, with Realm remaining the single source of truthinitially.

Q3: How will the migration be rolled out? Will it affect all users at once?

A: We're taking a cautious approach. We've added two custom configuration flags (see Custom configuration):

- records DbMigration From Realm Enabled:This allows us to enable the migration for a small subset of users initially.
- records DbMigration From Realm Max Retries: This controls the number of times the app will attempt the migration if it encounters an issue. The default is10 retries, with anexponential backoff policy(Backoff Policy.EXPONENTIAL) between attempts. This phased rollout will help us monitor the process and ensure a smooth transition for everyone.

Q4: How will we track the migration progress and any issues?

A: There are two key reports available: one for successfully completed migrations and another for failed migrations. Both reports include details such as the date, device IDs, and app versions involved. Additionally, all migration logs are being sent to Google Analytics to assist our team in quickly identifying and resolving potential issues.

The Completed Migrationsreport is available [here], and the Failed Migrationsreport can be accessed [here].

In production, app events usually appear in Google Analytics within15 to 30 minutes. However, in some cases, it may takeup to a few hours, especially if the app was in the background or the device was offline. If you don’t see the event right away, please check again later.

Q5: How can I check which database is currently active in the SID app?

To check which database is currently active in the SID app, navigate to the Troubleshootingscreen and open theMIGRATIONtab. This section provides key details such as the migration status (e.g., SUCCESS or FAILED), number of retries, and down sync status, along with the active database’s name, version, file path, size, and number of stored subjects. If the Realm database information is shown and the migration status indicatesFAILED, it means the app is still using Realm.
Q6: What are the conditions for the migration to run? How long will it take?

A: The migration will only run when the device hasenough storageand issufficiently charged(Workmangerconstraintsset Requires Storage Not Lowandset Requires Battery Not Low). For a Samsung Galaxy Tab A7 device with 3 Gigi of RAM and 50,000 enrolment records (each containing 2 ROCv3 templates), the migration is expected to takeless than 6 minutesrunning in the background.

Q7: How does the migration handle large amounts of data?

A: To prevent “Out of Memory” (OOM) errors, the migration is processed inbatches of 500 records.

Q8: What happens to the old Realm database during migration? What if an error occurs?

A: The Realm database is not modifiedduring the migration process to ensure its data remains consistent. If an error occurs during the migration to Room, thenew Room database will be reset. This ensures that any subsequent migration attempts start with a clean slate, preventing data corruption.

Q9: Will app functionalities like data syncing be affected?

A: Thedown-sync process(inserting data from the backend into SID) will bepaused during the migrationand automatically resumed once the migration is complete. Any subject enrolled during the migration will be added to both old and new DBs.

Q10: Will Realm be removed immediately after this release?

A: No, we willkeep Realm included in subsequent app releasesfor a period. This will continue until we are confident that all active users have successfully migrated to the new Room database.

Q11: What's the plan if a user's app encounters a non-recoverable issue during or after migration?

A: We've designed the migration process tonot interfere with the up-sync process(uploading user sessions to the backend). This ensures that all user data is safely backed up. In a worst-case scenario where the app becomes unusable, the user candelete the app and reinstall it. Upon reinstallation and login, their data will be synced back down.

Q12: Will users notice any difference in app performance after the migration?

Users may notice faster identification, as our benchmarks show improved performance in this area (exact figures to be added after merging the PRs). Inserting records may take slightly longer, but this only occurs during occasional down syncs or new enrolments, so the overall user experience remains smooth.

Q13: Is there any action required from the user for this migration?

No, the migration happens automatically in the background. Users won’t need to do anything and won’t notice any changes while using the app.

Q14: What happens if a user doesn't update their app to SID 2025.2.0 or later versions?

They may miss important updates and face limited support in the future. We recommend updating for the best experience.

Q15: How does this migration impact data security and privacy?

Just like Realm, Room also supports strong encryption using SQLCipher, which means the biometric data will continue to be protected with industry-standard security. Both systems keep the data encrypted. This change does not reduce privacy or security in any way.

---

## Frequently Asked Questions

**Q: How many fingers should be scanned?**
A: The number is configured per project, typically 2-4 fingers. Follow the on-screen guidance.

**Q: What if the subject refuses consent?**
A: You cannot proceed without consent. Explain the purpose and try again, or skip this person.

**Q: Can I use the app without internet?**
A: Yes, biometric capture and local matching work offline. However, syncing data to the server and downloading configuration require internet.

**Q: What does "No match found" mean?**
A: The biometric search did not find a matching record. The person may not be enrolled yet. You can use "Enrol Last Biometric" to register them.

**Q: How do I update the app?**
A: Updates are distributed through the Google Play Store or your organisation's MDM solution.

**Q: What is the difference between Identification and Verification?**
A: Identification searches all records to find who someone is (1:N search). Verification confirms that a specific person is who they claim to be (1:1 match).

**Q: Why does the scanner LED turn red?**
A: Red indicates a poor quality scan. Ask the subject to clean their finger, ensure it covers the sensor fully, and apply gentle even pressure. See the Vero Scan Feedback section for details on LED colors.

**Q: What are the minimum device requirements?**
A: Android 7.0 (API 24) or higher. At least 2GB RAM recommended. Bluetooth required for Vero fingerprint scanner. Camera required for face capture.

**Q: How do I enable the flashlight during face capture?**
A: The flashlight can be enabled in project configuration. When enabled, the device flash will illuminate in low-light conditions to improve face capture quality.

**Q: What is Multi-Factor Identification (MFID)?**
A: MFID uses multiple biometric modalities (fingerprint + face) together for identification. It improves accuracy by combining match scores from both modalities.

**Q: What should I do if sync is stuck?**
A: Check your internet connection. Try manual sync from the dashboard. If the problem persists, check the sync status screen for error details. Restart the app if needed.

**Q: What is the exit form?**
A: The exit form appears when a user navigates back during a workflow. It asks why they are leaving (e.g., subject refused, technical issue) for analytics purposes.
