package PAL2.SystemHandling

import GUI.DownloadsAnchor
import SystemHandling.deleteFile
import javafx.scene.image.Image
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 *
 */
class FileDownloader
{
    private var maxSize = 0
    private var currSize = 0
    private lateinit var downloadBar: DownloadsAnchor
    private var blockSize = 128

    fun downloadFile(url: URL, temp_download_dir: File, block: Int, image: Image, aid: Int)
    {
        var store_location = temp_download_dir
        if (store_location.isDirectory)
        {
            val splits = url.file.split("/")
            store_location = File("${store_location.path}${File.separator}${splits[splits.size-1]}")
        }

        if (store_location.exists())
            deleteFile(store_location)

        blockSize = block

        val httpConnection = url.openConnection() as HttpURLConnection
        httpConnection.addRequestProperty("User-Agent", "Mozilla/4.0")
        val filesize = httpConnection.contentLength
        maxSize = filesize

        val input = BufferedInputStream(httpConnection.inputStream)
        val output = FileOutputStream(store_location)

        val buff = BufferedOutputStream(output, blockSize)

        val data = ByteArray(blockSize)
        var downloadedFileSize = 0
        var x: Int

        val splits = url.file.split("/")
        downloadBar = DownloadsAnchor(splits[splits.size-1])
        downloadBar.setImg(image)
        downloadBar.attachToListView()
        updateProgress(0, maxSize, 0.0)
        functionCallerOnDelay()

        while (true)
        {
            x = input.read(data, 0, blockSize)

            if (x < 0)
                break

            downloadedFileSize += x
            currSize = downloadedFileSize

            buff.write(data, 0, x)
        }

        input.close()
        buff.close()
        output.close()


        // Download finished run installation.
        val addon = GlobalData.getAddonByID(aid)
        if (addon != null)
        {
            val installer = InstallHandler(addon, store_location)
        }
    }

    fun functionCallerOnDelay()
    {
        GlobalScope.launch {
            var timeSpent = 0.0
            while (currSize < maxSize)
            {
                val down_speed = (currSize/1024/1024/timeSpent)
                updateProgress(currSize, maxSize, Math.round(down_speed * 100.0) / 100.0)
                delay(250)
                timeSpent += 0.25
            }
            updateProgress(maxSize, maxSize, 0.0)
        }
    }

    fun updateProgress(min_size: Int, maxSize: Int, downSpeed: Double)
    {
        //AnchorTest.controller.updateProgressbar(d / 100.0)
        downloadBar.setProgress((min_size.toDouble() / maxSize.toDouble()))
        downloadBar.addDownloadText("[${min_size/1024/1024}MB / ${maxSize/1024/1024}MB] ~${downSpeed}MB/s")
    }
}
