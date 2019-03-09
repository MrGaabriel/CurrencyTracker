package net.mrgaabriel.currencytracker.commands

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.mrgaabriel.currencytracker.utils.CurrencyTrackerUtils
import net.mrgaabriel.currencytracker.utils.commands.Command
import net.mrgaabriel.currencytracker.utils.commands.CommandContext
import net.mrgaabriel.currencytracker.utils.currencyTracker
import net.mrgaabriel.currencytracker.utils.database
import net.perfectdreams.commands.annotation.Subcommand
import org.jetbrains.exposed.sql.transactions.transaction

class TrackCommand : Command("track") {

    override val discordPermissions: List<Permission>
        get() = listOf(Permission.MANAGE_SERVER)

    @Subcommand
    suspend fun root(context: CommandContext) {
        context.reply("\nUse `${currencyTracker.config.commandPrefix}track <from> <to> <channel>`!\nExample: `${currencyTracker.config.commandPrefix}track USD BRL #general`")
    }

    @Subcommand
    suspend fun track(context: CommandContext, from: String, to: String, channel: TextChannel) {
        if (from !in CurrencyTrackerUtils.getAvailableCurrencyCodes() || to !in CurrencyTrackerUtils.getAvailableCurrencyCodes()) {
            context.reply("That's not a valid currency! See `${currencyTracker.config.commandPrefix}currencies` to see all available currencies!")
            return
        }

        if (!channel.canTalk()) {
            context.reply("I can't send messages in ${channel.asMention}! Please fix that! Thank you~")
            return
        }

        val tracker = currencyTracker.getTracker(from.toUpperCase(), to.toUpperCase())
        val list = tracker.channelIds.toMutableList()

        if (channel.id in list) {
            context.reply("I'm already sending `${from.toUpperCase()}` to `${to.toUpperCase()}` updates in channel ${channel.asMention}! Use `${currencyTracker.config.commandPrefix}stoptracking #${channel.name}` to stop sending notifications!")
            return
        }

        list.add(channel.id)

        transaction(database) {
            tracker.channelIds = list.toTypedArray()
        }

        context.reply("Now I'll send `${from.toUpperCase()}` to `${to.toUpperCase()}` updates in channel ${channel.asMention}! Use `${currencyTracker.config.commandPrefix}stoptracking #${channel.name}` to stop sending notifications!")
    }
}