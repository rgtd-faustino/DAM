package dam.a51394.nevazio.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dam.a51394.nevazio.ui.theme.DarkGreen
import dam.a51394.nevazio.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLogoutSuccess: () -> Unit,
    onFamilyChanged: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    
    // 'loadedFamilyCode' começa a null para distinguir três estados distintos:
    // 1. null  → carregamento inicial ainda não concluído (Firestore ainda não respondeu)
    // 2. valor → carregamento concluído, este é o código no momento de entrada no ecrã
    // A abordagem com 'remember { uiState.familyCode }' falhava porque capturava sempre ""
    // (o valor por defeito do UiState) antes da Firestore responder, fazendo com que a
    // transição "" → "A9K3T2" (carregamento normal) fosse confundida com uma mudança real,
    // provocando um reset imediato da navegação ao abrir o Perfil.
    var loadedFamilyCode by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLogoutSuccess()
        }
    }
    
    // Quando o familyCode muda (utilizador junta-se ou sai de uma família), todos os ViewModels
    // da aplicação (Home, Shopping) têm o currentFridgeId obsoleto, porque calcularam-no no init.
    // A única forma segura de garantir que leem o frigorífico correto sem introduzir acoplamento
    // entre ViewModels é forçar a recriação de toda a stack de navegação via callback.
    LaunchedEffect(uiState.isLoading, uiState.familyCode) {
        if (!uiState.isLoading) {
            if (loadedFamilyCode == null) {
                // Primeiro carregamento concluído: regista o código atual como referência.
                // Não dispara navegação — esta é a baseline.
                loadedFamilyCode = uiState.familyCode
            } else if (loadedFamilyCode != uiState.familyCode) {
                // Mudança real após carregamento: utilizador juntou-se ou saiu de uma família.
                // Todos os ViewModels (Home, Shopping) têm o fridge ID antigo em memória,
                // pelo que o reset completo da stack garante a sua recriação com o ID correto.
                onFamilyChanged()
            }
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        if (uiState.errorMessage != null || uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SuccessGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(DarkGreen),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = uiState.name.firstOrNull()?.toString() 
                        ?: uiState.email.firstOrNull()?.toString() 
                        ?: "U"
                    Text(
                        text = initial.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // User Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = uiState.name.ifBlank { "Utilizador" },
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.toggleChangeNameDialog(true) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Mudar nome", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Text(
                    text = uiState.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { viewModel.toggleChangePasswordDialog(true) },
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mudar Password")
                }

                // Family Code Section
                if (uiState.familyCode.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "O teu Código de Família",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = uiState.familyCode,
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp,
                                    color = SuccessGreen
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(uiState.familyCode))
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Código", tint = SuccessGreen)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Partilha este código com a tua família para partilharem o mesmo frigorífico e lista de compras.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { viewModel.toggleLeaveFamilyDialog(true) },
                        modifier = Modifier.padding(bottom = 24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sair da Família")
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // O botão fica desativado durante isLoading para prevenir cliques duplos
                        // que criariam dois documentos 'family_codes' distintos para o mesmo utilizador.
                        OutlinedButton(
                            onClick = { viewModel.createFamily() },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Criar Família")
                            }
                        }
                        
                        OutlinedButton(
                            onClick = { viewModel.toggleJoinFamilyDialog(true) },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isLoading
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Juntar")
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // As mensagens de erro e sucesso são apresentadas em posición fixa acima do botão de logout,
                // garantindo visibilidade independentemente do estado de scroll do conteúdo acima.
                if (uiState.errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                uiState.errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                if (uiState.successMessage != null) {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.12f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                uiState.successMessage!!,
                                color = SuccessGreen,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Logout Button
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Terminar Sessão", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Terminar Sessão") },
            text = { Text("Tens a certeza que queres terminar sessão?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sair")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (uiState.showChangeNameDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleChangeNameDialog(false) },
            title = { Text("Mudar Nome") },
            text = {
                OutlinedTextField(
                    value = uiState.newNameInput,
                    onValueChange = { viewModel.onNewNameChange(it) },
                    label = { Text("Novo Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.updateName() }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleChangeNameDialog(false) }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (uiState.showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleChangePasswordDialog(false) },
            title = { Text("Mudar Password") },
            text = {
                OutlinedTextField(
                    value = uiState.newPasswordInput,
                    onValueChange = { viewModel.onNewPasswordChange(it) },
                    label = { Text("Nova Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.updatePassword() }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleChangePasswordDialog(false) }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (uiState.showJoinFamilyDialog) {
        AlertDialog(
            // O dismiss é bloqueado durante o loading para evitar que o utilizador feche o dialog
            // enquanto a validação do código ainda está em curso na Firestore.
            onDismissRequest = { if (!uiState.isLoading) viewModel.toggleJoinFamilyDialog(false) },
            title = { Text("Juntar a uma Família") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.newFamilyCodeInput,
                        onValueChange = { viewModel.onNewFamilyCodeChange(it) },
                        label = { Text("Código de Família") },
                        placeholder = { Text("Ex: A9K3T2") },
                        singleLine = true,
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.joinFamily() },
                    enabled = !uiState.isLoading && uiState.newFamilyCodeInput.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Juntar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.toggleJoinFamilyDialog(false) },
                    enabled = !uiState.isLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (uiState.showLeaveFamilyDialog) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isLoading) viewModel.toggleLeaveFamilyDialog(false) },
            title = { Text("Sair da Família") },
            text = { Text("Tens a certeza que queres sair desta família? Vais voltar ao teu frigorífico pessoal.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.leaveFamily() },
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sair")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.toggleLeaveFamilyDialog(false) },
                    enabled = !uiState.isLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
