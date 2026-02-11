package spring.iris.infra.scheduler

import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import spring.iris.infra.iris.client.IrisSQLQueryClient
import spring.iris.infra.iris.dto.request.QueryRequest

@Component
class DailyScheduler(
    private val irisQueryClient: IrisSQLQueryClient
) : KLogging() {
    companion object {
        private const val DELETE_QUERY = "DELETE FROM chat_logs"
    }

    @Scheduled(cron = "0 0 12 * * *")
    suspend fun deleteIrisChatLogs() = supervisorScope {
        launch {
            logger.info { "IRIS에 delete 쿼리 날리는 중" }

            val request = QueryRequest(
                query = DELETE_QUERY
            )

            irisQueryClient.query(request)

            logger.info { "IRIS에 delete 쿼리 성공!" }
        }

    }
}
