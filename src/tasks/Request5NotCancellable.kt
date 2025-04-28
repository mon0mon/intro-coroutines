package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.async
import kotlin.coroutines.coroutineContext

@OptIn(DelicateCoroutinesApi::class)
suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req = req, response = it) }
            .bodyList()

        val deferreds = repos.map { repo ->
            GlobalScope.async {
                log("starting loading for ${repo.name}")
                service.getRepoContributors(owner = req.org, repo = repo.name)
                    .also { logUsers(repo = repo, response = it) }
                    .bodyList()
            }
        }
        return deferreds.awaitAll().flatten().aggregate()
}
