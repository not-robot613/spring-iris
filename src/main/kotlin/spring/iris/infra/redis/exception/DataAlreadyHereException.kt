package spring.iris.infra.redis.exception

import spring.iris.infra.exception.ServerException


class DataAlreadyHereException(msg: String) : ServerException(msg)