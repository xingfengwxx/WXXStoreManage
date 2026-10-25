/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 玩Android 统一响应包；注意业务失败同样返回 HTTP 200，必须优先判断 errorCode。
 */
package com.wangxingxing.wxxstoremanage.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val data: T? = null,
    val errorCode: Int,
    val errorMsg: String = "",
)
