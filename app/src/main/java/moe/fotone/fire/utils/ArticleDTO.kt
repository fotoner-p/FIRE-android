package moe.fotone.fire.utils

data class ArticleDTO(var uid:String? = null,
                      var name:String? = null,
                      var timestamp: Long? = null,
                      var main:String? = null,
                      var favoriteCount: Int = 0,
                      var commentCount: Int = 0,
                      var favorites: MutableMap<String, Boolean> = HashMap()){

    data class Comment(var uid: String? = null,
                       var userId: String? = null,
                       var comment: String? = null,
                       var timestamp: Long? = null)
}

