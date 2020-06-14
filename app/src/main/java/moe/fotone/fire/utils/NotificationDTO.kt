package moe.fotone.fire.utils

data class NotificationDTO(
    var targetUID: String? = null,
    var startName: String? = null,
    var startUID: String? = null,
    var kind: Int = 0, //0 : 좋아요, 1: 코멘트, 2: 팔로우
    var message: String? = null,
    var timestamp: Long? = null
)