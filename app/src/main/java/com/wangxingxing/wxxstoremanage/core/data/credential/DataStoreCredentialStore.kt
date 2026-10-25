/**
 * author : 王星星
 * date : 2026-10-25 17:27
 * email : 1099420259@qq.com
 * description : 记住的登录凭据落盘实现；账号密码经 Keystore 加密写入独立 DataStore，解密失败按未记住处理并清除坏数据。
 */
package com.wangxingxing.wxxstoremanage.core.data.credential

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogPhase
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.common.di.IoDispatcher
import com.wangxingxing.wxxstoremanage.core.data.di.CredentialDataStore
import com.wangxingxing.wxxstoremanage.core.data.session.SessionCipher
import com.wangxingxing.wxxstoremanage.core.model.CredentialPreference
import com.wangxingxing.wxxstoremanage.core.model.RememberedCredentials
import java.io.IOException
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Singleton
class DataStoreCredentialStore @Inject constructor(
    @param:CredentialDataStore private val dataStore: DataStore<Preferences>,
    private val cipher: SessionCipher,
    private val json: Json,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val logger: AppLogger,
) : CredentialStore {

    override suspend fun read(): CredentialPreference = withContext(ioDispatcher) {
        try {
            val preferences = dataStore.data.first()
            CredentialPreference(
                credentials = preferences.decodeCredentials(),
                rememberEnabled = preferences[REMEMBER_ENABLED] ?: true,
            )
        } catch (throwable: IOException) {
            logger.log(
                AppLogEvent(
                    feature = FEATURE,
                    action = ACTION_READ_CREDENTIALS,
                    phase = AppLogPhase.Failure,
                    throwable = throwable,
                ),
            )
            // 读取失败时保守地不记住，避免在不稳定的存储上悄悄保存密码。
            CredentialPreference(credentials = null, rememberEnabled = false)
        }
    }

    override suspend fun save(credentials: RememberedCredentials) {
        withContext(ioDispatcher) {
            val payload = cipher.encrypt(
                json.encodeToString(
                    StoredCredentials(
                        username = credentials.username,
                        password = credentials.password,
                    ),
                ),
            )
            dataStore.edit { preferences ->
                preferences[CREDENTIALS_PAYLOAD] = payload
                preferences[REMEMBER_ENABLED] = true
            }
        }
    }

    override suspend fun clear() {
        withContext(ioDispatcher) {
            dataStore.edit { preferences ->
                preferences.remove(CREDENTIALS_PAYLOAD)
                preferences[REMEMBER_ENABLED] = false
            }
        }
    }

    private suspend fun Preferences.decodeCredentials(): RememberedCredentials? {
        val payload = this[CREDENTIALS_PAYLOAD] ?: return null
        return try {
            json.decodeFromString<StoredCredentials>(cipher.decrypt(payload))
                .toRememberedCredentials()
        } catch (throwable: GeneralSecurityException) {
            clearCorruptedCredentials(throwable)
            null
        } catch (throwable: IllegalArgumentException) {
            // Base64、报文结构或字段校验失败都无法恢复，同样按未记住处理。
            clearCorruptedCredentials(throwable)
            null
        }
    }

    private suspend fun clearCorruptedCredentials(cause: Throwable) {
        logger.log(
            AppLogEvent(
                feature = FEATURE,
                action = ACTION_READ_CREDENTIALS,
                phase = AppLogPhase.Failure,
                context = mapOf("reason" to "corruptedCredentials"),
                throwable = cause,
            ),
        )
        try {
            dataStore.edit { preferences ->
                preferences.remove(CREDENTIALS_PAYLOAD)
            }
        } catch (throwable: IOException) {
            logger.log(
                AppLogEvent(
                    feature = FEATURE,
                    action = ACTION_CLEAR_CREDENTIALS,
                    phase = AppLogPhase.Failure,
                    throwable = throwable,
                ),
            )
        }
    }

    private companion object {
        const val FEATURE = "credential"
        const val ACTION_READ_CREDENTIALS = "readCredentials"
        const val ACTION_CLEAR_CREDENTIALS = "clearCredentials"
        val CREDENTIALS_PAYLOAD = stringPreferencesKey("credentials_payload")
        val REMEMBER_ENABLED = booleanPreferencesKey("remember_enabled")
    }
}

@Serializable
private data class StoredCredentials(
    val username: String,
    val password: String,
) {
    init {
        require(username.isNotBlank()) { "Credential username must not be blank" }
        require(password.isNotEmpty()) { "Credential password must not be empty" }
    }
}

private fun StoredCredentials.toRememberedCredentials(): RememberedCredentials =
    RememberedCredentials(username = username, password = password)
