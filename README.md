# AI-Assisted Resume-to-Job Matching Engine

Takes a resume and a set of job postings and returns a ranked, **explainable**
match score — not just a similarity float, but a breakdown of which skills
matched and which are missing. Built as a direct answer to two things in
Indeed's job description: the company's stated interest in exploring how AI
can help people get jobs, and the explicit ask to accelerate delivery using
approved LLM tools.

## Why this exists

A raw cosine-similarity score between two blobs of text is not something a
job seeker or a recruiter can act on. This engine blends semantic similarity
with a transparent skill-overlap layer so every score comes with a "why":
matched skills, missing skills, and the weighting between the two signals.

## Architecture

```
resume text ──┐
              ├─▶ TfIdfVectorizer ──▶ cosine similarity ──┐
job posting ──┘                                            ├─▶ weighted score ──▶ ranked results
resume text ──┐                                            │      + matched/missing skills
              ├─▶ SkillExtractor ───▶ overlap ratio ───────┘
job posting ──┘
```

- **Semantic similarity**: `TfIdfVectorizer` + cosine similarity. This is the
  local, dependency-free stand-in for a real embedding call. `EmbeddingClient.kt`
  sketches exactly what changes to go from TF-IDF to OpenAI/Cohere embeddings —
  the rest of `MatchingEngine` is untouched, because both approaches just produce
  "text -> vector" and "vector, vector -> similarity".
- **Explainability**: `SkillExtractor` matches against a curated skill taxonomy
  and returns matched vs. missing skills. This is deliberately kept
  interpretable rather than embedding-based — a candidate should be able to see
  *why* they scored low, not just that they did.
- **Evaluation**: `Evaluator.kt` computes precision@k and NDCG@k against a small
  labeled set (resume → set of relevant job IDs), so the weighting between
  semantic and skill scores is tuned against actual measured accuracy, not gut feel.

## Running the demo (no external infra/API key required)

```bash
kotlinc src/main/kotlin/matcher/*.kt -d out/matcher.jar
java -cp "out/matcher.jar:$(find / -name 'kotlin-stdlib*.jar' | tr '\n' ':')" matcher.MainKt
```

Actual output from a run against 5 sample job postings and a Kotlin/backend-flavored resume:

```
Ranked matches for resume:

1. Backend Software Engineer (Kotlin)  (score=0.385, semantic=0.250, skill=0.700)
   matched skills: [kotlin, java, kafka, redis, ci/cd, postgresql, system design]
   missing skills: [grafana, prometheus, distributed systems]

2. Machine Learning Engineer  (score=0.206, semantic=0.188, skill=0.250)
3. DevOps / Platform Engineer  (score=0.197, semantic=0.139, skill=0.333)
4. Frontend Engineer (React)   (score=0.061, semantic=0.087, skill=0.000)
5. Data Analyst                (score=0.035, semantic=0.050, skill=0.000)

--- Evaluation ---
Precision@2: 0.50
NDCG@5:      0.98
```

The ranking is sensible (the Kotlin backend role wins by a wide margin) and the
skill gaps are genuinely useful — a candidate reading this knows exactly what
to learn next (Grafana/Prometheus/distributed systems) to close the gap on
their top match.

## Moving from "resume project" to "real" service

- Swap `TfIdfVectorizer` for a real embedding API call (`EmbeddingClient.kt` has
  the interface + wiring sketch) — this is the single highest-impact upgrade,
  since TF-IDF only catches keyword overlap, not semantic meaning ("built
  scalable APIs" vs. "developed high-throughput services" would score low on
  TF-IDF but high on embeddings).
- Cache embeddings in Redis keyed by a content hash, since resumes/postings
  don't change per-request.
- Move the curated skill taxonomy out of code and into a maintained data source.
- Add a Ktor REST endpoint (`POST /match`) instead of calling `MatchingEngine`
  directly from `main()`.

## Note on AI-assisted development

Per the JD's call to "accelerate code delivery by utilizing approved LLM
tools" — this project's scaffolding (Gradle config, Docker/CI boilerplate,
initial test structure) was drafted with an AI coding assistant and then
reviewed and corrected by hand, particularly around Kotlin stdlib
version-compatibility issues that only show up when actually compiling and
running the code, not just reading it.
