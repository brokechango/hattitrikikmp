package com.brokechango.hattitriki.feature.home

import com.brokechango.hattitriki.core.model.PlayerRankingCategory
import com.russhwolf.settings.Settings

interface HomeStatsOrderStore {
    fun loadOrder(): List<PlayerRankingCategory>

    fun saveOrder(order: List<PlayerRankingCategory>)
}

class MultiplatformSettingsHomeStatsOrderStore(
    private val settings: Settings
) : HomeStatsOrderStore {
    override fun loadOrder(): List<PlayerRankingCategory> =
        settings.getStringOrNull(ORDER_KEY)
            .orEmpty()
            .split(',')
            .mapNotNull { savedName ->
                PlayerRankingCategory.entries.firstOrNull { category ->
                    category.name == savedName
                }
            }

    override fun saveOrder(order: List<PlayerRankingCategory>) {
        settings.putString(
            key = ORDER_KEY,
            value = order.joinToString(",") { it.name }
        )
    }

    private companion object {
        const val ORDER_KEY = "featured_stats_order"
    }
}

object NoOpHomeStatsOrderStore : HomeStatsOrderStore {
    override fun loadOrder(): List<PlayerRankingCategory> = emptyList()

    override fun saveOrder(order: List<PlayerRankingCategory>) = Unit
}

internal val defaultHomeStatsOrder = listOf(
    PlayerRankingCategory.TOP_SCORER,
    PlayerRankingCategory.GOALS_PER_MATCH,
    PlayerRankingCategory.ZAMORA,
    PlayerRankingCategory.GOALS_CONCEDED_PER_MATCH,
    PlayerRankingCategory.MOST_PLAYED,
    PlayerRankingCategory.MOST_WINS
)

internal fun normalizeHomeStatsOrder(
    savedOrder: List<PlayerRankingCategory>
): List<PlayerRankingCategory> {
    val validSavedOrder = savedOrder
        .distinct()
        .filter { it in defaultHomeStatsOrder }

    return validSavedOrder + defaultHomeStatsOrder.filterNot(validSavedOrder::contains)
}

internal fun mergeVisibleHomeStatsOrder(
    currentOrder: List<PlayerRankingCategory>,
    visibleOrder: List<PlayerRankingCategory>
): List<PlayerRankingCategory> {
    val normalizedCurrentOrder = normalizeHomeStatsOrder(currentOrder)
    val normalizedVisibleOrder = visibleOrder
        .distinct()
        .filter(normalizedCurrentOrder::contains)

    if (normalizedVisibleOrder.isEmpty()) return normalizedCurrentOrder

    val visibleCategories = normalizedVisibleOrder.toSet()
    val visibleIterator = normalizedVisibleOrder.iterator()
    return normalizedCurrentOrder.map { category ->
        if (category in visibleCategories) visibleIterator.next() else category
    }
}
