package Data;

/**
 *
 */
public class PALsettings
{
    private boolean wait_for_updates = false;
    private String loot_filter_dir = "";
    private boolean github_api_enabled = true;
    private String pref_version = "";
    private boolean filterblast_api = true;
    private boolean down_on_launch = false;
    private String AHK_Folder = "";
    private String github_token = "";
    private boolean github_api_token_enabled = false;
    private boolean run_poe_on_launch = false;

    private PALsettings()
    {}

    public PALsettings(boolean wait_for_updates, String loot_filter_dir, boolean github_api_enabled, String pref_version, boolean filterblast_api, boolean down_on_launch, String AHK_Folder, String github_token, boolean github_api_token_enabled, boolean run_poe_on_launch)
    {
        this.wait_for_updates = wait_for_updates;
        this.loot_filter_dir = loot_filter_dir;
        this.github_api_enabled = github_api_enabled;
        this.pref_version = pref_version;
        this.filterblast_api = filterblast_api;
        this.down_on_launch = down_on_launch;
        this.AHK_Folder = AHK_Folder;
        this.github_token = github_token;
        this.github_api_token_enabled = github_api_token_enabled;
        this.run_poe_on_launch = run_poe_on_launch;
    }

    public boolean isWait_for_updates()
    {
        return wait_for_updates;
    }

    public void setWait_for_updates(boolean wait_for_updates)
    {
        this.wait_for_updates = wait_for_updates;
    }

    public String getLoot_filter_dir()
    {
        return loot_filter_dir;
    }

    public void setLoot_filter_dir(String loot_filter_dir)
    {
        this.loot_filter_dir = loot_filter_dir;
    }

    public boolean isGithub_api_enabled()
    {
        return github_api_enabled;
    }

    public void setGithub_api_enabled(boolean github_api_enabled)
    {
        this.github_api_enabled = github_api_enabled;
    }

    public String getPref_version()
    {
        return pref_version;
    }

    public void setPref_version(String pref_version)
    {
        this.pref_version = pref_version;
    }

    public boolean isFilterblast_api()
    {
        return filterblast_api;
    }

    public void setFilterblast_api(boolean filterblast_api)
    {
        this.filterblast_api = filterblast_api;
    }

    public boolean isDown_on_launch()
    {
        return down_on_launch;
    }

    public void setDown_on_launch(boolean down_on_launch)
    {
        this.down_on_launch = down_on_launch;
    }

    public String getAHK_Folder()
    {
        return AHK_Folder;
    }

    public void setAHK_Folder(String AHK_Folder)
    {
        this.AHK_Folder = AHK_Folder;
    }

    public String getGithub_token()
    {
        return github_token;
    }

    public void setGithub_token(String github_token)
    {
        this.github_token = github_token;
    }

    public boolean isGithub_api_token_enabled()
    {
        return github_api_token_enabled;
    }

    public void setGithub_api_token_enabled(boolean github_api_token_enabled)
    {
        this.github_api_token_enabled = github_api_token_enabled;
    }

    public boolean isRun_poe_on_launch()
    {
        return run_poe_on_launch;
    }

    public void setRun_poe_on_launch(boolean run_poe_on_launch)
    {
        this.run_poe_on_launch = run_poe_on_launch;
    }
}
