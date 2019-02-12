package PAL2.GUI

import GUI.DownloadsAnchor
import GUI.PopUp.Updated_HTML_Popup
import PAL2.Database.*
import PAL2.GUI.Loader.Loader
import PAL2.SystemHandling.FileDownloader
import PAL2.SystemHandling.removeAddon
import PAL2.SystemHandling.updateAddon
import PAL_DataClasses.PAL_AddonTableRow
import SystemHandling.checkForUseableDownloads
import SystemHandling.init
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.coroutines.*
import java.io.File
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.util.*
import java.util.regex.Pattern


/**
 *
 */
class CoreController : Initializable
{
    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        Loader().start(Stage())
        populateSettingsList()
        GlobalScope.launch {
            init()
            setSettings()
            showMOTD()
        }
        core_tabpane.tabs.remove(addonDescTab)
    }

    private fun showMOTD()
    {
        Platform.runLater {
            val up = Updated_HTML_Popup()
            up.start(Stage())
        }
    }

    fun addInstalledAnchor(installedAnchor: InstalledAnchor)
    {
        for (ap in listViewInstalledAddons.items)
        {
            if (ap.id.toInt() == installedAnchor.aid)
            {
                val text = ap.lookup("#textButton") as Text
                val textV = ap.lookup("#bTextVersion") as Text

                Platform.runLater {
                    text.text = "Up-to-date"
                    textV.text = installedAnchor.version
                }
                return
            }
        }
        Platform.runLater { listViewInstalledAddons.items.add(installedAnchor.anchorPane) }
    }

    fun addInstalledAnchorTEST()
    {
        Platform.runLater {
            for (a in GlobalData.set_of_addons)
            {
                listViewInstalledAddons.items.add(InstalledAnchor(a.aid, a.icon_url, a.name, a.version_text, a.version_text, a.last_update, "https://www.github.com/${a.gh_username}/${a.gh_reponame}").anchorPane)
            }
        }
    }

    fun setDownloadableAddon(id: Int)
    {
        Platform.runLater {
            for (anchor in addonListView.items)
            {
                if (anchor.id == id.toString())
                {
                    val rect = anchor.lookup("#buttonRect") as Rectangle
                    val text = anchor.lookup("#buttonText") as Text

                    rect.isVisible = true
                    text.text = "Download"
                }

            }
        }
    }

    fun setText(id: Int, arg: String)
    {
        Platform.runLater {
            for (anchor in addonListView.items)
            {
                if (anchor.id == id.toString())
                {
                    val rect = anchor.lookup("#buttonRect") as Rectangle
                    val text = anchor.lookup("#buttonText") as Text

                    rect.isVisible = false
                    text.text = arg
                }

            }
        }
    }

    fun setInstalled(id: Int)
    {
        setText(id, "Installed")
    }

    fun setDownloading(id: Int)
    {
        setText(id, "Downloading")
    }


    fun addActiveDownload(arg: DownloadsAnchor)
    {
        // Add to the top of the list view...
        val old = listViewDownloads.items
        val new = FXCollections.observableArrayList<AnchorPane>()
        new.add(arg.anchorPane)

        if (old.size > 0)
        {
            for (anchor in old)
            {
                new.add(anchor)
            }
        }

        Platform.runLater {
            listViewDownloads.items = new
        }

    }

    var currentAIDdesc = 0
    fun setDescInfo(image: Image, name: String, desc: String, aid: Int)
    {
        currentAIDdesc = aid
        Platform.runLater {
            addonDescImg.image = image
            addonDescName.text = name
            addonDescTextDesc.text = desc
            activateDescInfoPage()
        }
    }

    fun activateDescInfoPage()
    {
        Platform.runLater {
            closable = false
            core_tabpane.tabs.add(addonDescTab)
            core_tabpane.selectionModel.select(addonDescTab)
            closable = true
        }

    }

    fun initAddonListView()
    {
        Platform.runLater {
            val addonCopy = GlobalData.set_of_addons

            val obAddons = FXCollections.observableArrayList<AddonAnchor>()
            for (a in addonCopy.reversed())
            {
                if (a.icon_url == null)
                {
                    a.icon_url = ""
                }

                var gh_username =  a.gh_username
                if (gh_username!!.toLowerCase() == "poe-addon-launcher")
                {
                    gh_username = "PAL_Repo"
                }

                val aa = AddonAnchor(a.name, a.version_text, "$gh_username\n${a.gh_reponame}", a.icon_url.toString(), a.aid)

                if (!obAddons.contains(aa))
                {
                    obAddons.add(aa)
                    addonListView.items.add(aa.anchorPane)
                }
            }
        }
    }

    /**
     * Change the image of an image view.
     * @param imageView ImageView to be changed.
     * @param s String location of the image. [(resources)/{your_folder}/your_file.ext]
     */
    fun changeImage(imageView: ImageView, s: String)
    {
        Platform.runLater { imageView.image = Image(javaClass.getResource(s).toString()) }
    }

    /***************
     * FXML STUFF  *
     ***************/
    @FXML
    private lateinit var listViewInstalledAddons: ListView<AnchorPane>

    @FXML
    private lateinit var core_tabpane: TabPane

    @FXML
    private lateinit var addonListView: ListView<AnchorPane>

    @FXML
    private lateinit var core_tab_installed: Tab

    @FXML
    private lateinit var installed_anchorpane: AnchorPane

    @FXML
    private lateinit var installed_tableView: TableView<*>

    @FXML
    private lateinit var installed_tc_type: TableColumn<*, *>

    @FXML
    private lateinit var installed_tc_name: TableColumn<*, *>

    @FXML
    private lateinit var installed_tc_status: TableColumn<*, *>

    @FXML
    private lateinit var installed_tc_version: TableColumn<*, *>

    @FXML
    private lateinit var installed_tc_latestVersion: TableColumn<*, *>

    @FXML
    private lateinit var installed_tc_source: TableColumn<*, *>

    @FXML
    private lateinit var installed_tc_creators: TableColumn<*, *>

    @FXML
    private lateinit var installed_refresh_img: ImageView

    @FXML
    private lateinit var installed_refresh_text: Text

    @FXML
    private lateinit var installed_update_img: ImageView

    @FXML
    private lateinit var installed_update_text: Text

    @FXML
    private lateinit var installed_updateAll_img: ImageView

    @FXML
    private lateinit var installed_updateAll_text: Text

    @FXML
    private lateinit var installed_remove_img: ImageView

    @FXML
    private lateinit var installed_remove_text: Text

    @FXML
    private lateinit var installed_searchField: TextField

    @FXML
    private lateinit var installed_refresh_text_hl: Text

    @FXML
    private lateinit var installed_update_text_hl: Text

    @FXML
    private lateinit var installed_updateAll_text_hl: Text

    @FXML
    private lateinit var installed_remove_text_hl: Text

    @FXML
    private lateinit var core_tab_addons: Tab

    @FXML
    private lateinit var addons_tableView: TableView<*>

    @FXML
    private lateinit var addons_tc_name: TableColumn<PAL_AddonTableRow, String>

    @FXML
    private lateinit var addon_tc_code: TableColumn<PAL_AddonTableRow, String>

    @FXML
    private lateinit var addon_tc_version: TableColumn<PAL_AddonTableRow, String>

    @FXML
    private lateinit var addon_tc_lastUpdate: TableColumn<PAL_AddonTableRow, String>

    @FXML
    private lateinit var addon_tc_source: TableColumn<PAL_AddonTableRow, String>

    @FXML
    private lateinit var addon_tc_creators: TableColumn<PAL_AddonTableRow, String>

    @FXML
    private lateinit var addon_refresh_img: ImageView

    @FXML
    private lateinit var addon_refresh_text: Text

    @FXML
    private lateinit var addon_download_img: ImageView

    @FXML
    private lateinit var addon_download_text: Text

    @FXML
    private lateinit var addon_search: TextField

    @FXML
    private lateinit var addon_download_text_hl: Text

    @FXML
    private lateinit var addon_refresh_text_hl: Text

    @FXML
    private lateinit var core_tab_filters: Tab

    @FXML
    private lateinit var filters_tableView: TableView<*>

    @FXML
    private lateinit var filters_tc_name: TableColumn<*, *>

    @FXML
    private lateinit var filters_tc_PoEVersion: TableColumn<*, *>

    @FXML
    private lateinit var filters_tc_version: TableColumn<*, *>

    @FXML
    private lateinit var filters_tc_lastUpdate: TableColumn<*, *>

    @FXML
    private lateinit var filters_tc_source: TableColumn<*, *>

    @FXML
    private lateinit var filters_tc_creators: TableColumn<*, *>

    @FXML
    private lateinit var filters_refresh_img: ImageView

    @FXML
    private lateinit var filters_refresh_text: Text

    @FXML
    private lateinit var filters_download_img: ImageView

    @FXML
    private lateinit var filters_download_text: Text

    @FXML
    private lateinit var filters_search: TextField

    @FXML
    private lateinit var filters_refresh_text_hl: Text

    @FXML
    private lateinit var filters_download_text_hl: Text

    @FXML
    private lateinit var addonDescTab: Tab

    @FXML
    private lateinit var addonDescAnchor: AnchorPane

    @FXML
    private lateinit var addonDescInnerAnchor: AnchorPane

    @FXML
    private lateinit var addonDescImg: ImageView

    @FXML
    private lateinit var addonDescRectImg: Rectangle

    @FXML
    private lateinit var addonDescAnchorDownloadButton: AnchorPane

    @FXML
    private lateinit var addonDescDownloadButtonRect: Rectangle

    @FXML
    private lateinit var addonDescDownloadText: Text

    @FXML
    private lateinit var addonDescName: Text

    @FXML
    private lateinit var addonDescProgressbar: ProgressBar

    @FXML
    private lateinit var addonDescInfoText: Text

    @FXML
    private lateinit var addonDescDesc: Text

    @FXML
    private lateinit var addonDescTextDesc: Text

    @FXML
    private lateinit var listViewDownloads: ListView<AnchorPane>

    @FXML
    private lateinit var desc_anchorpane: AnchorPane

    @FXML
    private lateinit var desc_image_closeWindow: ImageView

    @FXML
    private lateinit var desc_like_dislike_percentage: Text

    @FXML
    private lateinit var desc_downloads_text: Text

    @FXML
    private lateinit var desc_like_dislike_bar: ProgressBar

    @FXML
    private lateinit var desc_like_image: ImageView

    @FXML
    private lateinit var desc_dislike_image: ImageView

    @FXML
    private lateinit var desc_like_count: Text

    @FXML
    private lateinit var desc_dislike_count: Text

    @FXML
    private lateinit var desc_Addon_Image: ImageView

    @FXML
    private lateinit var desc_Title: Text

    @FXML
    private lateinit var desc_long_text: Text

    @FXML
    private lateinit var desc_download_icon: ImageView

    @FXML
    private lateinit var launch_anchorpane: AnchorPane

    @FXML
    private lateinit var launch_text: Text

    @FXML
    private lateinit var show_while_downloading: ProgressIndicator

    @FXML
    private lateinit var topbar_anchorpane: AnchorPane

    @FXML
    private lateinit var topbar_image_closeWindow: ImageView

    @FXML
    private lateinit var topbar_image_minimizeWindow: ImageView

    @FXML
    private lateinit var topbar_image_settings: ImageView

    @FXML
    private lateinit var txtWarning: Text

    @FXML
    private lateinit var installed_edit_text: Text

    @FXML
    private lateinit var installed_edit_text_hl: Text

    @FXML
    private lateinit var selectDownload: Text

    @FXML
    private lateinit var anchorDownload: AnchorPane

    @FXML
    private lateinit var listViewDownloadSelect: ListView<String>

    @FXML
    private lateinit var anchorDownloadpopup: AnchorPane

    @FXML
    private lateinit var textDownloadPopup: Text

    @FXML
    private lateinit var imgViewDownloadPopup: ImageView

    @FXML
    private lateinit var core_tab_downloads: Tab

    @FXML
    private lateinit var anchorModifyConfig: AnchorPane

    @FXML
    private lateinit var runAddonOnLaunch: CheckBox

    @FXML
    private lateinit var inputLaunchCommand: TextField

    @FXML
    private lateinit var inputInstallDir: TextField

    @FXML
    private lateinit var inputAddonID: TextField

    @FXML
    private lateinit var inputAddonName: TextField

    @FXML
    private lateinit var inputVersion: TextField

    @FXML
    private lateinit var inputLastUpdate: TextField

    @FXML
    fun desc_image_close_onMouseClicked(event: MouseEvent)
    {
        Platform.runLater { desc_anchorpane.isVisible = false }
    }

    @FXML
    fun desc_image_close_onMouseEntered(event: MouseEvent)
    {
        changeImage(desc_image_closeWindow, "/icons/cancel.png")
    }

    @FXML
    fun desc_image_close_onMouseExit(event: MouseEvent)
    {
        changeImage(desc_image_closeWindow, "/icons/cancel0.png")
    }

    private var dismissLootFilterWarning = false
    @FXML
    fun hideTxtWarning(event: MouseEvent)
    {
        dismissLootFilterWarning = true
        Platform.runLater { txtWarning.isVisible = false }
    }

    @FXML
    fun installUpdateClick(event: MouseEvent)
    {

    }

    @FXML
    fun installUpdateMouseEnter(event: MouseEvent)
    {
        installed_update_text.isVisible = false
        installed_update_text_hl.isVisible = true
    }

    @FXML
    fun installUpdateMouseExit(event: MouseEvent)
    {
        installed_update_text.isVisible = true
        installed_update_text_hl.isVisible = false
    }

    @FXML
    fun launchPoE(event: MouseEvent)
    {
        PAL2.SystemHandling.launchPoE()
    }

    @FXML
    fun launchPoEEnter(event: MouseEvent)
    {
        Platform.runLater {
            launch_anchorpane.style = ""
            launch_anchorpane.style = "-fx-background-color: #515658"
        }
    }

    @FXML
    fun launchPoEExit(event: MouseEvent)
    {
        Platform.runLater {
            launch_anchorpane.style = ""
            launch_anchorpane.style = "-fx-background-color: #3C3F41"
        }
    }

    private var xOffset = 0.0
    private var yOffset = 0.0

    @FXML
    fun onMouseDragged(event: MouseEvent)
    {
        CoreApplication.stage.x = event.screenX + xOffset
        CoreApplication.stage.y = event.screenY + yOffset
    }

    @FXML
    fun onMousePressed(event: MouseEvent)
    {
        xOffset = CoreApplication.stage.x - event.screenX
        yOffset = CoreApplication.stage.y - event.screenY
    }

    @FXML
    fun refreshOnClick(event: MouseEvent)
    {
        //TODO: Force Checking for GitHub release

    }

    @FXML
    fun refreshOnMouseEnter(event: MouseEvent)
    {
        Platform.runLater {
            installed_refresh_text_hl.isVisible = true
            installed_refresh_text.isVisible = false
        }
    }

    @FXML
    fun refreshOnMouseExit(event: MouseEvent)
    {
        Platform.runLater {
            installed_refresh_text_hl.isVisible = false
            installed_refresh_text.isVisible = true
        }
    }

    @FXML
    fun removeClick(event: MouseEvent)
    {
        if (event.button == MouseButton.PRIMARY)
        {
            val sel = listViewInstalledAddons.selectionModel.selectedItem

            if (sel != null)
            {
                val aid = sel.id.toInt()
                removeAddon(aid)
                setDownloadableAddon(aid)
                Platform.runLater {
                    listViewInstalledAddons.selectionModel.clearSelection()
                    listViewInstalledAddons.items.remove(sel)
                }
            }
        }
    }

    @FXML
    fun removeEnter(event: MouseEvent)
    {
        installed_remove_text.isVisible = false
        installed_remove_text_hl.isVisible = true
    }

    @FXML
    fun removeExit(event: MouseEvent)
    {
        installed_remove_text.isVisible = true
        installed_remove_text_hl.isVisible = false
    }

    @FXML
    fun topbar_closeWIndow_onMouseClicked(event: MouseEvent)
    {
        System.exit(0)
    }

    @FXML
    fun topbar_closeWindow_onMouseEntered(event: MouseEvent)
    {
        changeImage(topbar_image_closeWindow, "/icons/cancel.png")
    }

    @FXML
    fun topbar_closeWindow_onMouseExited(event: MouseEvent)
    {
        changeImage(topbar_image_closeWindow, "/icons/cancel0.png")
    }

    @FXML
    fun topbar_minimizeWindow_onMouseClicked(event: MouseEvent)
    {
        CoreApplication.stage.isIconified = true
    }

    @FXML
    fun topbar_minimizeWindow_onMouseEntered(event: MouseEvent)
    {
        changeImage(topbar_image_minimizeWindow, "/icons/minimize_hl.png")
    }

    @FXML
    fun topbar_minimizeWindow_onMouseExited(event: MouseEvent)
    {
        changeImage(topbar_image_minimizeWindow, "/icons/minimize.png")
    }

    var settingsShowing = false

    @FXML
    fun topbar_settings_onMouseClicked(event: MouseEvent)
    {
        if (settingsShowing)
        {
            // Sync with Global Data
            syncSettingsWithGlobal()

            // Sync with DB
            syncGlobalWithDB()
        }

        settingsShowing = !settingsShowing
        Platform.runLater { anchorSettings.isVisible = settingsShowing }
    }

    fun syncSettingsWithGlobal()
    {
        // AHK
        GlobalData.ahkFolder = File(sAHKFolder.text)
        GlobalData.ahk_scripts.addAll(sListViewAHKScripts.items)

        // General Settings
        GlobalData.steam_poe = useSteam.isSelected
        if (sComboPoE.selectionModel.selectedItem != null)
        {
            val f = File(sComboPoE.selectionModel.selectedItem)
            if (f.exists())
            {
                GlobalData.primaryPoEFile = f
            }
        }

        GlobalData.launchPOEonLaunch = launchPoEOnPalLaunch.isSelected
        GlobalData.showUpdateNotesOnUpdate = showUpdateNotes.isSelected
        GlobalData.gitHubAPIEnabled = useGitHubApi.isSelected
        GlobalData.github_token = textFieldGitHubToken.text

        // Folders
        val fAddon = File(sAddonFolder.text)
        if (fAddon.exists())
        {
            GlobalData.addonFolder = fAddon
        }

        val fTempD = File(sTempDownFolder.text)
        if (fTempD.exists())
        {
            GlobalData.temp_down_folder = fTempD
        }

        // PoE Locations are set when they are changed no need to update.
    }

    @FXML
    fun topbar_settings_onMouseEntered(event: MouseEvent)
    {
        changeImage(topbar_image_settings, "/icons/settings_hl.png")
    }

    @FXML
    fun topbar_settings_onMouseExited(event: MouseEvent)
    {
        changeImage(topbar_image_settings, "/icons/settings.png")
    }

    @FXML
    fun updateAllClick(event: MouseEvent)
    {
        for (ap in listViewInstalledAddons.items)
        {
            GlobalScope.launch {
                Platform.runLater {
                    val rect = ap.lookup("#rectButton") as Rectangle
                    val text = ap.lookup("#textButton") as Text

                    rect.isVisible = false
                    text.text = "Downloading"
                }
                val displayImage = ap.lookup("#imageInfo") as ImageView
                updateAddon(ap.id.toInt(), displayImage.image)
            }

        }
    }

    @FXML
    fun updateAllEnter(event: MouseEvent)
    {
        installed_updateAll_text.isVisible = false
        installed_updateAll_text_hl.isVisible = true
    }

    @FXML
    fun updateAllExit(event: MouseEvent)
    {
        installed_updateAll_text.isVisible = true
        installed_updateAll_text_hl.isVisible = false
    }

    fun editClick(mouseEvent: MouseEvent)
    {
        val sel = listViewInstalledAddons.selectionModel.selectedItem ?: return
        val aid = sel.id.toInt()
        val a = GlobalData.getAddonByID(aid) ?: return
        val img = sel.lookup("#imageInfo") as ImageView
        val bVersion = sel.lookup("#bTextVersion") as Text
        val tAddonName = sel.lookup("#textAddonName") as Text
        val lastUpdateCheckText = sel.lookup("#bTextLastCheck") as Text

        Platform.runLater {
            // TODO: Show/Hide Download button
            addonDescAnchorDownloadButton.isVisible = false
            setDescInfo(img.image, a.name, "", aid)

            inputLaunchCommand.text = getLaunchCommand(aid)
            inputInstallDir.text = getInstallDir(aid)!!.path
            inputAddonID.text = aid.toString()
            inputAddonName.text = tAddonName.text
            inputVersion.text = bVersion.text
            inputLastUpdate.text = lastUpdateCheckText.text
            runAddonOnLaunch.isSelected = getRunAddonOnLaunch(aid)

            showConfigWindow()
        }
    }

    fun showConfigWindow()
    {
        Platform.runLater { anchorModifyConfig.isVisible = true }
    }

    fun hideConfigWindow()
    {
        Platform.runLater { anchorModifyConfig.isVisible = false }
    }

    fun editEnter(mouseEvent: MouseEvent)
    {
        installed_edit_text.isVisible = false
        installed_edit_text_hl.isVisible = true
    }

    fun editExit(mouseEvent: MouseEvent)
    {
        installed_edit_text.isVisible = true
        installed_edit_text_hl.isVisible = false
    }

    var closable = false
    fun hideAddonDescTab(event: Event)
    {
        if (closable)
        {
            Platform.runLater {
                anchorModifyConfig.isVisible = false
                addonDescAnchorDownloadButton.isVisible = true
                core_tabpane.tabs.remove(addonDescTab)
                hideDownloadsPage()
            }
        }

    }

    fun downEnter(mouseEvent: MouseEvent)
    {
        Platform.runLater { addonDescDownloadButtonRect.stroke = Color(1.0, 0.0, 1.0, 1.0) }
    }

    fun downExit(mouseEvent: MouseEvent)
    {
        Platform.runLater { addonDescDownloadButtonRect.stroke = Color.WHITE }
    }

    fun downclick(mouseEvent: MouseEvent)
    {
        val addon = GlobalData.getAddonByID(currentAIDdesc)
        if (addon != null)
        {
            if (anchorDownload.isVisible)
            {
                val selected = listViewDownloadSelect.selectionModel.selectedItem

                if (selected != null)
                {
                    var download_url = ""
                    for (dl in addon.download_urls)
                    {
                        if (dl.endsWith(selected))
                            download_url = dl
                    }

                    if (download_url != "")
                    {
                        startDownload(download_url, currentAIDdesc, null)
                    }
                }
            }
            else
            {
                val download_urls = checkForUseableDownloads(addon.download_urls, addon.aid)
                if (download_urls.size == 1)
                {
                    startDownload(download_urls[0], currentAIDdesc, null)
                }
                else
                {
                    Platform.runLater {
                        showDownloadsPage(download_urls)
                    }
                }
            }
        }


    }

    fun showDownloadsPage(download_urls: Array<String>)
    {
        val arr = ArrayList<String>()
        for (dl in download_urls)
        {
            val f = File(dl)
            f.name
            arr.add(f.name)
        }


        Platform.runLater {
            anchorDownload.isVisible = true
            addonDescDownloadButtonRect.opacity = 0.3
            addonDescDownloadText.opacity = 0.3
            listViewDownloadSelect.items.removeAll()
            listViewDownloadSelect.items.addAll(arr)
        }
    }

    fun hideDownloadsPage()
    {
        Platform.runLater {
            anchorDownload.isVisible = false
            addonDescDownloadButtonRect.opacity = 1.0
            addonDescDownloadText.opacity = 1.0
        }
    }

    fun updateDownload(mouseEvent: MouseEvent)
    {
        if (addonDescDownloadButtonRect.opacity != 1.0)
        {
            if (listViewDownloadSelect.selectionModel.selectedItem != null)
            {
                Platform.runLater {
                    addonDescDownloadButtonRect.opacity = 1.0
                    addonDescDownloadText.opacity = 1.0
                }
            }

        }
    }

    fun startDownload(download_url: String, aid: Int, image: Image?)
    {
        showPopup = false
        Platform.runLater {
            setDownloading(aid)

            anchorDownloadpopup.isVisible = true
            if (image == null)
            {
                imgViewDownloadPopup.image = addonDescImg.image
            }
            else
            {
                addonDescImg.image = image
                imgViewDownloadPopup.image = image
            }

            textDownloadPopup.text = "Downloading: ${File(download_url).name}"
            anchorDownloadpopup.opacity = 1.0
            timerDeletePopup(5.0)
        }

        GlobalScope.launch {
            val fd = FileDownloader()
            //TODO: Change to GlobalData temp folder
            fd.downloadFile(URL(download_url), File("C:\\Users\\Kirishima\\AppData\\Local\\PAL\\temp_downloads"), 1024, addonDescImg.image, aid)
        }
    }

    var showPopup = true
    fun timerDeletePopup(timeout: Double)
    {

        var time = 0.0
        GlobalScope.launch {

            showPopup = false
            delay(100)
            showPopup = true

            while (showPopup)
            {
                delay(100)
                time += 0.1
                if (time > timeout)
                {
                    Platform.runLater { anchorDownloadpopup.isVisible = false }
                    break
                }
                else
                {
                    val op = 1.0 - (time / timeout)
                    Platform.runLater{ anchorDownloadpopup.opacity = op}
                }
            }
            Platform.runLater { anchorDownloadpopup.opacity = 0.0 }
        }
    }

    fun mouseClickedPopupAnchor(mouseEvent: MouseEvent)
    {
        if (mouseEvent.button == MouseButton.PRIMARY)
        {
            Platform.runLater {
                core_tabpane.selectionModel.select(core_tab_downloads)
            }
        }
        Platform.runLater { anchorDownloadpopup.isVisible = false }
    }

    fun saveConfig(actionEvent: ActionEvent)
    {
        // TODO: Remove current anchor and replace it with a new one with the selected data...
        val sel = listViewInstalledAddons.selectionModel.selectedItem ?: return
        val aid = sel.id.toInt()

        Platform.runLater {
            listViewInstalledAddons.items.remove(sel)
            // Add a new IA
            val afd = GlobalData.getAddonByID(aid) ?: return@runLater
            val ia = InstalledAnchor(inputAddonID.text.toInt(), afd.icon_url, inputAddonName.text, inputVersion.text, afd.version_text, LocalDate.now().toString(), "https://www.github.com/${afd.gh_username}/${afd.gh_reponame}")
            //ia.checkUpdateAble()
            CoreApplication.controller.addInstalledAnchor(ia)
            CoreApplication.controller.setInstalled(afd.aid)
        }




        Platform.runLater {
            core_tabpane.selectionModel.select(core_tab_installed)
        }

        GlobalScope.launch {
            updateAddonConfig(inputLaunchCommand.text, inputInstallDir.text, inputAddonID.text.toInt(), inputAddonName.text,
                    inputVersion.text, runAddonOnLaunch.isSelected)
        }

    }

    /**
     * SETTINGS
     */
    @FXML
    private lateinit var anchorSettings: AnchorPane

    @FXML
    private lateinit var listViewSettings: ListView<String>

    @FXML
    private lateinit var sAutoHotKey: AnchorPane

    @FXML
    private lateinit var sFolders: AnchorPane

    @FXML
    private lateinit var sAbout: AnchorPane

    @FXML
    private lateinit var sGeneral: AnchorPane


    val settingsArray = arrayOf("About", "AutoHotKey", "General Settings", "Folders")
    fun populateSettingsList()
    {
        Platform.runLater { listViewSettings.items.addAll(settingsArray) }
    }

    private fun setSettings()
    {
        Platform.runLater {
            SETAHKSettings()
            SETGeneralSettings()
            SETFolders()
        }
    }

    private fun SETFolders()
    {
        sAddonFolder.text = GlobalData.addonFolder.path
        sTempDownFolder.text = GlobalData.temp_down_folder.path
        sLootFilterFolder.text = GlobalData.loot_filter_path

        for (pl in GlobalData.poeLocations)
        {
            if (!poeLocations.items.contains(pl))
            {
                poeLocations.items.add(pl)
            }
        }
    }

    private fun SETGeneralSettings()
    {
        useSteam.isSelected = GlobalData.steam_poe
        launchPoEOnPalLaunch.isSelected = GlobalData.launchPOEonLaunch
        showUpdateNotes.isSelected = GlobalData.showUpdateNotesOnUpdate
        useGitHubApi.isSelected = GlobalData.gitHubAPIEnabled
        textFieldGitHubToken.text = GlobalData.github_token
        checkUseSteam()
        val primPoE = GlobalData.primaryPoEFile
        if (primPoE != null)
        {
            if (sComboPoE.items.contains(primPoE.path))
            {
                sComboPoE.selectionModel.select(primPoE.path)
            }
        }
    }

    fun checkUseSteam()
    {
        if (useSteam.isSelected)
        {
            sComboPoE.isDisable = true
        }
        else
        {
            sComboPoE.isDisable = false
            // Populate list
            for (pl in GlobalData.poeLocations)
            {
                if (!sComboPoE.items.contains(pl))
                    sComboPoE.items.add(pl)
            }
        }
    }

    fun SETAHKSettings()
    {
        sAHKFolder.text = GlobalData.ahkFolder.path
        sListViewAHKScripts.items.addAll(GlobalData.ahk_scripts)
    }

    fun settingsListViewClicked(event: MouseEvent)
    {
        if (event.button == MouseButton.PRIMARY)
        {
            val sel = listViewSettings.selectionModel.selectedItem
            if (sel != null)
            {
                when (sel)
                {
                    "About" -> showAbout()
                    "AutoHotKey" -> showAHK()
                    "General Settings" -> showGeneralSettings()
                    "Folders" -> showFolders()
                }
            }
        }
    }

    private fun showFolders()
    {
        hideAll()
        Platform.runLater { sFolders.isVisible = true }
    }

    private fun showGeneralSettings()
    {
        hideAll()
        Platform.runLater { sGeneral.isVisible = true }
    }

    private fun showAHK()
    {
        hideAll()
        Platform.runLater { sAutoHotKey.isVisible = true }
    }

    private fun showAbout()
    {
        hideAll()
        Platform.runLater { sAbout.isVisible = true }
    }

    private fun hideAll()
    {
        Platform.runLater {
            sAutoHotKey.isVisible = false
            sFolders.isVisible = false
            sGeneral.isVisible = false
            sAbout.isVisible = false
        }
    }

    fun installAHK(actionEvent: ActionEvent)
    {
        val inputStream = URI.create("https://github.com/Lexikos/AutoHotkey_L/releases/download/v1.1.30.00/AutoHotkey_1.1.30.00_setup.exe").toURL().openStream()


        val location = "${GlobalData.temp_down_folder.path}${File.separator}AutoHotkey_1.1.30.00_setup.exe"

        val installer = File(location)
        if (installer.exists())
            installer.delete()

        Files.copy(inputStream, Paths.get(location))
        Runtime.getRuntime().exec("cmd /c $location")
    }

    fun addAHK()
    {
        Platform.runLater {
            val chosenFile = browseFile("Choose an AHK file to add")
            if (chosenFile != null)
            {
                if (chosenFile.extension == "ahk")
                {
                    if (!sListViewAHKScripts.items.contains(chosenFile.path))
                    {
                        sListViewAHKScripts.items.add(chosenFile.path)
                    }
                }
            }
        }
    }

    fun removeAHK()
    {
        val sel = sListViewAHKScripts.selectionModel.selectedItem
        if (sel != null)
        {
            Platform.runLater { sListViewAHKScripts.items.remove(sel) }
            GlobalData.ahk_scripts.remove(sel)
            removeAHKscript(sel)
        }
    }

    fun browse(title: String): File?
    {
        val directoryChooser = DirectoryChooser()
        directoryChooser.title = title
        return directoryChooser.showDialog(CoreApplication.stage)
    }

    fun browseFile(title: String): File?
    {
        val fileChooser = FileChooser()
        fileChooser.title = title
        return fileChooser.showOpenDialog(CoreApplication.stage)
    }

    fun removePoELocation(actionEvent: ActionEvent)
    {
        val sel = poeLocations.selectionModel.selectedItem
        if (sel != null)
        {
            Platform.runLater {
                poeLocations.selectionModel.clearSelection()
                poeLocations.items.remove(sel)
                if (sComboPoE.selectionModel.selectedItem == sel)
                {
                    sComboPoE.selectionModel.clearSelection()
                }
                sComboPoE.items.remove(sel)
                GlobalData.poeLocations.remove(sel)
                removePoELoc(sel)
            }

        }
    }

    fun addPoELocation(actionEvent: ActionEvent)
    {
        Platform.runLater {
            val selected = browse("Select a Path of Exile Folder")
            if (selected != null)
            {
                poeExeLocationsArrayList = ArrayList()
                recursiveScan(selected)

                for (foundExe in poeExeLocationsArrayList)
                {
                    if (!poeLocations.items.contains(foundExe))
                    {
                        poeLocations.items.add(foundExe)
                        sComboPoE.items.add(foundExe)
                        GlobalData.poeLocations.add(foundExe)
                    }
                }
            }
        }
    }

    var poeExeLocationsArrayList = ArrayList<String>()
    fun recursiveScan(location: File)
    {
        val pattern: Pattern = Pattern.compile("pathofexile.*\\.exe", Pattern.CASE_INSENSITIVE)
        location.walkTopDown().forEach {
            GlobalScope.launch {
                if (pattern.matcher(it.name).matches())
                {
                    poeExeLocationsArrayList.add(it.path)
                }
            }
        }
    }

    fun browseAHKFolder(actionEvent: ActionEvent)
    {
        val file = browse("Select your AHK Folder")
        if (file != null)
        {
            Platform.runLater { sAHKFolder.text = file.path }
        }
    }

    fun browseAddonFolder(actionEvent: ActionEvent)
    {
        val file = browse("Select your addon folder")
        if (file != null)
        {
            Platform.runLater { sAddonFolder.text = file.path }
        }
    }

    fun browseTempDown(actionEvent: ActionEvent)
    {
        val file = browse("Select your temp download folder")
        if (file != null)
        {
            Platform.runLater { sTempDownFolder.text = file.path }
        }
    }

    fun browseLootFilter(actionEvent: ActionEvent)
    {
        val file = browse("Select your loot filter folder")
        if (file != null)
        {
            Platform.runLater { sLootFilterFolder.text = file.path }
        }
    }

    @FXML
    private lateinit var reddit: Hyperlink

    @FXML
    private lateinit var discord: Hyperlink

    @FXML
    private lateinit var twitter: Hyperlink

    @FXML
    private lateinit var twitch: Hyperlink

    @FXML
    private lateinit var youtube: Hyperlink

    @FXML
    private lateinit var github: Hyperlink

    @FXML
    private lateinit var patreon: Hyperlink

    @FXML
    private lateinit var sAddonFolder: TextField

    @FXML
    private lateinit var bBrowseMainAddonFolder: Button

    @FXML
    private lateinit var sTempDownFolder: TextField

    @FXML
    private lateinit var bBrowseTempDownFolder: Button

    @FXML
    private lateinit var sLootFilterFolder: TextField

    @FXML
    private lateinit var bBrowseLootFilterFolder: Button

    @FXML
    private lateinit var poeLocations: ListView<String>

    @FXML
    private lateinit var bAddPoELocation: Button

    @FXML
    private lateinit var bRemovePoELocation: Button

    @FXML
    private lateinit var sAHKFolder: TextField

    @FXML
    private lateinit var bBrowseAHK: Button

    @FXML
    private lateinit var installAutoHotKey: Hyperlink

    @FXML
    private lateinit var sListViewAHKScripts: ListView<String>

    @FXML
    private lateinit var bAddAHKScript: Button

    @FXML
    private lateinit var bRemoveAHKScript: Button

    @FXML
    private lateinit var useGitHubApi: CheckBox

    @FXML
    private lateinit var useSteam: CheckBox

    @FXML
    private lateinit var launchPoEOnPalLaunch: CheckBox

    @FXML
    private lateinit var showUpdateNotes: CheckBox

    @FXML
    private lateinit var textFieldGitHubToken: TextField

    // TODO: Active
    @FXML
    private lateinit var howDoIGetAGithubToken: Hyperlink

    @FXML
    private lateinit var sComboPoE: ComboBox<String>

}
