package tasks

import contributors.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req = req, response = it) }
        .bodyList()

    val deferreds = repos.map { repo ->
        async {
            log("starting loading for ${repo.name}")
            service.getRepoContributors(owner = req.org, repo = repo.name)
                .also { logUsers(repo = repo, response = it) }
                .bodyList()
        }
    }
    deferreds.awaitAll().flatten().aggregate()
}
