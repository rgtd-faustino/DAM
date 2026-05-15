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
import dam.a51394.nevazio.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToScan: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showAddSheet) {
        AddIngredientSheet(
            onDismiss = viewModel::hideAddSheet,
            onAdd = viewModel::addIngredient
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
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
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "NeVazio",
                            color = SuccessGreen,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Pesquisar", tint = MaterialTheme.colorScheme.onSurface)
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

        if (uiState.filteredIngredients.isEmpty() && !uiState.isLoading) {
            // Empty state
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
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = CircleShape
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.outline)
                            Text(
                                "Procurar ingrediente...",
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Tab row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Frigorífico
                        TabChip(
                            label = "🟢  Frigorífico",
                            selected = uiState.selectedTab == 0,
                            onClick = { viewModel.onTabSelected(0) }
                        )
                        // Despensa
                        TabChip(
                            label = "Despensa",
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

                // Ingredient cards
                items(uiState.filteredIngredients) { ingredient ->
                    IngredientCard(ingredient)
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
fun IngredientCard(ingredient: Ingredient) {
    val isExpiring = ingredient.status == ExpiryStatus.EXPIRING_SOON || ingredient.status == ExpiryStatus.EXPIRES_TODAY
    val cardBgColor = if (isExpiring) Color(0xFFFFF5F5) else Color.White
    val borderColor = if (isExpiring) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val iconBgColor = when (ingredient.status) {
        ExpiryStatus.FRESH -> FreshGreenBg
        ExpiryStatus.EXPIRING_SOON -> Color(0xFFFFEBEE)
        ExpiryStatus.EXPIRES_TODAY -> Color(0xFFFFEBEE)
        ExpiryStatus.EXPIRED -> Color(0xFFFFEBEE)
    }
    val iconTint = when (ingredient.status) {
        ExpiryStatus.FRESH -> FreshGreenIcon
        else -> Color(0xFFE53935)
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            color = cardBgColor,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        "eco" -> "🥦"
                        "icecream" -> "🍦"
                        "egg" -> "🥚"
                        "cheese" -> "🧀"
                        "grain" -> "🌾"
                        else -> "🥗"
                    }
                    Text(emoji, fontSize = 22.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        ingredient.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${ingredient.quantity} • ${ingredient.expiryLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status badge
                when (ingredient.status) {
                    ExpiryStatus.EXPIRES_TODAY -> {
                        Surface(
                            color = Color(0xFFE53935),
                            shape = CircleShape
                        ) {
                            Text(
                                "Hoje!",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    ExpiryStatus.EXPIRING_SOON -> {
                        Surface(
                            color = Color(0xFFE53935),
                            shape = CircleShape
                        ) {
                            Text(
                                ingredient.expiryLabel,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Red left accent bar for expiring items
        if (isExpiring) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(Color(0xFFE53935))
                    .align(Alignment.CenterStart)
            )
        }
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
        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(30.dp))
                .padding(4.dp)
        ) {
            Box(modifier = Modifier.weight(1f).padding(4.dp), contentAlignment = Alignment.Center) {
                Text("Visão Geral", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                modifier = Modifier.weight(1f),
                color = SuccessGreen,
                shape = RoundedCornerShape(26.dp)
            ) {
                Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                    Text("Despensa", style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

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
            "O teu frigorífico está vazio",
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
