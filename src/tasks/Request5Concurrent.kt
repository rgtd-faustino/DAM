package tasks

import contributors.*
import kotlinx.coroutines.*


suspend fun loadContributorsConcurrent(
    service: GitHubService,
    req: RequestData
): List<User> = coroutineScope {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val deferreds = repos.map { repo ->
        // a diferença para as suspend functions é que agora disparamos os pedidos de todos os repos
        // ao mesmo tempo com o async (cada repo tem a sua própria coroutine) em vez de esperar
        // pela resposta de cada repo antes de pedir o próximo
        async(Dispatchers.Default) {
            log("starting loading for ${repo.name}")
            // o delay serve para termos tempo de cancelar os pedidos antes de serem mandados
            delay(3000)
            service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }
    deferreds.awaitAll().flatten().aggregate()
}