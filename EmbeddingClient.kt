package matcher

/**
 * Production embedding client sketch — NOT wired into the demo, since it requires
 * network access and an API key. Swapping the algorithm is intentionally isolated
 * to this one interface: `MatchingEngine` only needs something that turns text into
 * a numeric vector and something that scores similarity between two vectors, so
 * replacing TfIdfVectorizer with this is a constructor-level change, not a rewrite.
 *
 * Example wiring (pseudo-code, once an API key + Ktor HttpClient are available):
 *
 *   val embeddingClient = OpenAiEmbeddingClient(apiKey = System.getenv("EMBEDDING_API_KEY"))
 *   val resumeVector = embeddingClient.embed(resumeText)   // -> DoubleArray, e.g. 1536-dim
 *   val jobVector = embeddingClient.embed(job.description)
 *   val score = cosineSimilarity(resumeVector, jobVector)  // dense-vector cosine, not TF-IDF
 *
 * Embeddings would be cached in Redis keyed by a hash of the input text, since resumes
 * and job descriptions don't change often and re-embedding on every request is wasted
 * cost and latency.
 */
interface EmbeddingClient {
    fun embed(text: String): DoubleArray
}

fun cosineSimilarityDense(a: DoubleArray, b: DoubleArray): Double {
    require(a.size == b.size) { "vectors must be the same dimensionality" }
    var dot = 0.0; var normA = 0.0; var normB = 0.0
    for (i in a.indices) {
        dot += a[i] * b[i]
        normA += a[i] * a[i]
        normB += b[i] * b[i]
    }
    if (normA == 0.0 || normB == 0.0) return 0.0
    return dot / (kotlin.math.sqrt(normA) * kotlin.math.sqrt(normB))
}
