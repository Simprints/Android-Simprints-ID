package com.simprints.infraimages.remote

import com.google.firebase.FirebaseApp
import com.simprints.core.sharedinterfaces.ImageUrlProvider
import com.simprints.infra.login.LoginManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class ImageRemoteDataSourceImplTest {

    companion object {
        private val fbMock = mockk<FirebaseApp>()
    }

    @Test
    fun `test image upload flow`() {

        val imgUrlProviderMock = mockk<ImageUrlProvider>()

        val loginManagerMock = mockk<LoginManager>(relaxed = true)
        every { loginManagerMock.getLegacyAppFallback() } returns fbMock


        val remoteDataSource = ImageRemoteDataSourceImpl(imgUrlProviderMock, loginManagerMock)

        //remoteDataSource.uploadImage(mockk(), mockk())

    }

}
