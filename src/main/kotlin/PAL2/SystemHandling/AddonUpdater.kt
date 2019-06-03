package PAL2.SystemHandling

import GlobalData
import SystemHandling.checkForUseableDownloads
import javafx.scene.image.Image
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL

/**
 *
 */
fun updateAddon(aid: Int, image: Image)
{
    closeAllAddons()
    GlobalScope.launch {
        val addon = GlobalData.getAddonByID(aid) ?: return@launch
        val download_urls = checkForUseableDownloads(addon.download_urls, addon.aid)

        if (download_urls.size == 1)
        {
            val fd = FileDownloader()
            fd.downloadFileAndInstall(URL(download_urls[0]), GlobalData.temp_down_folder, 1024, image, aid)
        }
    }
}

fun closeAllAddons()
{
    taskKill("autohotkey.exe /F")
    taskKill("Path of Maps Client.exe")
    taskKill("TraderForPoe.exe")
    taskKill("javaw.exe")
    taskKill("java.exe")
    taskKill("POE-Trades-Companion.exe")
    taskKill("XenonTrade.exe")
    taskKill("LabCompass.exe")
    taskKill("Exilence.exe")
    taskKill("CurrencyCop.exe")
    taskKill("PoE Custom Soundtrack.exe")

}

fun taskKill(name: String): Process
{
    if (name == "java")
    {
        taskKill("javaw.exe").waitFor()
        return taskKill("java.exe")
    }
    return Runtime.getRuntime().exec("taskkill /IM \"$name\"")
}