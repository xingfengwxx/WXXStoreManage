/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 会话读写契约；会话只经此入口落盘或读取，feature 不得触碰 Cookie 与加密实现。
 */
package com.wangxingxing.wxxstoremanage.core.data.session

import kotlinx.coroutines.flow.Flow

interface SessionStore {
    val session: Flow<StoredSession?>

    suspend fun read(): StoredSession?

    suspend fun save(session: StoredSession)

    suspend fun clear()
}
