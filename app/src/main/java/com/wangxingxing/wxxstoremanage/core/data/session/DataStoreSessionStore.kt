/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 会话落盘实现；密文写入独立 DataStore，解密失败视为未登录并清除损坏数据。
 */
package com.wangxingxing.wxxstoremanage.core.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogPhase
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.common.di.IoDispatcher
import com.wangxingxing.wxxstoremanage.core.data.di.SessionDataStore
import java.io.IOException
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Singleton
class DataStoreSessionStore @Inject constructor(
    @param:SessionDataStore private val dataStore: DataStore<Preferences>,
    private val cipher: SessionCipher,
    private val json: Json,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val logger: AppLogger,
) : SessionStore {

    override val session: Flow<StoredSession?> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                logger.log(
                    AppLogEvent(
                        feature = FEATURE,
                        action = ACTION_READ_SESSION,
                        phase = AppLogPhase.Failure,
                        throwable = throwable,
                    ),
                )
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences -> preferences.decodeStoredSession() }
        .flowOn(ioDispatcher)

    override suspend fun read(): StoredSession? = withContext(ioDispatcher) {
        try {
            dataStore.data.first().decodeStoredSession()
        } catch (throwable: IOException) {
            logger.log(
                AppLogEvent(
                    feature = FEATURE,
                    action = ACTION_READ_SESSION,
                    phase = AppLogPhase.Failure,
                    throwable = throwable,
                ),
            )
            null
        }
    }

    override suspend fun save(session: StoredSession) {
        withContext(ioDispatcher) {
            val payload = cipher.encrypt(json.encodeToString(session))
            dataStore.edit { preferences ->
                preferences[SESSION_PAYLOAD] = payload
            }
        }
    }

    override suspend fun clear() {
        withContext(ioDispatcher) {
            dataStore.edit { preferences ->
                preferences.remove(SESSION_PAYLOAD)
            }
        }
    }

    private suspend fun Preferences.decodeStoredSession(): StoredSession? {
        val payload = this[SESSION_PAYLOAD] ?: return null
        return try {
            json.decodeFromString<StoredSession>(cipher.decrypt(payload))
        } catch (throwable: GeneralSecurityException) {
            clearCorruptedSession(throwable)
            null
        } catch (throwable: IllegalArgumentException) {
            // Base64、报文结构或字段校验失败都无法恢复，同样按未登录处理。
            clearCorruptedSession(throwable)
            null
        }
    }

    private suspend fun clearCorruptedSession(cause: Throwable) {
        logger.log(
            AppLogEvent(
                feature = FEATURE,
                action = ACTION_READ_SESSION,
                phase = AppLogPhase.Failure,
                context = mapOf("reason" to "corruptedSession"),
                throwable = cause,
            ),
        )
        try {
            dataStore.edit { preferences ->
                preferences.remove(SESSION_PAYLOAD)
            }
        } catch (throwable: IOException) {
            logger.log(
                AppLogEvent(
                    feature = FEATURE,
                    action = ACTION_CLEAR_SESSION,
                    phase = AppLogPhase.Failure,
                    throwable = throwable,
                ),
            )
        }
    }

    private companion object {
        const val FEATURE = "session"
        const val ACTION_READ_SESSION = "readSession"
        const val ACTION_CLEAR_SESSION = "clearSession"
        val SESSION_PAYLOAD = stringPreferencesKey("session_payload")
    }
}
