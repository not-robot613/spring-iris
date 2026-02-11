package spring.iris.infra.kakaoLink.exception

class KakaoLinkSendException(message: String = "메시지 전송에 실패했습니다") : KakaoLinkException(message)