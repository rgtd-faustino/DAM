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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.ui.tooling.preview.Preview
import dam.a51394.nevazio.ui.theme.NeVazioTheme

import dam.a51394.nevazio.ui.home.AddIngredientSheet
import dam.a51394.nevazio.data.model.StorageLocation
import androidx.compose.runtime.*

@Composable
fun ShoppingListScreen(viewModel: ShoppingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val totalItems = uiState.itemsToBuy.size + uiState.itemsBought.size

    // ── Add new item sheet ───────────────────────────────────────────────
    if (uiState.showAddDialog) {
        AddIngredientSheet(
            onDismiss = viewModel::hideAddDialog,
            title = "Adicionar à Lista",
            buttonText = "ADICIONAR À LISTA",
            onAdd = { name, quantity, unit, location, expiryMillis ->
                viewModel.addItem(name, quantity, unit, location, expiryMillis)
            }
        )
    }

    // ── Edit existing item sheet ────────────────────────────────────────
    if (uiState.editingItem != null) {
        val item = uiState.editingItem!!
        AddIngredientSheet(
            onDismiss = viewModel::hideEditSheet,
            initialName = item.name,
            initialQuantity = item.quantity,
            initialUnit = item.unit,
            initialLocation = item.location,
            initialExpiryMillis = item.expiryDate?.time,
            title = "Editar Item",
            buttonText = "GUARDAR ALTERAÇÕES",
            onAdd = { name, quantity, unit, location, expiryMillis ->
                viewModel.updateItem(name, quantity, unit, location, expiryMillis)
            }
        )
    }

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
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::showAddDialog,
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
        if (totalItems == 0) {
            // A reutilização do componente EmptyState do HomeScreen assegura consistência visual 
            // transversal à aplicação. A prevenção de ecrãs em branco evita a perceção de falha ou 
            // bloqueio no carregamento de dados. A inclusão de um estado vazio com um "Call to Action"
            // (Adicionar) orienta o comportamento e melhora a experiência de utilização.
            dam.a51394.nevazio.ui.home.EmptyState(
                onAddClick = viewModel::showAddDialog,
                modifier = Modifier.padding(padding)
            )
        } else {
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

                items(uiState.itemsToBuy, key = { it.id }) { item ->
                    ShoppingItemCard(
                        item = item,
                        isDone = false,
                        onCheckboxToggle = { viewModel.markAsBought(item) },
                        onCardClick = { viewModel.showEditSheet(item) },
                        onDelete = { viewModel.deleteItem(item) }
                    )
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

                items(uiState.itemsBought, key = { it.id }) { item ->
                    ShoppingItemCard(
                        item = item,
                        isDone = true,
                        onCheckboxToggle = { viewModel.markAsNotBought(item) },
                        onCardClick = { }, // bought items don't open edit
                        onDelete = { viewModel.deleteItem(item) }
                    )
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun ShoppingItemCard(
    item: ShoppingItem,
    isDone: Boolean,
    onCheckboxToggle: () -> Unit,
    onCardClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onCardClick,
        enabled = !isDone,
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
            // Checkbox – separate click target
            // O isolamento do evento de clique do Checkbox (que despoleta a movimentação do item)
            // relativamente ao clique no Cartão inteiro (que aciona a edição) previne ações indesejadas.
            // Esta separação garante que o utilizador não abre acidentalmente a janela de edição 
            // quando a intenção é apenas sinalizar o item como comprado.
            Checkbox(
                checked = isDone,
                onCheckedChange = { onCheckboxToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SuccessGreen,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.name}  (${item.quantity}${item.unit})",
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

            // Delete button – always visible
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShoppingItemCardPreview() {
    NeVazioTheme {
        ShoppingItemCard(
            item = ShoppingItem(id = "1", name = "Leite", quantity = "1", unit = "L", addedBy = "João"),
            isDone = false,
            onCheckboxToggle = {},
            onCardClick = {},
            onDelete = {}
        )
    }
}
