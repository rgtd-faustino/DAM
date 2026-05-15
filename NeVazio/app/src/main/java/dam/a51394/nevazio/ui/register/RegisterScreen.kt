package dam.a51394.nevazio.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.a51394.nevazio.ui.theme.DarkGreen
import dam.a51394.nevazio.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAF5EA))
    ) {
        // Back button
        IconButton(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = DarkGreen)
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 64.dp, bottom = 24.dp)
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "NeVazio",
                    style = MaterialTheme.typography.displayLarge,
                    color = DarkGreen,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Cria a tua conta para gerir a cozinha",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Nome
                FormLabel("Nome")
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder = { Text("O teu nome", color = MaterialTheme.colorScheme.outline) },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.outline) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = outlinedFieldColors()
                )

                // Email
                FormLabel("Email")
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = { Text("O teu email", color = MaterialTheme.colorScheme.outline) },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.outline) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = outlinedFieldColors()
                )

                // Password
                FormLabel("Password")
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = { Text("Mínimo 8 caracteres", color = MaterialTheme.colorScheme.outline) },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.outline) },
                    trailingIcon = {
                        IconButton(onClick = viewModel::togglePasswordVisibility) {
                            Icon(
                                if (uiState.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    },
                    visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = outlinedFieldColors()
                )

                // Confirmar Password
                FormLabel("Confirmar Password")
                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    placeholder = { Text("Repete a password", color = MaterialTheme.colorScheme.outline) },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.outline) },
                    trailingIcon = {
                        IconButton(onClick = viewModel::toggleConfirmPasswordVisibility) {
                            Icon(
                                if (uiState.confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    },
                    visualTransformation = if (uiState.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = outlinedFieldColors()
                )

                // Código de família
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Código de família (opcional)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                }
                OutlinedTextField(
                    value = uiState.familyCode,
                    onValueChange = viewModel::onFamilyCodeChange,
                    placeholder = { Text("EX:  A1B2C3", color = MaterialTheme.colorScheme.outline) },
                    leadingIcon = { Icon(Icons.Default.GroupAdd, null, tint = MaterialTheme.colorScheme.outline) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = SuccessGreen,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )

                // Terms checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.termsAccepted,
                        onCheckedChange = viewModel::toggleTerms,
                        colors = CheckboxDefaults.colors(checkedColor = SuccessGreen)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        buildAnnotatedString {
                            append("Concordo com os ")
                            withStyle(SpanStyle(color = SuccessGreen, fontWeight = FontWeight.SemiBold)) {
                                append("Termos de Serviço")
                            }
                            append(" e a ")
                            withStyle(SpanStyle(color = SuccessGreen, fontWeight = FontWeight.SemiBold)) {
                                append("Política de Privacidade")
                            }
                            append(".")
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Error
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Criar conta button
                Button(
                    onClick = { viewModel.onRegister(onRegisterSuccess) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        "CRIAR CONTA",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Divider "ou"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f))
                    Text("  ou  ", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodyMedium)
                    Divider(modifier = Modifier.weight(1f))
                }

                // Google button
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text("G  ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF4285F4))
                    Text(
                        "Continuar com Google",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Login link
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        buildAnnotatedString {
                            append("Já tens conta? ")
                            withStyle(SpanStyle(color = SuccessGreen, fontWeight = FontWeight.Bold)) {
                                append("Entrar")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor = SuccessGreen
)
