package PAL2.SystemHandling

import GlobalData
import PAL2.Database.getExternalsOnLaunchCommands
import PAL2.Database.getInstallDir
import PAL2.Database.getLaunchCommand
import PAL2.Database.getRunOnLaunchCommands
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.awt.Desktop.getDesktop
import java.io.File
import java.io.IOException
import java.net.URI


private val logger = KotlinLogging.logger {}
/**
 *
 */
fun launchAddon(aid: Int)
{
    // Grab launch command from DB
    val lc = getLaunchCommand(aid)

    when (aid)
    {
        14 -> procurementHandler(aid)
        12 -> runPathOfMaps(lc)
        15 -> leagueOverlayHandler(aid, lc)
        else -> defaultHandler(aid, lc)
        }
    }

fun defaultHandler(aid: Int, lc: String)
{
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
            val dir = getInstallDir(aid)
            if (dir != null)
            {
                Runtime.getRuntime().exec(lc, null, dir)
            }
            else
            {
                Runtime.getRuntime().exec(lc)
            }

        }
}


}

fun runPathOfMaps(lc: String)
{
    GlobalScope.launch {
        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler $lc")
    }

}

fun leagueOverlayHandler(aid: Int, launch_command: String)
{
    GlobalScope.launch {
        val dir = getInstallDir(aid)
        if (dir != null)
        {
            Runtime.getRuntime().exec(launch_command, null, dir)
        }
    }
}

fun procurementHandler(aid: Int)
{
    GlobalScope.launch {
        val f = getInstallDir(aid)

        if (f != null)
        {
            var exe = ""
            for (_f in f.listFiles())
            {
                if (_f.name == "Procurement.exe")
                {
                    exe = _f.path
                }
            }

            if (exe != "")
                Runtime.getRuntime().exec("\"$exe\"", null, f)
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
        GlobalData.launch_addons = false
    }
}

@Deprecated("Uses different method to launch AHKs now.")
fun launchAHKScripts()
{
    // TODO: Convert current AHKs users have to externals.
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
        GlobalScope.launch {
            GlobalScope.launch {
                launchAddons()
            }
            GlobalScope.launch {
                launchExternals()
            }
            GlobalScope.launch {
                runSteamPoE()
            }
        }
        return
    }

    if (GlobalData.poeLocations.size == 0)
    {
        logger.error { "No path of exile directories are known." }
    }
    else if (GlobalData.poeLocations.size == 1)
    {
        GlobalScope.launch {
            GlobalScope.launch {
                launchAddons()
            }
            GlobalScope.launch {
                launchExternals()
            }
            GlobalScope.launch {
                launch_poe(GlobalData.poeLocations[0])
            }
        }
    }
    else
    {
        val f = GlobalData.primaryPoEFile
        if (f != null)
        {
            if (f.exists())
            {
                GlobalScope.launch {
                    GlobalScope.launch {
                        launchAddons()
                    }
                    GlobalScope.launch {
                        launchExternals()
                    }
                    GlobalScope.launch {
                        launch_poe(f.path)
                    }
                }
            }
        }
    }
}

fun launchExternals()
{
    if (!GlobalData.launch_externals)
        return

    val arr = getExternalsOnLaunchCommands() ?: return

    if (arr.isEmpty())
        return

    for (str in arr)
    {
        GlobalScope.launch { Runtime.getRuntime().exec(str) }
    }

    GlobalData.launch_externals = false
}
