package SystemHandling

import Github.getAddonsFromGHRepo
import PAL2.Database.*
import PAL2.GUI.Configurator.ConfiguratorApplication
import PAL2.GUI.CoreApplication
import PAL2.GUI.Loader.Loader
import PAL2.Github.getUpdate
import PAL_DataClasses.PAL_AddonFullData
import PAL_DataClasses.initObjectMapper
import javafx.application.Platform
import javafx.stage.Stage
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL

private val logger = KotlinLogging.logger {}
/**
 *
 */
var configurating = false

fun loader(arg: String, d: Double)
{
    Loader.controller.setText(arg)
    Loader.controller.updateProgressbar(d)
}

fun init()
{
    Platform.runLater { CoreApplication.stage.opacity = 0.01 }

    loader("Initializing Folders", 0.0)
    logger.debug { "Init: Folders" }
    initFolders()
    logger.debug { "Init: DB" }
    loader("Initializing Database", 0.1)
    initDB()

    logger.debug { "Init: Misc" }
    loader("Initializing User Data", 0.33)
    moreInit()


    //CoreApplication.controller.addInstalledAnchorTEST()
}

fun initFolders()
{
    verifyFolder(GlobalData.pal_folder)
    verifyFolder(GlobalData.pal_data_folder)
}

fun initDB()
{
    if (!GlobalData.db_file.exists())
    {
        configurating = true
        createDB()

        // Ask user to enter information we need.
        // Launch simple UI to ask for it.
        Platform.runLater {
            val c = ConfiguratorApplication()
            c.start(Stage())
        }

    }
    GlobalData.first_launch = false
}

// TODO: MORE INIT STUFF, IN TERMS OF DB SETTINGS AND FINISH CONFIGURATOR

fun moreInit()
{
    loader("Waiting on configurator", 0.34)
    while (configurating)
    {
        Thread.sleep(250)
    }
    //TODO: Read data from database
    if (!GlobalData.first_launch)
    {
        if (GlobalData.showUpdateNotesOnUpdate)
        {
            if (getLastPALVersion() != GlobalData.version)
            {
                setPALVersion()
                GlobalData.show_update_note
            }
        }

        loader("Getting previous configuration", 0.4)
        retreiveConfig()
        //Get AHK Scripts
        loader("Getting AHK Scripts", 0.5)
        getAHKScripts()

        val f = getPrimaryPoE()
        if (f != "")
        {
            GlobalData.primaryPoEFile = File(f)
        }
    }

    loader("Checking for Updates to PAL", 0.5)
    getUpdate(GlobalData.install_dir)
    loader("No updates to PAL!", 0.6)


    loader("Downloading Addons from Repository", 0.6)
    // Check if a Token has been set
    if (GlobalData.github_token == "" || !GlobalData.gitHubAPIEnabled)
    {
        staticRepoAddons()
    }
    else
    {
        // TODO: Parse repos from DB
        // Use GitHub repo
        val addons = getAddonsFromGHRepo("POE-Addon-Launcher/server")
        if (addons == null)
        {
            staticRepoAddons()
        }
        else
        {
            GlobalData.set_of_addons.addAll(addons)
        }
    }
    loader("Initializing: Addon List", 0.8)
    CoreApplication.controller.initAddonListView()

    // Get Installed Addons
    loader("Checking for your installed addons", 0.9)
    getInstalledAddons()

    loader("Initialization done!", 1.0)
    Loader.controller.close()
    Platform.runLater { CoreApplication.stage.opacity = 1.0 }
}

fun staticRepoAddons()
{
    logger.debug { "Getting addons from static file!" }
    // Use static repo
    // https://raw.githubusercontent.com/POE-Addon-Launcher/server/master/addons.json
    val addonsJson = File("${GlobalData.pal_folder}${File.separator}addons.json")
    deleteFile(addonsJson)

    FileUtils.copyURLToFile(URL("https://raw.githubusercontent.com/POE-Addon-Launcher/server/master/addons.json"), addonsJson)

    val objectMapper = initObjectMapper()
    GlobalData.addToListOfAddons(objectMapper.readValue(addonsJson, Array<PAL_AddonFullData>::class.java))
    deleteFile(addonsJson)

    // Extra sanity check!
    GlobalData.checkAddonListForDuplicates()
}