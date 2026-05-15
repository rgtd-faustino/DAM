package tasks

import contributors.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) = coroutineScope {

    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    // criamos um channel que serve como uma fila de mensagens entre as coroutines de cada repo e a coroutine que
    // atualiza a UI ao contrário do CONCURRENT que aguardava que todos os repos terminassem com awaitAll(), assim
    // cada repo envia os seus resultados assim que termina
    val channel = Channel<List<User>>()

    // disparamos uma coroutine por cada repo como no concurrent, a diferença é que em vez de guardar um Deferred
    // para fazer awaitAll() no fim, cada coroutine envia o resultado direto para o channel assim que termina
    for (repo in repos) {
        launch {
            val users = service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
            // se o channel estiver ocupado esta coroutine fica suspensa até poder enviar
            channel.send(users)
        }
    }

    var allUsers = emptyList<User>()

    // ao contrário do progress que era sequencial aqui recebemos os resultados pela ordem em que chegam, ou seja,
    // o repo mais rápido a responder é o primeiro a ser processado
    // o receive() suspende a coroutine enquanto o channel está vazio para libertar a thread para outras tarefas
    repeat(repos.size) {
        val users = channel.receive()
        allUsers = (allUsers + users).aggregate()
        // Atualizamos a UI com os resultados acumulados.
        // O segundo argumento é true apenas quando recebemos o último repo.
        updateResults(allUsers, it == repos.lastIndex)
    }
}