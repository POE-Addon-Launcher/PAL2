package PAL_DataClasses

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import javafx.beans.property.SimpleStringProperty
import java.io.File
import java.lang.StringBuilder
import java.time.LocalDate
import java.util.*

/**
 * PAL Data Classes; mimic database entries.
 */
data class PAL_External_Addon
(
        var eid: Int,
        var name: String,
        var checksum: String,
        var newCheckSum: String,
        var iconUrl: String?,
        var lastCheck: String,
        var webSource: String,
        var launchCMD: String
)

data class PAL_Addon
(
        var id: Int,
        var name: String,
        var gh_username: String?,
        var gh_reponame: String?,
        var icon: String?,
        var description: String?
)

data class PAL_DownloadInfo
(
        var id: Int,
        var download_url: String
)

data class PAL_AddonVersion
(
        var id: Int,
        var version_text: String,
        var addon: PAL_Addon,
        var downloadInfo: PAL_DownloadInfo,
        var launch_command: String,
        var last_update: LocalDate,
        var html_description: String?
)

data class PAL_AddonFullData
(
        var name: String,
        var aid: Int,
        var gh_username: String?,
        var gh_reponame: String?,
        var icon_url: String?,
        var description: String?,
        var download_urls: Array<String>,
        var avid: Int,
        var version_text: String,
        var last_update: String,
        var html_description: String?,
        var extra_flags: Array<String>
)
{


        override fun hashCode(): Int
        {
                var result = name.hashCode()
                result = 31 * result + (gh_username?.hashCode() ?: 0)
                result = 31 * result + (gh_reponame?.hashCode() ?: 0)
                result = 31 * result + (icon_url?.hashCode() ?: 0)
                result = 31 * result + (description?.hashCode() ?: 0)
                result = 31 * result + download_urls.contentHashCode()
                result = 31 * result + version_text.hashCode()
                result = 31 * result + last_update.hashCode()
                result = 31 * result + (html_description?.hashCode() ?: 0)
                result = 31 * result + extra_flags.contentHashCode()
                return result
        }

        override fun equals(other: Any?): Boolean
        {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as PAL_AddonFullData

                if (name != other.name) return false
                if (aid != other.aid) return false
                if (gh_username != other.gh_username) return false
                if (gh_reponame != other.gh_reponame) return false
                if (icon_url != other.icon_url) return false
                if (description != other.description) return false
                if (!download_urls.contentEquals(other.download_urls)) return false
                if (avid != other.avid) return false
                if (version_text != other.version_text) return false
                if (last_update != other.last_update) return false
                if (html_description != other.html_description) return false
                if (!extra_flags.contentEquals(other.extra_flags)) return false

                return true
        }

        fun toAddonTableRow(): PAL_AddonTableRow
        {
                return PAL_AddonTableRow(SimpleStringProperty(name), SimpleStringProperty(version_text), SimpleStringProperty(last_update), SimpleStringProperty(gh_reponame), SimpleStringProperty(gh_reponame), SimpleStringProperty(description))
        }
}

data class PAL_Creator
(
        var id: Int,
        var name: String
)

data class PAL_Ownership
(
        var creator: PAL_Creator,
        var addon: PAL_Addon
)

data class PAL_FilterVariation
(
        var id: Int,
        var key: String,
        var name: String,
        var filter: PAL_Filter
)

data class PAL_Filter
(
        var name: String,
        var version: String,
        var PoE_version: String,
        var forumthread: String,
        var description: String,
        var html_description: String,
        var variations: Array<String>,
        var key: String
)
{
        override fun toString(): String
        {
                return "PAL_Filter(name='$name', version='$version', PoE_version='$PoE_version', forumthread='$forumthread', description='$description', html_description='$html_description', variations=${Arrays.toString(variations)}, key='$key')"
        }
}

data class PAL_GH_Info
(
        var id: Int,
        var username: String,
        var reponame: String,
        var addon: PAL_Addon
)

data class PAL_InstalledAddon
(
        var id: Int,
        var install_location: String,
        var local_version: String,
        var install_date: LocalDate,
        var executable_location: String,
        var addonVersion: PAL_AddonVersion
)

data class PAL_InstalledFilter
(
        var id: Int,
        var filter: PAL_Filter,
        var install_date: LocalDate,
        var local_version: String
)

data class PAL_User(val username: String, val poe_account_name: String, val displayName: String, val email:String, val registeredAt: String,
                    var account_desc: String)

data class PAL_AddonMetaData
(
        var aid: Int,
        var likes: Int,
        var dislikes: Int,
        var downloads: Int,
        var description: String,
        var html_description: String,
        var icon_url: String,
        var ggg_aproved: Boolean,
        var creators: String
)


fun initObjectMapper(): ObjectMapper
{
        return ObjectMapper()
                .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
                .registerModule(KotlinModule())
}

fun initObjectMapperWithoutStreamClose(): ObjectMapper
{
        val mpf = JsonFactory()
        mpf.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)
        return ObjectMapper(mpf)
                .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
                .registerModule(KotlinModule())
}

/**
 * Addon Data
 */
data class Releases
(
        var name: String,
        var release_list: Array<*>,
        var release_date: LocalDate,
        var tag: String
)
{
        override fun toString(): String
        {
                val str = StringBuilder()
                str.append("$name | $tag | $release_date\n")

                for (r in release_list)
                {
                        if (r != null)
                        {
                                str.append("\t> $r\n")
                        }
                        else
                        {
                                str.append("\t> NULL\n")
                        }
                }

                return str.toString()
        }
}

data class Release
(
        var name: String,
        var dl_link: String,
        var content_type: String,
        var download_size: Long
)
{
        override fun toString(): String
        {
                // Magic Number to convert download size to MB: 1048576
                return "$name $content_type ${converToMB(download_size)}"
        }

        fun converToMB(bits: Long): String
        {
                return "${String.format("%.1f", bits/1048576.0)} MB"
        }
}

fun createAFD(a: PAL_Addon, di: Array<String>, av: PAL_AddonVersion, ef: Array<String>): PAL_AddonFullData
{
        return PAL_AddonFullData(
                a.name,
                a.id,
                a.gh_username,
                a.gh_reponame,
                a.icon,
                a.description,
                di,
                av.id,
                av.version_text,
                createDate(av.last_update),
                av.html_description,
                ef
                                )
}

fun createDate(arg:LocalDate):String
{
        return "${arg.year}-${arg.month}-${arg.dayOfMonth}"
}

/**
 * DEPRECATED DATA CLASSES
 */

@Deprecated("Used for compatibility with old PAL")
data class PAL_AddonJson
(
        var name :String,
        var version :String,
        var creators :String,
        var gh_username :String,
        var gh_reponame :String,
        var download_url :String,
        var icon_url :String,
        var description :String,
        var file_launch :String,
        var programming_language :String
)

/**
 * FXML
 */
data class PAL_AddonTableRow
(
        var name: SimpleStringProperty,
        var version: SimpleStringProperty,
        var last_update: SimpleStringProperty,
        var gh_reponame: SimpleStringProperty,
        var gh_username: SimpleStringProperty,
        var description: SimpleStringProperty
)