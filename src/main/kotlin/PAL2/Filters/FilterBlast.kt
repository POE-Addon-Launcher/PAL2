package PAL2.Filters

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList

/**
 *
 */
data class FilterBlastFilter(
        val name: String,
        val version: String,
        val lastUpdate: String,
        val poe_version: String,
        val forumThread: String,
        val variations: ArrayList<FilterBlastVariation>
                            )
{
    override fun toString(): String
    {
        return name
    }
}

data class FilterBlastVariation(val key: String, val fileName: String)


object FilterBlast
{
    private val FILTER_LIST = "https://filterblast.oversoul.xyz/api/ListFilters/"

    private fun create(input: String): FilterBlastVariation
    {
        var process = input.replace("{", "")
        process = process.replace("}", "")
        process = process.replace("\"", "")
        val splits = process.split(":")
        return FilterBlastVariation(splits[0], splits[1])
    }

    fun downloadListOfFilters(): ArrayList<FilterBlastFilter>
    {
        val list = ArrayList<FilterBlastFilter>()

        val url = URL(FILTER_LIST)

        val httpcon = url.openConnection() as HttpURLConnection
        httpcon.addRequestProperty("User-Agent", "Mozilla/4.0")

        var foo = convertStreamToString(httpcon.inputStream)
        // Remove hidden character from the start.
        foo = findValidString(foo)

        //System.out.println(foo);

        val objectMapper = ObjectMapper()
        val node = objectMapper.readTree(foo)
        val elements = node.elements()
        while (elements.hasNext())
        {
            val variations = ArrayList<String>()
            val n = elements.next()
            val parts = n.get("Presets").toString().split(",")
            val arr = ArrayList<FilterBlastVariation>()

            for (str in parts)
            {
                arr.add(create(str))
            }


            val presets = n.get("Presets").elements()
            while (presets.hasNext())
            {
                val no = presets.next()
                variations.add(no.toString().replace("\"", ""))
            }

            val f = FilterBlastFilter(findKey(n.get("Name").toString().replace("\"", "")), n.get("Version").toString(), n.get("LastUpdate").toString(), n.get("PoEVersion").toString(), n.get("ForumThread").toString(), arr)
            list.add(f)
        }
        return list
    }

    private fun findKey(name: String): String
    {
        return if (name.split("'")[0] == "Highwind")
            "ffhighwind"
        else if (name.split("'")[0] == "Lumpa")
            "Lumpaa"
        else if (name.split("'")[0] == "Dsgreat")
            "Dsgreat3"
        else if (name.split("'")[0] == "Ment")
            "ment2008"
        else if (name.split("'")[0] == "Vexi")
            "Vexivian"
        else if (name == "Sayk Loot Filters")
            "Sayk"
        else if (name == "PoE Default Filter")
            "Default"
        else
            name.split("'")[0]
    }

    /**
     * Filterblast has some weird things where it adds some weird characters we're gonna try to attempt to remove them.
     * @return
     */
    fun findValidString(`in`: String): String
    {
        var count = 0
        for (c in 0 until `in`.length)
        {
            if (`in`[c] == '{')
            {
                break
            }
            else
            {
                count++
            }
        }
        return `in`.substring(count)
    }

    fun convertStreamToString(`is`: java.io.InputStream): String
    {
        val s = java.util.Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}