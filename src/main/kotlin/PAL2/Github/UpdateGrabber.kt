package PAL2.Github

import Github.connect
import SystemHandling.deleteFile
import SystemHandling.unzip
import SystemHandling.verifyFolder
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL

/**
 *
 */
fun getUpdate(install: String)
{
    // Get Token from DB if DB exists and if token is != ""
    if (!GlobalData.debugging)
    {
        val gh = connect()
        if (gh.rateLimit.remaining > 5)
        {
            val repo = gh.getRepository("POE-Addon-Launcher/PAL2")
            val latest = repo.latestRelease
            val latest_tag = latest.tagName
            if(latest_tag != GlobalData.version)
            {
                val down_folder = File("${GlobalData.pal_folder}")

                val temp_update = File("$install${File.separator}latest")

                verifyFolder(down_folder)
                verifyFolder(temp_update)

                val dest = File("${down_folder.path}${File.separator}pal.zip")
                if (dest.exists())
                {
                    deleteFile(dest)
                }

                val download_url = latest.assets[0].browserDownloadUrl
                FileUtils.copyURLToFile(URL(download_url), dest)

                println("Unzipping: ${dest.path} in ${temp_update.path}")
                unzip(dest, temp_update)

                val stateONE = File("${GlobalData.pal_folder.path}${File.separator}1.state")
                stateONE.createNewFile()
                // Launch the newest version instead.
                Runtime.getRuntime().exec("\"$temp_update${File.separator}PAL2.exe\"")

                System.exit(99)
            }
        }
    }
}