package phraseapp

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import phraseapp.internal.platforms.Platform
import phraseapp.repositories.checks.CheckRepository

open class CheckTask : DefaultTask() {
    lateinit var baseUrl: String
    lateinit var platform: Platform
    lateinit var authToken: String
    lateinit var projectId: String
    lateinit var localeNameRegex: String
    lateinit var output: String

    @TaskAction
    fun action() {
        var throwable: Throwable? = null
        val repository = CheckRepository.newInstance(baseUrl, output, localeNameRegex, authToken, projectId, platform)
        repository.check().subscribe({
            logger.info("You don't have any error in your translations!")
        }, {
            throwable = it
        })
        if (throwable != null) {
            throw GradleException("You have errors in your translations", throwable!!)
        }
    }
}