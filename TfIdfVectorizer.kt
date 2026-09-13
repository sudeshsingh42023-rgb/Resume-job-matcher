package matcher

import kotlin.math.ln

/**
 * TF-IDF vectorizer over a fixed corpus (all job postings + the resume being scored).
 *
 * This stands in for a real embedding call (OpenAI/Cohere `text-embedding-3-small`,
 * or a local sentence-transformer) so the whole pipeline runs offline with zero
 * external dependencies. Swapping this out for `EmbeddingClient.embed(text)` and
 * comparing dense vectors instead of sparse TF-IDF vectors is the only change
 * `MatchingEngine` needs to go from "demo" to "production" — see README.
 */
class TfIdfVectorizer(corpus: List<String>) {

    private val documentsTokens: List<List<String>> = corpus.map { TextPreprocessor.tokenize(it) }
    private val vocabulary: List<String> = documentsTokens.flatten().distinct()
    private val idf: Map<String, Double> = vocabulary.associateWith { term ->
        val docsContaining = documentsTokens.count { it.contains(term) }
        ln((documentsTokens.size + 1).toDouble() / (docsContaining + 1)) + 1.0
    }

    fun vectorize(text: String): Map<String, Double> {
        val tokens = TextPreprocessor.tokenize(text)
        if (tokens.isEmpty()) return emptyMap()
        val termFreq = tokens.groupingBy { it }.eachCount()
        val maxFreq = termFreq.values.max() ?: 1
        return termFreq.mapValues { (term, count) ->
            val tf = 0.5 + 0.5 * (count.toDouble() / maxFreq)
            tf * (idf[term] ?: ln((documentsTokens.size + 1).toDouble()) + 1.0)
        }
    }
}

object Similarity {
    fun cosine(a: Map<String, Double>, b: Map<String, Double>): Double {
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val sharedKeys = a.keys.intersect(b.keys)
        val dot = sharedKeys.fold(0.0) { acc, key -> acc + a.getValue(key) * b.getValue(key) }
        val normA = kotlin.math.sqrt(a.values.fold(0.0) { acc, v -> acc + v * v })
        val normB = kotlin.math.sqrt(b.values.fold(0.0) { acc, v -> acc + v * v })
        if (normA == 0.0 || normB == 0.0) return 0.0
        return dot / (normA * normB)
    }
}
