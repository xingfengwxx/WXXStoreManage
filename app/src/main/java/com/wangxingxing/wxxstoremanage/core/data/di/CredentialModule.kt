/**
 * author : 王星星
 * date : 2026-10-25 17:27
 * email : 1099420259@qq.com
 * description : 提供独立的登录凭据 DataStore；文件名固定为 credential_store，备份规则必须排除该文件。
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
annotation class CredentialDataStore

private val Context.credentialDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "credential_store",
)

@Module
@InstallIn(SingletonComponent::class)
object CredentialModule {

    @Provides
    @Singleton
    @CredentialDataStore
    fun provideCredentialDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.credentialDataStore
}
