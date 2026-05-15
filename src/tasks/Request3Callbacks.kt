package tasks

import contributors.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

fun loadContributorsCallbacks(service: GitHubService, req: RequestData, updateResults: (List<User>) -> Unit) {
    service.getOrgReposCall(req.org).onResponse { responseRepos ->
        logRepos(req, responseRepos)
        val repos = responseRepos.bodyList()
        val allUsers = mutableListOf<User>()
        var countDown = repos.size
        for (repo in repos) {
            service.getRepoContributorsCall(req.org, repo.name).onResponse { responseUsers ->
                logUsers(repo, responseUsers)
                val users = responseUsers.bodyList()
                allUsers += users
                countDown--
                if (countDown == 0) {
                    updateResults(allUsers.aggregate())
                }
            }
        }
        // TODO: Why this code doesn't work? How to fix that?
        // isto não vai funcionar porque a chamada dentro do loop para adicionar os users à lista é feita de modo
        // assíncrono e uma vez que metemos cada repo a ser assíncrono o loop acaba e depois chamamos esta função
        // para dar update e vai estar vazia, então a maneira que arranjamos isot é que só damos update dos resultados
        // quando soubermos que já percorremos o loop inteiro com um simples contador
        //updateResults(allUsers.aggregate())
    }
}

inline fun <T> Call<T>.onResponse(crossinline callback: (Response<T>) -> Unit) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            callback(response)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            log.error("Call failed", t)
        }
    })
}
