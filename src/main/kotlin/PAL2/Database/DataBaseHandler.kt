package PAL2.Database

import Github.connect
import PAL2.GUI.Configurator.Configurator
import PAL2.GUI.CoreApplication
import PAL2.GUI.InstalledAnchor
import PAL_DataClasses.PAL_AddonFullData
import PAL_DataClasses.createDate
import mu.KotlinLogging
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.time.LocalDate
import kotlin.math.log

private val logger = KotlinLogging.logger {}

/**
 *
 */
fun connectToDB(): Connection
{
    return DriverManager.getConnection("jdbc:sqlite:${GlobalData.db_file.path}")
}

// Reads GlobalData to sync
fun syncGlobalWithDB()
{
    val connection = connectToDB()
    connection.createStatement().executeUpdate("update Flags set launch_poe_on_pal_launch = ${GlobalData.launchPOEonLaunch}, Steam_POE = ${GlobalData.steam_poe}, GitHub_API = ${GlobalData.gitHubAPIEnabled}, show_motd = ${GlobalData.showUpdateNotesOnUpdate}")
    connection.createStatement().executeUpdate("update Addons_Folder set location = \'${GlobalData.addonFolder}\', `primary` = 1;")
    connection.createStatement().executeUpdate("update Meta set " +
            "ahk_folder = \'${GlobalData.ahkFolder.path}\', " +
            "github_API_token = \'${GlobalData.github_token}\', " +
            "temp_down_folder = \'${GlobalData.temp_down_folder}\';")

    val primaryPoE = GlobalData.primaryPoEFile
    if (primaryPoE != null)
    {
        connection.createStatement().execute("delete from PoE_Locations where `primary` = true")
        val rs = connection.createStatement().executeQuery("select count(*) from PoE_Locations where location = \'${primaryPoE.path}\';")
        val c = rs.getInt(1)
        if (c > 0)
        {
            connection.createStatement().execute("delete from PoE_Locations where location = \'${primaryPoE.path}\'")
        }
        connection.createStatement().execute("insert into PoE_Locations values (\'${primaryPoE.path}\', true);")
    }

    syncAHKScripts(connection)
    syncPoEFolders(connection)

    connection.close()
}

fun syncPoEFolders(connection: Connection)
{
    val poe = GlobalData.poeLocations
    for (s in poe)
    {
        val rs = connection.createStatement().executeQuery("select count(*) from PoE_Locations where location = \'$s\';")
        val c = rs.getInt(1)
        if (c == 0)
        {
            connection.createStatement().execute("insert into PoE_Locations values (\'$s\', false);")
        }
    }
}

fun syncAHKScripts(connection: Connection)
{
    val scripts = GlobalData.ahk_scripts

    for (s in scripts)
    {
        val rs = connection.createStatement().executeQuery("select count(*) from AHK_Scripts where location = \'$s\';")
        val c = rs.getInt(1)
        if (c == 0)
        {
            connection.createStatement().execute("insert into AHK_Scripts values (\'$s\', true);")
        }
    }
}

fun removeAHKscript(string: String)
{
    val connection = connectToDB()
    val rs = connection.createStatement().executeQuery("select count(*) from AHK_Scripts where location = \'$string\';")
    val c = rs.getInt(1)
    if (c > 0)
    {
        connection.createStatement().execute("delete from AHK_Scripts where location = \'$string\'")
    }
    connection.close()
}

fun removePoELoc(string: String)
{
    val connection = connectToDB()
    val rs = connection.createStatement().executeQuery("select count(*) from PoE_Locations where location = \'$string\';")
    val c = rs.getInt(1)
    if (c > 0)
    {
        connection.createStatement().execute("delete from PoE_Locations where location = \'$string\';")
    }
    connection.close()
}

fun updateRunAddonWhenLaunching(runOnLaunch: Boolean, aid: Int)
{
    val connection = connectToDB()
    connection.createStatement().executeUpdate("update InstalledAddon set run_on_launch = $runOnLaunch where aid = $aid;")
    connection.close()
}


fun updateAddonConfig(lc: String, installDir: String, aid: Int, addonName: String, version:String, runOnLaunch: Boolean)
{
    val connection = connectToDB()
    connection.createStatement().executeUpdate("update InstalledAddon set name = \"$addonName\", " +
            "install_location = \'$installDir\', " +
            "version = \'$version\', " +
            "last_update = \"${createDate(LocalDate.now())}\", " +
            "launch_command = \'$lc\', " +
            "run_on_launch = $runOnLaunch " +
            "WHERE aid = $aid;")
    connection.close()
}

fun getRunAddonOnLaunch(aid: Int): Boolean
{
    val connection = connectToDB()
    var rs = connection.createStatement().executeQuery("select run_on_launch from InstalledAddon where aid = $aid;")
    val result = rs.getBoolean(1)
    connection.close()
    return result
}

fun removeInstalledAddon(aid: Int)
{
    val connection = connectToDB()
    connection.createStatement().execute("delete from InstalledAddon where aid = $aid;")
    connection.close()
}

fun getInstallDir(aid: Int): File?
{
    val connection = connectToDB()
    var rs = connection.createStatement().executeQuery("select count(install_location) from InstalledAddon where aid = $aid;")
    val c = rs.getInt(1)

    if (c != 0)
    {
        rs = connection.createStatement().executeQuery("select install_location from InstalledAddon where aid = $aid")
        var location = rs.getString(1)
        connection.close()
        return File(location)
    }
    connection.close()
    return null
}

fun getPrimaryPoE(): String
{
    val connection = connectToDB()
    val c = connection.createStatement().executeQuery("select count(*) from PoE_Locations where `primary` = true ").getInt(1)
    if (c > 0)
    {
        val res = connection.createStatement().executeQuery("select location from PoE_Locations where `primary` = true;")
        val rs = res.getString(1)
        connection.close()
        return rs
    }
    connection.close()
    return ""
}

fun getLaunchCommand(aid: Int): String
{
    val connection = connectToDB()
    var rs = connection.createStatement().executeQuery("Select launch_command from InstalledAddon where aid = $aid;")
    val lc = rs.getString(1)
    connection.close()
    return lc
}

fun updateLaunchCommandInstalledAddon(launch_command: String, aid: Int)
{
    val connection = connectToDB()
    connection.createStatement().execute("UPDATE InstalledAddon set launch_command = \'$launch_command\' where aid = $aid;")
    connection.close()
}

fun addInstalledAddon(afd: PAL_AddonFullData, installLocation: File)
{
    var connection = connectToDB()
    val rs = connection.createStatement().executeQuery("Select count(*) from InstalledAddon where aid = ${afd.aid};")
    // Check if installed addon is already here, if it is delete it.
    val c = rs.getInt(1)
    connection.close()
    if (c > 0)
    {
        removeInstalledAddon(afd.aid)
    }
    connection = connectToDB()

    connection.createStatement().execute("Insert into InstalledAddon values (${afd.aid}, \"${afd.name}\", \"$installLocation\", \"${afd.version_text}\", \"${LocalDate.now()}\", \"?\", \"?\", false)")
    connection.close()
}

fun getInstalledAddons()
{
    val connection = connectToDB()
    val statement = connection.createStatement()
    var rs = statement.executeQuery("select count(*) from InstalledAddon")
    val c = rs.getInt(1)
    if (c != 0)
    {
        rs = statement.executeQuery("select aid, name, install_location, version, last_update, creators, launch_command, run_on_launch from InstalledAddon")
        while (rs.next())
        {
            val aid = rs.getInt(1)
            val addon = GlobalData.getAddonByID(aid) ?: break

            val ia = InstalledAnchor(rs.getInt(1), addon.icon_url, addon.name, rs.getString(4), addon.version_text, LocalDate.now().toString(), "https://www.github.com/${addon.name}/${addon.gh_reponame}")
            logger.debug { "$ia" }
            CoreApplication.controller.addInstalledAnchor(ia)
            CoreApplication.controller.setInstalled(addon.aid)
        }
    }
    else
    {
        logger.debug { "No installed addons!" }
    }
    connection.close()
}

fun getAHKScripts()
{
    val connection = connectToDB()
    val statement = connection.createStatement()
    var rs = statement.executeQuery("SELECT count(*) from AHK_Scripts")
    val c = rs.getInt(1)
    if (c != 0)
    {
        rs = statement.executeQuery("SELECT location, run_on_launch from AHK_Scripts")
        while (rs.next())
        {
            GlobalData.ahk_scripts.add(rs.getString(1))
            logger.debug { "Found AHK Script: ${rs.getString(1)} run on launch: ${rs.getBoolean(2)}" }
        }
    }
    else
    {
        logger.debug { "No AHK Scripts stored." }
    }

    connection.close()
}

fun getRunOnLaunchCommands(): Array<String>?
{
    val connection = connectToDB()
    val res = connection.createStatement().executeQuery("select count(*) from InstalledAddon where  run_on_launch = 1")
    val c = res.getInt(1)
    if (c > 0)
    {
        val array = ArrayList<String>()
        val rs = connection.createStatement().executeQuery("select launch_command from InstalledAddon where run_on_launch = 1")
        while (rs.next())
        {
            array.add(rs.getString(1))
        }
        connection.close()
        return array.toTypedArray()
    }
    connection.close()
    return null
}

fun retreiveConfig()
{
    val connection = connectToDB()
    var rs = connection.createStatement().executeQuery("Select * from Addons_Folder where `primary` = 1")
    GlobalData.addonFolder = File(rs.getString(1))


    rs = connection.createStatement().executeQuery("SELECT launch_poe_on_pal_launch, Steam_POE, GitHub_API, show_motd from Flags")
    GlobalData.launchPOEonLaunch = rs.getBoolean(1)
    GlobalData.steam_poe = rs.getBoolean(2)
    GlobalData.gitHubAPIEnabled = rs.getBoolean(3)
    GlobalData.showUpdateNotesOnUpdate = rs.getBoolean(4)
    logger.debug { "Flags {${GlobalData.launchPOEonLaunch}, ${GlobalData.gitHubAPIEnabled}, ${GlobalData.showUpdateNotesOnUpdate}}" }

    rs = connection.createStatement().executeQuery("Select ahk_folder, pal_version, github_API_token, temp_down_folder from Meta")

    if (rs.getString(1) != "")
        GlobalData.ahkFolder = File(rs.getString(1))
    if (GlobalData.version == rs.getString(2))
    {
        GlobalData.first_launch_after_update = false
        logger.debug { "No new update since last launch!" }
    }
    GlobalData.github_token = rs.getString(3)
    logger.debug { "Github Token: ${GlobalData.github_token}" }

    val temp_down = rs.getString(4)
    if (temp_down != "")
    {
        val temp_down_f = File(temp_down)
        if (temp_down_f.exists())
        {
            GlobalData.temp_down_folder = temp_down_f
            logger.debug { "Set: temp down folder to: ${temp_down_f.path}" }
        }
    }


    rs = connection.createStatement().executeQuery("Select count(*) from PoE_Locations where `primary` = 1")
    val c = rs.getInt(1)
    if (c != 0)
    {
        GlobalData.primaryPoEFile = File(connection.createStatement().executeQuery("Select location from PoE_Locations where `primary` = 1").getString(1))
        logger.debug { "Primary PoE: ${GlobalData.primaryPoEFile!!.path}" }
    }

    rs = connection.createStatement().executeQuery("SELECT count(*) from PoE_Locations")
    val count = rs.getInt(1)
    if (count != 0)
    {
        rs = connection.createStatement().executeQuery("SELECT location from PoE_Locations")
        while (rs.next())
        {
            GlobalData.poeLocations.add(rs.getString(1))
            logger.debug { "Adding: ${rs.getString(1)}" }
        }
    }

    connection.close()
}

fun getLastPALVersion(): String
{
    val connection = connectToDB()
    val rs = connection.createStatement().executeQuery("Select pal_version from Meta")
    val result = rs.getString(1)
    connection.close()
    return result
}

fun setPALVersion()
{
    val connection = connectToDB()
    connection.createStatement().executeUpdate("UPDATE Meta set pal_version = \"${GlobalData.version}\";")
    connection.close()
}

fun insertConfiguratorData(config: Configurator)
{
    //TODO: Set TEmp Down folder
    val connection = connectToDB()

    var ahkFolder = ""
    if (config.ahkFolder != null)
    {
        ahkFolder = config.ahkFolder!!.path
        GlobalData.ahkFolder = File(ahkFolder)
    }

    // Iterate over PoE Folders
    if (config.poeFolder != null)
    {
        if (config.poeFolder!!.size > 0)
        {
            for (f in config.poeFolder!!)
            {
                connection.createStatement().execute("INSERT into POE_Locations values(\"${f.path}\", false )")
                GlobalData.poeLocations.add(f.path)
            }
        }
    }

    // Set addon folder
    var addonFolder = "%addons%"
    if (config.addonfolder != null)
    {
        addonFolder = config.addonfolder!!.path
    }
    connection.createStatement().execute("INSERT into Addons_Folder values (\"$addonFolder\", true);")
    connection.createStatement().execute("INSERT into Flags values (${config.launchPOEonLaunch}, ${config.useSteamPoE}, ${config.githubAPIenabled}, ${config.showUpdateNotesOnUpdate});")
    connection.createStatement().execute("INSERT into Meta values (\'\', \'${ahkFolder}\', \'${GlobalData.version}\', \'${config.githubToken}\', \'${config.tempDownFolder}\');")
    connection.close()

    GlobalData.addonFolder = File(addonFolder)
    GlobalData.launchPOEonLaunch = config.launchPOEonLaunch
    GlobalData.gitHubAPIEnabled = config.githubAPIenabled
    GlobalData.showUpdateNotesOnUpdate = config.showUpdateNotesOnUpdate
    GlobalData.github_token = config.githubToken
}

fun createDB()
{
    logger.debug { "Creating db at: ${GlobalData.db_file.path}" }

    GlobalData.db_file.createNewFile()

    val connection = DriverManager.getConnection("jdbc:sqlite:${GlobalData.db_file.path}")
    val statement = connection.createStatement()

    createTables(connection)

    connection.close()
}

fun createTables(connection: Connection)
{
    connection.createStatement().execute("CREATE TABLE AHK_Scripts (\n" +
            "    location text NOT NULL,\n" +
            "    run_on_launch boolean NOT NULL\n" +
            ");")

    connection.createStatement().execute("CREATE TABLE Addons_Folder (\n" +
            "    location text NOT NULL,\n" +
            "    \"primary\" boolean NOT NULL\n" +
            ");")

    connection.createStatement().execute("CREATE TABLE CustomPrograms (\n" +
            "    name text NOT NULL,\n" +
            "    install_location text NOT NULL,\n" +
            "    launch_command text NOT NULL,\n" +
            "    version text NOT NULL,\n" +
            "    run_on_launch boolean NOT NULL\n" +
            ");")

    connection.createStatement().execute("CREATE TABLE Data (\n" +
            "    username text NOT NULL,\n" +
            "    pass text NOT NULL\n" +
            ");")

    connection.createStatement().execute("CREATE TABLE Flags (\n" +
            "    launch_poe_on_pal_launch boolean NOT NULL,\n" +
            "    Steam_POE boolean NOT NULL,\n" +
            "    GitHub_API boolean NOT NULL,\n" +
            "    show_motd boolean NOT NULL\n" +
            ");")

    connection.createStatement().execute("CREATE TABLE InstalledAddon (\n" +
            "    aid integer NOT NULL CONSTRAINT InstalledAddon_pk PRIMARY KEY,\n" +
            "    name text NOT NULL,\n" +
            "    install_location text NOT NULL,\n" +
            "    version text NOT NULL,\n" +
            "    last_update text NOT NULL,\n" +
            "    creators text NOT NULL,\n" +
            "    launch_command text NOT NULL,\n" +
            "    run_on_launch boolean NOT NULL\n" +
            ");")

    connection.createStatement().execute("CREATE TABLE Meta (\n" +
            "    loot_filter_folder text NOT NULL,\n" +
            "    ahk_folder text NOT NULL,\n" +
            "    pal_version text NOT NULL,\n" +
            "    github_API_token text NOT NULL,\n" +
            "    temp_down_folder text NOT NULL\n" +
            ");")

    connection.createStatement().execute("CREATE TABLE PoE_Locations (\n" +
            "    location text NOT NULL,\n" +
            "    \"primary\" boolean NOT NULL\n" +
            ");")

    connection.createStatement().execute("CREATE TABLE Repos (\n" +
            "    repo text NOT NULL\n" +
            ");")
}