package dam.a51394.nevazio.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.a51394.nevazio.data.model.StorageLocation
import dam.a51394.nevazio.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientSheet(
    onDismiss: () -> Unit,
    onAdd: (name: String, quantity: String, unit: String, location: StorageLocation) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("0") }
    var selectedUnit by remember { mutableStateOf("un") }
    var expiryDate by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf(StorageLocation.FRIDGE) }
    var unitExpanded by remember { mutableStateOf(false) }

    val units = listOf("un", "kg", "g", "L", "ml", "dz")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF8FBF8),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .background(Color(0xFFCCCCCC), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Adicionar Ingrediente",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold
            )

            // Nome
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Nome do ingrediente", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Ex: Leite, Maçã...", color = MaterialTheme.colorScheme.outline) },
                    leadingIcon = { Icon(Icons.Default.QrCodeScanner, null, tint = MaterialTheme.colorScheme.outline) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = SuccessGreen,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
            }

            // Quantidade + Unidade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Quantidade", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = SuccessGreen,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Unidade", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedUnit,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = SuccessGreen,
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        selectedUnit = unit
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Data de validade
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Data de validade", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    placeholder = { Text("mm/dd/yyyy", color = MaterialTheme.colorScheme.outline) },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.outline) },
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.outline) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = SuccessGreen,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
            }

            // Localização
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Localização", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Frigorífico tab
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = if (selectedLocation == StorageLocation.FRIDGE) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(10.dp),
                        shadowElevation = if (selectedLocation == StorageLocation.FRIDGE) 2.dp else 0.dp,
                        onClick = { selectedLocation = StorageLocation.FRIDGE }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Kitchen, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Frigorífico", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                    }
                    // Despensa tab
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = if (selectedLocation == StorageLocation.PANTRY) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(10.dp),
                        shadowElevation = if (selectedLocation == StorageLocation.PANTRY) 2.dp else 0.dp,
                        onClick = { selectedLocation = StorageLocation.PANTRY }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Storage, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Despensa", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ADICIONAR button
            Button(
                onClick = { onAdd(name, quantity, selectedUnit, selectedLocation) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    "ADICIONAR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            // Cancelar
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        }
    }
}
