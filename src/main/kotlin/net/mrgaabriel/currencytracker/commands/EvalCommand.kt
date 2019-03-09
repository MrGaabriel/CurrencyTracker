package net.mrgaabriel.currencytracker.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.mrgaabriel.currencytracker.utils.commands.Command
import net.mrgaabriel.currencytracker.utils.commands.CommandContext
import net.perfectdreams.commands.annotation.Subcommand
import java.awt.Color
import java.io.PrintWriter
import java.io.StringWriter
import java.time.OffsetDateTime
import javax.script.ScriptContext
import javax.script.ScriptEngineManager

class EvalCommand : Command("eval") {

    override val onlyOwner: Boolean
        get() = true

    @Subcommand
    suspend fun eval(context: CommandContext, array: Array<String>) {
        val code = array.joinToString(" ")
        val scriptEngine = ScriptEngineManager().getEngineByName("kotlin")

        scriptEngine.put("scriptContext", scriptEngine.context)
        scriptEngine.put("context", context)

        val bindings = buildString {
            scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).forEach { key, value ->
                if ("." !in key) {
                    val name: String = value::class.qualifiedName!!
                    val bind = """val $key = bindings["$key"] as $name"""
                    appendln(bind)
                }
            }
        }

        val script = """
import net.mrgaabriel.currencytracker.*
import net.mrgaabriel.currencytracker.commands.*
import net.mrgaabriel.currencytracker.config.*
import net.mrgaabriel.currencytracker.dao.*
import net.mrgaabriel.currencytracker.listeners.*
import net.mrgaabriel.currencytracker.manager.*
import net.mrgaabriel.currencytracker.tables.*
import net.mrgaabriel.currencytracker.tasks.*
import net.mrgaabriel.currencytracker.utils.*
import net.mrgaabriel.currencytracker.utils.commands.*
import net.mrgaabriel.currencytracker.utils.exposed.*

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import java.io.PrintWriter
import java.io.StringWriter
import java.time.OffsetDateTime
import java.awt.Color
import net.dv8tion.jda.api.*
import net.dv8tion.jda.api.entities.*
import org.jetbrains.exposed.sql.transactions.transaction
import com.github.kevinsawicki.http.HttpRequest
$bindings
val deferred = GlobalScope.async {
    try {
        $code
    } catch (e: Exception) {
        exception(e)
    }
}
GlobalScope.launch {
    try {
        val await = deferred.await()
        context.sendMessage("```xl\n" + await + "```")
    } catch (e: Exception) {
        exception(e)
    }
}
fun exception(e: Exception) {
    GlobalScope.launch {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        e.printStackTrace(printWriter)
        val builder = EmbedBuilder()
        builder.setAuthor("Whoops! \uD83D\uDE2D")
        builder.setDescription("```" + stringWriter.toString().trim() + "```")
        builder.setTimestamp(OffsetDateTime.now())
        builder.setColor(Color.RED)
        context.reply(builder.build())
    }
}
        """.trimIndent()

        try {
            scriptEngine.eval(script)
        } catch (e: Exception) {
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            e.printStackTrace(printWriter)

            val builder = EmbedBuilder()

            builder.setAuthor("Whoops! \uD83D\uDE2D")
            builder.setDescription("```${stringWriter.toString().trim()}```")

            builder.setTimestamp(OffsetDateTime.now())
            builder.setFooter("#trost", null)

            builder.setColor(Color.RED)
            context.reply(builder.build())
        }
    }
}