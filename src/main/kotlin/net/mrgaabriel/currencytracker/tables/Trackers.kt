package net.mrgaabriel.currencytracker.tables

import net.mrgaabriel.currencytracker.utils.exposed.array
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.TextColumnType

object Trackers : LongIdTable() {

    val from = text("from")
    val to = text("to")

    val channelIds = array<String>("channel_ids", TextColumnType())

    val lastValue = double("last_value").default(0.0)
}