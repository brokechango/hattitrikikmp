package com.brokechango.hattitriki.core.model

enum class PlayerRankingCategory(
    val title: String,
    val icon: String,
    val detail: String
) {
    TOP_SCORER("Maximo goleador", "⚽", "goles"),
    GOALS_PER_MATCH("Goles / partido", "🎯", "goles por partido"),
    ZAMORA("Zamora", "🧤", "goles recibidos"),
    GOALS_CONCEDED_PER_MATCH("GC / partido", "🥅", "como portero"),
    MOST_PLAYED("Mas jugado", "👟", "partidos"),
    MOST_WINS("Mas ganador", "🏆", "victorias"),
    PLAYER_ON_FORM("Jugador en racha", "🔥", "rendimiento total")
}
