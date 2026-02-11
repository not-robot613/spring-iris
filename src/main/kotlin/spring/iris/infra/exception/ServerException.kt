package spring.iris.infra.exception

import org.springframework.core.NestedRuntimeException

open class ServerException(msg: String) : NestedRuntimeException(msg)