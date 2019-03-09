package net.mrgaabriel.currencytracker.utils

import net.mrgaabriel.currencyconverter.CurrencyConverterUtils

object CurrencyTrackerUtils {

    fun getAvailableCurrencyCodes(): List<String> {
        return CurrencyConverterUtils.getCurrencyCodes()
    }
}