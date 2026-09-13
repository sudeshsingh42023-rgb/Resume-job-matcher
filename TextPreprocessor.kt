package matcher

/**
 * Lightweight text normalization. In production the embedding API (OpenAI/Cohere)
 * handles semantics far better than bag-of-words TF-IDF — this preprocessing still
 * matters upstream of that call (stripping boilerplate, normalizing case) and matters
 * a lot for the keyword-overlap explainability layer, which intentionally stays
 * interpretable rather than embedding-based.
 */
object TextPreprocessor {

    private val stopwords = setOf(
        "a", "an", "the", "and", "or", "but", "is", "are", "was", "were", "be", "been",
        "being", "to", "of", "in", "on", "for", "with", "at", "by", "from", "as", "this",
        "that", "these", "those", "it", "its", "into", "we", "you", "your", "will",
        "have", "has", "had", "our", "their", "they", "he", "she"
    )

    fun tokenize(text: String): List<String> =
        text.toLowerCase()
            .replace(Regex("[^a-z0-9+#./\\s-]"), " ") // keep tokens like "c++", "node.js", "ci/cd"
            .split(Regex("\\s+"))
            .map { it.trim('-') }
            .filter { token -> token.isNotBlank() && token !in stopwords && token.length > 1 }
}
