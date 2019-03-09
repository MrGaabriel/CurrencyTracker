package net.mrgaabriel.currencytracker.dao

import net.mrgaabriel.currencytracker.tables.Trackers
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class Tracker(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Tracker>(Trackers)

    var from by Trackers.from
    var to by Trackers.to

    var channelIds by Trackers.channelIds

    var lastValue by Trackers.lastValue
}