package net.mrgaabriel.currencytracker.commands

import net.mrgaabriel.currencytracker.utils.await
import net.mrgaabriel.currencytracker.utils.commands.Command
import net.mrgaabriel.currencytracker.utils.commands.CommandContext
import net.perfectdreams.commands.annotation.Subcommand

class PingCommand : Command("ping") {

    @Subcommand
    suspend fun root(context: CommandContext) {
        context.reply("**Pong!** :ping_pong:\n**Gateway Ping:** `${context.jda.gatewayPing}ms`\n**API Ping:** `${context.jda.restPing.await()}ms`")
    }
}