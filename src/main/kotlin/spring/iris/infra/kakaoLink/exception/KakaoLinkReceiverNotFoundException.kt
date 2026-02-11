package spring.iris.infra.kakaoLink.exception

class KakaoLinkReceiverNotFoundException(message: String = "수신자를 찾을 수 없습니다") : KakaoLinkException(message)