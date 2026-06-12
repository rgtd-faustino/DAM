package dam.a51394.nevazio.ui.scan

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

// O modelo genérico do ML Kit tem uma hierarquia de confiança invertida para comida:
// categorias pai como "Food" ou "Produce" chegam com 95%+, enquanto rótulos específicos
// como "Lettuce" ou "Egg" raramente ultrapassam 65-70%. Um threshold único alto elimina
// exactamente os itens que interessam. A whitelist resolve isto ao aplicar um threshold
// mais baixo (55%) especificamente para os alimentos que o sistema conhece — cobre
// frigorífico e despensa, singular e plural, para não perder deteções por variação gramatical.
private val KNOWN_FOOD_LABELS = setOf(
    // --- Lácteos e ovos ---
    "egg", "eggs",
    "milk", "whole milk", "skim milk", "skimmed milk",
    "cheese", "cheddar", "mozzarella", "parmesan", "feta", "gouda", "brie", "ricotta",
    "cream cheese",
    "butter",
    "cream", "whipped cream", "sour cream", "heavy cream",
    "yogurt", "yoghurt", "greek yogurt",
    // --- Carnes e aves ---
    "chicken", "chicken breast", "chicken leg", "chicken wing", "chicken thigh",
    "beef", "ground beef", "steak",
    "pork", "pork chop",
    "lamb",
    "turkey",
    "duck",
    "veal",
    "ham", "prosciutto", "salami", "pepperoni",
    "bacon",
    "sausage", "sausages", "chorizo", "hot dog",
    // --- Peixe e marisco ---
    "fish",
    "salmon",
    "tuna",
    "cod", "codfish",
    "sardine", "sardines",
    "mackerel",
    "shrimp", "shrimps",
    "prawn", "prawns",
    "squid",
    "octopus",
    "crab",
    "mussel", "mussels",
    "clam", "clams",
    // --- Legumes ---
    "tomato", "tomatoes", "cherry tomato", "cherry tomatoes",
    "potato", "potatoes", "sweet potato", "sweet potatoes",
    "onion", "onions", "red onion",
    "garlic", "garlic clove",
    "carrot", "carrots",
    "lettuce", "iceberg lettuce", "romaine lettuce",
    "broccoli",
    "spinach",
    "cucumber", "cucumbers",
    "zucchini", "courgette",
    "eggplant", "aubergine",
    "mushroom", "mushrooms", "button mushroom",
    "bell pepper", "red pepper", "green pepper", "yellow pepper",
    "corn", "sweetcorn",
    "peas", "green peas",
    "green bean", "green beans",
    "asparagus",
    "celery",
    "leek", "leeks",
    "cauliflower",
    "cabbage",
    "kale",
    "artichoke",
    "beetroot", "beet",
    "radish", "radishes",
    "turnip",
    "pumpkin",
    "squash",
    "fennel",
    "ginger",
    "chili", "chilli", "chilli pepper",
    "avocado",
    "olive", "olives",
    // --- Frutas ---
    "apple", "apples",
    "banana", "bananas",
    "orange", "oranges",
    "lemon", "lemons",
    "lime", "limes",
    "pear", "pears",
    "peach", "peaches",
    "grape", "grapes",
    "strawberry", "strawberries",
    "pineapple",
    "mango", "mangoes",
    "watermelon",
    "melon", "cantaloupe",
    "kiwi",
    "cherry", "cherries",
    "plum", "plums",
    "apricot", "apricots",
    "fig", "figs",
    "blueberry", "blueberries",
    "raspberry", "raspberries",
    "blackberry", "blackberries",
    "coconut",
    "pomegranate",
    "papaya",
    "passion fruit",
    // --- Cereais, massas e pão ---
    "bread", "white bread", "brown bread", "whole wheat bread", "sourdough",
    "baguette", "toast",
    "rice", "white rice", "brown rice",
    "pasta", "spaghetti", "penne", "fusilli", "tagliatelle", "lasagne",
    "noodle", "noodles",
    "flour", "wheat flour",
    "oat", "oats", "oatmeal",
    "cereal",
    "granola",
    "quinoa",
    "couscous",
    "tortilla",
    "cracker", "crackers",
    "biscuit", "biscuits",
    // --- Leguminosas e frutos secos ---
    "bean", "beans", "kidney bean", "black bean",
    "lentil", "lentils",
    "chickpea", "chickpeas",
    "soy", "tofu",
    "peanut", "peanuts",
    "almond", "almonds",
    "walnut", "walnuts",
    "cashew", "cashews",
    "hazelnut", "hazelnuts",
    "pistachio", "pistachios",
    "pecan", "pecans",
    "sunflower seed", "sunflower seeds",
    "chia", "flaxseed",
    "sesame",
    // --- Condimentos, molhos e temperos ---
    "salt",
    "pepper", "black pepper",
    "sugar", "brown sugar",
    "honey",
    "oil", "olive oil", "vegetable oil", "sunflower oil",
    "vinegar", "apple cider vinegar",
    "sauce", "tomato sauce", "hot sauce", "soy sauce", "barbecue sauce", "barbeque sauce",
    "ketchup",
    "mustard",
    "mayonnaise",
    "pesto",
    "tomato paste",
    "jam", "strawberry jam",
    "marmalade",
    "peanut butter",
    "nutella", "chocolate spread",
    "syrup", "maple syrup",
    "tahini",
    "hummus",
    // --- Ervas e especiarias ---
    "basil",
    "parsley",
    "oregano",
    "thyme",
    "rosemary",
    "coriander", "cilantro",
    "mint",
    "dill",
    "bay leaf",
    "cinnamon",
    "paprika",
    "cumin",
    "turmeric",
    "nutmeg",
    "vanilla",
    // --- Padaria e confeitaria ---
    "chocolate", "dark chocolate", "milk chocolate", "white chocolate",
    "cocoa", "cocoa powder",
    "baking powder",
    "yeast",
    // --- Bebidas ---
    "coffee", "espresso",
    "tea",
    "juice", "orange juice", "apple juice",
    "wine", "red wine", "white wine",
    "beer",
    "water", "sparkling water",
    "bottle", "water bottle",
    "soda",
    // --- Pratos preparados e outros ---
    "egg whites", "egg yolk",
    "soup",
    "ice cream",
    "butter milk", "buttermilk"
)

// A blacklist existe porque o modelo sempre retorna categorias pai juntamente com os
// rótulos específicos — e fá-lo com confiança muito alta. Sem isto, a lista de candidatos
// ficaria dominada por entradas inúteis como "Food" (98%) ou "Produce" (92%) em vez de
// mostrar "Apple" (68%). Qualquer label de categoria adicionado à whitelist acidentalmente
// seria aqui travado como último recurso.
private val GENERIC_BLACKLIST = setOf(
    "food", "produce", "ingredient", "cuisine", "dish", "recipe",
    "plant", "liquid", "packaged goods", "plastic", "wood",
    "tableware", "table", "drinkware", "fast food", "junk food",
    "fruit", "vegetable", "natural foods", "staple food", "whole food",
    "convenience food", "superfood", "organic food", "raw food",
    "dairy", "dairy product", "condiment", "spice", "herb",
    "meat", "seafood", "grain", "legume", "nut", "seed"
)

@Composable
fun ScanScreen(onNavigateBack: (String?) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // O executor de thread única é necessário porque a câmara entrega frames em contínuo
    // e o ImageLabeler é bloqueante durante a análise. Sem esta separação, cada frame
    // bloquearia a main thread e a interface ficaria sem resposta durante os picos de análise.
    val cameraExecutor = remember { java.util.concurrent.Executors.newSingleThreadExecutor() }

    // Sem este cleanup, ao navegar para fora do ecrã de scan o executor continuava activo
    // em background, mantendo a thread da câmara viva e a consumir bateria indefinidamente.
    // O DisposableEffect garante que o shutdown acontece exactamente quando o composable
    // sai da árvore, independentemente de como a navegação foi desencadeada.
    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    // DEFAULT_OPTIONS usa o modelo base do ML Kit (MobileNet V2). Já foi considerado usar
    // um modelo customizado treinado especificamente em alimentos, mas isso exigiria treino
    // externo e hosting do modelo. A solução actual com whitelist+threshold diferenciado
    // resolve o problema sem complexidade adicional de infra-estrutura.
    val options = ImageLabelerOptions.DEFAULT_OPTIONS
    val imageLabeler = remember { ImageLabeling.getClient(options) }

    // Dois estados distintos para dois propósitos diferentes: currentCandidates representa
    // o que a câmara vê agora (transitório, muda a cada frame), enquanto confirmedItems
    // representa as decisões do utilizador (persistente durante a sessão de scan).
    // Esta separação existiu porque uma versão anterior adicionava tudo automaticamente,
    // o que gerava muitos itens espúrios capturados em meios frames de transição.
    val confirmedItems = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()
    var currentCandidates by remember { mutableStateOf<List<String>>(emptyList()) }

    // O LaunchedEffect em confirmedItems.size garante que a lista acompanha visualmente
    // cada item confirmado. Sem isto, ao confirmar o 10.º item ele ficaria fora do viewport
    // e o utilizador não teria feedback imediato de que foi adicionado com sucesso.
    LaunchedEffect(confirmedItems.size) {
        if (confirmedItems.isNotEmpty()) {
            listState.animateScrollToItem(confirmedItems.size - 1)
        }
    }

    Scaffold { innerPadding ->
        // A divisão por weight() em vez de altura fixa ou offset negativo resolve um problema
        // de layout que existia na versão anterior: o viewfinder ficava parcialmente coberto
        // pela lista de itens porque ambos eram posicionados em sobreposição com Box+align.
        // Separar em duas secções com pesos garante que a câmara ocupa sempre os 60% superiores
        // e os controlos ficam nos 40% inferiores, independentemente do tamanho do ecrã.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- SECÇÃO SUPERIOR: Câmara e Viewfinder (60% do ecrã) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.60f),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val imageAnalysis = ImageAnalysis.Builder()
                                    // STRATEGY_KEEP_ONLY_LATEST descarta frames intermédios quando o
                                    // ImageLabeler não consegue acompanhar a cadência da câmara. Sem isto,
                                    // acumularia uma fila de frames por processar e a deteção ficaria
                                    // progressivamente mais atrasada em relação ao que o utilizador vê.
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also {
                                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                                            processImageProxy(imageLabeler, imageProxy) { newCandidates ->
                                                // currentCandidates é apenas actualizado, nunca acumulado.
                                                // O acúmulo de candidatos é responsabilidade do utilizador
                                                // via o botão Confirmar — este callback é só o "agora".
                                                currentCandidates = newCandidates
                                            }
                                        }
                                    }

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageAnalysis
                                    )
                                } catch (e: Exception) {
                                    // A excepção de binding pode ocorrer em dispositivos que não
                                    // têm câmara traseira ou onde outra app tem acesso exclusivo.
                                    // Ignorar aqui é seguro porque o estado hasCameraPermission
                                    // já trata o caso de câmara indisponível na UI.
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        }
                    )

                    // O texto de estado serve de feedback visual imediato entre frames.
                    // O utilizador não tem nenhuma indicação de que a análise está activa
                    // a não ser por este texto — sem ele, a câmara parece estar parada.
                    // Quando há candidatos, mostra o nome do melhor resultado em vez de
                    // "a analisar", para preparar o utilizador antes de clicar Confirmar.
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (currentCandidates.isNotEmpty())
                                    "✓ ${currentCandidates.first()} detetado"
                                else
                                    "A analisar ambiente...",
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // O viewfinder é puramente decorativo — não afecta a área analisada
                        // pelo ML Kit, que processa o frame completo independentemente do que
                        // está dentro do rectângulo. Existe exclusivamente para guiar o utilizador
                        // a centrar o alimento e criar a sensação de um scanner activo.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .aspectRatio(1f)
                                .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        )
                    }
                } else {
                    // Este ramo existe para o caso de a permissão ter sido negada de forma
                    // permanente — nessa situação o sistema Android não volta a perguntar,
                    // e sem este fallback explícito a interface ficaria bloqueada num ecrã
                    // de câmara vazio sem qualquer caminho de saída.
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("É necessária permissão de câmara para fazer o scan.", textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        // ACTION_APPLICATION_DETAILS_SETTINGS é a única forma fiável de
                        // levar o utilizador directamente à página de permissões da app,
                        // sem depender de que a permissão CAMERA possa ser re-solicitada
                        // (o que o Android bloqueia após 1 recusa com "Não voltar a perguntar").
                        Button(onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }) {
                            Text("Abrir Definições")
                        }
                    }
                }
            }

            // --- SECÇÃO INFERIOR: Controlos e lista de confirmados (40% do ecrã) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.40f)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // O card de candidato só aparece quando há algo detetado no frame actual.
                // A confirmação é manual para evitar que um alimento capturado durante
                // meio segundo de transição acabe na lista sem intenção do utilizador.
                // A versão anterior adicionava automaticamente e gerava listas com itens
                // como "Rectangle" ou "Plastic" que o filtro ainda não apanhava a tempo.
                if (currentCandidates.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Detetado agora:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    currentCandidates.first(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (currentCandidates.size > 1) {
                                    Text(
                                        "também: ${currentCandidates.drop(1).joinToString(", ")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    val top = currentCandidates.first()
                                    // Verifica duplicado antes de adicionar porque o utilizador pode
                                    // confirmar o mesmo alimento várias vezes enquanto a câmara oscila.
                                    // O inglês original é guardado aqui — a tradução acontece
                                    // no HomeViewModel.showAddSheet quando o item é seleccionado.
                                    if (!confirmedItems.contains(top)) {
                                        confirmedItems.add(top)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Confirmar")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Confirmar")
                            }
                        }
                    }
                }

                // Lista dos itens confirmados nesta sessão de scan. O toque num item
                // dispara onNavigateBack com o nome em inglês, que o HomeViewModel
                // intercepta, traduz para português e injeta no formulário de adição.
                // Cada item tem botão de remoção para corrigir confirmações acidentais
                // sem ter de sair e voltar ao scan.
                if (confirmedItems.isNotEmpty()) {
                    Text(
                        "Confirmados (${confirmedItems.size}) — Toca para adicionar ao frigorífico:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .simpleVerticalScrollbar(listState)
                    ) {
                        items(confirmedItems) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateBack(item) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = dam.a51394.nevazio.ui.theme.SuccessGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { confirmedItems.remove(item) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remover",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Divider()
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Aponta a câmara para um alimento\ne prime Confirmar para o adicionar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Botão de Voltar sempre visível no fundo.
                OutlinedButton(
                    onClick = { onNavigateBack(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voltar sem adicionar")
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(
    detector: ImageLabeler,
    imageProxy: ImageProxy,
    onLabelsDetected: (List<String>) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        detector.process(image)
            .addOnSuccessListener { detectedLabels ->
                // A filtragem em duas camadas resolve o problema central deste ecrã:
                // o modelo genérico retorna "Food" com 98% e "Lettuce" com 65%.
                // Um threshold único alto eliminaria os alimentos específicos que interessam.
                // A solução é tratar os alimentos conhecidos com tolerância baixa (55%)
                // e exigir 80% para todo o resto — evitando ao mesmo tempo os labels
                // genéricos que viveriam facilmente acima de 80%.
                val knownFoods = detectedLabels
                    .filter { KNOWN_FOOD_LABELS.contains(it.text.lowercase()) }
                    .filter { it.confidence > 0.55f }
                    .map { it.text }

                val otherSpecifics = detectedLabels
                    .filter { !KNOWN_FOOD_LABELS.contains(it.text.lowercase()) }
                    .filter { it.confidence > 0.80f }
                    .filter { !GENERIC_BLACKLIST.contains(it.text.lowercase()) }
                    .map { it.text }

                val validLabels = (knownFoods + otherSpecifics).distinct()

                if (validLabels.isNotEmpty()) {
                    onLabelsDetected(validLabels)
                }
            }
            .addOnFailureListener {
                // A análise de um frame falha silenciosamente — o imageProxy é sempre
                // fechado no addOnCompleteListener, por isso não há risco de leak mesmo aqui.
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

// A LazyColumn nativa do Compose não tem scrollbar integrada.
// Esta extensão desenha uma barra proporcional por cima do conteúdo usando drawWithContent,
// calculando posição e altura a partir do layoutInfo do estado da lista.
// A barra só aparece quando há mais itens do que os visíveis — quando cabe tudo no viewport
// não aparece nada, o que é o comportamento esperado e não um bug.
fun Modifier.simpleVerticalScrollbar(
    state: androidx.compose.foundation.lazy.LazyListState,
    width: Dp = 4.dp
): Modifier = this.drawWithContent {
    drawContent()
    val firstVisibleIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: return@drawWithContent
    val needScrollbar = state.layoutInfo.visibleItemsInfo.size < state.layoutInfo.totalItemsCount
    if (needScrollbar) {
        val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
        val scrollbarOffsetY = firstVisibleIndex * elementHeight
        val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight
        drawRect(
            color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.5f),
            topLeft = androidx.compose.ui.geometry.Offset(this.size.width - width.toPx(), scrollbarOffsetY),
            size = Size(width.toPx(), scrollbarHeight)
        )
    }
}
