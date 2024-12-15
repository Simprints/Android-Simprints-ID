package com.simprints.fingerprint.infra.scannermock.simulated.common

enum class SimulatedFinger {
    PERSON_1_FINGER_1_VERSION_1_GOOD_SCAN,
    PERSON_1_FINGER_2_VERSION_1_GOOD_SCAN,
    PERSON_1_FINGER_1_VERSION_2_GOOD_SCAN,
    PERSON_1_FINGER_2_VERSION_2_GOOD_SCAN,
    PERSON_2_FINGER_1_VERSION_1_GOOD_SCAN,
    PERSON_2_FINGER_2_VERSION_1_GOOD_SCAN,
    PERSON_2_FINGER_1_VERSION_2_GOOD_SCAN,
    PERSON_2_FINGER_2_VERSION_2_GOOD_SCAN,
    PERSON_1_FINGER_1_VERSION_1_BAD_SCAN,
    PERSON_1_FINGER_2_VERSION_1_BAD_SCAN,
    NO_FINGER,
    ;

    companion object {
        val person1TwoFingersGoodScan =
            arrayOf(
                PERSON_1_FINGER_1_VERSION_1_GOOD_SCAN,
                PERSON_1_FINGER_2_VERSION_1_GOOD_SCAN,
            )
    }
}
