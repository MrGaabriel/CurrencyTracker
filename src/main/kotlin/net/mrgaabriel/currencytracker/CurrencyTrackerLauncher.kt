package net.mrgaabriel.currencytracker

import mu.KotlinLogging
import net.mrgaabriel.currencytracker.config.CurrencyTrackerConfig
import net.mrgaabriel.currencytracker.utils.Static
import java.io.File
import java.nio.file.Paths
import java.util.jar.Attributes
import java.util.jar.JarFile
import javax.script.ScriptEngineManager
import kotlin.concurrent.thread

object CurrencyTrackerLauncher {

    lateinit var currencyTracker: CurrencyTracker

    val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        if (File("do_not_start").exists()) {
            logger.error { "File \"do_not_start\" exists, aborting initialization!!!" }

            System.exit(1)
        }

        // https://www.reddit.com/r/Kotlin/comments/8qdd4x/kotlin_script_engine_and_your_classpaths_what/
        val path = this::class.java.protectionDomain.codeSource.location.path
        val jar = JarFile(path)
        val mf = jar.manifest
        val mattr = mf.mainAttributes
        // Yes, you SHOULD USE Attributes.Name.CLASS_PATH! Don't try using "Class-Path", it won't work!
        val manifestClassPath = mattr[Attributes.Name.CLASS_PATH] as String

        // The format within the Class-Path attribute is different than the one expected by the property, so let's fix it!
        // By the way, don't forget to append your original JAR at the end of the string!
        val clazz = this::class.java
        val protectionDomain = clazz.protectionDomain
        val propClassPath = manifestClassPath.replace(
            " ",
            ":"
        ) + ":${Paths.get(protectionDomain.codeSource.location.toURI()).fileName}"

        // Now we set it to our own classpath
        System.setProperty("kotlin.script.classpath", propClassPath)

        thread {
            val scriptEngine = ScriptEngineManager().getEngineByName("kotlin")
            scriptEngine.eval("println()")
        }

        if (!File("logs").exists()) {
            File("logs").mkdir()
        }

        val configFile = File("config.yml")

        if (!configFile.exists()) {
            configFile.createNewFile()
            configFile.writeText(Static.YAML_MAPPER.writeValueAsString(CurrencyTrackerConfig()))

            logger.info { "Looks like you're running Currency Tracker for the first time!" }
            logger.info { "Setup it in the \"config.yml\" file!" }
            System.exit(0)
        }

        val config = Static.YAML_MAPPER.readValue(configFile, CurrencyTrackerConfig::class.java)

        currencyTracker = CurrencyTracker(config)
        currencyTracker.start()
    }
}