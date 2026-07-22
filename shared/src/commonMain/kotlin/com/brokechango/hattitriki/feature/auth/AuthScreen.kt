package com.brokechango.hattitriki.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.PitchBackground
import com.brokechango.hattitriki.ui.composables.SupabaseLoadingState
import hattitriki.shared.generated.resources.Res
import hattitriki.shared.generated.resources.hattitriki_app_icon
import hattitriki.shared.generated.resources.icon_visibility
import hattitriki.shared.generated.resources.icon_visibility_off
import org.jetbrains.compose.resources.painterResource

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onEvent: (AuthEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    PitchBackground(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val gateState = uiState.gateState) {
                AuthGateState.Loading -> LoadingCard()
                AuthGateState.SignedOut -> LoginCard(uiState, onEvent)
                AuthGateState.InvitationSetup -> InvitationSetupCard(uiState, onEvent)
                AuthGateState.PasswordRecoveryRequest -> PasswordRecoveryRequestCard(uiState, onEvent)
                AuthGateState.PasswordRecoverySetup -> PasswordRecoverySetupCard(uiState, onEvent)
                is AuthGateState.AccessError -> AccessErrorCard(gateState.message, onEvent)
                is AuthGateState.Authenticated -> Unit
            }
        }
    }
}

@Composable
private fun LoginCard(uiState: AuthUiState, onEvent: (AuthEvent) -> Unit) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    FootballCard(
        modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
        highlight = true
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.hattitriki_app_icon),
                contentDescription = "Hattitriki FC",
                modifier = Modifier.size(88.dp)
            )
            Text(
                text = "HATTITRIKI FC",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Acceso privado para miembros de la liga",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = uiState.email,
                onValueChange = { onEvent(AuthEvent.EmailChanged(it)) },
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                enabled = uiState.isAuthConfigured && !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { onEvent(AuthEvent.PasswordChanged(it)) },
                label = { Text("Contraseña") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = if (isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    PasswordVisibilityButton(
                        isVisible = isPasswordVisible,
                        onToggle = { isPasswordVisible = !isPasswordVisible },
                        enabled = uiState.isAuthConfigured && !uiState.isSubmitting
                    )
                },
                enabled = uiState.isAuthConfigured && !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = { onEvent(AuthEvent.SubmitLogin) },
                enabled = uiState.canSubmitLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isSubmitting) "Entrando…" else "Entrar")
            }
            TextButton(
                onClick = { onEvent(AuthEvent.OpenPasswordRecovery) },
                enabled = uiState.isAuthConfigured && !uiState.isSubmitting
            ) {
                Text("¿Has olvidado la contraseña?")
            }
        }
    }
}

@Composable
private fun InvitationSetupCard(uiState: AuthUiState, onEvent: (AuthEvent) -> Unit) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmationVisible by rememberSaveable { mutableStateOf(false) }

    FootballCard(
        modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
        highlight = true
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.hattitriki_app_icon),
                contentDescription = "Hattitriki FC",
                modifier = Modifier.size(78.dp)
            )
            Text(
                text = "Completa tu invitación",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (uiState.email.isBlank()) {
                    "Elige la contraseña con la que entrarás en Hattitriki."
                } else {
                    "Invitación para ${uiState.email}. Elige la contraseña con la que entrarás."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            SecurePasswordField(
                value = uiState.newPassword,
                onValueChange = { onEvent(AuthEvent.InvitationPasswordChanged(it)) },
                label = "Nueva contraseña",
                isVisible = isPasswordVisible,
                onVisibilityChanged = { isPasswordVisible = !isPasswordVisible },
                imeAction = ImeAction.Next,
                errorMessage = uiState.newPasswordError,
                enabled = !uiState.isSubmitting
            )
            SecurePasswordField(
                value = uiState.confirmPassword,
                onValueChange = {
                    onEvent(AuthEvent.InvitationPasswordConfirmationChanged(it))
                },
                label = "Repite la contraseña",
                isVisible = isConfirmationVisible,
                onVisibilityChanged = { isConfirmationVisible = !isConfirmationVisible },
                imeAction = ImeAction.Done,
                errorMessage = uiState.confirmPasswordError,
                enabled = !uiState.isSubmitting
            )

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = { onEvent(AuthEvent.SubmitInvitation) },
                enabled = uiState.canSubmitInvitation,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(if (uiState.isSubmitting) "Guardando…" else "Guardar contraseña")
            }
            OutlinedButton(
                onClick = { onEvent(AuthEvent.CancelInvitation) },
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al inicio de sesión")
            }
        }
    }
}

@Composable
private fun PasswordRecoveryRequestCard(uiState: AuthUiState, onEvent: (AuthEvent) -> Unit) {
    FootballCard(
        modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
        highlight = true
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.hattitriki_app_icon),
                contentDescription = "Hattitriki FC",
                modifier = Modifier.size(78.dp)
            )
            Text(
                text = if (uiState.passwordRecoveryEmailSent) {
                    "Revisa tu correo"
                } else {
                    "Recupera tu contraseña"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            if (uiState.passwordRecoveryEmailSent) {
                Text(
                    text = "Hemos enviado un enlace para crear una nueva contraseña a ${uiState.email}.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Si no lo recibes en unos minutos, revisa la carpeta de spam o solicita otro enlace.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { onEvent(AuthEvent.SubmitPasswordRecovery) },
                    enabled = uiState.canSubmitPasswordRecovery,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enviar otro enlace")
                }
            } else {
                Text(
                    text = "Indica el correo con el que accedes a la liga y te enviaremos un enlace seguro.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { onEvent(AuthEvent.EmailChanged(it)) },
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    enabled = uiState.isAuthConfigured && !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onEvent(AuthEvent.SubmitPasswordRecovery) },
                    enabled = uiState.canSubmitPasswordRecovery,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.isSubmitting) "Enviando…" else "Enviar enlace de recuperación")
                }
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            OutlinedButton(
                onClick = { onEvent(AuthEvent.CancelPasswordRecovery) },
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al inicio de sesión")
            }
        }
    }
}

@Composable
private fun PasswordRecoverySetupCard(uiState: AuthUiState, onEvent: (AuthEvent) -> Unit) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmationVisible by rememberSaveable { mutableStateOf(false) }

    FootballCard(
        modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
        highlight = true
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.hattitriki_app_icon),
                contentDescription = "Hattitriki FC",
                modifier = Modifier.size(78.dp)
            )
            Text(
                text = "Crea una nueva contraseña",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (uiState.email.isBlank()) {
                    "Elige una contraseña nueva para volver a entrar en Hattitriki."
                } else {
                    "Recuperación para ${uiState.email}. Elige una contraseña nueva."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            SecurePasswordField(
                value = uiState.newPassword,
                onValueChange = { onEvent(AuthEvent.RecoveryPasswordChanged(it)) },
                label = "Nueva contraseña",
                isVisible = isPasswordVisible,
                onVisibilityChanged = { isPasswordVisible = !isPasswordVisible },
                imeAction = ImeAction.Next,
                errorMessage = uiState.newPasswordError,
                enabled = !uiState.isSubmitting
            )
            SecurePasswordField(
                value = uiState.confirmPassword,
                onValueChange = {
                    onEvent(AuthEvent.RecoveryPasswordConfirmationChanged(it))
                },
                label = "Repite la contraseña",
                isVisible = isConfirmationVisible,
                onVisibilityChanged = { isConfirmationVisible = !isConfirmationVisible },
                imeAction = ImeAction.Done,
                errorMessage = uiState.confirmPasswordError,
                enabled = !uiState.isSubmitting
            )

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = { onEvent(AuthEvent.SubmitPasswordRecoverySetup) },
                enabled = uiState.canSubmitPasswordRecoverySetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(if (uiState.isSubmitting) "Guardando…" else "Guardar nueva contraseña")
            }
            OutlinedButton(
                onClick = { onEvent(AuthEvent.CancelPasswordRecovery) },
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al inicio de sesión")
            }
        }
    }
}

@Composable
private fun SecurePasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onVisibilityChanged: () -> Unit,
    imeAction: ImeAction,
    errorMessage: String?,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        visualTransformation = if (isVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            PasswordVisibilityButton(
                isVisible = isVisible,
                onToggle = onVisibilityChanged,
                enabled = enabled
            )
        },
        supportingText = errorMessage?.let { message ->
            { Text(message) }
        },
        isError = errorMessage != null,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordVisibilityButton(
    isVisible: Boolean,
    onToggle: () -> Unit,
    enabled: Boolean
) {
    IconButton(onClick = onToggle, enabled = enabled) {
        Icon(
            painter = painterResource(
                if (isVisible) {
                    Res.drawable.icon_visibility_off
                } else {
                    Res.drawable.icon_visibility
                }
            ),
            contentDescription = if (isVisible) {
                "Ocultar contraseña"
            } else {
                "Mostrar contraseña"
            }
        )
    }
}

@Composable
private fun LoadingCard() {
    FootballCard(modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth()) {
        SupabaseLoadingState(
            message = "Comprobando el acceso a la liga…",
            compact = true,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun AccessErrorCard(message: String, onEvent: (AuthEvent) -> Unit) {
    FootballCard(
        modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
        highlight = true
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "No hemos podido validar tu acceso",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { onEvent(AuthEvent.RetryAccess) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reintentar")
            }
            OutlinedButton(
                onClick = { onEvent(AuthEvent.Logout) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
