package com.example.cryptoexchange.presentation.util

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PriceFormatter {

    private val usSymbols = DecimalFormatSymbols(Locale.US)

    fun formatUsdPrice(price: Double?): String {
        if (price == null || price.isNaN() || price.isInfinite()) {
            return "Немає даних"
        }
        if (price == 0.0) {
            return "$0.00"
        }

        return when {
            price >= 1000.0 -> {
                val df = DecimalFormat("#,##0.00", usSymbols)
                "$${df.format(price)}"
            }
            price >= 1.0 -> {
                val df = DecimalFormat("0.00##", usSymbols)
                "$${df.format(price)}"
            }
            price >= 0.01 -> {
                val df = DecimalFormat("0.0000", usSymbols)
                "$${df.format(price)}"
            }
            else -> {
                // Зберігаємо 4 значущі цифри для мікрокапів (наприклад, $0.00002505)
                val significant = BigDecimal.valueOf(price)
                    .round(MathContext(4, RoundingMode.HALF_UP))
                    .stripTrailingZeros()
                    .toPlainString()
                "$$significant"
            }
        }
    }

    fun formatTimestamp(millis: Long?): String {
        if (millis == null || millis <= 0L) return "Ще не оновлювалось"
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(millis))
    }
}