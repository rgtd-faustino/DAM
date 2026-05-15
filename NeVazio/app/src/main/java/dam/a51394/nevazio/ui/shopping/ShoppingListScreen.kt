package dam.a51394.nevazio.ui.shopping

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.a51394.nevazio.data.model.ShoppingItem
import dam.a51394.nevazio.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: ShoppingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val totalItems = uiState.itemsToBuy.size + uiState.itemsBought.size

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SuccessGreen)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        "Lista de Compras",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 26.sp
                    )
                    Text(
                        "Partilhada com a família · $totalItems itens",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                // + button top right
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Add, null, tint = Color.White)
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(Modifier.height(16.dp)) }

            // "Por Comprar" section
            item {
                Text(
                    "POR COMPRAR",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.outline,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            items(uiState.itemsToBuy) { item ->
                ShoppingItemCard(item = item, isDone = false)
            }

            item { Spacer(Modifier.height(8.dp)) }

            // "Comprado" section
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        "COMPRADO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.outline,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Default.Check,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = SuccessGreen
                    )
                }
            }

            items(uiState.itemsBought) { item ->
                ShoppingItemCard(item = item, isDone = true)
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun ShoppingItemCard(item: ShoppingItem, isDone: Boolean) {
    Surface(
        color = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f) else Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (isDone) 0.dp else 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isDone) 0.75f else 1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Checkbox
            if (isDone) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                )
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.name}  (${item.quantity})",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (isDone) TextDecoration.LineThrough else null,
                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person, null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isDone) "por ${item.addedBy}" else "adicionado por ${item.addedBy}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
