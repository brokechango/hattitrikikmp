package com.brokechango.hattitriki.feature.home

import com.brokechango.hattitriki.core.model.PlayerRankingCategory
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeStatsOrderStoreTest {

    @Test
    fun normalizeOrderRestoresMissingCategoriesAndRemovesDuplicates() {
        val savedOrder = listOf(
            PlayerRankingCategory.MOST_WINS,
            PlayerRankingCategory.TOP_SCORER,
            PlayerRankingCategory.MOST_WINS,
            PlayerRankingCategory.PLAYER_ON_FORM
        )

        assertEquals(
            listOf(
                PlayerRankingCategory.MOST_WINS,
                PlayerRankingCategory.TOP_SCORER,
                PlayerRankingCategory.GOALS_PER_MATCH,
                PlayerRankingCategory.ZAMORA,
                PlayerRankingCategory.GOALS_CONCEDED_PER_MATCH,
                PlayerRankingCategory.MOST_PLAYED
            ),
            normalizeHomeStatsOrder(savedOrder)
        )
    }

    @Test
    fun mergeVisibleOrderKeepsUnavailableStatsInTheirRelativeSlots() {
        val visibleOrder = listOf(
            PlayerRankingCategory.MOST_WINS,
            PlayerRankingCategory.TOP_SCORER,
            PlayerRankingCategory.GOALS_PER_MATCH,
            PlayerRankingCategory.MOST_PLAYED
        )

        assertEquals(
            listOf(
                PlayerRankingCategory.MOST_WINS,
                PlayerRankingCategory.TOP_SCORER,
                PlayerRankingCategory.ZAMORA,
                PlayerRankingCategory.GOALS_CONCEDED_PER_MATCH,
                PlayerRankingCategory.GOALS_PER_MATCH,
                PlayerRankingCategory.MOST_PLAYED
            ),
            mergeVisibleHomeStatsOrder(defaultHomeStatsOrder, visibleOrder)
        )
    }

    @Test
    fun settingsStorePersistsAndRestoresTheSelectedOrder() {
        val store = MultiplatformSettingsHomeStatsOrderStore(MapSettings())
        val selectedOrder = listOf(
            PlayerRankingCategory.MOST_WINS,
            PlayerRankingCategory.MOST_PLAYED,
            PlayerRankingCategory.TOP_SCORER
        )

        store.saveOrder(selectedOrder)

        assertEquals(selectedOrder, store.loadOrder())
    }
}
