package com.brokechango.hattitriki.ui.icons

import com.brokechango.hattitriki.core.model.PlayerRankingCategory
import hattitriki.shared.generated.resources.Res
import hattitriki.shared.generated.resources.emoji_fire
import hattitriki.shared.generated.resources.emoji_football
import hattitriki.shared.generated.resources.emoji_gloves
import hattitriki.shared.generated.resources.emoji_goal
import hattitriki.shared.generated.resources.emoji_shoe
import hattitriki.shared.generated.resources.emoji_target
import hattitriki.shared.generated.resources.emoji_trophy
import org.jetbrains.compose.resources.DrawableResource

val PlayerRankingCategory.rankingEmojiResource: DrawableResource
    get() = when (this) {
        PlayerRankingCategory.TOP_SCORER -> Res.drawable.emoji_football
        PlayerRankingCategory.GOALS_PER_MATCH -> Res.drawable.emoji_target
        PlayerRankingCategory.ZAMORA -> Res.drawable.emoji_gloves
        PlayerRankingCategory.GOALS_CONCEDED_PER_MATCH -> Res.drawable.emoji_goal
        PlayerRankingCategory.MOST_PLAYED -> Res.drawable.emoji_shoe
        PlayerRankingCategory.MOST_WINS -> Res.drawable.emoji_trophy
        PlayerRankingCategory.PLAYER_ON_FORM -> Res.drawable.emoji_fire
    }
