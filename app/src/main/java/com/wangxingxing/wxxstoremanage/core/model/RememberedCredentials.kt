/**
 * author : 王星星
 * date : 2026-10-25 17:27
 * email : 1099420259@qq.com
 * description : 登录表单可回填的账号与密码；只用于本机回填，禁止写入日志或分析事件。
 */
package com.wangxingxing.wxxstoremanage.core.model

data class RememberedCredentials(
    val username: String,
    val password: String,
)
