package net.mrgaabriel.currencytracker.commands

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.mrgaabriel.currencytracker.dao.Tracker
import net.mrgaabriel.currencytracker.utils.commands.Command
import net.mrgaabriel.currencytracker.utils.commands.CommandContext
import net.mrgaabriel.currencytracker.utils.currencyTracker
import net.mrgaabriel.currencytracker.utils.database
import net.perfectdreams.commands.annotation.Subcommand
import org.jetbrains.exposed.sql.transactions.transaction

class StopTrackingCommand : Command("stoptracking", "trackstop") {

    override val discordPermissions: List<Permission>
        get() = listOf(Permission.MANAGE_SERVER)

    @Subcommand
    suspend fun root(context: CommandContext) {
        context.reply("\nUse `${currencyTracker.config.commandPrefix}stoptracking <channel>`")
    }

    @Subcommand
    suspend fun stoptracking(context: CommandContext, channel: TextChannel) {
        val trackers = transaction(database) {
            Tracker.all().toMutableList()
        }

        for (tracker in trackers) {
            val list = tracker.channelIds.toMutableList()
            list.remove(channel.id)

            transaction(database) {
                tracker.channelIds = list.toTypedArray()
            }
        }

        context.reply("OK! I'll not send anymore notifications in ${channel.asMention}")
    }
}