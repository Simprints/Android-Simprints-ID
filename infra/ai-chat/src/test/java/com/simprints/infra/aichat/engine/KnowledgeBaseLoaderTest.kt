package com.simprints.infra.aichat.engine

import android.content.Context
import android.content.res.AssetManager
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class KnowledgeBaseLoaderTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var assetManager: AssetManager

    private lateinit var loader: KnowledgeBaseLoader

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.assets } returns assetManager
        loader = KnowledgeBaseLoader(context)
    }

    @Test
    fun `loads knowledge base from assets`() {
        val kbContent = "# Test Knowledge Base\nSome content here."
        val refContent = "# Internal Reference"
        every { assetManager.open("knowledge_base.md") } returns ByteArrayInputStream(kbContent.toByteArray())
        every { assetManager.open("internal_reference.md") } returns ByteArrayInputStream(refContent.toByteArray())

        val result = loader.load()

        assertThat(result).contains(kbContent)
        assertThat(result).contains(refContent)
    }

    @Test
    fun `caches result after first load`() {
        val kbContent = "Cached content"
        val refContent = "Reference content"
        every { assetManager.open("knowledge_base.md") } returns ByteArrayInputStream(kbContent.toByteArray())
        every { assetManager.open("internal_reference.md") } returns ByteArrayInputStream(refContent.toByteArray())

        val first = loader.load()
        val second = loader.load()

        assertThat(first).isEqualTo(second)
        verify(exactly = 1) { assetManager.open("knowledge_base.md") }
    }
}
