package contributors

import contributors.Contributors.LoadingStatus.*
import contributors.Variant.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import tasks.*
import java.awt.event.ActionListener
import javax.swing.SwingUtilities
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

enum class Variant {
    BLOCKING,         // Request1Blocking
    BACKGROUND,       // Request2Background
    CALLBACKS,        // Request3Callbacks
    SUSPEND,          // Request4Coroutine
    CONCURRENT,       // Request5Concurrent
    NOT_CANCELLABLE,  // Request6NotCancellable
    PROGRESS,         // Request6Progress
    CHANNELS          // Request7Channels
}

interface Contributors: CoroutineScope {

    val job: Job
    val loadingState: StateFlow<LoadingStateData>

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private fun calculateElapsedTime(startTime: Long): String {
        val time = System.currentTimeMillis() - startTime
        return "${(time / 1000)}.${time % 1000 / 100} sec"
    }

    fun updateLoadingStatus(newStatus: LoadingStateData)

    fun init() {
        // Start a new loading on 'load' click
        addLoadListener {
            saveParams()
            loadContributors()
        }

        // Save preferences and exit on closing the window
        addOnWindowClosingListener {
            job.cancel()
            saveParams()
            exitProcess(0)
        }

        // Load stored params (user & password values)
        loadInitialParams()
    }

    fun loadContributors() {
        val (username, password, org, _) = getParams()
        val req = RequestData(username, password, org)

        clearResults()
        val service = createGitHubService(req.username, req.password)

        val startTime = System.currentTimeMillis()
        when (getSelectedVariant()) {
            BLOCKING -> { // Blocking UI thread
                val users = loadContributorsBlocking(service, req)
                updateResults(users, startTime)
            }
            BACKGROUND -> { // Blocking a background thread
                loadContributorsBackground(service, req) { users ->
                    SwingUtilities.invokeLater {
                        updateResults(users, startTime)
                    }
                }
            }
            CALLBACKS -> { // Using callbacks
                loadContributorsCallbacks(service, req) { users ->
                    SwingUtilities.invokeLater {
                        updateResults(users, startTime)
                    }
                }
            }
            SUSPEND -> { // Using coroutines
                launch {
                    val users = loadContributorsSuspend(service, req)
                    updateResults(users, startTime)
                }.setUpCancellation()
            }
            CONCURRENT -> { // Performing requests concurrently
                launch {
                    val users = loadContributorsConcurrent(service, req)
                    updateResults(users, startTime)
                }.setUpCancellation()
            }
            NOT_CANCELLABLE -> { // Performing requests in a non-cancellable way
                launch {
                    val users = loadContributorsNotCancellable(service, req)
                    updateResults(users, startTime)
                }.setUpCancellation()
            }
            PROGRESS -> { // Showing progress
                launch(Dispatchers.Default) {
                    loadContributorsProgress(service, req) { users, completed ->
                        withContext(Dispatchers.Main) {
                            updateResults(users, startTime, completed)
                        }
                    }
                }.setUpCancellation()
            }
            CHANNELS -> {  // Performing requests concurrently and showing progress
                // o código antigo chamava updateResults diretamente no callback do loadContributorsChannels
                // o problema é que a UI podia não conseguir acompanhar a velocidade dos updates e não havia forma de
                // controlar isso, então agora metemos um canal no meio (progressChannel) que faz de buffer entre
                // quem produz os dados e quem atualiza a UI
                launch(Dispatchers.Default) {
                    // o Channel.BUFFERED permite que o produtor (loadContributorsChannels, é ele que apanha os dados
                    // através da API) continue a enviar dados mesmo que o consumidor (a UI que depois lê os valores)
                    // ainda esteja a processar o update anterior
                    val progressChannel = Channel<Pair<List<User>, Boolean>>(Channel.BUFFERED)

                    // esta coroutine carrega os dados e manda para o progressChannel, quando termina fecha o canal
                    // para que o for loop abaixo saiba que acabou
                    launch(Dispatchers.Default) {
                        loadContributorsChannels(service, req) { users, completed ->
                            progressChannel.send(Pair(users, completed))
                        }
                        // fechamos o canal para libertar recursos
                        progressChannel.close()
                    }

                    // esta parte lê do progressChannel e atualiza a UI, o for loop suspende automaticamente enquanto
                    // o canal está vazio e termina automaticamente quando o canal é fechado
                    for ((users, completed) in progressChannel) {
                        withContext(Dispatchers.Main) {
                            updateResults(users, startTime, completed)
                        }
                    }
                }.setUpCancellation()
            }
        }
    }

    // adicionámos o INIT para mostrar uma mensagem inicial antes de qualquer pedido ser feito
    private enum class LoadingStatus { INIT, COMPLETED, CANCELED, IN_PROGRESS }

    // em vez de passar strings e booleans separados para atualizar o estado, agrupamos
    // tudo numa data class para que cada emissão no StateFlow possa carregar toda a informação
    // necessária para a UI atualizar
    data class LoadingStateData (
        val status : LoadingStatus = LoadingStatus.INIT ,
        val startTime : Long ? = null ,
        val elapsedTime : String = ""
    )

    private fun clearResults() {
        updateContributors(listOf())
        updateLoadingStatus(LoadingStateData(status = IN_PROGRESS))
        setActionsStatus(newLoadingEnabled = false)
    }

    private fun updateResults(
        users: List<User>,
        startTime: Long,
        completed: Boolean = true
    ) {
        updateContributors(users)
        val status = if (completed) COMPLETED else IN_PROGRESS
        val elapsedTime = calculateElapsedTime(startTime)
        updateLoadingStatus(LoadingStateData(status = status, startTime = startTime, elapsedTime = elapsedTime))
        if (completed) {
            setActionsStatus(newLoadingEnabled = true)
        }
    }

    // este método foi substituído pelo novo updateLoadingStatus(LoadingStateData) que manda para o StateFlow em
    // vez de chamar o setLoadingStatus diretamente
    /*private fun updateLoadingStatus(
        status: LoadingStatus,
        startTime: Long? = null
    ) {
        val time = if (startTime != null) {
            val time = System.currentTimeMillis() - startTime
            "${(time / 1000)}.${time % 1000 / 100} sec"
        } else ""

        val text = "Loading status: " +
                when (status) {
                    COMPLETED -> "completed in $time"
                    IN_PROGRESS -> "in progress $time"
                    CANCELED -> "canceled"
                }
        setLoadingStatus(text, status == IN_PROGRESS)
    }*/

    private fun Job.setUpCancellation() {
        // make active the 'cancel' button
        setActionsStatus(newLoadingEnabled = false, cancellationEnabled = true)

        val loadingJob = this

        // cancel the loading job if the 'cancel' button was clicked
        val listener = ActionListener {
            loadingJob.cancel()
            // antes chamávamos setLoadingStatus diretamente com uma string, mas agora fazemos um LoadingStateData para
            // o StateFlow, assim  o coletor em observeLoadingStatus() atualiza a UI sempre que há alguma mudança
            updateLoadingStatus(LoadingStateData(status = CANCELED))
        }
        addCancelListener(listener)

        // update the status and remove the listener after the loading job is completed
        launch {
            loadingJob.join()
            setActionsStatus(newLoadingEnabled = true)
            removeCancelListener(listener)
        }
    }

    // este método é necessário na interface para que o ContributorsUI o possa sobrescrever
    // é aqui que iniciamos o "listener" do StateFlow para quando o estado muda a UI possa ser atualizada automaticamente
    // em vez de termos de chamar setLoadingStatus manualmente
    fun observeLoadingStatus()

    fun loadInitialParams() {
        setParams(loadStoredParams())
    }

    fun saveParams() {
        val params = getParams()
        if (params.username.isEmpty() && params.password.isEmpty()) {
            removeStoredParams()
        }
        else {
            saveParams(params)
        }
    }

    fun getSelectedVariant(): Variant

    fun updateContributors(users: List<User>)

    // setLoadingStatus foi removido da interface porque a UI é agora atualizada pelo StateFlow
    /*fun setLoadingStatus(text: String, iconRunning: Boolean)*/

    fun setActionsStatus(newLoadingEnabled: Boolean, cancellationEnabled: Boolean = false)

    fun addCancelListener(listener: ActionListener)

    fun removeCancelListener(listener: ActionListener)

    fun addLoadListener(listener: () -> Unit)

    fun addOnWindowClosingListener(listener: () -> Unit)

    fun setParams(params: Params)

    fun getParams(): Params
}
