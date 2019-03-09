package net.mrgaabriel.currencytracker.commands

import net.mrgaabriel.currencytracker.utils.CurrencyTrackerUtils
import net.mrgaabriel.currencytracker.utils.commands.Command
import net.mrgaabriel.currencytracker.utils.commands.CommandContext
import net.perfectdreams.commands.annotation.Subcommand

class CurrenciesCommand : Command("currencies") {

    @Subcommand
    suspend fun currencies(context: CommandContext) {
        context.reply("Valid currencies:\n${CurrencyTrackerUtils.getAvailableCurrencyCodes().joinToString(", ", transform = { "`$it`" })}")
    }
}