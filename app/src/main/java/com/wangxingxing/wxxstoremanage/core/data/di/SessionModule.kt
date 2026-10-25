/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 提供独立的会话 DataStore；文件名固定为 session_store，备份规则必须排除该文件。
 */
package com.wangxingxing.wxxstoremanage.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SessionDataStore

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session_store",
)

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides
    @Singleton
    @SessionDataStore
    fun provideSessionDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.sessionDataStore
}
