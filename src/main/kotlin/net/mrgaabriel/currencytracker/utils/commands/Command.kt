package net.mrgaabriel.currencytracker.utils.commands

import net.dv8tion.jda.api.Permission
import net.perfectdreams.commands.Command

open class Command(vararg labels: String) : Command(*labels) {

    open val description = "Insira descrição do comando aqui"
    open val usage = ""

    open val onlyOwner = false

    open val discordPermissions = listOf<Permission>()
    open val botPermissions = listOf<Permission>()

    open val cooldown = 2500.toLong()

    open val canHandle: ((CommandContext) -> Boolean) = { true }
}