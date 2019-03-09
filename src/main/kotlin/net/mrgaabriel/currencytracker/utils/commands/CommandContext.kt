package net.mrgaabriel.currencytracker.utils.commands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.*
import net.mrgaabriel.currencytracker.utils.await

class CommandContext(val message: Message, val command: Command, val args: MutableList<String>) {

    val author: User
        get() = message.author

    val guild: Guild
        get() = message.guild

    val channel: MessageChannel
        get() = message.channel

    val textChannel: TextChannel
        get() = message.textChannel

    val jda: JDA
        get() = message.jda

    suspend fun sendMessage(embed: MessageEmbed): Message {
        return channel.sendMessage(embed).await()
    }

    suspend fun sendMessage(embed: MessageEmbed, content: Any): Message {
        return channel.sendMessage(MessageBuilder().apply {
            setContent(content.toString())
            setEmbed(embed)
        }.build()).await()
    }

    suspend fun sendMessage(content: Any): Message {
        return channel.sendMessage(content.toString()).await()
    }

    suspend fun reply(embed: MessageEmbed): Message {
        return channel.sendMessage(MessageBuilder().apply {
            setContent(author.asMention)
            setEmbed(embed)
        }.build()).await()
    }

    suspend fun reply(embed: MessageEmbed, content: Any): Message {
        return channel.sendMessage(MessageBuilder().apply {
            setContent("${author.asMention} $content")
            setEmbed(embed)
        }.build()).await()
    }

    suspend fun reply(content: Any): Message {
        return channel.sendMessage("${author.asMention} $content").await()
    }
}