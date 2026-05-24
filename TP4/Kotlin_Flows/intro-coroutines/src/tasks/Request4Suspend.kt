package tasks

import contributors.*

// não precisamos de chamar o executre() porque o retrofit sabe que é uma suspend function e então faz o pedido
// http por detrás anyways só que sem bloquear a thread
suspend fun loadContributorsSuspend(service: GitHubService, req: RequestData): List<User> {
    val repos = service
        // não chamamos o execute()
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    return repos.flatMap { repo ->
        // não chamamos o execute()
        service.getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()
    }.aggregate()
}

