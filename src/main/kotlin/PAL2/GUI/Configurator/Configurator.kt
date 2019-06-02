package PAL2.GUI.Configurator

import Data.PALsettings
import GlobalData
import PAL2.Database.insertConfiguratorData
import PAL_DataClasses.initObjectMapper
import SystemHandling.configurating
import SystemHandling.verifyFolder
import javafx.application.Application
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.layout.AnchorPane
import javafx.scene.text.Text
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.kohsuke.github.GitHub
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.net.URL
import java.util.*
import kotlin.collections.HashSet

private val logger = KotlinLogging.logger {}
/**
 *
 */
data class Configurator(var addonfolder: File?, var tempDownFolder: File?, var poeFolder: HashSet<File>?, var ahkFolder: File?,
                        var githubToken: String, var launchPOEonLaunch: Boolean, var githubAPIenabled: Boolean, var showUpdateNotesOnUpdate: Boolean,
                        var useSteamPoE: Boolean)

class ConfiguratorApplication: Application()
{
    companion object
    {
        lateinit var stage: Stage
    }

    override fun start(primaryStage: Stage?)
    {
        stage = Stage()
        val fxmlLoader = FXMLLoader()
        stage.initStyle(StageStyle.UNDECORATED)
        val root = fxmlLoader.load<Parent>(javaClass.getResource("/Configurator.fxml").openStream())

        stage.title = "PAL: Core"
        stage.icons.add(Image(javaClass.getResource("/witch.png").toString()))
        val scene = Scene(root, 600.0, 400.0)
        scene.stylesheets.add("layout_settings.css")
        stage.scene = scene
        stage.show()
    }

}

class ConfiguratorController: Initializable
{
    var config = Configurator(null, null, null, null, "", false, true, true, false)
    var step = 1

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        checkForPoEPaths()
        checkForOldSettings()

        GlobalScope.launch {
            tokenChecker()
        }
        Platform.runLater {
            tempDownFolder.text = "${GlobalData.pal_folder}${File.separator}temp_downloads"
            addonInstallFolder.text = "${GlobalData.pal_folder}${File.separator}Addons"
            txtWarn.isVisible = false
            step1.isVisible = true
        }
    }

    private fun checkForOldSettings()
    {
        val path = "${GlobalData.pal_folder}${File.separator}core_settings.pal"
        val coreSettings = File(path)
        if (coreSettings.exists())
        {
            val om = initObjectMapper()
            val res = om.readValue(coreSettings, PALsettings::class.java)

            val ahk = res.ahK_Folder
            if (File(ahk).exists())
            {
                config.ahkFolder = File(ahk)
                Platform.runLater { ahk_folder.text = ahk }
            }

            Platform.runLater {
                api_token.text = res.github_token
                checkBoxGithubAPI.isSelected = res.isGithub_api_enabled
                checkShowUpdateNotes.isSelected = true
                checkmarkLaunchPOEPAL.isSelected = res.isRun_poe_on_launch
            }
        }
    }

    private fun checkForPoEPaths()
    {
        val path = "${GlobalData.pal_folder}${File.separator}poe_paths.pal"
        val poePathsFile = File(path)
        if (poePathsFile.exists())
        {
            val om = initObjectMapper()
            val res = om.readValue(poePathsFile, Array<String>::class.java)
            for (str in res)
            {
                poeExes.add(File(str))
            }
            Platform.runLater { poeFolder.text = "Loaded data from previous installation!" }
        }
    }

    private fun tokenChecker()
    {
        var cache = ""
        GlobalScope.launch {
            while (configurating)
            {
                if (api_token.text != cache)
                {
                    showCheckToken()
                    logger.debug { "Checking: ${api_token.text}" }
                    cache = api_token.text
                    checkToken()
                }
                delay(100)
            }
        }
    }

    private fun checkToken()
    {
        Platform.runLater {
            if (api_token.text.length >= 40)
            {
                try
                {
                    val gh = GitHub.connectUsingOAuth(api_token.text)
                    if (gh.rateLimit.remaining > 0)
                    {
                        txtInValidToken.isVisible = false
                        txtValidToken.isVisible = true
                        txtcheckingToken.isVisible = false
                    }
                }
                catch (ex: Exception)
                {
                    txtInValidToken.isVisible = true
                    txtValidToken.isVisible = false
                    txtcheckingToken.isVisible = false
                }
            }
            else
            {
                txtInValidToken.isVisible = true
                txtValidToken.isVisible = false
                txtcheckingToken.isVisible = false
            }
        }
    }

    fun showCheckToken()
    {
        Platform.runLater {
            txtcheckingToken.isVisible = true
            txtInValidToken.isVisible = false
            txtValidToken.isVisible = false
        }
    }

    var warnNoAPIToken = false

    fun continueConfig()
    {
        if (step == 1)
        {
            if (addonInstallFolder.text != "")
            {
                if (checkFolders())
                {
                    step = 2
                    Platform.runLater {
                        step1.isVisible = false
                        page2.isVisible = true
                        totalProgress.progress = 0.5
                    }
                }
            }
        }
        else if (step == 2)
        {
            if (txtValidToken.isVisible)
            {
                Platform.runLater {
                    page2Setter()
                    totalProgress.progress = 1.0

                    insertConfiguratorData(config)

                    println("?")
                    configurating = false
                    ConfiguratorApplication.stage.close()

                }
            }
            else if (warnNoAPIToken)
            {
                Platform.runLater {
                    page2Setter()
                    totalProgress.progress = 1.0

                    insertConfiguratorData(config)

                    println("?")

                    configurating = false
                    ConfiguratorApplication.stage.close()

                }
            }
            else
            {
                Platform.runLater{
                    txtWarn.isVisible = true
                    warnNoAPIToken = true
                }
            }
        }
    }

    private fun page2Setter()
    {
        config.launchPOEonLaunch = checkmarkLaunchPOEPAL.isSelected
        config.githubAPIenabled = checkBoxGithubAPI.isSelected
        config.showUpdateNotesOnUpdate = checkShowUpdateNotes.isSelected
        config.githubToken = api_token.text
        config.useSteamPoE = steamPoE.isSelected
    }

    val poeExes = HashSet<File>()
    lateinit var temp_down_folder: File
    lateinit var addon_install_folder: File

    private fun checkFolders(): Boolean
    {
        val tempDown = File(tempDownFolder.text)
        val addonInstall = File(addonInstallFolder.text)

        verifyFolder(tempDown)
        temp_down_folder = tempDown
        if (tempDown.isDirectory)
        {
            GlobalData.temp_down_folder = temp_down_folder
        }
        else
        {
            // Use appdata
            val temp_down = File("${GlobalData.pal_folder.path}${File.separator}temp_down")
            verifyFolder(temp_down)
            temp_down_folder = temp_down
        }

        config.tempDownFolder = temp_down_folder

        verifyFolder(addonInstall)
        if (addonInstall.isDirectory)
        {
            addon_install_folder = addonInstall
            config.addonfolder = addon_install_folder
        }



        // PoE Folder
        if (poeFolder.text == "")
        {

        }
        if (poeFolder.text == "Loaded data from previous installation!")
        {
            config.poeFolder = poeExes
        }
        else
        {
            val poeFolder = File(poeFolder.text)
            if (poeFolder.isDirectory)
            {
                checkDir(poeFolder.listFiles())
                config.poeFolder = poeExes
            }
        }

        if (ahk_folder.text != "")
        {
            val ahk_folder = File(ahk_folder.text)
            if (ahk_folder.isDirectory)
            {
                config.ahkFolder = ahk_folder
            }
        }

        return true
    }

    private fun emptyDirEnforcer(f: File, folderName: String): File
    {
        val deeper = File("${f.path}${File.separator}$folderName")

        if (deeper.exists())
        {
            return emptyDirEnforcer(deeper, folderName)
        }
        deeper.mkdir()
        return deeper
    }

    private fun checkDir(filesInDir: Array<File>)
    {
        for (f in filesInDir)
        {
            checkForPoE(f)
        }
    }

    private fun checkForPoE(f: File)
    {
        val filename = f.name
        if (filename == "PathOfExile_x64Steam.exe")
        {
            poeExes.add(f)
        }
        else if (filename == "PathOfExile_x64.exe")
        {
            poeExes.add(f)
        }
        else if (filename.endsWith(".exe") && filename.contains("PathOfExile"))
        {
            poeExes.add(f)
        }
    }

    fun openWebsite()
    {
        // https://imgur.com/a/QOayVqU - Tutorial
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
        {
            Desktop.getDesktop().browse(URI("https://imgur.com/a/QOayVqU"))
        }
    }

    /**
     * Method for Opening the DirectoryChooser.
     */
    fun browse(title: String): File?
    {
        val directoryChooser = DirectoryChooser()
        directoryChooser.title = title
        return directoryChooser.showDialog(ConfiguratorApplication.stage)
    }

    fun browseForAHK(actionEvent: ActionEvent)
    {
        val f = browse("Browse for your AHK Folder") ?: return

        if (f.isDirectory)
        {
            Platform.runLater { ahk_folder.text = f.path }
        }
    }

    fun browseForPOE(actionEvent: ActionEvent)
    {
        val f = browse("Browse for your Path of Exile folder") ?: return
        if (f.isDirectory)
        {
            Platform.runLater { poeFolder.text = f.path }
        }
    }

    fun browseForAddonFolder(actionEvent: ActionEvent)
    {
        val f = browse("Browse for your default addon folder") ?: return
        if (f.isDirectory)
        {
            Platform.runLater { addonInstallFolder.text = f.path }
        }
    }

    fun browseTempDown(actionEvent: ActionEvent)
    {
        val f = browse("Browse for your Temp Download Folder") ?: return
        if (f.isDirectory)
        {
            Platform.runLater { tempDownFolder.text = f.path }
        }
    }

    @FXML
    lateinit var txtWarn: Text

    @FXML
    lateinit var step1: AnchorPane

    @FXML
    lateinit var tempDownFolder: TextField

    @FXML
    lateinit var addonInstallFolder: TextField

    @FXML
    lateinit var poeFolder: TextField

    @FXML
    lateinit var bBrowseForTempDownloads: Button

    @FXML
    lateinit var bBrowseForAddons: Button

    @FXML
    lateinit var bBrowseForPOE: Button

    @FXML
    lateinit var ahk_folder: TextField

    @FXML
    lateinit var bAHK: Button

    @FXML
    lateinit var bNext: Button

    @FXML
    lateinit var page2: AnchorPane

    @FXML
    lateinit var api_token: TextField

    @FXML
    lateinit var txtValidToken: Text

    @FXML
    lateinit var checkmarkLaunchPOEPAL: CheckBox

    @FXML
    lateinit var checkBoxGithubAPI: CheckBox

    @FXML
    lateinit var checkShowUpdateNotes: CheckBox

    @FXML
    lateinit var howToGetAPIToken: Hyperlink

    @FXML
    lateinit var txtInValidToken: Text

    @FXML
    lateinit var totalProgress: ProgressBar

    @FXML
    lateinit var txtcheckingToken: Text

    @FXML
    lateinit var steamPoE: CheckBox

}