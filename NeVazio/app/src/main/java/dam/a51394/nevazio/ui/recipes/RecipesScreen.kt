package dam.a51394.nevazio.ui.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Schedule
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
import dam.a51394.nevazio.data.model.Recipe
import dam.a51394.nevazio.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    viewModel: RecipesViewModel,
    onNavigateToRecipeDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Receitas Sugeridas",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                            "Com base nos teus ingredientes atuais",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }
            items(uiState.recipes) { recipe ->
                RecipeCard(recipe = recipe, onClick = { onNavigateToRecipeDetail(recipe.id) })
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    val tagColors = mapOf(
        "Fácil" to Pair(Color(0xFFFFE0E0), Color(0xFFE53935)),
        "Vegetariano" to Pair(Color(0xFFE0F5E9), SuccessGreen),
        "Saudável" to Pair(Color(0xFFE0F5E9), SuccessGreen),
        "Médio" to Pair(Color(0xFFFFF3E0), Color(0xFFF57C00))
    )

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Image placeholder with match badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Food emoji placeholder
                val emoji = when (recipe.id) {
                    "1" -> "🍳"
                    "2" -> "🥣"
                    "3" -> "🍳"
                    else -> "🍽️"
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            when (recipe.id) {
                                "1" -> Color(0xFFFFF8E1)
                                "2" -> Color(0xFFE8F5E9)
                                else -> Color(0xFFFFF8E1)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 64.sp)
                }

                // Match badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    color = SuccessGreen,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Bolt, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text(
                            "${recipe.matchPercentage}% Match",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Recipe info
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    recipe.name,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold
                )

                // Tags row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Time chip
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = CircleShape
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${recipe.timeMinutes}min",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Other tags
                    recipe.tags.forEach { tag ->
                        val colors = tagColors[tag] ?: Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            color = colors.first,
                            shape = CircleShape
                        ) {
                            Text(
                                tag,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.second,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Bottom row: missing ingredients + button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Faltam ${recipe.missingIngredients} ingredientes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (recipe.missingIngredients == 0) {
                        Button(
                            onClick = onClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Ver Receita", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onClick,
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp)
                        ) {
                            Text(
                                "Ver Receita",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }
                }
            }
        }
    }
}
