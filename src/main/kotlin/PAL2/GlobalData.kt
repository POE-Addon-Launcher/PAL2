import PAL_DataClasses.PAL_AddonFullData
import java.io.File

/**
 *
 */
class GlobalData
{
    companion object
    {
        var version = "1.0.9"
        var debugging = false
        var install_dir = ""
        var github_token = ""
        var list_of_addons = ArrayList<PAL_AddonFullData>()
        var set_of_addons = HashSet<PAL_AddonFullData>()
        var list_of_repos = ArrayList<String>()
        var pal_folder = File("${System.getenv("LOCALAPPDATA")}${File.separator}PAL")
        var pal_data_folder = File("${pal_folder.path}${File.separator}Data")
        var db_file = File("${pal_data_folder}${File.separator}PALData.db")
        var server_enabled = false
        var first_launch = true
        var first_launch_after_update = true
        var primaryPoEFile: File? = null
        var poeLocations = ArrayList<String>()
        var logFile = ""

        var loot_filter_path = ""
        var show_update_note = false
        var launch_addons = true

        /**
         * DB Data
         */
        //var poePaths = HashSet<File>()
        var addonFolder = File("${pal_folder}${File.separator}Addons")
        var ahkFolder = File("")
        var launchPOEonLaunch = false
        var gitHubAPIEnabled = true
        var showUpdateNotesOnUpdate = true
        var ahk_scripts = HashSet<String>()

        var temp_down_folder = File("${pal_folder.path}${File.separator}temp_downloads")
        var launchList = ArrayList<File>()
        var steam_poe = false

        fun addToListOfAddons(arr: Array<PAL_AddonFullData>)
        {
            for (a in arr)
            {
                if (!list_of_addons.contains(a))
                {
                    list_of_addons.add(a)
                }
            }
        }

        fun getAddonByID(aid: Int): PAL_AddonFullData?
        {
            for (a in set_of_addons)
            {
                if (a.aid == aid)
                {
                    return a
                }
            }
            return null
        }

        fun checkAddonListForDuplicates()
        {
            set_of_addons.addAll(list_of_addons)
        }

        fun addToRepoList(repo: String)
        {
            if (!list_of_repos.contains(repo))
            {
                list_of_repos.add(repo)
            }
        }
    }
}