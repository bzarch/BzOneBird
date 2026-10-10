package com.example

import com.example.util.Formatters
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testRupiahFormatting() {
    val formatted = Formatters.formatRupiah(1500000.0)
    assertTrue(formatted.contains("1.500.000") || formatted.contains("1,500,000") || formatted.startsWith("Rp"))
  }

  @Test
  fun testAgeCalculation() {
    val now = System.currentTimeMillis()
    val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
    val age = Formatters.calculateAge(thirtyDaysAgo)
    assertTrue(age.contains("bulan") || age.contains("hari"))
  }
}
