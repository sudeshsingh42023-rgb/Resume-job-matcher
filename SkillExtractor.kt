package matcher

/**
 * A small curated skill taxonomy used purely for the explainability layer — the
 * score itself comes from TF-IDF/embedding cosine similarity, but a raw float
 * doesn't tell a candidate or a recruiter *why* two documents matched. This gives
 * an interpretable "matched skills" / "missing skills" breakdown alongside the score.
 *
 * In production this taxonomy would be pulled from a maintained skills database
 * (e.g. ESCO, LinkedIn Skills, or an internal Indeed taxonomy) instead of being
 * hardcoded, but the extraction logic (substring match against normalized text)
 * stays the same.
 */
object SkillExtractor {

    private val knownSkills = listOf(
        "kotlin", "java", "python", "sql", "kafka", "redis", "docker", "kubernetes",
        "aws", "gcp", "azure", "microservices", "rest api", "grpc", "ci/cd", "git",
        "postgresql", "mysql", "mongodb", "elasticsearch", "spring", "ktor",
        "react", "typescript", "javascript", "terraform", "jenkins", "grafana",
        "prometheus", "machine learning", "nlp", "distributed systems", "testing",
        "agile", "system design", "data structures", "algorithms"
    )

    fun extract(text: String): Set<String> {
        val normalized = " ${text.toLowerCase()} "
        return knownSkills.filter { skill -> normalized.contains(" $skill ") || normalized.contains(" $skill,") || normalized.contains(" $skill.") }
            .toSet()
    }

    fun compare(resumeText: String, jobText: String): SkillComparison {
        val resumeSkills = extract(resumeText)
        val jobSkills = extract(jobText)
        return SkillComparison(
            matched = resumeSkills.intersect(jobSkills),
            missing = jobSkills.subtract(resumeSkills)
        )
    }
}

data class SkillComparison(val matched: Set<String>, val missing: Set<String>)
