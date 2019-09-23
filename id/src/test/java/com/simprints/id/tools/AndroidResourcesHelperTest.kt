package com.simprints.id.tools

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.core.tools.LanguageHelper
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class AndroidResourcesHelperTest {

    private val ctx: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun getStringPlural(){
        val androidResourcesHelper = AndroidResourcesHelperImpl(ctx)
        val translated = androidResourcesHelper.getStringPlural(R.string.loaded_candidates_quantity_key, 1, arrayOf(1))
        Truth.assertThat(translated).isEqualTo("Loaded 1 candidate.")
    }

    @Test
    fun getStringPluralMissingTranslation(){
        val localCtx = LanguageHelper.contextWithSpecificLanguage(ctx, "bn")
        val androidResourcesHelper = AndroidResourcesHelperImpl(localCtx)
        val translated = androidResourcesHelper.getStringPlural(R.string.loaded_candidates_quantity_key, 1, arrayOf(1))
        Truth.assertThat(translated).isEqualTo("Loaded 1 candidate.")
    }

    @Test
    fun getStringPluralDifferentTranslation(){
        val ctx = ApplicationProvider.getApplicationContext<Application>()
        val localCtx = LanguageHelper.contextWithSpecificLanguage(ctx, "fa")
        val androidResourcesHelper = AndroidResourcesHelperImpl(localCtx)
        val translated = androidResourcesHelper.getStringPlural(R.string.loaded_candidates_quantity_key, 1, arrayOf(1))
        Truth.assertThat(translated).isEqualTo("فرد یا اشتراک کننده آماده است 1")
    }
}
