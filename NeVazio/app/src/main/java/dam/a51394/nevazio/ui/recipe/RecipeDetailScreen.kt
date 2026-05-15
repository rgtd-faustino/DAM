package dam.a51394.nevazio.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import dam.a51394.nevazio.ui.theme.SuccessGreen
import dam.a51394.nevazio.ui.theme.DarkGreen

@Composable
fun RecipeDetailScreen(
    viewModel: RecipeViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Hero image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    // Image placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFFF8E1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍳", fontSize = 80.sp)
                    }

                    // Back button overlay
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                            .align(Alignment.TopStart),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                        }
                    }
                }
            }

            // Title & chips
            item {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        uiState.title,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    // Chips row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Time chip
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = CircleShape
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(uiState.time, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        // Difficulty chip
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = CircleShape
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Restaurant, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(uiState.difficulty, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        // Vegetarian chip
                        if (uiState.isVegetarian) {
                            Surface(
                                color = SuccessGreen.copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Eco, null, modifier = Modifier.size(14.dp), tint = SuccessGreen)
                                    Text("Vegetariano", style = MaterialTheme.typography.labelMedium, color = SuccessGreen)
                                }
                            }
                        }
                    }
                }
            }

            // Ingredients section header
            item {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Kitchen, null, tint = SuccessGreen, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Ingredientes necessários",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Ingredient list
            itemsIndexed(uiState.ingredients) { _, (ingredient, isAvailable) ->
                RecipeIngredientItem(ingredient, isAvailable)
            }

            // Steps section header
            item {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FormatListNumbered, null, tint = SuccessGreen, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Passos",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Steps list
            itemsIndexed(uiState.steps) { index, step ->
                RecipeStepItem(
                    number = index + 1,
                    text = step,
                    isLast = index == uiState.steps.size - 1
                )
            }
        }

        // Bottom CTA
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 12.dp
        ) {
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "COMEÇAR A COZINHAR",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun RecipeIngredientItem(ingredient: Ingredient, isAvailable: Boolean) {
    val color = if (isAvailable) SuccessGreen else MaterialTheme.colorScheme.error
    val bgColor = if (isAvailable) Color.White else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)
    val iconBg = if (isAvailable) SuccessGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
    val iconTint = if (isAvailable) SuccessGreen else MaterialTheme.colorScheme.error

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = bgColor,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                val emoji = when (ingredient.iconName) {
                    "egg" -> "🥚"
                    "cheese" -> "🧀"
                    "water_drop" -> "💧"
                    "eco" -> "🥦"
                    else -> "🥗"
                }
                Text(emoji, fontSize = 18.sp)
            }
            Text(
                ingredient.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                ingredient.quantity,
                style = MaterialTheme.typography.labelMedium,
                color = if (isAvailable) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = if (isAvailable) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun RecipeStepItem(number: Int, text: String, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        // Number circle + line
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    number.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 24.dp)
                .weight(1f),
            lineHeight = 24.sp
        )
    }
}
