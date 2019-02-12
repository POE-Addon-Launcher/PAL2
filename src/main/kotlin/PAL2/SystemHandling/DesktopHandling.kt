package PAL2.SystemHandling

import com.sun.jna.Native
import com.sun.jna.platform.win32.User32



/**
 *
 */
fun getActiveWindow(): String
{
    val MAX_TITLE_LENGTH = 1024
    val buffer = CharArray(MAX_TITLE_LENGTH * 2)
    val hwnd = User32.INSTANCE.GetForegroundWindow()
    User32.INSTANCE.GetWindowText(hwnd, buffer, MAX_TITLE_LENGTH)
    return Native.toString(buffer)
}


