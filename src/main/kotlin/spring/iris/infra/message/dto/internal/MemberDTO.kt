package spring.iris.infra.message.dto.internal

import spring.iris.common.enums.Role


data class MemberDTO(
    val id: Long,
    val name: String,
    val role: Role,
)
