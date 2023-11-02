package com.example.sqlproject.Fragment.Adapter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.sqlproject.MainActivity; // Import the activity where you want to navigate
import com.example.sqlproject.R;
import com.example.sqlproject.SqlActivity;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Play the alarm sound
        MediaPlayer music = MediaPlayer.create(context, R.raw.ringtone);
        music.start();

        String description=intent.getStringExtra("description");
        String date=intent.getStringExtra("date");
        String time=intent.getStringExtra("time");

        Intent i=new Intent(context, SqlActivity.class);
        i.putExtra("from","notification");
        i.putExtra("description",description);
        i.putExtra("date",date);
        i.putExtra("time",time);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        context.startActivity(i);

        int notificationId= (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, i, PendingIntent.FLAG_IMMUTABLE);

        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.ringtone);
       // String description = intent.getStringExtra("description");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "androidKnowledge")
                .setSmallIcon(R.drawable.notifications_active_24)
                .setContentTitle("Reminder")
                .setContentText("It's time to wake up: " + description)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        // Check for permission and then show the notification
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // If you haven't already, consider calling ActivityCompat#requestPermissions here
            // to request the missing permissions. Then, handle the case where the user grants the permission.
        } else {
            notificationManagerCompat.notify(notificationId, builder.build());
        }
    }
}
