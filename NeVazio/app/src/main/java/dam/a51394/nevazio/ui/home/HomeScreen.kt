package dam.a51394.nevazio.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.a51394.nevazio.data.model.ExpiryStatus
import dam.a51394.nevazio.data.model.Ingredient
import dam.a51394.nevazio.data.model.StorageLocation
import dam.a51394.nevazio.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToScan: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // A utilização de collectAsStateWithLifecycle() em oposição a collectAsState() 
    // evita o consumo desnecessário de recursos quando a aplicação transita para background. 
    // A interrupção da escuta do StateFlow poupa bateria do dispositivo e minimiza o tráfego de rede.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshUserProfile()
    }

    if (uiState.showAddSheet) {
        val location = if (uiState.selectedTab == 1) StorageLocation.PANTRY else StorageLocation.FRIDGE
        AddIngredientSheet(
            onDismiss = viewModel::hideAddSheet,
            initialName = uiState.initialAddName,
            initialLocation = location,
            onAdd = viewModel::addIngredient
        )
    }

    if (uiState.editingIngredient != null) {
        val item = uiState.editingIngredient!!
        val quantityStr = item.quantity.filter { it.isDigit() || it == '.' }
        val unitStr = item.quantity.filter { it.isLetter() }.ifBlank { "un" }

        AddIngredientSheet(
            onDismiss = viewModel::hideEditSheet,
            initialName = item.name,
            initialQuantity = quantityStr,
            initialUnit = unitStr,
            initialLocation = item.location,
            initialExpiryMillis = item.expiryDate?.time,
            title = "Editar Ingrediente",
            buttonText = "GUARDAR ALTERAÇÕES",
            onAdd = { name, quantity, unit, location, expiryMillis ->
                viewModel.updateIngredient(item.id, name, quantity, unit, location, expiryMillis)
            }
        )
    }

    if (uiState.ingredientToDelete != null) {
        val ingredient = uiState.ingredientToDelete!!
        AlertDialog(
            onDismissRequest = viewModel::hideDeletePrompt,
            title = { Text("O ingrediente acabou?", fontWeight = FontWeight.Bold) },
            text = { Text("Queres adicionar '${ingredient.name}' à tua lista de compras antes de eliminar?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete(addToShoppingList = true) }) {
                    Text("Adicionar às Compras", color = dam.a51394.nevazio.ui.theme.SuccessGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.confirmDelete(addToShoppingList = false) }) {
                    Text("Só Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    // A distinção entre o estado "Totalmente Vazio" (ausência absoluta de itens após criação de conta)
    // e "Categoria Vazia" (existência de itens noutra secção mas não na atual) dita a interface adequada.
    // O estado totalmente vazio apresenta um ecrã de boas-vindas expansivo (EmptyState completo),
    // enquanto o estado de categoria vazia exibe um aviso localizado, melhorando a progressividade da UX.
    val isTrulyEmpty = uiState.ingredients.isEmpty() && !uiState.isLoading
    val isCategoryEmpty = uiState.filteredIngredients.isEmpty() && !isTrulyEmpty && !uiState.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar (Click to go to profile)
                        Surface(
                            onClick = onNavigateToProfile,
                            shape = CircleShape,
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(DarkGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.userName.firstOrNull()?.toString() ?: "U",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "NeVazio",
                            color = SuccessGreen,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::showAddSheet,
                containerColor = SuccessGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (isTrulyEmpty) {
            // Truly empty: no ingredients at all
            EmptyState(
                onAddClick = viewModel::showAddSheet,
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                // Header
                item {
                    Column {
                        Text(
                            "Olá, ${uiState.userName} 👋",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            uiState.familyName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Expiry warning
                if (uiState.expiryWarning != null) {
                    item {
                        Surface(
                            color = Color(0xFFFFF0F0),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    uiState.expiryWarning!!,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFFB71C1C),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Search bar
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Procurar ingrediente...",
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.outline)
                        },
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedBorderColor = SuccessGreen,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }

                // Tab row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val fridgeCount = uiState.ingredients.count { it.location == dam.a51394.nevazio.data.model.StorageLocation.FRIDGE }
                        val pantryCount = uiState.ingredients.count { it.location == dam.a51394.nevazio.data.model.StorageLocation.PANTRY }
                        
                        // Frigorífico
                        TabChip(
                            label = "Frigorífico ($fridgeCount)",
                            selected = uiState.selectedTab == 0,
                            onClick = { viewModel.onTabSelected(0) }
                        )
                        // Despensa
                        TabChip(
                            label = "Despensa ($pantryCount)",
                            selected = uiState.selectedTab == 1,
                            onClick = { viewModel.onTabSelected(1) }
                        )
                        // Scan
                        Surface(
                            onClick = { onNavigateToScan() },
                            color = if (uiState.selectedTab == 2) SuccessGreen.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape,
                            border = if (uiState.selectedTab == 2)
                                androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                            else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner, null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (uiState.selectedTab == 2) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Scan",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (uiState.selectedTab == 2) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (uiState.selectedTab == 2) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Section label
                item {
                    Text(
                        "INGREDIENTES (${uiState.filteredIngredients.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        letterSpacing = 1.sp
                    )
                }

                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = SuccessGreen)
                        }
                    }
                } else if (isCategoryEmpty) {
                    // Category-specific empty: show inline message
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val categoryName = if (uiState.selectedTab == 0) "O teu frigorífico está vazio" else "A tua despensa está vazia"
                            Text("📭", fontSize = 48.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                categoryName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Adiciona ingredientes ou muda de categoria.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(onClick = viewModel::showAddSheet) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Adicionar aqui")
                            }
                        }
                    }
                } else {
                    // Ingredient cards
                    items(uiState.filteredIngredients, key = { it.id }) { ingredient ->
                        IngredientCard(
                            ingredient = ingredient,
                            onEdit = { viewModel.showEditSheet(ingredient) },
                            onDelete = { viewModel.promptRemoveIngredient(ingredient) }
                        )
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) SuccessGreen.copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = CircleShape,
        border = if (selected)
            androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
        else null
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun IngredientCard(ingredient: Ingredient, onEdit: () -> Unit = {}, onDelete: () -> Unit = {}) {
    val isExpired      = ingredient.status == ExpiryStatus.EXPIRED
    val isExpiresToday = ingredient.status == ExpiryStatus.EXPIRES_TODAY
    val isExpiringSoon = ingredient.status == ExpiryStatus.EXPIRING_SOON
    val isFresh        = ingredient.status == ExpiryStatus.FRESH

    // Card colours
    val cardBgColor = when (ingredient.status) {
        ExpiryStatus.EXPIRED       -> Color(0xFFF5F5F5)
        ExpiryStatus.EXPIRES_TODAY -> Color(0xFFFFF0F0)
        ExpiryStatus.EXPIRING_SOON -> Color(0xFFFFFBF0)
        ExpiryStatus.FRESH         -> Color.White
    }
    val accentColor = when (ingredient.status) {
        ExpiryStatus.EXPIRED       -> Color(0xFF9E9E9E)
        ExpiryStatus.EXPIRES_TODAY -> Color(0xFFE53935)
        ExpiryStatus.EXPIRING_SOON -> Color(0xFFFFA000)
        ExpiryStatus.FRESH         -> SuccessGreen
    }
    val borderColor = accentColor.copy(alpha = if (isFresh) 0.2f else 0.4f)
    val iconBgColor = accentColor.copy(alpha = 0.12f)

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            onClick = onEdit,
            color = cardBgColor,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Icon box
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(iconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        val emoji = when (ingredient.iconName) {
                            "water_drop" -> "💧"
                            "eco"        -> "🥦"
                            "icecream"   -> "🍦"
                            "egg"        -> "🥚"
                            "cheese"     -> "🧀"
                            "grain"      -> "🌾"
                            "restaurant" -> "🥩"
                            "fish"       -> "🐟"
                            "fruit"      -> "🍎"
                            "food"       -> "🥘"
                            else         -> "🥗"
                        }
                        Text(emoji, fontSize = 22.sp)
                    }

                    // Name + quantity
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            ingredient.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            color = if (isExpired) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                    else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            ingredient.quantity,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Bottom expiry progress bar (only when date is known)
                if (ingredient.expiryDate != null) {
                    ExpiryProgressBar(ingredient, accentColor)
                }
            }
        }

        // Coloured left accent bar
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                .background(accentColor)
                .align(Alignment.CenterStart)
        )
    }
}

@Composable
fun ExpiryProgressBar(ingredient: Ingredient, accentColor: androidx.compose.ui.graphics.Color) {
    // Progress 1.0 = loads of time left, 0.0 = expired/today
    val totalDaysAssumed = 30 // assume max shelf life for display
    val now = java.util.Date()
    val diffMs = (ingredient.expiryDate?.time ?: now.time) - now.time
    val daysLeft = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffMs).coerceIn(-1L, totalDaysAssumed.toLong())
    val progress = ((daysLeft + 1f) / (totalDaysAssumed + 1f)).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Validade",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                ingredient.expiryLabel,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(50)),
            color = accentColor,
            trackColor = accentColor.copy(alpha = 0.15f)
        )
    }
}

@Composable
fun EmptyState(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(40.dp))

        // Illustration placeholder
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(SuccessGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("📱", fontSize = 80.sp)
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Não tens ingredientes guardados",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.ExtraBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Adiciona ingredientes para começares a gerir a tua despensa e evitares desperdícios!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onAddClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text(
                "Adicionar Ingredientes",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    NeVazioTheme {
        EmptyState(onAddClick = {})
    }
}
