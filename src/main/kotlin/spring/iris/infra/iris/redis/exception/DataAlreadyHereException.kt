package spring.iris.infra.iris.redis.exception

import spring.iris.infra.exception.ServerException


class DataAlreadyHereException(msg: String) : ServerException(msg)