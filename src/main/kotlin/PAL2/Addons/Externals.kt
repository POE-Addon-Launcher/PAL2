package PAL2.Addons

import GlobalData
import PAL2.Database.countAHKs
import PAL2.Database.countExternalAddon
import PAL2.Database.getAHKScriptsArray
import PAL2.Database.nukeAHK
import PAL2.GUI.CoreApplication
import PAL2.SystemHandling.FileDownloader
import PAL2.SystemHandling.InstallHandlerHelpers
import PAL_DataClasses.PAL_External_Addon
import SystemHandling.deleteFile
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.CRC32

/**
 *
 */
object Externals
{
    val LUTBOT_DL = "http://lutbot.com/ahk/macro.ahk"
    val SIC_DL = "https://synthesisparser.herokuapp.com/SynthesisParser.ahk"

    fun calcCRC32(bytes: ByteArray): String
    {
        val hash = CRC32()
        hash.update(bytes)
        return hash.value.toString(16)
    }

    fun calcCRC32(file: File): String
    {
        val bytes = Files.readAllBytes(Paths.get(file.toURI()))
        val hash = CRC32()
        hash.update(bytes)
        return hash.value.toString(16)
    }

    fun determineCMD(file: File): String
    {
        return when (file.extension)
        {
            "exe" -> InstallHandlerHelpers.createExeLaunchCommandWithElevation(file.path)
            "ahk" -> InstallHandlerHelpers.createAHKLaunchCommand(file.path)
            "jar" -> InstallHandlerHelpers.createJARLaunchCommand(file.path)
            else -> ""
        }
    }

    // Checks if Externals Table Exists, if it doesn't creates it.
    fun determineDBID(): Int
    {
        return countExternalAddon()+1
    }

    fun syncAHKsWithExternals()
    {
        val ahks = countAHKs()

        if (ahks == 0)
            return

        // Retrieve AHKs
        val scripts = getAHKScriptsArray()

        for (ahk in scripts)
        {
            val file = File(ahk.location)
            if (file.exists() && !file.isDirectory)
            {
                val crc32 = Externals.calcCRC32(file)
                val cmd = Externals.determineCMD(file)
                val dbid = Externals.determineDBID()
                val name = file.nameWithoutExtension
                val path = file.path
                val ea = PAL_External_Addon(dbid, name, crc32, "", "", "", "", cmd, path, ahk.runOnLaunch)
                CoreApplication.controller.saveExternal(ea)

            }
        }

        // Delete AHKs from DB
        nukeAHK()
    }

    fun checkForUpdatesAndUpdateExternals()
    {
        //TODO: This
        // Check for updates


        // If update found back up current file


        //
    }

    /**
     * Returns AID for setting installed, matches based on "website_source"
     */
    fun isMajorAddon(string: String): Int
    {
        return when (string)
        {
            LUTBOT_DL -> 9
            SIC_DL -> 17
            else -> 0
        }
    }

    fun isExternal(aid: Int): Boolean
    {
        return when (aid)
        {
            17 -> true // Synthesized-Implicit-Calculator
            9 -> true // Lutbot
            else -> false
        }
    }

    fun addLutBot()
    {
        if (!GlobalData.addonFolder.exists())
            GlobalData.addonFolder.mkdir()

        var install = GlobalData.addonFolder.path + File.separator + "lutbot"

        if (!File(install).exists())
            File(install).mkdir()

        install += File.separator + "macro.ahk"

        val ea = PAL_External_Addon(-1, "Lutbot", "", "", null, "", LUTBOT_DL, determineCMD(File(install)), install, false)

        // Download
        CoreApplication.controller.showDownloadPopup(File(ea.webSource).name)
        val location = FileDownloader().downloadFile(URL(ea.webSource), GlobalData.temp_down_folder, 1024, GlobalData.noIcon)
        val dest = File(ea.path)
        deleteFile(dest)
        Files.copy(location.toPath(), dest.toPath())
        ea.checksum = calcCRC32(dest)

        CoreApplication.controller.saveExternal(ea)
    }

    fun addSIC()
    {
        if (!GlobalData.addonFolder.exists())
            GlobalData.addonFolder.mkdir()

        var install = GlobalData.addonFolder.path + File.separator + "Synthesized-Implicit-Calculator"

        if (!File(install).exists())
            File(install).mkdir()

        install += File.separator + "SynthesisParser.ahk"

        val ea = PAL_External_Addon(-1, "Synthesized Implicit Calculator", "", "", null, "", SIC_DL, determineCMD(File(install)), install, false)

        CoreApplication.controller.showDownloadPopup(File(ea.webSource).name)
        val location = FileDownloader().downloadFile(URL(ea.webSource), GlobalData.temp_down_folder, 1024, GlobalData.noIcon)
        val dest = File(ea.path)
        deleteFile(dest)
        Files.copy(location.toPath(), dest.toPath())
        ea.checksum = calcCRC32(dest)

        CoreApplication.controller.saveExternal(ea)
    }
}