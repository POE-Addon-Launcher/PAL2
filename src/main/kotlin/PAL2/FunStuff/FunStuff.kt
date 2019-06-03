package PAL2.FunStuff

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 *
 */
object FunStuff
{
    val europe_release = LocalDateTime.of(2019, 6, 7, 22, 0)
    val zdt = ZonedDateTime.of(europe_release, ZoneId.of("UTC+2"))
    val local_release = zdt.toLocalDateTime()

    fun legionCountDownTimer()
    {
        // Add Legion Countdowner if it's not yet at the date of the release!
        val now = LocalDateTime.now()

        println("days: ${now.until(local_release, ChronoUnit.DAYS)}")

    }
}