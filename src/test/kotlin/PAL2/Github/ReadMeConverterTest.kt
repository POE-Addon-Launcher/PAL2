package PAL2.Github

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.junit.Assert.*
import org.junit.Test
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 *
 */
class ReadMeConverterTest
{
    @Test
    fun testReadMeDownloading()
    {
        val url = "https://raw.githubusercontent.com/Exslims/MercuryTrade/master/README.md"
        val httpConnection = URL(url).openConnection() as HttpURLConnection
        httpConnection.addRequestProperty("User-Agent", "Mozilla/4.0")
        val input = BufferedInputStream(httpConnection.inputStream)
        val str = String(input.readBytes())
        val parser = Parser.builder().build()
        val doc = parser.parse(str)
        val renderer = HtmlRenderer.builder().build()
        val html = renderer.render(doc)
        println(html)
    }
}