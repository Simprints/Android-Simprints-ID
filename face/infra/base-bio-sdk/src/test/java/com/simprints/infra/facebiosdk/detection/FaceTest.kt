package com.simprints.infra.facebiosdk.detection

import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.face.infra.basebiosdk.detection.Face
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FaceTest {
    @Test
    fun getRelativeBoundingBox() {
        // Given
        val face = Face(
            sourceWidth = 100,
            sourceHeight = 100,
            yaw = 0f,
            roll = 0f,
            quality = 1f,
            template = byteArrayOf(0),
            format = "format",
            absoluteBoundingBox = Rect(0, 0, 50, 100),
        )
        // when
        val relativeBoundingBox = face.relativeBoundingBox
        // Then
        assertThat(relativeBoundingBox.left).isEqualTo(0f)
        assertThat(relativeBoundingBox.top).isEqualTo(0f)
        assertThat(relativeBoundingBox.right).isEqualTo(0.5f)
        assertThat(relativeBoundingBox.bottom).isEqualTo(1f)
    }
}
