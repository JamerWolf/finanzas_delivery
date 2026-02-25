package com.control_delivery.finanzas_delivery.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

object DateUtils {
    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val zoneId = ZoneId.systemDefault()

    /**
     * Retorna un par de milisegundos (inicio, fin) basado en una cadena de texto.
     * Soporta: "TODAY", "THIS_WEEK", "dd-MM-yyyy" y "dd-MM-yyyy/dd-MM-yyyy"
     */
    fun getTimestampRange(input: String): Pair<Long, Long> {
        return when {
            input.equals("TODAY", true) -> getTodayRange()
            input.equals("THIS_WEEK", true) -> getThisWeekRange()
            input.contains("/") -> parseDateRange(input)
            else -> parseSingleDate(input)
        }
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val now = LocalDate.now()
        val start = now.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = now.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
        return Pair(start, end)
    }

    private fun getThisWeekRange(): Pair<Long, Long> {
        val today = LocalDate.now()
        val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
        return Pair(start, end)
    }

    private fun parseSingleDate(dateStr: String): Pair<Long, Long> {
        return try {
            val date = LocalDate.parse(dateStr, formatter)
            val start = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val end = date.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
            Pair(start, end)
        } catch (e: Exception) {
            getTodayRange() // Default safe fallback
        }
    }

    private fun parseDateRange(rangeStr: String): Pair<Long, Long> {
        return try {
            val dates = rangeStr.split("/")
            val startDate = LocalDate.parse(dates[0], formatter)
            val endDate = LocalDate.parse(dates[1], formatter)
            
            val start = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val end = endDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
            Pair(start, end)
        } catch (e: Exception) {
            getTodayRange() // Default safe fallback
        }
    }
}
