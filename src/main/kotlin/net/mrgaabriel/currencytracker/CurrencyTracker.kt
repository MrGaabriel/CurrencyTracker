package net.mrgaabriel.currencytracker

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.asCoroutineDispatcher
import mu.KotlinLogging
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.mrgaabriel.currencytracker.config.CurrencyTrackerConfig
import net.mrgaabriel.currencytracker.dao.Tracker
import net.mrgaabriel.currencytracker.listeners.MessageListeners
import net.mrgaabriel.currencytracker.manager.CommandManager
import net.mrgaabriel.currencytracker.tables.Trackers
import net.mrgaabriel.currencytracker.tasks.GameTask
import net.mrgaabriel.currencytracker.tasks.TrackerTasks
import net.mrgaabriel.currencytracker.utils.database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CurrencyTracker(var config: CurrencyTrackerConfig) {

    val logger = KotlinLogging.logger {}

    var builder = DefaultShardManagerBuilder()
        .setToken(config.clientToken)
        .setStatus(OnlineStatus.valueOf(config.onlineStatus))
        .setShardsTotal(config.shards)
        .setCallbackPool(Executors.newSingleThreadExecutor())
        .addEventListeners(MessageListeners())

    lateinit var commandManager: CommandManager

    fun createThreadPool(name: String): ExecutorService {
        return Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat(name).build())
    }

    val defaultPool = createThreadPool("Dispatcher Thread %d")
    val defaultDispatcher = defaultPool.asCoroutineDispatcher()

    lateinit var shardManager: ShardManager

    fun start() {
        logger.info { "Starting! (Discord)" }
        shardManager = builder.build()
        logger.info { "OK! Shard Manager successfully started!" }

        commandManager = CommandManager()

        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(
                Trackers
            )
        }

        GameTask.startTask()
        TrackerTasks.startTasks()
    }

    fun getTracker(from: String, to: String): Tracker {
        return transaction(database) {
            Tracker.find { (Trackers.from eq from) and (Trackers.to eq to) }.firstOrNull()
                ?: Tracker.new {
                    this.from = from
                    this.to = to

                    this.channelIds = arrayOf()
                }.apply {
                    TrackerTasks.startTask(this)
                }
        }
    }
}