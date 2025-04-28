package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User
import contributors.log
import contributors.logRepos
import contributors.logUsers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

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
