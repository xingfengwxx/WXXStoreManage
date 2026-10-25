/**
 * author : 王星星
 * date : 2026-10-25 16:12
 * email : 1099420259@qq.com
 * description : 覆盖 LogcatAppLogger 的超长日志分片逻辑，确保按 UTF-8 字节切分且不破坏多字节字符。
 */
package com.wangxingxing.wxxstoremanage.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogcatAppLoggerTest {

    private val maxBytes = 3_900

    @Test
    fun `text within byte limit stays as single chunk`() {
        val text = "短日志 context=库存"

        assertEquals(listOf(text), text.chunkByUtf8Bytes(maxBytes))
    }

    @Test
    fun `long ascii text is split without losing content`() {
        val text = "a".repeat(10_000)
        val chunks = text.chunkByUtf8Bytes(maxBytes)

        assertTrue(chunks.size > 1)
        assertTrue(chunks.all { it.toByteArray(Charsets.UTF_8).size <= maxBytes })
        assertEquals(text, chunks.joinToString(""))
    }

    @Test
    fun `multibyte text is not split inside a character`() {
        val text = "库存盘点".repeat(2_000)
        val chunks = text.chunkByUtf8Bytes(maxBytes)

        assertTrue(chunks.all { it.toByteArray(Charsets.UTF_8).size <= maxBytes })
        assertEquals(text, chunks.joinToString(""))
    }

    @Test
    fun `surrogate pair stays in the same chunk`() {
        val text = "😀".repeat(2_000)
        val chunks = text.chunkByUtf8Bytes(maxBytes)

        assertTrue(chunks.all { it.toByteArray(Charsets.UTF_8).size <= maxBytes })
        assertEquals(text, chunks.joinToString(""))
    }
}
