package PAL2.Filters

import PAL_DataClasses.Filter
import SystemHandling.deleteFile
import mu.KotlinLogging
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files

/**
 *
 */
private val logger = KotlinLogging.logger {}

object FilterDownloader
{
    val URL_FB_API = "https://filterblast.xyz/api/FilterFile/?filter=!K&preset=!P"

    fun updateFilter(filter: Filter): File?
    {
        val url = URL(filter.webSource)
        val httpConnection = url.openConnection() as HttpURLConnection
        httpConnection.addRequestProperty("User-Agent", "Mozilla/4.0")
        val f = File(filter.path)

        if (f.exists())
        {
            deleteFile(f)
            Files.copy(httpConnection.inputStream, f.toPath())
            return f
        }
        logger.error {"Attempting to update a filter that doesn't exist. ${filter.name}"}
        return null
    }

    fun downloadFilter(filterVar: String, name: String, filterBlastFilter: FilterBlastFilter): Filter
    {
        val url_safe = filterVar.replace(" ", "%20")
        val sub_url = URL_FB_API.replace("!P", url_safe).replace("!K", name)
        val url = URL(sub_url)
        val httpConnection = url.openConnection() as HttpURLConnection
        httpConnection.addRequestProperty("User-Agent", "Mozilla/4.0")
        val f = File(GlobalData.loot_filter_path + File.separator + buildFileName(name, filterVar))

        if (f.exists())
            deleteFile(f)

        Files.copy(httpConnection.inputStream, f.toPath())
        return FilterContainer.makeAnchorData(filterBlastFilter, f, sub_url, filterVar)
    }

    fun buildFileName(name: String, key: String): String
    {
        return "$name - $key.filter"
    }
}