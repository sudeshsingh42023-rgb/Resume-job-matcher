package matcher

fun main() {
    println("AI-Assisted Resume-to-Job Matching Engine — demo run\n")

    val jobPostings = listOf(
        JobPosting(
            "J1", "Backend Software Engineer (Kotlin)",
            """We're looking for a Backend Software Engineer to build scalable microservices
               in Kotlin and Java. You'll work with Kafka, Redis, PostgreSQL, and Docker/Kubernetes
               to design reliable distributed systems. Experience with REST APIs, CI/CD, and
               system design is a plus. You'll collaborate closely with SREs on observability
               using Prometheus and Grafana."""
        ),
        JobPosting(
            "J2", "Frontend Engineer (React)",
            """Seeking a Frontend Engineer with strong React and TypeScript skills to build
               user-facing product experiences. Familiarity with JavaScript, REST APIs, and
               agile development practices required. No backend experience necessary."""
        ),
        JobPosting(
            "J3", "Machine Learning Engineer",
            """Join our ML team to build and deploy machine learning and NLP models at scale.
               Requires strong Python, experience with distributed systems, and familiarity
               with cloud platforms like AWS or GCP. Kubernetes and Docker experience preferred."""
        ),
        JobPosting(
            "J4", "DevOps / Platform Engineer",
            """Looking for a DevOps engineer to manage CI/CD pipelines, Kubernetes clusters,
               and infrastructure as code with Terraform. Experience with Jenkins, Docker,
               AWS, and monitoring tools like Prometheus and Grafana required. Some Python
               or Java scripting experience helpful."""
        ),
        JobPosting(
            "J5", "Data Analyst",
            """We need a Data Analyst skilled in SQL and data visualization to support
               business decisions. Experience with dashboards, MySQL, and stakeholder
               communication required. No coding experience beyond SQL necessary."""
        )
    )

    val sudeshResume = """
        B.Tech student with hands-on experience building backend systems in Kotlin and Java.
        Built a distributed job queue microservice using Kafka, Redis, and PostgreSQL, with
        Docker and Kubernetes deployment, circuit breakers, and Prometheus/Grafana monitoring.
        Comfortable with REST APIs, git, CI/CD pipelines, and system design. Some exposure to
        Python and machine learning through academic projects. Strong in data structures and
        algorithms.
    """.trimIndent()

    val engine = MatchingEngine(jobPostings)
    val results = engine.rank(sudeshResume)

    println("Ranked matches for resume:\n")
    results.forEachIndexed { i, r ->
        println("${i + 1}. ${r.title}  (score=${"%.3f".format(r.score)}, semantic=${"%.3f".format(r.semanticScore)}, skill=${"%.3f".format(r.skillScore)})")
        println("   matched skills: ${r.matchedSkills.ifEmpty { setOf("none") }}")
        println("   missing skills: ${r.missingSkills.ifEmpty { setOf("none") }}")
        println()
    }

    // --- Evaluation against a small labeled set ---
    val labeledExamples = listOf(
        LabeledExample(sudeshResume, relevantJobIds = setOf("J1", "J4")),
        LabeledExample(
            resumeText = "Experienced React and TypeScript developer building modern web UIs with agile teams.",
            relevantJobIds = setOf("J2")
        ),
        LabeledExample(
            resumeText = "Python engineer specializing in NLP and machine learning models deployed on AWS with Docker.",
            relevantJobIds = setOf("J3")
        ),
        LabeledExample(
            resumeText = "SQL-focused data analyst experienced building dashboards for business stakeholders.",
            relevantJobIds = setOf("J5")
        )
    )

    val p5 = Evaluator.precisionAtK(engine, labeledExamples, k = 2)
    val ndcg5 = Evaluator.ndcgAtK(engine, labeledExamples, k = 5)

    println("--- Evaluation ---")
    println("Precision@2: ${"%.2f".format(p5)}")
    println("NDCG@5:      ${"%.2f".format(ndcg5)}")
}
