package spring.iris.infra.kakaoLink.exception

class KakaoLink2FAException(message: String = "2차 인증에 실패했습니다") : KakaoLinkException(message)