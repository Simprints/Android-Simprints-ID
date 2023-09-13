package com.simprints.feature.orchestrator.cache

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepStatus
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParcelableConverterTest {

    private lateinit var parcelableConverter: ParcelableConverter

    @Before
    fun setUp() {
        parcelableConverter = ParcelableConverter()
    }

    @Test
    fun `Correctly marshals and unmarshalls the action request`() {
        val request = ActionRequest.EnrolActionRequest(
            actionIdentifier = ActionRequestIdentifier("action", "package"),
            projectId = "projectId",
            userId = "userId",
            moduleId = "moduleId",
            metadata = "metadata",
            unknownExtras = listOf("key" to "value", "key2" to 42),
        )

        val bytes = parcelableConverter.marshall(OrchestratorCache.ActionRequestWrapper(request))
        val resultRequest = parcelableConverter.unmarshall(bytes, OrchestratorCache.ActionRequestWrapper.CREATOR).request

        with(resultRequest as ActionRequest.EnrolActionRequest) {
            assertThat(actionIdentifier).isEqualTo(ActionRequestIdentifier("action", "package"))
            assertThat(projectId).isEqualTo("projectId")
            assertThat(userId).isEqualTo("userId")
            assertThat(moduleId).isEqualTo("moduleId")
            assertThat(metadata).isEqualTo("metadata")
            assertThat(unknownExtras).containsExactly("key" to "value", "key2" to 42)
        }
    }

    @Test
    fun `Correctly marshals and unmarshalls the step`() {
        val step = Step(
            navigationActionId = 42,
            destinationId = 33,
            payload = bundleOf("key" to "value"),
            status = StepStatus.IN_PROGRESS,
            resultType = StubParcelable::class.java,
            result = null,
        )

        val bytes = parcelableConverter.marshall(step)
        val resultStep = parcelableConverter.unmarshall(bytes, Step.CREATOR)

        with(resultStep) {
            assertThat(navigationActionId).isEqualTo(42)
            assertThat(destinationId).isEqualTo(33)
            assertThat(payload.getString("key")).isEqualTo("value")
            assertThat(status).isEqualTo(StepStatus.IN_PROGRESS)

            assertThat(resultType).isEqualTo(StubParcelable::class.java)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `Correctly marshals and unmarshalls the step with result`() {
        val step = Step(
            navigationActionId = 42,
            destinationId = 33,
            payload = bundleOf("key" to "value"),
            status = StepStatus.IN_PROGRESS,
            resultType = StubParcelable::class.java,
            result = StubParcelable(1, "text"),
        )

        val bytes = parcelableConverter.marshall(step)
        val resultStep = parcelableConverter.unmarshall(bytes, Step.CREATOR)

        with(resultStep) {
            assertThat(navigationActionId).isEqualTo(42)
            assertThat(destinationId).isEqualTo(33)
            assertThat(payload.getString("key")).isEqualTo("value")
            assertThat(status).isEqualTo(StepStatus.IN_PROGRESS)

            assertThat(resultType).isEqualTo(StubParcelable::class.java)

            assertThat(result).isInstanceOf(StubParcelable::class.java)
            with(result as StubParcelable) {
                assertThat(number).isEqualTo(1)
                assertThat(text).isEqualTo("text")
            }
        }
    }

    private class StubParcelable(
        val number: Int,
        val text: String,
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString().orEmpty()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(number)
            parcel.writeString(text)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<StubParcelable> {
            override fun createFromParcel(parcel: Parcel): StubParcelable = StubParcelable(parcel)
            override fun newArray(size: Int): Array<StubParcelable?> = arrayOfNulls(size)
        }
    }

}
