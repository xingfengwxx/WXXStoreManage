/**
 * author : 王星星
 * date : 2026-10-25 17:27
 * email : 1099420259@qq.com
 * description : 记住的登录凭据读写契约；凭据只经此入口落盘或读取，feature 不得接触密文与 Keystore。
 */
package com.wangxingxing.wxxstoremanage.core.data.credential

import com.wangxingxing.wxxstoremanage.core.model.CredentialPreference
import com.wangxingxing.wxxstoremanage.core.model.RememberedCredentials

interface CredentialStore {
    suspend fun read(): CredentialPreference

    suspend fun save(credentials: RememberedCredentials)

    suspend fun clear()
}
