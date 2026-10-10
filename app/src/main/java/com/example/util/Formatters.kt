package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val indonesianLocale = Locale("id", "ID")

    fun formatRupiah(amount: Double): String {
        return try {
            val format = NumberFormat.getNumberInstance(indonesianLocale)
            "Rp " + format.format(amount.toLong())
        } catch (e: Exception) {
            "Rp ${amount.toLong()}"
        }
    }

    fun formatDate(timestamp: Long?): String {
        if (timestamp == null || timestamp == 0L) return "-"
        val sdf = SimpleDateFormat("dd MMM yyyy", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long?): String {
        if (timestamp == null || timestamp == 0L) return "-"
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun calculateAge(birthTimestamp: Long?): String {
        if (birthTimestamp == null || birthTimestamp == 0L) return "Umur tidak diketahui"
        val now = System.currentTimeMillis()
        val diffMillis = now - birthTimestamp
        if (diffMillis < 0) return "Baru lahir"
        val days = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        val months = days / 30
        val years = months / 12
        return when {
            years > 0 -> {
                val remainingMonths = months % 12
                if (remainingMonths > 0) "$years thn $remainingMonths bln" else "$years tahun"
            }
            months > 0 -> {
                val remainingDays = days % 30
                if (remainingDays > 0) "$months bln $remainingDays hari" else "$months bulan"
            }
            else -> "$days hari"
        }
    }
}
