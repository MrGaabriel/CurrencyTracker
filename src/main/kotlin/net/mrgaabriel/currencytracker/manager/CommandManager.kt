package net.mrgaabriel.currencytracker.manager

import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.utils.MiscUtil
import net.mrgaabriel.currencytracker.commands.*
import net.mrgaabriel.currencytracker.utils.await
import net.mrgaabriel.currencytracker.utils.commands.Command
import net.mrgaabriel.currencytracker.utils.commands.CommandContext
import net.mrgaabriel.currencytracker.utils.currencyTracker
import net.mrgaabriel.currencytracker.utils.fancy
import net.mrgaabriel.currencytracker.utils.t
import net.perfectdreams.commands.dsl.BaseDSLCommand
import net.perfectdreams.commands.manager.CommandManager
import java.util.regex.Pattern
import kotlin.reflect.full.isSubclassOf

class CommandManager : CommandManager<CommandContext, Command, BaseDSLCommand>() {

    val logger = KotlinLogging.logger {}
    val commands = mutableListOf<Command>()

    fun registerCommands() {
        registerCommand(PingCommand())
        registerCommand(CurrenciesCommand())
        registerCommand(TrackCommand())
        registerCommand(StopTrackingCommand())
        registerCommand(EvalCommand())
        registerCommand(ReloadCommand())
        registerCommand(ConvertCommand())
    }

    override fun registerCommand(command: Command) {
        commands.add(command)
    }

    override fun unregisterCommand(command: Command) {
        commands.add(command)
    }

    override fun getRegisteredCommands(): List<Command> {
        return commands
    }

    init {
        registerCommands()

        contextManager.registerContext<TextChannel>(
            { clazz -> clazz == TextChannel::class || clazz.isSubclassOf(TextChannel::class) },
            { context, clazz, stack ->
                val pop = stack.pop()

                val channels = context.guild.getTextChannelsByName(pop, false)
                if (channels.isNotEmpty()) {
                    return@registerContext channels.first()
                }

                val id = pop.replace(Regex("[<#>]"), "")

                try { MiscUtil.parseSnowflake(id) } catch (e: Exception) { return@registerContext null }

                return@registerContext context.guild.getTextChannelById(id)
            }
        )
    }

    suspend fun dispatch(message: Message): Boolean {
        val rawMessage = message.contentRaw
        val rawArgs = rawMessage.split(" ").toMutableList()

        for (command in getRegisteredCommands()) {
            if (verifyAndDispatch(command, rawArgs, message))
                return true
        }

        return false
    }

    suspend fun verifyAndDispatch(command: Command, rawArgs: MutableList<String>, message: Message): Boolean {
        for (subCommand in command.subcommands) {
            if (dispatch(subCommand as Command, rawArgs.drop(1).toMutableList(), message,true))
                return true
        }

        if (dispatch(command, rawArgs, message))
            return true

        return false
    }

    suspend fun dispatch(command: Command, rawArgs: MutableList<String>, message: Message, isSubcommand: Boolean = false): Boolean {
        val labels = command.labels

        val matcher = Pattern.compile("^(<@!?${currencyTracker.config.clientId}>\\s+(?:${currencyTracker.config.commandPrefix}\\s*)?|${currencyTracker.config.commandPrefix}\\s*)([^\\s]+)")
            .matcher(message.contentRaw)

        if (!matcher.find())
            return false

        val label = matcher.group(0)
        val valid = labels.any { (currencyTracker.config.commandPrefix + it).equals(label.replace(" ", ""), true) }

        if (!valid)
            return false

        message.channel.sendTyping().await()

        val args = message.contentRaw.substring(label.length).split(" ").toMutableList()
        args.removeAt(0)

        val context = CommandContext(message, command, args)

        val start = System.currentTimeMillis()

        if (command.onlyOwner && context.author.id != currencyTracker.config.ownerId) {
            context.reply("No permission!")
            return true
        }

        if (!(command.canHandle.invoke(context))) {
            context.reply("No permission!")
            return true
        }

        val missingMemberPermissions = command.discordPermissions.filter { !message.member.hasPermission(message.textChannel, it) }

        if (missingMemberPermissions.isNotEmpty()) {
            context.reply("No permission!")
            return true
        }

        val allBotPermissions = mutableListOf(
            Permission.MESSAGE_WRITE,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_EMBED_LINKS,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_ADD_REACTION,
            Permission.MESSAGE_HISTORY
        )

        allBotPermissions.addAll(command.botPermissions)

        val missingBotPermissions = allBotPermissions.filter { !message.guild.selfMember.hasPermission(message.textChannel, it) }

        if (missingBotPermissions.isNotEmpty()) {
            if (Permission.MESSAGE_WRITE in missingBotPermissions) {
                try {
                    val channel = message.author.openPrivateChannel().await()

                    channel.sendMessage("${message.author.asMention} I have no permission to send messages in ${message.textChannel.asMention}! Ask to server staff to give me permissions!").queue()
                } catch (e: ErrorResponseException) { }

                return true
            }

            context.reply("I want to execute this command, but I have no permission! (Required Permissions: `${missingBotPermissions.joinToString(", ", transform = { it.name.split(" ").joinToString(" ", transform = { it.fancy() }) })}`)")
            return true
        }

        logger.info { "${t.yellow}[COMMAND EXECUTED]${t.reset} (${context.guild.name} -> #${context.channel.name}) ${context.author.asTag}: ${context.message.contentRaw}" }
        logger.debug { "${t.blue}[COMMAND INFO]${t.reset} command.labels: ${command.labels}, args: $args, isSubCommand: $isSubcommand" }

        try {
            execute(context, command, args.toTypedArray())

            logger.info { "${t.green}[COMMAND EXECUTED]${t.reset} (${context.guild.name} -> #${context.channel.name}) ${context.author.asTag}: ${context.message.contentRaw} - OK! ${System.currentTimeMillis() - start}ms" }
        } catch (e: Exception) {
            context.reply("Something terribly wrong happened while trying to execute this command. Sorry <:sadcat:526799733063155722>")

            logger.error(e) { "${t.red}[COMMAND STATUS]${t.reset} (${context.guild.name} -> #${context.channel.name}) ${context.author.asTag}: ${context.message.contentRaw} - ERROR" }
        }

        return true
    }
}