package dam.a51394.nevazio.ui.home

import androidx.lifecycle.ViewModel
import dam.a51394.nevazio.data.model.ExpiryStatus
import dam.a51394.nevazio.data.model.Ingredient
import dam.a51394.nevazio.data.model.StorageLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

import androidx.lifecycle.viewModelScope
import dam.a51394.nevazio.data.repository.AuthRepository
import dam.a51394.nevazio.data.repository.FridgeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val fridgeRepository: FridgeRepository
) : ViewModel() {

    companion object {
        fun inferIconName(name: String): String {
            val n = name.lowercase()
            // Este mapeamento por substring em vez de igualdade exacta é intencional —
            // permite apanhar "Peito de Frango", "Frango Assado", etc. com a mesma regra.
            // O else final "food" serve de fallback universal para qualquer ingrediente
            // não mapeado, garantindo que o ícone nunca fica null ou vazio.
            return when {
                n.contains("ovo") || n.contains("egg") -> "egg"
                n.contains("queijo") || n.contains("cheese") -> "cheese"
                n.contains("leite") || n.contains("milk") || n.contains("água") || n.contains("agua") || n.contains("water") || n.contains("sumo") || n.contains("juice") -> "water_drop"
                n.contains("carne") || n.contains("frango") || n.contains("porco") || n.contains("vaca") || n.contains("peru") || n.contains("chicken") || n.contains("beef") || n.contains("pork") || n.contains("meat") || n.contains("salsich") || n.contains("fiambre") || n.contains("presunto") -> "restaurant"
                n.contains("peixe") || n.contains("fish") || n.contains("atum") || n.contains("salmão") || n.contains("salmon") || n.contains("bacalhau") || n.contains("camarão") -> "fish"
                n.contains("pão") || n.contains("bread") || n.contains("cereais") || n.contains("arroz") || n.contains("rice") || n.contains("massa") || n.contains("pasta") || n.contains("farinha") || n.contains("flour") -> "grain"
                n.contains("gelado") || n.contains("ice") -> "icecream"
                n.contains("tomate") || n.contains("alface") || n.contains("cenoura") || n.contains("cebola") || n.contains("alho") || n.contains("batata") || n.contains("pepino") || n.contains("courgette") || n.contains("brócolos") || n.contains("brócolo") || n.contains("espinafre") || n.contains("vegetable") || n.contains("legume") -> "eco"
                n.contains("maçã") || n.contains("banana") || n.contains("laranja") || n.contains("fruta") || n.contains("morango") || n.contains("uva") || n.contains("limão") || n.contains("fruit") -> "fruit"
                else -> "food"
            }
        }

        fun translateEnToPt(en: String): String {
            // Este método existe no companion object porque precisa de ser chamado
            // estaticamente a partir do HomeViewModel.showAddSheet sem depender de uma
            // instância do ViewModel. Mover isto para fora do companion exigiria injectar
            // o ViewModel no ScanScreen só para a tradução, o que quebra a separação de camadas.
            //
            // O mapa está sincronizado com KNOWN_FOOD_LABELS no ScanScreen.kt.
            // Se um label for adicionado à whitelist de deteção sem ser adicionado aqui,
            // o ingrediente aparecerá em inglês no formulário, o que é confuso para o utilizador.
            // Os dois ficheiros têm de ser actualizados em conjunto sempre que a lista cresce.
            val map = mapOf(
                // --- Lácteos e ovos ---
                "egg" to "Ovo", "eggs" to "Ovo",
                "egg whites" to "Claras de Ovo", "egg yolk" to "Gema de Ovo",
                "milk" to "Leite", "whole milk" to "Leite Gordo",
                "skim milk" to "Leite Magro", "skimmed milk" to "Leite Magro",
                "buttermilk" to "Leitelho", "butter milk" to "Leitelho",
                "cheese" to "Queijo", "cheddar" to "Queijo Cheddar",
                "mozzarella" to "Mozzarella", "parmesan" to "Parmesão",
                "feta" to "Queijo Feta", "gouda" to "Queijo Gouda",
                "brie" to "Queijo Brie", "ricotta" to "Ricotta",
                "cream cheese" to "Queijo Creme",
                "butter" to "Manteiga",
                "cream" to "Natas", "whipped cream" to "Natas Batidas",
                "sour cream" to "Natas Ácidas", "heavy cream" to "Natas Frescas",
                "yogurt" to "Iogurte", "yoghurt" to "Iogurte",
                "greek yogurt" to "Iogurte Grego",
                // --- Carnes e aves ---
                "chicken" to "Frango", "chicken breast" to "Peito de Frango",
                "chicken leg" to "Perna de Frango", "chicken wing" to "Asa de Frango",
                "chicken thigh" to "Coxa de Frango",
                "beef" to "Carne de Vaca", "ground beef" to "Carne Picada",
                "steak" to "Bife",
                "pork" to "Carne de Porco", "pork chop" to "Costeleta de Porco",
                "lamb" to "Borrego",
                "turkey" to "Peru",
                "duck" to "Pato",
                "veal" to "Vitela",
                "ham" to "Fiambre", "prosciutto" to "Presunto",
                "salami" to "Salame", "pepperoni" to "Pepperoni",
                "bacon" to "Bacon",
                "sausage" to "Salsicha", "sausages" to "Salsichas",
                "chorizo" to "Chouriço", "hot dog" to "Salsicha de Cachorro",
                // --- Peixe e marisco ---
                "fish" to "Peixe",
                "salmon" to "Salmão",
                "tuna" to "Atum",
                "cod" to "Bacalhau", "codfish" to "Bacalhau",
                "sardine" to "Sardinha", "sardines" to "Sardinhas",
                "mackerel" to "Cavala",
                "shrimp" to "Camarão", "shrimps" to "Camarão",
                "prawn" to "Camarão", "prawns" to "Camarão",
                "squid" to "Lula",
                "octopus" to "Polvo",
                "crab" to "Caranguejo",
                "mussel" to "Mexilhão", "mussels" to "Mexilhões",
                "clam" to "Amêijoa", "clams" to "Amêijoas",
                // --- Legumes ---
                "tomato" to "Tomate", "tomatoes" to "Tomates",
                "cherry tomato" to "Tomate Cherry", "cherry tomatoes" to "Tomates Cherry",
                "potato" to "Batata", "potatoes" to "Batatas",
                "sweet potato" to "Batata Doce", "sweet potatoes" to "Batatas Doces",
                "onion" to "Cebola", "onions" to "Cebolas", "red onion" to "Cebola Roxa",
                "garlic" to "Alho", "garlic clove" to "Dente de Alho",
                "carrot" to "Cenoura", "carrots" to "Cenouras",
                "lettuce" to "Alface", "iceberg lettuce" to "Alface Iceberg",
                "romaine lettuce" to "Alface Romana",
                "broccoli" to "Brócolos",
                "spinach" to "Espinafres",
                "cucumber" to "Pepino", "cucumbers" to "Pepinos",
                "zucchini" to "Courgette", "courgette" to "Courgette",
                "eggplant" to "Beringela", "aubergine" to "Beringela",
                "mushroom" to "Cogumelo", "mushrooms" to "Cogumelos",
                "button mushroom" to "Cogumelo Branco",
                "bell pepper" to "Pimento", "red pepper" to "Pimento Vermelho",
                "green pepper" to "Pimento Verde", "yellow pepper" to "Pimento Amarelo",
                "corn" to "Milho", "sweetcorn" to "Milho Doce",
                "peas" to "Ervilhas", "green peas" to "Ervilhas Verdes",
                "green bean" to "Feijão Verde", "green beans" to "Feijão Verde",
                "asparagus" to "Espargos",
                "celery" to "Aipo",
                "leek" to "Alho-Francês", "leeks" to "Alho-Francês",
                "cauliflower" to "Couve-Flor",
                "cabbage" to "Couve",
                "kale" to "Couve Galega",
                "artichoke" to "Alcachofra",
                "beetroot" to "Beterraba", "beet" to "Beterraba",
                "radish" to "Rábano", "radishes" to "Rábanos",
                "turnip" to "Nabo",
                "pumpkin" to "Abóbora",
                "squash" to "Abóbora",
                "fennel" to "Funcho",
                "ginger" to "Gengibre",
                "chili" to "Malagueta", "chilli" to "Malagueta",
                "chilli pepper" to "Malagueta",
                "avocado" to "Abacate",
                "olive" to "Azeitona", "olives" to "Azeitonas",
                // --- Frutas ---
                "apple" to "Maçã", "apples" to "Maçãs",
                "banana" to "Banana", "bananas" to "Bananas",
                "orange" to "Laranja", "oranges" to "Laranjas",
                "lemon" to "Limão", "lemons" to "Limões",
                "lime" to "Lima", "limes" to "Limas",
                "pear" to "Pêra", "pears" to "Pêras",
                "peach" to "Pêssego", "peaches" to "Pêssegos",
                "grape" to "Uva", "grapes" to "Uvas",
                "strawberry" to "Morango", "strawberries" to "Morangos",
                "pineapple" to "Ananás",
                "mango" to "Manga", "mangoes" to "Mangas",
                "watermelon" to "Melancia",
                "melon" to "Melão", "cantaloupe" to "Melão",
                "kiwi" to "Kiwi",
                "cherry" to "Cereja", "cherries" to "Cerejas",
                "plum" to "Ameixa", "plums" to "Ameixas",
                "apricot" to "Alperce", "apricots" to "Alperces",
                "fig" to "Figo", "figs" to "Figos",
                "blueberry" to "Mirtilo", "blueberries" to "Mirtilos",
                "raspberry" to "Framboesa", "raspberries" to "Framboesas",
                "blackberry" to "Amora", "blackberries" to "Amoras",
                "coconut" to "Coco",
                "pomegranate" to "Romã",
                "papaya" to "Papaia",
                "passion fruit" to "Maracujá",
                // --- Cereais, massas e pão ---
                "bread" to "Pão", "white bread" to "Pão Branco",
                "brown bread" to "Pão Escuro", "whole wheat bread" to "Pão Integral",
                "sourdough" to "Pão de Fermentação Lenta", "baguette" to "Baguete",
                "toast" to "Torrada",
                "rice" to "Arroz", "white rice" to "Arroz Branco",
                "brown rice" to "Arroz Integral",
                "pasta" to "Massa", "spaghetti" to "Esparguete",
                "penne" to "Penne", "fusilli" to "Fusilli",
                "tagliatelle" to "Tagliatelle", "lasagne" to "Lasanha",
                "noodle" to "Noodle", "noodles" to "Noodles",
                "flour" to "Farinha", "wheat flour" to "Farinha de Trigo",
                "oat" to "Aveia", "oats" to "Aveia", "oatmeal" to "Papas de Aveia",
                "cereal" to "Cereais",
                "granola" to "Granola",
                "quinoa" to "Quinoa",
                "couscous" to "Cuscuz",
                "tortilla" to "Tortilha",
                "cracker" to "Bolacha", "crackers" to "Bolachas",
                "biscuit" to "Bolacha", "biscuits" to "Bolachas",
                // --- Leguminosas e frutos secos ---
                "bean" to "Feijão", "beans" to "Feijão",
                "kidney bean" to "Feijão Vermelho", "black bean" to "Feijão Preto",
                "lentil" to "Lentilha", "lentils" to "Lentilhas",
                "chickpea" to "Grão de Bico", "chickpeas" to "Grão de Bico",
                "soy" to "Soja", "tofu" to "Tofu",
                "peanut" to "Amendoim", "peanuts" to "Amendoins",
                "almond" to "Amêndoa", "almonds" to "Amêndoas",
                "walnut" to "Noz", "walnuts" to "Nozes",
                "cashew" to "Caju", "cashews" to "Cajus",
                "hazelnut" to "Avelã", "hazelnuts" to "Avelãs",
                "pistachio" to "Pistácio", "pistachios" to "Pistácios",
                "pecan" to "Pecã", "pecans" to "Pecãs",
                "sunflower seed" to "Semente de Girassol",
                "sunflower seeds" to "Sementes de Girassol",
                "chia" to "Chia", "flaxseed" to "Linhaça",
                "sesame" to "Sésamo",
                // --- Condimentos, molhos e temperos ---
                "salt" to "Sal",
                "pepper" to "Pimenta", "black pepper" to "Pimenta Preta",
                "sugar" to "Açúcar", "brown sugar" to "Açúcar Amarelo",
                "honey" to "Mel",
                "oil" to "Óleo", "olive oil" to "Azeite",
                "vegetable oil" to "Óleo Vegetal", "sunflower oil" to "Óleo de Girassol",
                "vinegar" to "Vinagre", "apple cider vinegar" to "Vinagre de Maçã",
                "sauce" to "Molho", "tomato sauce" to "Molho de Tomate",
                "hot sauce" to "Molho Picante", "soy sauce" to "Molho de Soja",
                "barbecue sauce" to "Molho Barbecue", "barbeque sauce" to "Molho Barbecue",
                "ketchup" to "Ketchup",
                "mustard" to "Mostarda",
                "mayonnaise" to "Maionese",
                "pesto" to "Pesto",
                "tomato paste" to "Concentrado de Tomate",
                "jam" to "Compota", "strawberry jam" to "Compota de Morango",
                "marmalade" to "Marmelada",
                "peanut butter" to "Manteiga de Amendoim",
                "nutella" to "Nutella", "chocolate spread" to "Creme de Chocolate",
                "syrup" to "Xarope", "maple syrup" to "Xarope de Ácer",
                "tahini" to "Tahini",
                "hummus" to "Hummus",
                // --- Ervas aromáticas e especiarias ---
                "basil" to "Manjericão",
                "parsley" to "Salsa",
                "oregano" to "Orégãos",
                "thyme" to "Tomilho",
                "rosemary" to "Alecrim",
                "coriander" to "Coentros", "cilantro" to "Coentros",
                "mint" to "Hortelã",
                "dill" to "Aneto",
                "bay leaf" to "Folha de Louro",
                "cinnamon" to "Canela",
                "paprika" to "Paprika",
                "cumin" to "Cominhos",
                "turmeric" to "Cúrcuma",
                "nutmeg" to "Noz Moscada",
                "vanilla" to "Baunilha",
                // --- Padaria e confeitaria ---
                "chocolate" to "Chocolate", "dark chocolate" to "Chocolate Negro",
                "milk chocolate" to "Chocolate de Leite", "white chocolate" to "Chocolate Branco",
                "cocoa" to "Cacau", "cocoa powder" to "Cacau em Pó",
                "baking powder" to "Fermento em Pó",
                "yeast" to "Levedura",
                "ice cream" to "Gelado",
                // --- Bebidas ---
                "coffee" to "Café", "espresso" to "Expresso",
                "tea" to "Chá",
                "juice" to "Sumo", "orange juice" to "Sumo de Laranja",
                "apple juice" to "Sumo de Maçã",
                "wine" to "Vinho", "red wine" to "Vinho Tinto",
                "white wine" to "Vinho Branco",
                "beer" to "Cerveja",
                "water" to "Água", "sparkling water" to "Água com Gás",
                "bottle" to "Garrafa", "water bottle" to "Garrafa de Água",
                "soda" to "Refrigerante",
                "soup" to "Sopa"
            )
            val lowerEn = en.lowercase().trim()
            // O fallback capitaliza o termo inglês quando não há tradução no mapa.
            // Isto garante que mesmo um label desconhecido aparece apresentável no formulário
            // em vez de surgir em lowercase como o ML Kit o devolveu (ex: "kale" -> "Kale").
            return map[lowerEn] ?: en.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
        }
    }
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allIngredients = emptyList<Ingredient>()
    // O fridgeId é resolvido assincronamente no loadData (via AuthRepository) e pode mudar
    // se o utilizador pertencer a uma família. "default_fridge" serve de fallback temporário
    // para que o estado inicial não fique com uma string vazia enquanto a Firestore responde.
    private var currentFridgeId: String = "default_fridge"

    init {
        loadData()
    }

    private fun loadData() {
        val user = authRepository.currentUser
        val email = user?.email ?: "Guest"
        _uiState.update { it.copy(userName = email.split("@").firstOrNull() ?: "Guest") }

        // A pesquisa do nome real na Firestore substitui o valor de fallback derivado do e-mail.
        // A operação é assíncrona e não bloqueia a renderização inicial da interface.
        refreshUserProfile()

        viewModelScope.launch {
            if (user != null) {
                // A resolução do fridgeId é delegada ao AuthRepository porque a lógica de
                // determinar se o utilizador pertence a uma família (e qual é o id do seu
                // frigorífico partilhado) não é responsabilidade do ViewModel. Se esta lógica
                // fosse duplicada aqui, qualquer alteração ao modelo de dados das famílias
                // exigiria mudar dois sítios.
                currentFridgeId = authRepository.getCurrentFridgeId()
            }
            
            // collectLatest em vez de collect é crítico aqui: a Firestore pode emitir
            // várias actualizações em rápida sucessão (ex: batch write, sync inicial).
            // Com collect, cada emissão seria processada completamente, potencialmente
            // causando recriações desnecessárias da UI. O collectLatest cancela o bloco
            // anterior se chegar uma nova emissão antes de o anterior terminar, mantendo
            // apenas o processamento mais recente activo.
            fridgeRepository.getIngredients(currentFridgeId).collectLatest { ingredients ->
                allIngredients = ingredients
                updateFilteredList()
                
                val expiringToday = ingredients.filter { it.status == ExpiryStatus.EXPIRES_TODAY }
                val warning = if (expiringToday.isNotEmpty()) {
                    "${expiringToday.first().name} expira${if (expiringToday.size > 1) "m" else ""} hoje! Usa-${if (expiringToday.size > 1) "os" else "o"} já."
                } else null
                
                _uiState.update {
                    it.copy(
                        ingredients = ingredients,
                        expiryWarning = warning,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refreshUserProfile() {
        val user = authRepository.currentUser ?: return
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("users").document(user.uid).get().await()
                val name = doc.getString("name")
                if (!name.isNullOrBlank()) {
                    _uiState.update { it.copy(userName = name) }
                }
            } catch (e: Exception) {
                // A pesquisa do nome real é uma melhoria opcional — se a Firestore estiver
                // indisponível ou o documento não existir ainda, o fallback do e-mail é suficiente
                // para não quebrar a experiência. Não faz sentido mostrar um erro ao utilizador
                // por não conseguir buscar o seu próprio nome de perfil.
            }
        }
    }

    private fun updateFilteredList() {
        val state = _uiState.value
        val location = if (state.selectedTab == 1) StorageLocation.PANTRY else StorageLocation.FRIDGE
        val filtered = if (state.selectedTab == 2) emptyList()
        else allIngredients.filter { it.location == location }
            .filter { if (state.searchQuery.isBlank()) true else it.name.contains(state.searchQuery, ignoreCase = true) }
            // O ordenamento coloca primeiro os ingredientes válidos ordenados por data de validade
            // (mais urgentes primeiro), depois os expirados (já não são consumíveis mas podem
            // ser removidos), e por último os sem data definida. Esta ordem foi escolhida porque
            // o caso de uso principal é o utilizador ver o que vai expirar brevemente — itens
            // sem data são tipicamente não-perecíveis e têm menos urgência de atenção.
            .sortedWith(
                compareBy<Ingredient> { it.expiryDate == null }
                    .thenBy { it.status == ExpiryStatus.EXPIRED }
                    .thenBy { it.expiryDate }
            )
        
        _uiState.update { it.copy(filteredIngredients = filtered) }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        updateFilteredList()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateFilteredList()
    }

    fun showAddSheet(initialName: String = "") {
        // A tradução é feita aqui e não no ScanScreen porque neste ponto o label inglês
        // já passou pelo filtro de qualidade e foi confirmado pelo utilizador. Traduzir
        // mais cedo complicaria a comparação de duplicados e a lógica de whitelist,
        // que trabalha com os termos originais do ML Kit.
        val translatedName = if (initialName.isNotBlank()) translateEnToPt(initialName) else ""
        
        // editingIngredient é explicitamente anulado para garantir que os modos "adicionar"
        // e "editar" são mutuamente exclusivos. Sem este reset, se um utilizador abrisse o
        // painel de edição e depois navegasse de volta com um scan, o estado de edição
        // persistia e o formulário mostrava os dados do ingrediente antigo em vez do novo.
        _uiState.update { 
            it.copy(
                showAddSheet = true, 
                initialAddName = translatedName, 
                editingIngredient = null 
            ) 
        }
    }
    fun hideAddSheet() = _uiState.update { it.copy(showAddSheet = false, initialAddName = "") }

    fun addIngredient(name: String, quantity: String, unit: String, location: StorageLocation, expiryMillis: Long?) {
        if (name.isBlank()) return
        
        val expiryDate = if (expiryMillis != null) Date(expiryMillis) else Date()
        
        val newIngredient = Ingredient(
            name = name,
            quantity = "$quantity$unit",
            expiryDate = expiryDate,
            location = location,
            iconName = inferIconName(name),
            status = ExpiryStatus.FRESH,
            expiryLabel = "Novo"
        )
        
        viewModelScope.launch {
            fridgeRepository.addIngredient(currentFridgeId, newIngredient)
            hideAddSheet()
        }
    }

    fun promptRemoveIngredient(ingredient: Ingredient) {
        _uiState.update { it.copy(ingredientToDelete = ingredient) }
    }

    fun hideDeletePrompt() {
        _uiState.update { it.copy(ingredientToDelete = null) }
    }

    fun confirmDelete(addToShoppingList: Boolean) {
        // A opção de adicionar à lista de compras ao eliminar existe porque o caso
        // de uso mais comum de remoção é o consumo do ingrediente, que implica que
        // vai ser preciso comprar mais. Este fluxo evita que o utilizador tenha de
        // navegar manualmente para o ecrã de compras a seguir à remoção.
        val ingredient = _uiState.value.ingredientToDelete ?: return
        viewModelScope.launch {
            fridgeRepository.removeIngredient(currentFridgeId, ingredient.id)
            if (addToShoppingList) {
                val itemId = java.util.UUID.randomUUID().toString()
                val shoppingItem = mapOf(
                    "id" to itemId,
                    "name" to ingredient.name,
                    "bought" to false,
                    "quantity" to ingredient.quantity.filter { it.isDigit() || it == '.' },
                    "unit" to ingredient.quantity.filter { it.isLetter() }.ifBlank { "un" },
                    "location" to ingredient.location.name,
                    "addedBy" to (_uiState.value.userName),
                    "addedAt" to Date()
                )
                try {
                    fridgeRepository.addShoppingItemAsMap(currentFridgeId, itemId, shoppingItem)
                } catch (e: Exception) {
                    // A escrita na lista de compras é uma operação secundária — a remoção
                    // do ingrediente já aconteceu e não pode ser revertida daqui. Falhar
                    // silenciosamente é preferível a mostrar um erro por uma funcionalidade
                    // opcional que o utilizador pode nem ter pedido.
                }
            }
            hideDeletePrompt()
        }
    }

    fun showEditSheet(ingredient: Ingredient) = _uiState.update { it.copy(editingIngredient = ingredient) }
    fun hideEditSheet() = _uiState.update { it.copy(editingIngredient = null) }

    fun updateIngredient(id: String, name: String, quantity: String, unit: String, location: StorageLocation, expiryMillis: Long?) {
        val expiryDate = if (expiryMillis != null) Date(expiryMillis) else Date()
        viewModelScope.launch {
            try {
                fridgeRepository.updateIngredient(
                    currentFridgeId, 
                    id, 
                    mapOf(
                        "name" to name,
                        "quantity" to "$quantity$unit",
                        "location" to location.name,
                        "expiryDate" to expiryDate,
                        "iconName" to inferIconName(name)
                    )
                )
            } catch (e: Exception) {
                // A actualização pode falhar se a Firestore estiver offline ou por
                // problemas de permissões. O hideEditSheet() corre sempre para que
                // o utilizador não fique preso no formulário, mesmo que a actualização
                // não tenha sido persistida — na prática o dado volta ao valor anterior
                // assim que a Firestore sincronizar de novo.
            }
            hideEditSheet()
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
