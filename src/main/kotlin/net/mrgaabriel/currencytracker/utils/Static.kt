package net.mrgaabriel.currencytracker.utils

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.ajalt.mordant.TermColors
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.mrgaabriel.currencytracker.CurrencyTrackerLauncher
import org.jetbrains.exposed.sql.Database

object Static {
    val YAML_MAPPER =
        ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)).registerKotlinModule()
    val JSON_MAPPER = ObjectMapper(JsonFactory()).registerKotlinModule()
}

val currencyTracker by lazy { CurrencyTrackerLauncher.currencyTracker }
val t = TermColors()

private val hikariConfig by lazy {
    val config = HikariConfig()
    config.jdbcUrl =
        "jdbc:postgresql://${currencyTracker.config.postgreIp}:${currencyTracker.config.postgrePort}/${currencyTracker.config.postgreDatabaseName}"
    config.username = currencyTracker.config.postgreUsername
    if (currencyTracker.config.postgrePassword.isNotEmpty())
        config.password = currencyTracker.config.postgrePassword
    config.driverClassName = "org.postgresql.Driver"

    config.addDataSourceProperty("cachePrepStmts", "true")
    config.addDataSourceProperty("prepStmtCacheSize", "250")
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    config.maximumPoolSize = 150
    return@lazy config
}

val dataSource by lazy { HikariDataSource(hikariConfig) }
val database by lazy { Database.connect(dataSource) }