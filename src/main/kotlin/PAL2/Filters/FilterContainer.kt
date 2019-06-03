package PAL2.Filters

import PAL2.Addons.Externals
import PAL2.Database.addFilter
import PAL_DataClasses.Filter
import PAL_DataClasses.PAL_External_Addon
import java.io.File

/**
 *
 */
object FilterContainer
{
    val filters = ArrayList<FilterBlastFilter>()

    fun makeAnchorData(f: FilterBlastFilter, location: File, web: String, variation: String): Filter
    {
        val crc32 = Externals.calcCRC32(location)
        val db_filter = addFilter(Filter(-1, f.name, crc32, web, location.path, variation))

        return db_filter
    }

}