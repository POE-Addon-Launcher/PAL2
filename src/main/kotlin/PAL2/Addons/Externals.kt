package PAL2.Addons

import PAL2.SystemHandling.InstallHandler
import PAL2.SystemHandling.InstallHandlerHelpers
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.zip.CRC32
import javax.xml.bind.DatatypeConverter

/**
 *
 */
object Externals
{
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
    fun determineDBID(file: File): Int
    {
        //TODO create schema, find ID, pass on ID, create entries upon save button, fix buttons.
        return -1
    }
}