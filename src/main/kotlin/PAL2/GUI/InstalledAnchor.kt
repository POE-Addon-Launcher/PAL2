package PAL2.GUI

import GlobalData
import PAL2.Database.getRunAddonOnLaunch
import PAL2.Database.updateRunAddonWhenLaunching
import PAL2.SystemHandling.launchAddon
import PAL2.SystemHandling.updateAddon
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.CheckBox
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import mu.KotlinLogging

/**
 *
 */
private val logger = KotlinLogging.logger {}

class InstalledAnchor(var aid: Int, var iconUrl: String?, var addonName: String, var version: String, var newestVersion: String, var lastCheck: String, var website: String)
{
    var anchorPane = AnchorPane()
    var displayImage = ImageView()
    lateinit var textAddonName: Text
    lateinit var textNewestVersion: Text
    lateinit var textLastCheck: Text
    lateinit var textEnabled: Text

    lateinit var bTextVersion: Text
    lateinit var bTextLastCheck: Text
    lateinit var bTextNewVersion: Text

    lateinit var checkBox: CheckBox

    //var imageViewInfo = ImageView()

    var anchorButton = AnchorPane()
    var rectButton = Rectangle(75.0, 25.0)
    var textButton = Text()

    init
    {
        anchorPane.id = aid.toString()
        initImg()
        initTopText()
        initBottomText()
        initButton()
        initCheckBox()

        // Check up to date
        if (version == newestVersion)
        {
            this.isUpToDate()
        }
        else
        {
            this.canBeUpdated()
        }

        setListners()
    }

    private fun initCheckBox()
    {
        checkBox = CheckBox("")
        anchorPane.children.add(checkBox)
        checkBox.layoutX = 542.5
        checkBox.layoutY = 20.0
        checkBox.isSelected = getRunAddonOnLaunch(aid)
    }

    private fun setListners()
    {
        anchorListner()
        setDownloadUpdateListner()
        checkBoxListener()
    }

    private fun checkBoxListener()
    {
        checkBox.onMouseClicked = EventHandler()
        {
            updateRunAddonWhenLaunching(checkBox.isSelected, aid)
        }
    }

    fun anchorListner()
    {
        anchorPane.onMouseClicked = EventHandler()
        {
            if (it.button == MouseButton.PRIMARY)
            {
                if (it.clickCount == 2)
                {
                    launchAddon(aid)
                }
            }
        }
    }

    fun setDownloadUpdateListner()
    {
        rectButton.onMouseEntered = EventHandler()
        {
            rectButton.stroke = Color(1.0, 0.0, 1.0, 1.0)
        }
        rectButton.onMouseExited = EventHandler()
        {
            rectButton.stroke = Color.WHITE
        }
        rectButton.onMouseClicked = EventHandler()
        {
            // TODO: Start Update Downloading.
            Platform.runLater {
                rectButton.isVisible = false
                textButton.text = "Downloading"
                updateAddon(aid, displayImage.image)
            }
        }
    }

    fun checkUpdateAble()
    {
        val most_recent = GlobalData.getAddonByID(aid)?: return
        if (most_recent.version_text != version)
        {
            canBeUpdated()
        }
        else
        {
            isUpToDate()
        }
    }

    fun isUpToDate()
    {
        Platform.runLater {
            rectButton.isVisible = false
            textButton.text = "Up-to-date"
            textButton.textAlignment = TextAlignment.CENTER
            textButton.layoutY = 25.0
        }
    }

    fun canBeUpdated()
    {
        Platform.runLater {
            rectButton.isVisible = true
            textButton.text = "Update"
            textButton.layoutY = 25.0
        }
    }
    /*
    fun isGGGAproved()
    {
        Platform.runLater {
            imageViewInfo.image = Image(javaClass.getResource("/icons/gggAprove.png").openStream())
        }
    }

    fun isGGGunknown()
    {
        Platform.runLater {
            imageViewInfo.image = Image(javaClass.getResource("/icons/gggAproveQ.png").openStream())
        }
    }*/

    private fun initButton()
    {
        textButton.id = "textButton"
        rectButton.id = "rectButton"
        initButton("Update", anchorButton, textButton, rectButton)
        anchorPane.children.add(anchorButton)
    }

    private fun initButton(arg: String, anchor: AnchorPane, text: Text, rect: Rectangle)
    {
        anchor.layoutX = 240.0
        anchor.layoutY = 0.0

        text.text = arg
        anchor.children.add(text)
        text.textAlignment = TextAlignment.CENTER
        text.layoutX = 0.0
        text.layoutY = 25.0
        text.wrappingWidth = 75.0
        text.fill = Color.WHITE

        anchor.children.add(rect)
        rect.layoutX = 0.0
        rect.layoutY = 7.5
        rect.stroke = Color.WHITE
        rect.fill = Color(1.0, 1.0, 1.0, 0.0)
    }

    private fun initBottomText()
    {
        bTextVersion = bottomTextFactory(version, 40.0, 200.0)
        bTextVersion.textAlignment = TextAlignment.LEFT
        bTextVersion.id = "bTextVersion"
        bTextLastCheck = bottomTextFactory(monthToNum(lastCheck), 425.0, 100.0)
        bTextLastCheck.id = "bTextLastCheck"

        bTextNewVersion = bottomTextFactory(newestVersion, 325.0, 100.0)

        anchorPane.children.addAll(bTextVersion, bTextLastCheck, bTextNewVersion)
    }

    private fun monthToNum(arg: String): String
    {
        when (true)
        {
            arg.toLowerCase().contains("january") -> return arg.replace("JANUARY", "01")
            arg.toLowerCase().contains("february") -> return arg.replace("FEBRUARY", "02")
            arg.toLowerCase().contains("march") -> return arg.replace("MARCH", "03")
            arg.toLowerCase().contains("april") -> return arg.replace("APRIL", "04")
            arg.toLowerCase().contains("may") -> return arg.replace("MAY", "05")
            arg.toLowerCase().contains("june") -> return arg.replace("JUNE", "06")
            arg.toLowerCase().contains("july") -> return arg.replace("july", "07")
            arg.toLowerCase().contains("august") -> return arg.replace("AUGUST", "08")
            arg.toLowerCase().contains("september") -> return arg.replace("SEPTEMBER", "09")
            arg.toLowerCase().contains("october") -> return arg.replace("OCTOBER", "10")
            arg.toLowerCase().contains("november") -> return arg.replace("NOVEMBER", "11")
            arg.toLowerCase().contains("december") -> return arg.replace("DECEMBER", "12")
            else -> return arg
        }
    }


    private fun initTopText()
    {
        textAddonName = textTopFactory(addonName, 40.0, 200.0)
        textAddonName.id = "textAddonName"
        textAddonName.textAlignment = TextAlignment.LEFT
        textNewestVersion = textTopFactory("Newest Version", 325.0, 100.0)
        textLastCheck = textTopFactory("Last Check", 425.0, 100.0)
        textEnabled = textTopFactory("Enabled", 525.0, 50.0)

        anchorPane.children.addAll(textAddonName, textNewestVersion, textLastCheck, textEnabled)
    }

    fun bottomTextFactory(arg: String, x: Double, ww: Double): Text
    {
        val text = Text()
        text.layoutX = x
        text.layoutY = 32.5
        text.wrappingWidth = ww
        text.textAlignment = TextAlignment.CENTER
        text.text = arg
        text.fill = Color.WHITE
        return text
    }

    fun textTopFactory(arg: String, x: Double, ww: Double): Text
    {
        val text = Text()
        text.fill = Color.WHITE
        text.layoutX = x
        text.layoutY = 15.0
        text.style = "-fx-font-weight: Bold;"
        text.text = arg
        text.wrappingWidth = ww
        text.textAlignment = TextAlignment.CENTER
        return text
    }

    fun initImg()
    {
        /*
        imageViewInfo.layoutX = 530.0
        imageViewInfo.layoutY = 2.5
        imageViewInfo.fitHeight = 35.0
        imageViewInfo.fitWidth = 35.0
        imageViewInfo.image = Image(javaClass.getResource("/icons/gggAproveQ.png").openStream())
        anchorPane.children.add(imageViewInfo)*/

        displayImage.layoutX = 2.5
        displayImage.layoutY = 2.5
        displayImage.fitWidth = 35.0
        displayImage.fitHeight = 35.0
        displayImage.id = "imageInfo"

        logger.debug{"$addonName | $iconUrl"}

        if (iconUrl == null)
        {
            displayImage.image = Image(javaClass.getResource("/icons/NoIcon.png").openStream())
        }
        else
        {
            if (iconUrl == "")
            {
                displayImage.image = Image(javaClass.getResource("/icons/NoIcon.png").openStream())
            }
            else
            {
                displayImage.image = Image(iconUrl)
            }
        }
        anchorPane.children.add(displayImage)
    }
}