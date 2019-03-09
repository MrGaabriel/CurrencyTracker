package net.mrgaabriel.currencytracker.tasks

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Activity
import net.mrgaabriel.currencytracker.utils.currencyTracker

object GameTask {

    val logger = KotlinLogging.logger {}

    fun startTask() {
        GlobalScope.launch(currencyTracker.defaultDispatcher) {
            while (true) {
                val game = currencyTracker.config.games.random()
                val activity =
                    Activity.of(Activity.ActivityType.valueOf(game.type), game.name, "https://www.twitch.tv/MrGaabriel")

                currencyTracker.shardManager.setGame(activity)
                logger.info { "Now playing game of type ${game.type} and name \"${game.name}\"" }

                delay(60 * 1000)
            }
        }
    }
}