package PAL2.GUI

import javafx.event.EventHandler
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.scene.text.Text


/**
 *
 */
class UtilityAnchor
{
    lateinit var anchorPane: AnchorPane

    var refresh = Text()
    var update= Text()
    var updateAll = Text()
    var editConfig = Text()
    var removeAddon = Text()

    var _refresh = Text()
    var _update = Text()
    var _updateAll = Text()
    var _editConfig = Text()
    var _removeAddon = Text()

    init
    {
        createAnchor()
        createImages()
        createTexts()

        setListners()
    }

    private fun setListners()
    {
        baseTextListner(refresh)
        baseTextListner(updateAll)
        baseTextListner(update)
        baseTextListner(editConfig)
        baseTextListner(removeAddon)
        baseTextListner(_refresh)
        baseTextListner(_updateAll)
        baseTextListner(_update)
        baseTextListner(_editConfig)
        baseTextListner(_removeAddon)
    }

    private fun baseTextListner(text: Text)
    {
        text.onMouseEntered = EventHandler()
        {
            text.isVisible = !text.isVisible
        }

        text.onMouseExited = EventHandler()
        {
            text.isVisible = !text.isVisible
        }
    }


    private fun createTexts()
    {
        shadowedText(refresh, _refresh, "Refresh", 32.0, 20.0, Color.WHITE, Color(0.192, 0.671, 0.776, 1.0))
        shadowedText(updateAll, _updateAll, "Update All", 220.0, 20.0, Color.WHITE, Color(0.192, 0.671, 0.776, 1.0))
        shadowedText(update, _update, "Update", 130.0, 20.0, Color.WHITE, Color(0.192, 0.671, 0.776, 1.0))
        shadowedText(editConfig, _editConfig, "Edit Configuration", 340.0, 20.0, Color.WHITE, Color(0.192, 0.671, 0.776, 1.0))
        shadowedText(removeAddon, _removeAddon, "Remove Addon", 500.0, 20.0, Color((196.0/255.0), (49.0/255.0), (49.0/255.0), 1.0), Color.WHITE)

        anchorPane.children.addAll(refresh, update, updateAll, editConfig, removeAddon,
                                    _refresh, _update, _updateAll, _editConfig, _removeAddon)
    }

    fun shadowedText(text: Text, _text: Text, s: String, x: Double, y: Double, c: Color, _c: Color)
    {
        subShadow(text, s, x, y)
        subShadow(_text, s, x, y)
        text.fill = c
        _text.fill = _c
        _text.isVisible = false
    }

    fun subShadow(text: Text, s:String, x: Double, y: Double)
    {
        text.text = s
        text.style = "-fx-font-weight: Bold"
        text.layoutY = y
        text.layoutX = x
    }

    private fun createImages()
    {
        val refresh = ImageView()
        val update = ImageView()
        val updateAll = ImageView()
        val editConfig = ImageView()
        val removeAddon = ImageView()

        anchorPane.children.addAll(refresh, update, updateAll, editConfig, removeAddon)

        setWidthHeight(refresh)
        setWidthHeight(update)
        setWidthHeight(updateAll)
        setWidthHeight(editConfig)
        setWidthHeight(removeAddon)

        setXY(refresh, 2.5, 2.5)
        setXY(update, 100.0, 2.5)
        setXY(updateAll, 190.0, 2.5)
        setXY(editConfig, 310.0, 2.5)
        setXY(removeAddon, 470.0, 2.5)

        setImg(refresh, "/icons/refresh_icon.png")
        setImg(update, "/icons/down.png")
        setImg(updateAll, "/icons/download_all.png")
        setImg(editConfig, "/icons/edit.png")
        setImg(removeAddon, "/icons/remove.png")
    }

    private fun setImg(imageView: ImageView, s: String)
    {
        imageView.image = Image(javaClass.getResource(s).toString())
    }

    private fun setXY(imageView: ImageView, x: Double, y: Double)
    {
        imageView.layoutY = y
        imageView.layoutX = x
    }

    private fun setWidthHeight(imageView: ImageView)
    {
        imageView.fitWidth = 25.0
        imageView.fitHeight = 25.0
    }

    private fun createAnchor()
    {
        anchorPane = AnchorPane()
    }
}