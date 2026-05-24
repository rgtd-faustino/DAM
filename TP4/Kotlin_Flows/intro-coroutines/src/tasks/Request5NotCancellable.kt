package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun loadContributorsNotCancellable(
    service: GitHubService,
    req: RequestData
): List<User> {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val deferreds = repos.map { repo ->
        // de modo que as corrotinas sejam globais, ou seja, não sejam filhas de um pai
        // assim mesmo se cancelarmos na UI os pedidos são mandados à mesma porque as corrotinas são independentes
        GlobalScope.async {
            log("starting loading for ${repo.name}")
            delay(3000)
            service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }
    return deferreds.awaitAll().flatten().aggregate()
}