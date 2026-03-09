package com.simprints.infra.aichat

import android.content.Context
import com.simprints.infra.aichat.database.ChatDao
import com.simprints.infra.aichat.database.ChatDatabase
import com.simprints.infra.aichat.database.FaqDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AiChatModule {
    @Provides
    @Singleton
    fun provideChatDatabase(
        @ApplicationContext context: Context,
    ): ChatDatabase = ChatDatabase.build(context)

    @Provides
    @Singleton
    fun provideChatDao(database: ChatDatabase): ChatDao = database.chatDao

    @Provides
    @Singleton
    fun provideFaqDao(database: ChatDatabase): FaqDao = database.faqDao
}
