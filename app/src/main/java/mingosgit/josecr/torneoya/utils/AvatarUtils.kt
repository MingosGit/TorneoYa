package mingosgit.josecr.torneoya.utils

import android.widget.ImageView
import android.content.Context

object AvatarUtils {
    fun setAvatarImage(imageView: ImageView, avatarNumber: Int?, context: Context) {
        val defaultRes = context.resources.getIdentifier("avatar_placeholder", "drawable", context.packageName)
        if (avatarNumber == null || avatarNumber <= 0) {
            imageView.setImageResource(defaultRes)
        } else {
            val resourceId = context.resources.getIdentifier(
                "avatar_$avatarNumber", "drawable", context.packageName
            )
            if (resourceId != 0) {
                imageView.setImageResource(resourceId)
            } else {
                imageView.setImageResource(defaultRes)
            }
        }
    }
}
