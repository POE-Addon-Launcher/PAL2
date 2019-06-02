package SystemHandling

import mu.KotlinLogging
import net.lingala.zip4j.core.ZipFile
import java.io.File
import java.net.URL

private val logger = KotlinLogging.logger {}

/**
 * Verify if a folder exists if it doesn't deletes it.
 */
fun verifyFolder(file: File)
{
    if (!file.exists())
    {
        logger.debug { "Creating folder: \"${file.path}\"" }
        file.mkdir()
    }
}

/**
 * WARNING: Recursively deletes all underlying folders aswell!
 */
fun deleteFile(file: File)
{
    if (file.exists())
    {
        if (file.isDirectory)
        {
            for (f in file.listFiles())
            {
                deleteFile(f)
            }
        }
        logger.debug { "Deleting: ${file.path}" }
        file.delete()
    }
}

fun checkForUseableDownloads(download_urls: Array<String>, aid: Int): Array<String>
{
    when (aid)
    {
        2 -> return usableDownloadsWithExtension(download_urls, "zip")
        5 -> return usableDownloadsWithExtension(download_urls, "zip")
        8 -> return usableDownloadsWithExtension(download_urls, "exe")
        10 -> return usableDownloadsWithExtension(download_urls, "exe")
        11 -> return usableDownloadsWithExtension(download_urls, "exe")
        else -> return defaultUsableDownloads(download_urls)
    }
}

fun removeTempDownloads()
{
    if (GlobalData.temp_down_folder.exists() && GlobalData.temp_down_folder.isDirectory)
    {
        deleteFile(GlobalData.temp_down_folder)
        GlobalData.temp_down_folder.mkdir()
    }
}

fun usableDownloadsWithExtension(download_urls: Array<String>, extension_format: String): Array<String>
{
    val arr = ArrayList<String>()

    for (dl in download_urls)
    {
        val splits = dl.split(".")
        val extension = splits[splits.size-1]

        when (extension)
        {
            extension_format -> arr.add(dl)
        }

    }
    return arr.toTypedArray()
}

fun unzip(archive: File, destination: File)
{
    val zip = ZipFile(archive)
    zip.extractAll(destination.path)
    deleteFile(archive)
}

fun defaultUsableDownloads(download_urls: Array<String>): Array<String>
{
    val arr = ArrayList<String>()

    for (dl in download_urls)
    {
        val splits = dl.split(".")
        val extension = splits[splits.size-1]

        when (extension)
        {
            "exe" -> arr.add(dl)
            "zip" -> arr.add(dl)
            "jar" -> arr.add(dl)
            "ahk" -> arr.add(dl)
            "msi" -> arr.add(dl)
        }

    }
    return arr.toTypedArray()
}