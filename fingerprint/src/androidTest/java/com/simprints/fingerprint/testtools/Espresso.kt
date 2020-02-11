package com.simprints.fingerprint.testtools
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions

// Using replace to workaround an espresso bug with the keyboard:
// https://stackoverflow.com/questions/20436968/espresso-typetext-not-working
fun typeText(stringToBeTyped: String ): ViewAction =
    ViewActions.replaceText(stringToBeTyped)
