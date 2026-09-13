package matcher

data class JobPosting(val id: String, val title: String, val description: String)

data class MatchResult(
    val jobId: String,
    val title: String,
    val score: Double,
    val semanticScore: Double,
    val skillScore: Double,
    val matchedSkills: Set<String>,
    val missingSkills: Set<String>
)

/**
 * Blends semantic similarity (TF-IDF cosine here, dense embedding cosine in prod)
 * with an explicit skill-overlap score, so the final ranking isn't just a black-box
 * float — it comes with a human-readable "why".
 *
 * Weighting (70% semantic / 30% skill overlap) is a starting point, tuned against
 * the labeled eval set in Evaluator.kt.
 */
class MatchingEngine(
    private val jobPostings: List<JobPosting>,
    private val semanticWeight: Double = 0.7,
    private val skillWeight: Double = 0.3
) {
    private val vectorizer = TfIdfVectorizer(jobPostings.map { it.description })

    fun rank(resumeText: String, topK: Int = jobPostings.size): List<MatchResult> {
        val resumeVector = vectorizer.vectorize(resumeText)

        return jobPostings.map { job ->
            val jobVector = vectorizer.vectorize(job.description)
            val semanticScore = Similarity.cosine(resumeVector, jobVector)

            val comparison = SkillExtractor.compare(resumeText, job.description)
            val totalRelevantSkills = comparison.matched.size + comparison.missing.size
            val skillScore = if (totalRelevantSkills == 0) 0.0
                              else comparison.matched.size.toDouble() / totalRelevantSkills

            val finalScore = (semanticWeight * semanticScore) + (skillWeight * skillScore)

            MatchResult(
                jobId = job.id,
                title = job.title,
                score = finalScore,
                semanticScore = semanticScore,
                skillScore = skillScore,
                matchedSkills = comparison.matched,
                missingSkills = comparison.missing
            )
        }.sortedByDescending { it.score }.take(topK)
    }
}
