package PAL2.Github

import PAL_DataClasses.PAL_AddonFullData
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 *
 */
object ReadMeConverter
{
    fun exceptions(a: PAL_AddonFullData): String
    {
        return when (a.aid)
        {
            6 -> "https://raw.githubusercontent.com/PoE-TradeMacro/POE-TradeMacro/master/README.markdown"
            8 -> "https://raw.githubusercontent.com/klayveR/xenontrade/master/readme.md"
            9 -> "https://gist.githubusercontent.com/POE-Addon-Launcher/2687293a2e255d394e16895a5bcd180d/raw/685db0260292a175270f1ca3922ee07acc73ab6c/lutbot.md"
            12 -> "https://gist.githubusercontent.com/POE-Addon-Launcher/0db7512cdfc44afdca3a66b6c8c240d8/raw/dc463bc87006fe5913c8ddd94c384b5be1bf9298/nope.md"
            17 -> "https://gist.githubusercontent.com/POE-Addon-Launcher/0db7512cdfc44afdca3a66b6c8c240d8/raw/dc463bc87006fe5913c8ddd94c384b5be1bf9298/nope.md"
            else -> "https://raw.githubusercontent.com/${a.gh_username}/${a.gh_reponame}/master/README.md"
        }
    }

    fun convert(a: PAL_AddonFullData): String
    {
        val url = exceptions(a)
        val httpConnection = URL(url).openConnection() as HttpURLConnection
        httpConnection.addRequestProperty("User-Agent", "Mozilla/4.0")
        val input = BufferedInputStream(httpConnection.inputStream)
        val str = String(input.readBytes())
        val parser = Parser.builder().build()
        val doc = parser.parse(str)
        val renderer = HtmlRenderer.builder().build()
        val html = renderer.render(doc)
        return html
    }
}