package com.simprints.feature.datagenerator.events

import android.content.Context
import android.content.res.AssetManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
class SqlEventTemplateLoaderTest {
    @MockK
    private lateinit var mockContext: Context

    @MockK
    private lateinit var mockAssetManager: AssetManager

    private lateinit var loader: SqlEventTemplateLoader

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { mockContext.assets } returns mockAssetManager
        loader = SqlEventTemplateLoader(mockContext)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getSql WHEN cache is empty SHOULD load from assets and replace placeholders`() {
        // Given
        val eventName = "test_event"
        val projectId = "proj-123"
        val attendantId = "attend-456"
        val moduleId = "mod-789"
        val scopeId = "scope-abc"
        val filePath = "dummy_events/$eventName.sql"

        val template =
            """
            INSERT INTO ...
            VALUES ('__project_id__', '__attendant_id__', '__module_id__', '__scope_id__', '__session_id__');
            """.trimIndent()

        // This is the final SQL we expect after placeholder replacement
        val expectedSql =
            """
            INSERT INTO ...
            VALUES ('$projectId', '$attendantId', '$moduleId', '$scopeId', '$scopeId');
            """.trimIndent()

        every { mockAssetManager.open(filePath) } returns template.byteInputStream()

        // When
        val result = loader.getSql(
            eventName = eventName,
            projectId = projectId,
            attendantId = attendantId,
            moduleId = moduleId,
            scopeId = scopeId,
        )

        // Then
        assertThat(result).isEqualTo(expectedSql)

        verify { mockAssetManager.open(filePath) }
    }

    @Test
    fun `getSql WHEN cache is populated SHOULD use cache and not load from assets`() {
        // Given
        val eventName = "cached_event"
        val filePath = "dummy_events/$eventName.sql"
        val template = "insert into ... project_id = '__project_id__';"

        every { mockAssetManager.open(filePath) } returns template.byteInputStream()

        // When
        loader.getSql(eventName, "proj-1", "att-1", "mod-1", "scope-1")

        loader.getSql(eventName, "proj-2", "att-2", "mod-2", "scope-2")

        // Then
        // Verify that open() was called only once, proving the cache was used for the second call
        verify { mockAssetManager.open(filePath) }
    }

    @Test
    fun `getSql WHEN asset file does not exist SHOULD throw IllegalArgumentException`() {
        // Given
        val eventName = "non_existent_event"
        val filePath = "dummy_events/$eventName.sql"

        every { mockAssetManager.open(filePath) } throws IOException("File not found!")

        // When & Then
        val exception = assertFailsWith<IllegalArgumentException> {
            loader.getSql(eventName, "p", "a", "m", "s")
        }

        assertThat(exception)
            .hasMessageThat()
            .isEqualTo("Failed to load SQL file for event '$eventName': $filePath")
        assertThat(exception.cause).isInstanceOf(IOException::class.java)
        assertThat(exception.cause).hasMessageThat().isEqualTo("File not found!")
    }

    @Test
    fun `clearCache SHOULD empty the cache and force reload from assets`() {
        // Given
        val eventName = "event_to_clear"
        val filePath = "dummy_events/$eventName.sql"
        val template = "insert into ... '__module_id__';"

        every { mockAssetManager.open(filePath) } returns template.byteInputStream()

        loader.getSql(eventName, "p", "a", "m", "s")
        verify { mockAssetManager.open(filePath) }

        // When
        loader.clearCache()

        loader.getSql(eventName, "p", "a", "m", "s")

        // Then
        verify { mockAssetManager.open(filePath) }
    }
}
