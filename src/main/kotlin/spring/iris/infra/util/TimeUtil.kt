package spring.iris.infra.util


import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

object TimeUtil {

    private val SEOUL_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")

    fun convertUnixToZonedDateTime(
        unixTimestampSeconds: Long, zoneId: ZoneId = SEOUL_ZONE_ID
    ): ZonedDateTime {
        val instant = Instant.ofEpochSecond(unixTimestampSeconds)
        return ZonedDateTime.ofInstant(instant, zoneId)
    }
}