package com.simprints.fingerprint.activities.alert

import com.google.common.truth.Truth
import com.simprints.fingerprint.activities.alert.AlertError.ButtonAction.*
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEvent
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class AlertPresenterTest {

    private val currentTime: Long = 123
    private lateinit var alertPresenter: AlertPresenter
    private val view: AlertContract.View = mockk(relaxed = true)

    private var alertEvent: AlertScreenEvent? = null
    private val sessionManager: FingerprintSessionEventsManager = mockk {
        every { addEventInBackground(any()) } answers {
            alertEvent = args[0] as AlertScreenEvent
        }
    }
    private val timeHelper: FingerprintTimeHelper = mockk {
        every { now() } returns currentTime
    }

    @Before
    fun setUp() {
        alertPresenter =
            AlertPresenter(view, FingerprintAlert.UNEXPECTED_ERROR, sessionManager, timeHelper)
    }

    @Test
    fun start() {
        //When
        alertPresenter.start()
        //Then
        verify { view.initLeftButton(any()) }
        verify { view.initRightButton(any()) }
        verify { sessionManager.addEventInBackground(any()) }
        Truth.assertThat(alertEvent?.alertType).isEqualTo(FingerprintAlert.UNEXPECTED_ERROR)
        Truth.assertThat(alertEvent?.startTime).isEqualTo(currentTime)

    }

    @Test
    fun handleButtonClick() {
        alertPresenter.handleButtonClick(None)
        verify(exactly = 0) { view.openBluetoothSettings() }

        alertPresenter.handleButtonClick(WifiSettings)
        verify { view.openWifiSettings() }

        alertPresenter.handleButtonClick(BluetoothSettings)
        verify { view.openBluetoothSettings() }

        alertPresenter.handleButtonClick(TryAgain)
        verify { view.finishWithAction(AlertTaskResult.CloseButtonAction.TRY_AGAIN) }

        alertPresenter.handleButtonClick(PairScanner)
        verify { view.openBluetoothSettings() }

        alertPresenter.handleButtonClick(Close)
        verify { view.finishWithAction(AlertTaskResult.CloseButtonAction.CLOSE) }
    }
    @Test
    fun `handleButtonClicked for low battery allerts opens the refusal activity`(){
        alertPresenter =
            AlertPresenter(view, FingerprintAlert.LOW_BATTERY, sessionManager, timeHelper)
        alertPresenter.handleButtonClick(Close)
        verify { view.startRefusalActivity() }
    }

    @Test
    fun `handleBackPressed for other alerts starts the refusal activity`() {
        alertPresenter =
            AlertPresenter(view, FingerprintAlert.DISCONNECTED, sessionManager, timeHelper)

        alertPresenter.handleBackPressed()
        verify { view.startRefusalActivity() }
    }

    @Test
    fun `handleBackPressed for UNEXPECTED_ERROR`() {
        alertPresenter.handleBackPressed()
        verify { view.finishWithAction(AlertTaskResult.CloseButtonAction.BACK) }
    }


    @Test
    fun `handleOnResume does nothing if bluetooth settings is closed`() {
        //When 
        alertPresenter.handleOnResume()
        //
        verify(exactly = 0) { view.finishWithAction(any()) }
    }

    @Test
    fun `handleOnResume if bluetooth settings is opened,should call finish with action`() {
        //Given
        alertPresenter.handleButtonClick(PairScanner)
        //When 
        alertPresenter.handleOnResume()
        //
        verify { view.finishWithAction(any()) }
    }
}
