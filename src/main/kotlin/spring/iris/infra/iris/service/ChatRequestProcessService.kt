package spring.iris.infra.iris.service

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import mu.KLogging
import org.springframework.stereotype.Service
import spring.iris.common.enums.Role
import spring.iris.common.enums.RoomType
import spring.iris.infra.exception.ServerException
import spring.iris.infra.iris.client.IrisSQLQueryClient
import spring.iris.infra.iris.dto.request.QueryRequest
import spring.iris.infra.iris.dto.response.QueryResponse
import spring.iris.infra.iris.websocket.enums.MessageType
import spring.iris.infra.iris.websocket.request.IrisRequest
import spring.iris.infra.message.dto.internal.ChatData
import spring.iris.infra.message.dto.internal.MemberDTO
import spring.iris.infra.message.dto.internal.MessageDTO
import spring.iris.infra.message.dto.internal.RoomDTO
import spring.iris.infra.redis.model.entity.Member
import spring.iris.infra.redis.model.entity.Room
import spring.iris.infra.redis.service.MemberRedisService
import spring.iris.infra.redis.service.RoomRedisService
import spring.iris.infra.util.TimeUtil

@Service
class ChatRequestProcessService(
    private val memberRedisService: MemberRedisService,
    private val roomRedisService: RoomRedisService,
    private val client: IrisSQLQueryClient
) : KLogging() {

    suspend fun makeChatData(
        request: IrisRequest,
        startTime: Long = System.currentTimeMillis()
    ): ChatData = supervisorScope {
        val roleDeferred = async { getRole(request) }
        val roomTypeDeferred = async { getRoomType(request) }
        val nicknameDeferred = if (request.sender == null) {
            async { getNickname(request) }
        } else null

        val referenceId: Long? = request.json.parsedAttachment?.srcLogId

        val finalNickname = request.sender
            ?: nicknameDeferred?.await()
            ?: throw ServerException("닉네임 조회 실패 -> $request")

        val member = MemberDTO(
            id = request.json.userId.toLong(),
            name = finalNickname,
            role = roleDeferred.await()
        )

        val room = RoomDTO(
            id = request.json.chatId.toLong(),
            name = request.room,
            type = roomTypeDeferred.await()
        )

        val msg = MessageDTO(
            id = request.json.id.toLong(),
            type = request.json.getRequestType(),
            content = if (request.json.getRequestType() == MessageType.EMOTICON) "이모티콘"
            else request.msg,
            referenceId = referenceId,
            createdAt =
                TimeUtil.convertUnixToZonedDateTime(
                    request.json.createdAt.toLong()
                ),
            isMine = request.json.parsedVData.isMine
        )

        ChatData(sender = member, msg = msg, room = room, startTime = startTime)
    }

    private suspend fun getNickname(request: IrisRequest): String? {
        val response: QueryResponse = client.query(makeSelectNicknameQuery(request))
        val result = response.data.firstOrNull() ?: return null
        logger.warn { "닉네임 쿼리 -> ${response.data}" }

        return result["nickname"] ?: result["name"]
    }

    private suspend fun getRole(request: IrisRequest): Role = coroutineScope {
        try {
            val id: Long = request.json.userId.toLong()
            memberRedisService.getMemberById(id)?.role
                ?: run {
                    val response: QueryResponse = client.query(makeSelectRoleQuery(request))

                    val memberType: Int? = response.data.firstOrNull()?.get("link_member_type")?.toIntOrNull()

                    val role: Role = Role.fromInt(memberType ?: 0)

                    if (role != Role.UNKNOWN) {
                        memberRedisService.saveMember(Member(id = id, role = role))
                    }

                    role
                }
        } catch (e: Exception) {
            logger.info { "예외 -> ${e.message}" }
            Role.REAL_PROFILE
        }
    }

    private suspend fun getRoomType(request: IrisRequest): RoomType = coroutineScope {
        try {
            val id: Long = request.json.chatId.toLong()
            roomRedisService.getRoomTypeById(id)
                ?: run {
                    val response: QueryResponse = client.query(makeSelectRoomTypeQuery(request))
                    val roomTypeString: String? = response.data.firstOrNull()?.get("type")
                    val roomType: RoomType = RoomType.fromString(roomTypeString ?: "")
                    if (roomType != RoomType.ETC)
                        roomRedisService.saveRoom(Room(id = id, type = roomType))

                    roomType
                }
        } catch (e: Exception) {
            logger.info { "예외 -> ${e.message}" }
            RoomType.ETC
        }
    }

    private fun makeSelectNicknameQuery(request: IrisRequest): QueryRequest {
        if (request.json.parsedVData.isMine) {
            return QueryRequest(
                query =
                    """
                    SELECT T2.nickname FROM chat_rooms AS T1
                    JOIN db2.open_profile AS T2 ON T1.link_id = T2.link_id
                    WHERE T1.id = ?
                """.trimIndent(),
                bind = listOf(request.json.chatId)
            )
        }

        if (request.json.userId.toLong() < 10000000000) {
            return QueryRequest(
                query = "SELECT name, enc FROM db2.friends WHERE id = ?",
                bind = listOf(request.json.userId)
            )
        }

        return QueryRequest(
            query = "SELECT nickname,enc FROM db2.open_chat_member WHERE user_id = ?",
            bind = listOf(request.json.userId)
        )
    }

    private fun makeSelectRoleQuery(request: IrisRequest): QueryRequest {

        if (request.json.parsedVData.isMine) {
            return QueryRequest(
                query =
                    """
                    SELECT T2.link_member_type FROM chat_rooms AS T1
                    INNER JOIN open_profile AS T2 ON T1.link_id = T2.link_id
                    WHERE T1.id = ?
                """.trimIndent(),
                bind = listOf(request.json.chatId)
            )
        }

        return QueryRequest(
            query = "SELECT link_member_type FROM db2.open_chat_member WHERE user_id = ?",
            bind = listOf(request.json.userId)
        )
    }

    private fun makeSelectRoomTypeQuery(request: IrisRequest): QueryRequest {

        return QueryRequest(
            query = "SELECT type FROM chat_rooms WHERE id = ?",
            bind = listOf(request.json.chatId)
        )
    }
}
