package dev.ajaykotian.hedwig.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import dev.ajaykotian.hedwig.MainActivity
import dev.ajaykotian.hedwig.R
import dev.ajaykotian.hedwig.di.ApplicationContext
import dev.ajaykotian.hedwig.ui.components.avatarColorFor
import dev.ajaykotian.hedwig.ui.components.initialsOf
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val manager = NotificationManagerCompat.from(context)


    fun createChannels() {
        val channel = NotificationChannel(
            CHANNEL_MESSAGES,
            context.getString(R.string.notification_channel_messages),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_messages_description)
        }
        manager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    fun showMessageNotification(chatId: String, senderId: String, senderName: String, message: String, timestamp: Long) {
        if (!canPostNotifications()) return

        val avatar = IconCompat.createWithBitmap(avatarBitmap(senderName, senderId))
        val sender = Person.Builder()
            .setKey(senderId)
            .setName(senderName)
            .setIcon(avatar)
            .build()

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW // shortcuts require an action
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_CHAT_USER_ID, senderId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // A long-lived shortcut per chat
        val shortcut = ShortcutInfoCompat.Builder(context, chatId)
            .setShortLabel(senderName)
            .setPerson(sender)
            .setIcon(avatar)
            .setIntent(intent)
            .setLongLived(true)
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)

        // Add to the messages already showing for this chat, or start a new conversation.
        val style = activeNotification(chatId)
            ?.let { NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(it) }
            ?: NotificationCompat.MessagingStyle(me)
        style.addMessage(message, timestamp, sender)

        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_notifications)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setStyle(style)
            .setShortcutInfo(shortcut)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(chatId.hashCode(), notification)
    }


    fun cancelChatNotification(chatId: String) = manager.cancel(chatId.hashCode())

    fun cancelAll() {
        manager.cancelAll()
        ShortcutManagerCompat.removeAllDynamicShortcuts(context)
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED


    /** MessagingStyle needs "who is the user"; their own messages aren't shown, so a label is enough. */
    private val me = Person.Builder().setName("You").build()

    private fun activeNotification(chatId: String): Notification? =
        context.getSystemService(NotificationManager::class.java)
            .activeNotifications
            .firstOrNull { it.id == chatId.hashCode() }
            ?.notification

    /** The in-app Avatar drawn into a bitmap: the user's colour with their initials. */
    private fun avatarBitmap(name: String, id: String): Bitmap {
        val size = (48 * context.resources.displayMetrics.density).roundToInt()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = avatarColorFor(id).toArgb()
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        val initials = initialsOf(name)
        if (initials.isNotEmpty()) {
            paint.color = android.graphics.Color.WHITE
            paint.textSize = size * 0.38f
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            // drawText's y is the baseline; this centres the text's visual height on the circle.
            val baseline = size / 2f - (paint.descent() + paint.ascent()) / 2
            canvas.drawText(initials, size / 2f, baseline, paint)
        }
        return bitmap
    }


    private companion object {
        const val CHANNEL_MESSAGES = "messages"
    }
}
