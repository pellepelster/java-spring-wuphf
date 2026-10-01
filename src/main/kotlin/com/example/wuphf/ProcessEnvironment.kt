package com.example.wuphf

class ProcessEnvironment(environment: Map<String, String>) {
    private val report = report(environment)

    fun report() = report

    companion object {
        const val DATABASE_PREFIX = "DATABASE_"

        private val BY_NAME =
            compareBy<EnvironmentVariable, String>(String.CASE_INSENSITIVE_ORDER) { it.name }
                .thenBy { it.name }

        private fun report(environment: Map<String, String>): EnvironmentReport {
            val (database, other) =
                environment
                    .map { (name, value) ->
                        EnvironmentVariable(
                            name,
                            Secrets.maskIfSecret(name, value),
                            Secrets.isSecretName(name),
                        )
                    }
                    .partition { it.name.startsWith(DATABASE_PREFIX) }

            return EnvironmentReport(database.sortedWith(BY_NAME), other.sortedWith(BY_NAME))
        }
    }
}
