package PAL2.Github

import Github.connect
import SystemHandling.deleteFile
import SystemHandling.unzip
import SystemHandling.verifyFolder
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL

private val logger = KotlinLogging.logger {}

/**
 *
 */
fun checkUpdate()
{
    if (!GlobalData.debugging)
    {
        val gh = connect()
        if (gh.rateLimit.remaining > 3)
        {
            val repo = gh.getRepository("")
        }
    }
}

fun getUpdate()
{
    // Get Token from DB if DB exists and if token is != ""
    if (!GlobalData.debugging)
    {
        val gh = connect()
        if (gh.rateLimit.remaining > 3)
        {
            val repo = gh.getRepository("POE-Addon-Launcher/PALRelease")
            val latest = repo.latestRelease
            val latest_tag = latest.tagName
            if(latest_tag != GlobalData.version)
            {
                val down_folder = File("${GlobalData.pal_folder}")
                verifyFolder(down_folder)

                val dest = File("${GlobalData.install_dir}${File.separator}new${File.separator}PAL2.jar")
                val destFolder = File("${GlobalData.install_dir}${File.separator}new")
                if (dest.exists())
                    deleteFile(dest)

                if (!destFolder.exists())
                    destFolder.mkdir()

                val download_url = latest.assets[0].browserDownloadUrl
                FileUtils.copyURLToFile(URL(download_url), dest)

                logger.debug { "Downloaded update [$latest_tag] to: $dest" }

                //rundll32 url.dll,FileProtocolHandler
                println(">>> \"${GlobalData.install_dir}${File.separator}PAL2.exe\" <<")
                Runtime.getRuntime().exec("\"${GlobalData.install_dir}${File.separator}PAL2.exe\"")

                System.exit(99)
            }
        }
    }
}