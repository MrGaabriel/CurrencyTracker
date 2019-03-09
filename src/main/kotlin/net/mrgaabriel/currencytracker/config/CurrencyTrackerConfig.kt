package net.mrgaabriel.currencytracker.config

import com.fasterxml.jackson.annotation.JsonProperty

class CurrencyTrackerConfig(
    @JsonProperty("client-token")
    val clientToken: String = "Client token do Bot",

    @JsonProperty("client-id")
    val clientId: String = "Client ID do Bot",

    @JsonProperty("owner-id")
    val ownerId: String = "ID do dono do Bot",

    @JsonProperty("online-status")
    val onlineStatus: String = "ONLINE",

    @JsonProperty("games")
    val games: List<GameWrapper> = listOf(),

    @JsonProperty("shards")
    val shards: Int = -1,

    @JsonProperty("command-prefix")
    val commandPrefix: String = "Prefixo usado para os comandos",

    @JsonProperty("postgre-ip")
    val postgreIp: String = "IP do PostgreSQL",

    @JsonProperty("postgre-port")
    val postgrePort: Int = 5432,

    @JsonProperty("postgre-database-name")
    val postgreDatabaseName: String = "Nome da database do PostgreSQL",

    @JsonProperty("postgre-username")
    val postgreUsername: String = "Usuário do PostgreSQL",

    @JsonProperty("postgre-password")
    val postgrePassword: String = "Senha do PostgreSQL"
)

class GameWrapper(val name: String, val type: String)