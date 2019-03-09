package net.mrgaabriel.currencytracker.commands

import net.mrgaabriel.currencyconverter.CurrencyConverter
import net.mrgaabriel.currencytracker.utils.CurrencyTrackerUtils
import net.mrgaabriel.currencytracker.utils.commands.Command
import net.mrgaabriel.currencytracker.utils.commands.CommandContext
import net.mrgaabriel.currencytracker.utils.currencyTracker
import net.perfectdreams.commands.annotation.Subcommand

class ConvertCommand : Command("convert") {

    @Subcommand
    suspend fun root(context: CommandContext) {
        context.reply("\nUse `${currencyTracker.config.commandPrefix}convert <from> <to> <quantity>`")
    }

    @Subcommand
    suspend fun convert(context: CommandContext, from: String, to: String, array: Array<String>) {
        if (from !in CurrencyTrackerUtils.getAvailableCurrencyCodes() || to !in CurrencyTrackerUtils.getAvailableCurrencyCodes()) {
            context.reply("That's not a valid currency! See `${currencyTracker.config.commandPrefix}currencies` to see all available currencies!")
            return
        }

        val quantity = array.firstOrNull()?.toDoubleOrNull() ?: 1.0
        val converted = CurrencyConverter.convert(quantity, from, to)

        context.reply("**`$quantity ${from.toUpperCase()}` **->** `${to.toUpperCase()}`:** `$converted ${to.toUpperCase()}`")
    }
}