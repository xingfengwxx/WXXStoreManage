/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 绑定仓库接口与实现，维持 feature 对抽象的数据访问依赖。
 */
package com.wangxingxing.wxxstoremanage.core.data.di

import com.wangxingxing.wxxstoremanage.core.data.credential.CredentialStore
import com.wangxingxing.wxxstoremanage.core.data.credential.DataStoreCredentialStore
import com.wangxingxing.wxxstoremanage.core.data.repository.AuthRepository
import com.wangxingxing.wxxstoremanage.core.data.repository.AuthRepositoryImpl
import com.wangxingxing.wxxstoremanage.core.data.repository.StockRepository
import com.wangxingxing.wxxstoremanage.core.data.repository.StockRepositoryImpl
import com.wangxingxing.wxxstoremanage.core.data.repository.WarehouseRepository
import com.wangxingxing.wxxstoremanage.core.data.repository.WarehouseRepositoryImpl
import com.wangxingxing.wxxstoremanage.core.data.session.BusinessDataCleaner
import com.wangxingxing.wxxstoremanage.core.data.session.DataStoreSessionStore
import com.wangxingxing.wxxstoremanage.core.data.session.DefaultSessionCleaner
import com.wangxingxing.wxxstoremanage.core.data.session.RoomBusinessDataCleaner
import com.wangxingxing.wxxstoremanage.core.data.session.SessionCleaner
import com.wangxingxing.wxxstoremanage.core.data.session.SessionStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindWarehouseRepository(impl: WarehouseRepositoryImpl): WarehouseRepository

    @Binds
    @Singleton
    abstract fun bindStockRepository(impl: StockRepositoryImpl): StockRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSessionStore(impl: DataStoreSessionStore): SessionStore

    @Binds
    @Singleton
    abstract fun bindCredentialStore(impl: DataStoreCredentialStore): CredentialStore

    @Binds
    @Singleton
    abstract fun bindSessionCleaner(impl: DefaultSessionCleaner): SessionCleaner

    @Binds
    @Singleton
    abstract fun bindBusinessDataCleaner(impl: RoomBusinessDataCleaner): BusinessDataCleaner
}
