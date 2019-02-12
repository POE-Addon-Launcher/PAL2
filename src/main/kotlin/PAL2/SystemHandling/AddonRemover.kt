package PAL2.SystemHandling

import PAL2.Database.getInstallDir
import PAL2.Database.removeInstalledAddon
import SystemHandling.deleteFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

/**
 *
 */
fun removeAddon(aid: Int)
{
    val install_dir = findInstall(aid)
    if (install_dir != null)
    {
        if (install_dir.exists() && install_dir.isDirectory)
        {
            GlobalScope.launch {
                deleteFile(install_dir)
            }
            removeInstalledAddon(aid)
        }
    }
}

fun findInstall(aid :Int): File?
{
    return getInstallDir(aid)
}