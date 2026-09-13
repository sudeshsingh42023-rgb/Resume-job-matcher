package matcher

/** One labeled example: this resume should rank these job IDs as relevant. */
data class LabeledExample(val resumeText: String, val relevantJobIds: Set<String>)

object Evaluator {

    /** Fraction of the top-k results that are in the labeled relevant set, averaged over examples. */
    fun precisionAtK(engine: MatchingEngine, examples: List<LabeledExample>, k: Int): Double {
        val precisions = examples.map { example ->
            val topK = engine.rank(example.resumeText, topK = k)
            val hits = topK.count { result -> result.jobId in example.relevantJobIds }
            hits.toDouble() / k
        }
        return precisions.average()
    }

    /** Normalized Discounted Cumulative Gain @ k — rewards relevant results ranked higher. */
    fun ndcgAtK(engine: MatchingEngine, examples: List<LabeledExample>, k: Int): Double {
        val scores = examples.map { example ->
            val topK = engine.rank(example.resumeText, topK = k)
            val dcg = topK.withIndex().fold(0.0) { acc, (i, result) ->
                val rel = if (result.jobId in example.relevantJobIds) 1.0 else 0.0
                acc + rel / log2(i + 2.0)
            }
            val idealHits = minOf(k, example.relevantJobIds.size)
            val idcg = (0 until idealHits).fold(0.0) { acc, i -> acc + 1.0 / log2(i + 2.0) }
            if (idcg == 0.0) 0.0 else dcg / idcg
        }
        return scores.average()
    }

    private fun log2(x: Double) = Math.log(x) / Math.log(2.0)
}
