package dev.ajaykotian.hedwig.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val uid: String = "",
    val phoneNumber: String = "",
    val name: String = "",
    val about: String = DEFAULT_ABOUT,
) {
    companion object {
        const val DEFAULT_ABOUT = "Hey there! I am using hedwig."
    }
}