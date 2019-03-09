package net.mrgaabriel.currencytracker.commands

import net.mrgaabriel.currencytracker.config.CurrencyTrackerConfig
import net.mrgaabriel.currencytracker.utils.Static
import net.mrgaabriel.currencytracker.utils.commands.Command
import net.mrgaabriel.currencytracker.utils.commands.CommandContext
import net.mrgaabriel.currencytracker.utils.currencyTracker
import net.perfectdreams.commands.annotation.Subcommand
import java.io.File

class ReloadCommand : Command("reload") {

    override val onlyOwner: Boolean
        get() = true

    @Subcommand(["config"])
    suspend fun config(context: CommandContext) {
        val file = File("config.yml")
        currencyTracker.config = Static.YAML_MAPPER.readValue(file, CurrencyTrackerConfig::class.java)

        context.reply("Config reloaded!")
    }

    @Subcommand(["commands"])
    suspend fun commands(context: CommandContext) {
        val oldCommands = currencyTracker.commandManager.commands

        currencyTracker.commandManager.unregisterAllCommands()
        currencyTracker.commandManager.registerCommands()

        context.reply("${currencyTracker.commandManager.commands.size} comandos recarregados! ${currencyTracker.commandManager.commands.size - oldCommands.size} novos")
    }
}