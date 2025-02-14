package com.riders.thelab.core.common.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper.MediaStyle
import timber.log.Timber

object LabNotificationManager {

    private fun createNotificationAction(
        context: Context,
        actionIcon: Int,
        actionTitle: Int,
        playbackAction: Long
    ) =
        NotificationCompat.Action.Builder(
            actionIcon,
            context.getString(actionTitle),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                playbackAction
            )
        ).build()


    fun createNotificationChannel(
        context: Context,
        @StringRes notificationName: Int,
        @StringRes notificationDescription: Int,
        notificationImportance: Int,
        @StringRes notificationChannelId: Int
    ) = createNotificationChannel(
        context = context,
        notificationName = context.getString(notificationName),
        notificationDescription = context.getString(notificationDescription),
        notificationImportance = notificationImportance,
        notificationChannelId = context.getString(notificationChannelId)
    )

    @SuppressLint("NewApi")
    fun createNotificationChannel(
        context: Context,
        notificationName: String,
        notificationDescription: String,
        notificationImportance: Int,
        notificationChannelId: String
    ) {
        Timber.d("createNotificationChannel()")

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (LabCompatibilityManager.isOreo()) {
            val channel =
                NotificationChannel(
                    notificationChannelId,
                    notificationName,
                    notificationImportance
                ).apply {
                    description = notificationDescription
                }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildMainNotification(
        context: Context,
        pendingIntent: PendingIntent,
        @DrawableRes smallIcon: Int,
        @StringRes contentTitle: Int,
        @StringRes contentText: Int,
        @StringRes bigText: Int,
    ): NotificationCompat.Builder =
        NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .apply {
                Timber.d("buildMainNotification()")
                setSmallIcon(smallIcon)
                setContentTitle(context.getString(contentTitle))
                setContentText(context.getString(contentText))
                priority = NotificationCompat.PRIORITY_HIGH
                // Set the intent that will fire when the user taps the notification
                setContentIntent(pendingIntent)
                setChannelId(Constants.NOTIFICATION_CHANNEL_ID)
                setStyle(
                    NotificationCompat
                        .BigTextStyle()
                        .bigText(context.getString(bigText))
                )
                setAutoCancel(false)
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            }

    fun createNotification(
        context: Context,
        notificationIntent: Intent,
        stopIntent: Intent,
        notificationChannelId: String,
        title: String,
        contentText: String,
        @DrawableRes smallIcon: Int
    ): Notification {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopPendingIntent =
            PendingIntent.getService(context, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(context, notificationChannelId)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(smallIcon)
            .setContentIntent(pendingIntent)
            .addAction(0, "Stop", stopPendingIntent)
            .build()
    }


    @OptIn(UnstableApi::class)
    @SuppressLint("MissingPermission", "ForegroundServiceType", "RestrictedApi")
    fun <T : Service> displayMusicNotification(
        context: Context,
        mediaSession: MediaSessionCompat,
        controller: MediaControllerCompat,
        mServiceMusic: T,
        @DrawableRes smallIcon: Int,
        contentTitle: String,
        contentText: String,
        @DrawableRes largeIcon: Int,
        @DrawableRes actionIcon: Int,
        @StringRes actionTitle: Int
    ) {
        Timber.d("displayMusicNotification()")

        val mediaMetadata = controller.metadata
        val description = mediaMetadata?.description

        val notification = NotificationCompat.Builder(
            context,
            Constants.NOTIFICATION_MUSIC_CHANNEL_ID
        ).apply {

            // Add the metadata for the currently playing track
            setContentTitle(description?.title)
            setContentText(description?.subtitle)
            setSubText(description?.description)
            setLargeIcon(description?.iconBitmap)

            // Enable launching the player by clicking the notification
            setContentIntent(controller.sessionActivity)

            // Stop the service when the notification is swiped away
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            // Show controls on lock screen even when user hides sensitive content.
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Add an app icon and set its accent color
            // Be careful about the color
            setSmallIcon(smallIcon)

            val pauseAction = createNotificationAction(
                context,
                actionIcon,
                actionTitle,
                PlaybackStateCompat.ACTION_PLAY_PAUSE
            )

            // Add a pause button
            addAction(pauseAction)

            // Create a MediaStyle object and supply your media session token to it.
            // Take advantage of MediaStyle features
            val mediaStyle: androidx.media.app.NotificationCompat.MediaStyle =
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    // Add media control buttons that invoke intents in your media service
                    //.addAction(R.drawable.ic_previous, "Previous", prevPendingIntent) // #0
                    //.addAction(R.drawable.ic_pause, "Pause", pausePendingIntent) // #1
                    //.addAction(R.drawable.ic_next, "Next", nextPendingIntent) // #2
                    .setShowActionsInCompactView(0)
                    // Add a cancel button
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )


            // Apply the media style template
            setStyle(mediaStyle)
            setContentTitle(contentTitle)
            setContentText(contentText)
            setLargeIcon(largeIcon.toBitmap(context))

            setSilent(true)
            setAutoCancel(true)
        }

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(Constants.NOTIFICATION_MUSIC_ID, notification.build())
            // Provide a unique integer for the "notificationId" of each notification.
            mServiceMusic.startForeground(Constants.NOTIFICATION_MUSIC_ID, notification.build())
        }
    }

    @OptIn(UnstableApi::class)
    @SuppressLint("MissingPermission", "ForegroundServiceType", "RestrictedApi")
    fun <T : Service> displayMusicNotification(
        context: Context,
        mediaSession: MediaSession,
        //controller: MediaController,
        mServiceMusic: T,
        @DrawableRes smallIcon: Int,
        contentTitle: String,
        contentText: String,
        @DrawableRes largeIcon: Int,
        @DrawableRes actionIcon: Int,
        @StringRes actionTitle: Int
    ) {
        Timber.d("displayMusicNotification()")

        /*val mediaMetadata = controller.mediaMetadata
        val title = mediaMetadata.title
        val subtitle = mediaMetadata.subtitle
        val description = mediaMetadata.description
        val icon = mediaMetadata.artworkData*/

        val notification = NotificationCompat.Builder(
            context,
            Constants.NOTIFICATION_MUSIC_CHANNEL_ID
        ).apply {

            // Add the metadata for the currently playing track
            /*setContentTitle(title)
            setContentText(subtitle)
            setSubText(description)*/
            //setLargeIcon(description?.iconBitmap)

            // Enable launching the player by clicking the notification
            //setContentIntent(controller.sessionActivity)

            // Stop the service when the notification is swiped away
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            // Show controls on lock screen even when user hides sensitive content.
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Add an app icon and set its accent color
            // Be careful about the color
            setSmallIcon(smallIcon)

            val pauseAction = createNotificationAction(
                context,
                actionIcon,
                actionTitle,
                PlaybackStateCompat.ACTION_PLAY_PAUSE
            )
            val previousAction = createNotificationAction(
                context,
                actionIcon,
                actionTitle,
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
            val nextAction = createNotificationAction(
                context,
                actionIcon,
                actionTitle,
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            )

            // Add a pause button
            addAction(pauseAction)
            addAction(previousAction)
            addAction(nextAction)

            // Create a MediaStyle object and supply your media session token to it.
            // Take advantage of MediaStyle features
            val mediaStyle: MediaStyle = MediaStyle(mediaSession)
                //.setMediaSession(mediaSession.token)
                // Add media control buttons that invoke intents in your media service
                //.addAction(R.drawable.ic_previous, "Previous", prevPendingIntent) // #0
                //.addAction(R.drawable.ic_pause, "Pause", pausePendingIntent) // #1
                //.addAction(R.drawable.ic_next, "Next", nextPendingIntent) // #2
                .setShowActionsInCompactView(0)
                // Add a cancel button
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )


            // Apply the media style template
            setStyle(mediaStyle)
            setContentTitle(contentTitle)
            setContentText(contentText)
            setLargeIcon(largeIcon.toBitmap(context))

            setSilent(true)
            setAutoCancel(true)
        }

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(Constants.NOTIFICATION_MUSIC_ID, notification.build())
            // Provide a unique integer for the "notificationId" of each notification.
            mServiceMusic.startForeground(Constants.NOTIFICATION_MUSIC_ID, notification.build())
        }
    }
}
