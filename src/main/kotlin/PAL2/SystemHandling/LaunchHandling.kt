package PAL2.SystemHandling

import PAL2.Database.getLaunchCommand
import PAL2.Database.getRunAddonOnLaunch
import PAL2.Database.getRunOnLaunchCommands
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.awt.Desktop.getDesktop
import java.io.File
import java.io.IOException
import java.lang.StringBuilder
import java.net.URI


private val logger = KotlinLogging.logger {}
/**
 *
 */
fun launchAddon(aid: Int)
{
    // Grab launch command from DB
    val lc = getLaunchCommand(aid)

    GlobalScope.launch {
        // Check for exceptions: ? "SET_AHK_FOLDER" etc
        if (lc == "?")
        {
            logger.error { "NO LAUNCH COMMAND SET!" }
        }
        else if (lc == "SET_AHK_FOLDER")
        {
            logger.error { "SET AHK FOLDER!" }
        }
        else
        {
            Runtime.getRuntime().exec(lc)
        }
    }
}

private fun launch_poe(exe: String)
{
    logger.debug { "CALLED: LAUNCH_POE(exe) | exe = \'$exe\'" }

    if (exe.contains("PathOfExileSteam.exe") || exe.contains("PathOfExile_x64Steam.exe"))
    {
        logger.debug { "Attempting to run Steam PoE" }
        runSteamPoE()
    }
    else if (exe.contains("PathOfExile.exe") || exe.contains("PathOfExile_x64.exe"))
    {
        val dir: String
        val executable: String
        if (exe.contains("PathOfExile_x64.exe"))
        {
            logger.debug { "Found PathOfExile_x64.exe" }
            executable = "PathOfExile_x64.exe"
            dir = exe.replace(executable, "")
            logger.debug { "dir = $dir" }
        }
        else
        {
            logger.debug { "ELSE case" }
            executable = "PathOfExile.exe"
            dir = exe.replace(executable, "")
            logger.debug { "dir = $dir" }
        }
        try
        {
            logger.debug { "Runtime command: \"$exe\", NULL, ${File(dir)}" }
            logger.debug { "dir = $dir" }
            Runtime.getRuntime().exec("\"$exe\"", null, File(dir))
        }
        catch (e: IOException)
        {
            logger.error { e.printStackTrace() }
        }

    }
}

fun runSteamPoE()
{
    val desktop = getDesktop()
    val steamProtocol = URI("steam://run/238960")
    desktop.browse(steamProtocol)
}

fun launchAddons()
{
    if (GlobalData.launch_addons)
    {
        val arr = getRunOnLaunchCommands()
        if (arr != null)
        {
            for (lc in arr)
            {
                GlobalScope.launch {
                    Runtime.getRuntime().exec(lc)
                }
            }
        }
        launchAHKScripts()
        GlobalData.launch_addons = false
    }
}

fun launchAHKScripts()
{
    var arr = GlobalData.ahk_scripts
    for (ahk in arr)
    {
        GlobalScope.launch {
            Runtime.getRuntime().exec(createAHKLaunchCommand(ahk))
        }
    }
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

fun launchPoE()
{
    if (GlobalData.steam_poe)
    {
        launchAddons()
        runSteamPoE()
        return
    }

    if (GlobalData.poeLocations.size == 0)
    {
        logger.error { "No path of exile directories are known." }
    }
    else if (GlobalData.poeLocations.size == 1)
    {
        launchAddons()
        launch_poe(GlobalData.poeLocations[0])
    }
    else
    {
        val f = GlobalData.primaryPoEFile
        if (f != null)
        {
            if (f.exists())
            {
                launchAddons()
                launch_poe(f.path)
            }
        }
    }
}