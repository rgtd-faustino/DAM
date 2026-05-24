package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User
import kotlin.concurrent.thread

fun loadContributorsBackground(service: GitHubService, req: RequestData, updateResults: (List<User>) -> Unit) {
    thread {
        val users = loadContributorsBlocking(service, req)
        // isto chama o callback que recebemos no argumento e é corrido na thread principal por causa do swing
        // utilities invoke later quando chamamos esta função principal
        updateResults(users)
    }
}