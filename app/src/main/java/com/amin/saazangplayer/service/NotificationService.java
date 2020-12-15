package com.amin.saazangplayer.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.amin.saazangplayer.R;
import com.amin.saazangplayer.constants.NotificationConstants;
import com.amin.saazangplayer.model.AudioModel;
import com.amin.saazangplayer.view.MainActivity;

import java.util.List;

public class NotificationService extends Service {
    public RemoteViews bigViews;
    private static NotificationService notificationService;
    private final String LOG_TAG = "NotificationService";
    private Notification notification;
    NotificationManager mNotificationManager;
    private IBinder iBinder = new LocalBinder();

    public static NotificationService getInstance() {
        return notificationService;
    }

    public class LocalBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationService = this;
        if (intent != null) {
            if (intent.getAction().equals(NotificationConstants.ACTION.STARTFOREGROUND_ACTION)) {
                showNotification();
                updateNotificationContents();
                // notification.bigContentView = bigViews;
                startAndUpdateState();

            } else if (MainActivity.getInstance() != null) {
                if (intent.getAction().equals(NotificationConstants.ACTION.PREV_ACTION)) {
                    MainActivity.getInstance().previousMusic();

                } else if (intent.getAction().equals(NotificationConstants.ACTION.PLAY_ACTION)) {
                    boolean isPlaying = MainActivity.getInstance().playAndPause();

                    if (isPlaying)
                        bigViews.setImageViewResource(R.id.imageButtonPlayNotification, R.drawable.ic_baseline_stop_64);
                    else
                        bigViews.setImageViewResource(R.id.imageButtonPlayNotification, R.drawable.ic_baseline_play_arrow_64);

                    startAndUpdateState();

                } else if (intent.getAction().equals(NotificationConstants.ACTION.NEXT_ACTION)) {
                    MainActivity.getInstance().nextMusic();

                } else if (intent.getAction().equals(NotificationConstants.ACTION.STOPFOREGROUND_ACTION)) {
                    stopForeground(true);
                    stopSelf();
                    System.exit(0);
                }
            }

        }
        return START_STICKY;
    }

    private void showNotification() {

// Using RemoteViews to bind custom layouts into Notification
        bigViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
        // showing default album image
        bigViews.setImageViewBitmap(R.id.imageViewAlbumArtNotification, NotificationConstants.getDefaultAlbumArt(this));

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent previousIntent = new Intent(this, NotificationService.class);
        previousIntent.setAction(NotificationConstants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, NotificationService.class);
        playIntent.setAction(NotificationConstants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, NotificationService.class);
        nextIntent.setAction(NotificationConstants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, NotificationService.class);
        closeIntent.setAction(NotificationConstants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        bigViews.setOnClickPendingIntent(R.id.imageButtonPlayNotification, pplayIntent);

        bigViews.setOnClickPendingIntent(R.id.imageButtonNextNotification, pnextIntent);

        bigViews.setOnClickPendingIntent(R.id.imageButtonPreviousNotification, ppreviousIntent);

        bigViews.setOnClickPendingIntent(R.id.imageButtonExit, pcloseIntent);

        bigViews.setImageViewResource(R.id.imageButtonPlayNotification, R.drawable.ic_baseline_stop_64);
        // bigViews.setTextViewText(R.id.textViewMusicNameNotification, "Song Title");
        //bigViews.setTextViewText(R.id.textViewArtistNotification, "Artist Name");
        //bigViews.setTextViewText(R.id.textViewAlbumNotification, "Album Name");
        notification = getNotification();
        notification.bigContentView = bigViews;
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        notification.icon = R.mipmap.ic_launcher_round;

        notification.contentIntent = pendingIntent;
        startForeground(NotificationConstants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);

    }

    public Notification getNotification() {

        String channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = createChannel();
        else {
            channel = "";
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channel).setSmallIcon(R.drawable.notification_icon).setContentTitle("Music Player");
        Notification notification = mBuilder
                .setPriority(0)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        return notification;
    }

    @NonNull
    @TargetApi(26)
    private synchronized String createChannel() {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        String name = "Splayer";
        String id = "Splayer";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }
        return id;
    }

    public void updateNotificationContents() {
        try {
            List<AudioModel> audioModels = MainActivity.getInstance().getSelectedAudioModelList();

            if (audioModels != null && audioModels.size() > 0) {
                AudioModel audioModel = audioModels.get(MainActivity.getInstance().getMusicIndex());

                bigViews.setImageViewBitmap(R.id.imageViewAlbumArtNotification, MainActivity.getInstance().musicViewModel.getAlbumArt(audioModel.getPath()));

               /* StringBuilder sb = new StringBuilder();
                sb.append(MainActivity.getInstance().getString(R.string.music_name))
                        .append(": ").append(audioModel.getName()).append("\n")
                        .append(MainActivity.getInstance().getString(R.string.artist))
                        .append(": ").append(audioModel.getArtist()).append("\n")
                        .append(MainActivity.getInstance().getString(R.string.album_name))
                        .append(": ").append(audioModel.getAlbum());*/
                bigViews.setTextViewText(R.id.textViewMusicNotification, MainActivity.getInstance().getString(R.string.music_name) + ": " + audioModel.getName());
                bigViews.setTextViewText(R.id.textViewMusicArtistNotification, MainActivity.getInstance().getString(R.string.artist) + ": " + audioModel.getArtist());
                bigViews.setTextViewText(R.id.textViewMusicAlbumNotification, MainActivity.getInstance().getString(R.string.album_name) + ": " + audioModel.getAlbum());


                mNotificationManager.notify(NotificationConstants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
                //startAndUpdateState();

            }

        } catch (Exception e) {
            Log.d("Notify", e.getMessage());
        }

    }


    public void startAndUpdateState() {
        if (notification != null)
            startForeground(NotificationConstants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
    }

    public RemoteViews getBigViews() {
        return bigViews;
    }


}
