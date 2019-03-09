package net.mrgaabriel.currencytracker.tasks

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.mrgaabriel.currencyconverter.CurrencyConverter
import net.mrgaabriel.currencytracker.dao.Tracker
import net.mrgaabriel.currencytracker.utils.currencyTracker
import net.mrgaabriel.currencytracker.utils.database
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.time.OffsetDateTime

object TrackerTasks {

    val logger = KotlinLogging.logger {}

    fun startTasks() {
        val trackers = transaction(database) {
            Tracker.all().toMutableList()
        }

        for (tracker in trackers) {
            startTask(tracker)
        }
    }

    fun startTask(tracker: Tracker) {
        GlobalScope.launch(currencyTracker.defaultDispatcher) {
            logger.info { "$tracker (${tracker.from} -> ${tracker.to}) task started" }

            while (true) {
                try {
                    val start = System.currentTimeMillis()
                    logger.info { "Checking tracker $tracker (${tracker.from} -> ${tracker.to})..." }

                    val from = tracker.from
                    val to = tracker.to

                    val convertedValue = CurrencyConverter.convert(1, from, to)
                    val value = "%.2f".format(convertedValue).replace(",", ".").toDouble()

                    val lastValue = tracker.lastValue

                    if (lastValue != value) {
                        transaction(database) {
                            tracker.lastValue = value
                        }

                        val channels = tracker.channelIds.map { currencyTracker.shardManager.getTextChannelById(it) }

                        val builder = EmbedBuilder()

                        val color = when {
                            lastValue == 0.0 -> Color(114, 137, 218)

                            value > lastValue -> Color(172, 26, 23)
                            value < lastValue -> Color(25, 167, 25)

                            else -> Color.YELLOW
                        }

                        val title = when {
                            lastValue == 0.0 -> "`$from` -> `$to` | \uD83D\uDCCA"

                            value > lastValue -> "`$from` -> `$to` | \uD83D\uDCC8"
                            value < lastValue -> "`$from` -> `$to` | \uD83D\uDCC9"

                            else -> "¯\\_(ツ)_/¯"
                        }

                        builder.setTitle(title)
                        builder.setColor(color)

                        builder.setDescription("Current price: `$value $to`")

                        builder.setTimestamp(OffsetDateTime.now())

                        channels.forEach {
                            try {
                                it.sendMessage(builder.build()).queue {
                                    logger.info { "Tracker $tracker (${tracker.from} -> ${tracker.to}) message sent to channel ${it.channel.id} in guild ${it.guild.id}" }
                                }
                            } catch (e: Exception) {
                                logger.error(e) { "Error while trying to send tracker $tracker (${tracker.from} -> ${tracker.to}) changes to channel ${it.id} (${it.guild} (${it.id}))" }
                            }
                        }
                    }

                    logger.info { "Finished checking tracker $tracker (${tracker.from} -> ${tracker.to})!!! Current value: $value - Took ${System.currentTimeMillis() - start}ms to check" }
                } catch (e: Exception) {
                    logger.error(e) { "Error while checking the tracker $tracker (${tracker.from} -> ${tracker.to})" }
                }

                delay(60 * 1000)
            }
        }
    }
}