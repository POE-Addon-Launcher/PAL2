package PAL2.SystemHandling

import PAL2.Database.addInstalledAddon
import PAL2.Database.updateLaunchCommandInstalledAddon
import PAL2.GUI.CoreApplication
import PAL2.GUI.InstalledAnchor
import PAL_DataClasses.PAL_AddonFullData
import SystemHandling.deleteFile
import javafx.application.Platform
import javafx.stage.DirectoryChooser
import mu.KotlinLogging
import net.lingala.zip4j.core.ZipFile
import org.apache.commons.io.FileUtils
import java.io.File
import java.lang.StringBuilder
import java.time.LocalDate
import java.util.*


/**
 *
 */
private val logger = KotlinLogging.logger {}

object InstallHandlerHelpers
{
    fun createJARLaunchCommand(jar_loc: String): String
    {
        val stringBuilder = StringBuilder()
        stringBuilder.append("java -jar \"")
        stringBuilder.append(jar_loc)
        stringBuilder.append("\"")
        return stringBuilder.toString()
    }

    fun createAHKLaunchCommand(ahk_file: String): String
    {
        val ahk_folder = GlobalData.ahkFolder
        if (ahk_folder.path != "")
        {
            if (ahk_folder.exists())
            {
                val stringBuilder = StringBuilder()
                stringBuilder.append("\"")
                stringBuilder.append(ahk_folder.path)
                stringBuilder.append(File.separator)
                stringBuilder.append("autohotkey.exe")
                stringBuilder.append("\" ")

                stringBuilder.append("\"")
                stringBuilder.append(ahk_file)
                stringBuilder.append("\" ")

                logger.debug { stringBuilder.toString() }
                return stringBuilder.toString()
            }
            else
            {
                return "SET_AHK_FOLDER"
            }
        }
        return "SET_AHK_FOLDER"
    }

    fun createExeLaunchCommandWithElevation(path: String): String
    {
        val stringBuilder = StringBuilder()
        stringBuilder.append("rundll32 url.dll,FileProtocolHandler ")
        stringBuilder.append("\"")
        stringBuilder.append(path)
        stringBuilder.append("\"")
        return stringBuilder.toString()
    }

    fun createEXELaunchCommand(path: String): String
    {
        val stringBuilder = StringBuilder()
        stringBuilder.append("\"")
        stringBuilder.append(path)
        stringBuilder.append("\"")
        return stringBuilder.toString()
    }
}

class InstallHandler(val afd: PAL_AddonFullData, val downloadedFile: File)
{
    var possibleEXEs = ArrayList<File>()
    var possibleAHKs = ArrayList<File>()
    var possibleJARs = ArrayList<File>()

    init
    {
        if (afd == null)
        {
            logger.error { "AFD is null! This shouldn't happen!" }
        }
        CoreApplication.controller.setText(afd.aid, "Installing")
        installAddon()
    }

    fun installAddon()
    {
        when (afd.aid)
        {
            4 -> currencyCopHandler(downloadedFile)
            5 -> poeTradesCompanion(downloadedFile)
            8 -> xenonTradeHandler(downloadedFile)
            10 -> exilenceHandler(downloadedFile)
            11 -> poeCustomSoundTrack(downloadedFile)
            12 -> defaultInstaller(downloadedFile, afd.name, true)
            else -> defaultInstaller(downloadedFile, afd.name, false)
        }
        create_installAnchor()
    }

    private fun poeCustomSoundTrack(downloadedFile: File)
    {
        val process = Runtime.getRuntime().exec(InstallHandlerHelpers.createEXELaunchCommand(downloadedFile.path))
        process.waitFor()

        val p = Runtime.getRuntime().exec("taskkill /IM \"PoE Custom Soundtrack.exe\"")
        p.waitFor()

        val local = System.getenv("LOCALAPPDATA")
        val exe = File("$local${File.separator}Programs${File.separator}PoECustomSoundtrack${File.separator}PoE Custom Soundtrack.exe")

        addInstalledAddon(afd, File(exe.parent))
        updateLaunchCommandInstalledAddon(InstallHandlerHelpers.createEXELaunchCommand(exe.path), afd.aid)
    }

    private fun poeTradesCompanion(downloadedFile: File)
    {
        val destination = File("${GlobalData.addonFolder}${File.separator}${afd.name}${File.separator}")
        unzip(downloadedFile, destination)

        addInstalledAddon(afd, destination)

        val content = destination.listFiles()
        for (f in content)
        {
               if (content.size == 1)
               {
                   findUsable(f.listFiles())
                   if (possibleAHKs.size == 1)
                   {
                       updateLaunchCommandInstalledAddon(InstallHandlerHelpers.createAHKLaunchCommand(possibleAHKs[0].path), afd.aid)
                   }
               }
        }
    }

    // TODO: TaskKill before updating!

    private fun xenonTradeHandler(downloadedFile: File)
    {
        val process = Runtime.getRuntime().exec(InstallHandlerHelpers.createExeLaunchCommandWithElevation(downloadedFile.path))
        process.waitFor()

        val p = Runtime.getRuntime().exec("taskkill /IM XenonTrade.exe")
        p.waitFor()

        Platform.runLater {
            val directoryChooser = DirectoryChooser()
            directoryChooser.title = "Please select your XenonTrade installation folder, WHEN YOU ARE DONE INSTALLING IT!"
            val file = directoryChooser.showDialog(CoreApplication.stage)

            if (file != null)
            {
                addInstalledAddon(afd, file)

                for (f in file.listFiles())
                {
                    if (f.isFile)
                    {
                        if (f.name == "XenonTrade.exe")
                        {
                            updateLaunchCommandInstalledAddon(InstallHandlerHelpers.createExeLaunchCommandWithElevation(f.path), afd.aid)
                        }
                    }
                }
            }
        }
    }

    private fun exilenceHandler(downloadedFile: File)
    {
        // Run the setup
        val process = Runtime.getRuntime().exec("\"$downloadedFile\"")
        process.waitFor()

        // Wait for "Exilence" to be an active window, then "taskkill /IM exilence.exe"
        val p = Runtime.getRuntime().exec("taskkill /IM Exilence.exe")
        p.waitFor()

        // LocalAppdata Programs is where it'll be installed to
        val local = System.getenv("LOCALAPPDATA")
        val exilenceExe = File("$local${File.separator}Programs${File.separator}exilence${File.separator}Exilence.exe")

        addInstalledAddon(afd, File(exilenceExe.parent))
        updateLaunchCommandInstalledAddon(InstallHandlerHelpers.createEXELaunchCommand(exilenceExe.path), afd.aid)
    }

    private fun currencyCopHandler(downloadedFile: File)
    {
        // Run the setup
        val process = Runtime.getRuntime().exec("\"$downloadedFile\"")
        process.waitFor()

        // Currency Cop takes long to launch...
        Thread.sleep(5000)

        // Wait for "Currency Cop" to be available, then: "taskkill /IM currencycop.exe"
        val p = Runtime.getRuntime().exec("taskkill /IM CurrencyCop.exe")
        p.waitFor()

        // LocalAppdata Programs is where it'll be installed to
        val local = System.getenv("LOCALAPPDATA")
        val ccExe = File("$local${File.separator}Programs${File.separator}currency-cop${File.separator}CurrencyCop.exe")

        addInstalledAddon(afd, File(ccExe.parent))
        updateLaunchCommandInstalledAddon(InstallHandlerHelpers.createEXELaunchCommand(ccExe.path), afd.aid)
    }


    /**
     * RAR IS NOT SUPPORTED, BECAUSE IT'S SHIT TO WORK WITH.
     */
    fun defaultInstaller(file: File, name: String, elevated: Boolean)
    {
        // Find out what file we're dealing with...
        when (true)
        {
            file.path.endsWith(".jar", true) -> javaInstaller(file)
            file.path.endsWith(".ahk", true) -> ahkInstaller(file)
            file.path.endsWith(".zip", true) -> zipInstaller(file)
            file.path.endsWith(".exe", true) -> exeInstaller(file, elevated)
        }
    }



    /**
     * Launch Command -> Ask user to provide on for next time, due to Installers.
     */
    fun exeInstaller(file: File, elevated: Boolean)
    {
        val dest = moveDownloadedfile(file)
        addInstalledAddon(afd, dest.parentFile)
        if (elevated)
        {
            updateLaunchCommandInstalledAddon(InstallHandlerHelpers.createExeLaunchCommandWithElevation(dest.path), afd.aid)
        }
        else
        {
            updateLaunchCommandInstalledAddon(InstallHandlerHelpers.createEXELaunchCommand(dest.path), afd.aid)
        }

    }


    /**
     * Launch Command -> Ask user to provide on for next time, due to Installers.
     */
    fun zipInstaller(file: File)
    {
        val destination = File("${GlobalData.addonFolder}${File.separator}${afd.name}${File.separator}")
        unzip(file, destination)

        addInstalledAddon(afd, destination)

        // Search the folder for an AHK, JAR or EXE
        findUsable(destination.listFiles())

        if (possibleAHKs.size == 1)
        {
            updateLaunchCommandInstalledAddon(InstallHandlerHelpers.createAHKLaunchCommand(possibleAHKs[0].path), afd.aid)
        }
        else if (possibleJARs.size == 1)
        {
            updateLaunchCommandInstalledAddon(InstallHandlerHelpers.createJARLaunchCommand(possibleJARs[0].path), afd.aid)
        }
        else if (possibleEXEs.size == 1)
        {
            updateLaunchCommandInstalledAddon(InstallHandlerHelpers.createEXELaunchCommand(possibleEXEs[0].path), afd.aid)
        }
        else
        {
            logger.debug { "PAL Can't decide what file to use for this addon." }
        }
    }


    fun findUsable(listFiles: Array<File>)
    {
        var c = 0
        for (f in listFiles)
        {
            if (f.isFile)
            {
                c++
                if (!onBlacklist(f))
                {
                    when (f.extension)
                    {
                        "exe" -> possibleEXEs.add(f)
                        "ahk" -> possibleAHKs.add(f)
                        "jar" -> possibleJARs.add(f)
                    }
                }

            }
        }
        if (c == 0)
        {
            findUsable(listFiles[0].listFiles())
        }
    }

    fun onBlacklist(file: File): Boolean
    {
        return when (file.name)
        {
            "Update.exe" -> true
            "_TradeMacroMain.ahk" -> true
            "Gdip_All.ahk" -> true
            "FileInstall_Cmds.ahk" -> true
            else -> false
        }
    }

    /**
     * Simple AHK Script, requires ahk.
     */
    fun ahkInstaller(file: File)
    {
        val dest = moveDownloadedfile(file)

        // Register to DB
        addInstalledAddon(afd, dest.parentFile)
        updateLaunchCommandInstalledAddon(InstallHandlerHelpers.createAHKLaunchCommand(dest.path), afd.aid)
    }

    /**
     * Simple jar, requires java.
     * Move to an <AddonNameFolder>/
     * execute jar
     */
    fun javaInstaller(file: File)
    {
        val dest = moveDownloadedfile(file)

        // Register to DB
        addInstalledAddon(afd, dest.parentFile)
        updateLaunchCommandInstalledAddon(InstallHandlerHelpers.createJARLaunchCommand(dest.path), afd.aid)
    }

    fun moveDownloadedfile(file: File): File
    {
        val destination = File("${GlobalData.addonFolder}${File.separator}${afd.name}${File.separator}${file.name}")
        FileUtils.copyFile(file, destination)
        return destination
    }

    fun create_installAnchor()
    {
        val ia = InstalledAnchor(afd.aid, afd.icon_url, afd.name, afd.version_text, afd.version_text, LocalDate.now().toString(), "https://www.github.com/${afd.gh_username}/${afd.gh_reponame}")
        //ia.isUpToDate()
        CoreApplication.controller.addInstalledAnchor(ia)
        CoreApplication.controller.setInstalled(afd.aid)
    }

    fun unzip(archive: File, destination: File)
    {
        val zip = ZipFile(archive)
        zip.extractAll(destination.path)
        deleteFile(archive)
    }
}

