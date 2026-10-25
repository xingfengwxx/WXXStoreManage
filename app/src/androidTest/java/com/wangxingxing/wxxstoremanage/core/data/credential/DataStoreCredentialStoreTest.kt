/**
 * author : 王星星
 * date : 2026-10-25 17:27
 * email : 1099420259@qq.com
 * description : 在真实设备上验证凭据以密文落盘、读取可回填，以及清除后保留不记住偏好；不校验 UI。
 */
package com.wangxingxing.wxxstoremanage.core.data.credential

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.data.session.SessionCipher
import com.wangxingxing.wxxstoremanage.core.model.CredentialPreference
import com.wangxingxing.wxxstoremanage.core.model.RememberedCredentials
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataStoreCredentialStoreTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var scope: CoroutineScope
    private lateinit var dataStoreFile: File
    private lateinit var store: DataStoreCredentialStore

    @Before
    fun setUp() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        dataStoreFile = File(temporaryFolder.root, "credential_store.preferences_pb")
        store = DataStoreCredentialStore(
            dataStore = PreferenceDataStoreFactory.create(
                scope = scope,
                produceFile = { dataStoreFile },
            ),
            cipher = SessionCipher(),
            json = Json,
            ioDispatcher = Dispatchers.IO,
            logger = RecordingLogger(),
        )
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun saveEncryptsCredentialsAndRestoresPreference() = runTest {
        val credentials = RememberedCredentials(
            username = "demo-user-name",
            password = "demo-secret-password",
        )

        store.save(credentials)

        val storedBytes = dataStoreFile.readText()
        assertFalse(storedBytes.contains(credentials.username))
        assertFalse(storedBytes.contains(credentials.password))
        assertEquals(
            CredentialPreference(credentials = credentials, rememberEnabled = true),
            store.read(),
        )
    }

    @Test
    fun clearRemovesCredentialsAndKeepsOptOut() = runTest {
        store.save(RememberedCredentials(username = "demo", password = "secret-password"))

        store.clear()

        val preference = store.read()
        assertNull(preference.credentials)
        assertFalse(preference.rememberEnabled)
        assertFalse(dataStoreFile.readText().contains("secret-password"))
    }
}

private class RecordingLogger : AppLogger {

    override fun log(event: AppLogEvent) = Unit

    override fun debug(section: String, message: String) = Unit
}
