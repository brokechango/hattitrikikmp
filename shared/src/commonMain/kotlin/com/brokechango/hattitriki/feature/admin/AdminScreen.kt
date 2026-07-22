package com.brokechango.hattitriki.feature.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import com.brokechango.hattitriki.ui.preview.HattitrikiPreview
import com.brokechango.hattitriki.ui.preview.PreviewTargets
import hattitriki.shared.generated.resources.Res
import hattitriki.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AdminScreen(
    appVersion: String,
    onNewMatch: () -> Unit,
    onAddPlayer: () -> Unit,
    onManageMatches: () -> Unit,
    onManagePlayers: () -> Unit,
    onInviteLeagueMember: () -> Unit,
    onTeamRandomizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ScreenTitle(
            title = stringResource(Res.string.admin_title),
            subtitle = stringResource(Res.string.admin_subtitle)
        )

        FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(Res.string.admin_tools),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = stringResource(Res.string.admin_welcome),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onNewMatch,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.admin_new_match))
                }
                OutlinedButton(
                    onClick = onManageMatches,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.admin_manage_matches))
                }
                OutlinedButton(
                    onClick = onAddPlayer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.admin_add_player))
                }
                OutlinedButton(
                    onClick = onManagePlayers,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.admin_manage_players))
                }
                OutlinedButton(
                    onClick = onInviteLeagueMember,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.admin_invite_to_league))
                }
                OutlinedButton(
                    onClick = onTeamRandomizer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.admin_team_randomizer))
                }
            }
        }

        Text(
            text = stringResource(Res.string.admin_version, appVersion),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@PreviewTargets
@Composable
private fun AdminScreenPreview() {
    HattitrikiPreview {
        AdminScreen(
            appVersion = "1.0.0",
            onNewMatch = {},
            onAddPlayer = {},
            onManageMatches = {},
            onManagePlayers = {},
            onInviteLeagueMember = {},
            onTeamRandomizer = {},
            modifier = Modifier.fillMaxSize().padding(20.dp)
        )
    }
}
