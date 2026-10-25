/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 覆盖异常到 AppErrorType 的映射，包括 IO、HTTP 状态码、未知异常与取消重抛。
 */
package com.wangxingxing.wxxstoremanage.core.data.mapper

import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import java.io.IOException
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AppErrorMapperTest {

    @Test
    fun `IOException maps to NetworkUnavailable`() {
        val cause = IOException("offline")

        val error = cause.toAppError()

        assertEquals(AppErrorType.NetworkUnavailable, error.type)
        assertSame(cause, error.cause)
    }

    @Test
    fun `validation status codes map to Validation`() {
        assertEquals(AppErrorType.Validation, httpException(400).toAppError().type)
        assertEquals(AppErrorType.Validation, httpException(422).toAppError().type)
    }

    @Test
    fun `unauthorized maps to SessionExpired`() {
        assertEquals(AppErrorType.SessionExpired, httpException(401).toAppError().type)
    }

    @Test
    fun `not found maps to NotFound`() {
        assertEquals(AppErrorType.NotFound, httpException(404).toAppError().type)
    }

    @Test
    fun `conflict maps to Conflict`() {
        assertEquals(AppErrorType.Conflict, httpException(409).toAppError().type)
    }

    @Test
    fun `server errors map to ServiceUnavailable`() {
        assertEquals(AppErrorType.ServiceUnavailable, httpException(500).toAppError().type)
        assertEquals(AppErrorType.ServiceUnavailable, httpException(503).toAppError().type)
    }

    @Test
    fun `unmapped status code maps to Unknown`() {
        assertEquals(AppErrorType.Unknown, httpException(418).toAppError().type)
    }

    @Test
    fun `unexpected throwable maps to Unknown`() {
        assertEquals(AppErrorType.Unknown, IllegalStateException("boom").toAppError().type)
    }

    @Test
    fun `cancellation is rethrown`() {
        val cancellation = CancellationException("cancelled")

        val thrown = assertThrows(CancellationException::class.java) { cancellation.toAppError() }

        assertSame(cancellation, thrown)
    }

    private fun httpException(code: Int): HttpException = HttpException(
        Response.error<Any>(
            code,
            "".toResponseBody("application/json".toMediaType()),
        ),
    )
}
