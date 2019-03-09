package net.mrgaabriel.currencytracker.listeners

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.mrgaabriel.currencytracker.utils.currencyTracker
import java.util.regex.Pattern

class MessageListeners : ListenerAdapter() {

    val logger = KotlinLogging.logger {}

    override fun onMessageReceived(e: MessageReceivedEvent) {
        if (e.author.isBot)
            return

        GlobalScope.launch(currencyTracker.defaultDispatcher) {
            try {
                val matcher = Pattern.compile("^<@[!]?${currencyTracker.config.clientId}>\$")
                    .matcher(e.message.contentRaw)

                if (matcher.matches()) {
                    e.channel.sendMessage("${e.author.asMention} Hello! I'm the Currency Converter bot for Discord and I can track any currency values in real time! `${currencyTracker.config.commandPrefix}track`").queue()
                }

                if (currencyTracker.commandManager.dispatch(e.message))
                    return@launch
            } catch (ex: Exception) {
                logger.error(ex) { "Erro ao processar mensagem: (${e.guild.name} -> #${e.channel.name}) ${e.author.asTag}: ${e.message.contentRaw}" }
            }
        }
    }

    override fun onMessageUpdate(e: MessageUpdateEvent) {
        if (e.author.isBot)
            return

        GlobalScope.launch(currencyTracker.defaultDispatcher) {
            try {
                val matcher = Pattern.compile("^<@[!]?${currencyTracker.config.clientId}>\$")
                    .matcher(e.message.contentRaw)

                if (matcher.matches()) {

                    e.channel.sendMessage("${e.author.asMention} Hello! I'm the Currency Converter bot for Discord and I can track any currency values in real time! `${currencyTracker.config.commandPrefix}track`").queue()
                }

                if (currencyTracker.commandManager.dispatch(e.message))
                    return@launch
            } catch (ex: Exception) {
                logger.error(ex) { "Erro ao processar mensagem: (${e.guild.name} -> #${e.channel.name}) ${e.author.asTag}: ${e.message.contentRaw}" }
            }
        }
    }
}