package com.simprints.feature.troubleshooting

import android.os.Build
import com.simprints.infra.logging.LogDirectoryProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TroubleshootingModule {
    @IsoDateTimeFormatter
    @Singleton
    @Provides
    fun provideDateTimeFormatter(): SimpleDateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // For some reason offset info is only available from API 24
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS XXX", Locale.US)
    } else {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    }.also { it.timeZone = TimeZone.getDefault() }

    @FileNameDateTimeFormatter
    @Singleton
    @Provides
    fun provideFileNameFormatter(): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)

    @Provides
    fun provideLogDirectoryProvider(): LogDirectoryProvider = LogDirectoryProvider()
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IsoDateTimeFormatter

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FileNameDateTimeFormatter
