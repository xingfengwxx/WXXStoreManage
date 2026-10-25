/**
 * author : 王星星
 * date : 2026-10-25 15:34
 * email : 1099420259@qq.com
 * description : 验证 HTTP 调试日志只遮蔽可重放凭据，保留请求方法、状态码与业务字段。
 */
package com.wangxingxing.wxxstoremanage.core.network.logging

import org.junit.Assert.assertEquals
import org.junit.Test

class HttpLogRedactorTest {

    @Test
    fun `masks form password and token but keeps username`() {
        val message = "POST /user/login\nusername=demo&password=secret&token=abc"

        assertEquals(
            "POST /user/login\nusername=demo&password=***&token=***",
            redactSensitiveLog(message),
        )
    }

    @Test
    fun `masks json password and token but keeps other fields`() {
        val message =
            """{"username":"demo","password":"secret","token":"abc","email":"demo@example.com"}"""

        assertEquals(
            """{"username":"demo","password":"***","token":"***","email":"demo@example.com"}""",
            redactSensitiveLog(message),
        )
    }

    @Test
    fun `masks credential headers case insensitively`() {
        val message = "Cookie: JSESSIONID=abc\nSet-Cookie: JSESSIONID=new; Path=/\nAUTHORIZATION: Bearer value"

        assertEquals(
            "Cookie: ***\nSet-Cookie: ***\nAUTHORIZATION: ***",
            redactSensitiveLog(message),
        )
    }

    @Test
    fun `masks standalone session cookie`() {
        assertEquals(
            "JSESSIONID=***; Path=/",
            redactSensitiveLog("JSESSIONID=abc; Path=/"),
        )
    }

    @Test
    fun `keeps non credential request and business fields`() {
        val message =
            "GET https://wanandroid.com/user/logout/json -> 200 errorCode=0 username=demo email=demo@example.com"

        assertEquals(message, redactSensitiveLog(message))
    }
}
