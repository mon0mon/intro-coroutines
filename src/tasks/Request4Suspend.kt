package tasks

import contributors.*

suspend fun loadContributorsSuspend(service: GitHubService, req: RequestData): List<User> {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req = req, response = it) }
        .bodyList()

    return repos.flatMap { repo ->
        service
            .getRepoContributors(owner = req.org, repo = repo.name)
            .also { logUsers(repo = repo, response = it) }
            .bodyList()
    }.aggregate()
}
