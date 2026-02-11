package spring.iris.infra.iris.dto.request

data class QueryRequest(
    val query: String,
    val bind: List<String>? = null
)