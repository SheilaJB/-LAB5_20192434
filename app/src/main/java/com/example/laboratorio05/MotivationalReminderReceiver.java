package com.example.laboratorio05;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.PendingIntent;
import static android.Manifest.permission.POST_NOTIFICATIONS;
public class MotivationalReminderReceiver extends BroadcastReceiver{

    private static final String SHARED_PREF_NAME = "MedicationAppPrefs";
    private static final String KEY_MOTIVATIONAL_MESSAGE = "motivational_message";
    private static final String CHANNEL_MOTIVACIONAL = "channel_motivacional";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MotivationalReceiver", "Notificación motivacional activada");

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String mensajeMotivacional = sharedPreferences.getString(KEY_MOTIVATIONAL_MESSAGE,
                "¡Hoy es un buen día para cuidar tu salud!");

        mostrarNotificacionMotivacional(context, mensajeMotivacional);
    }

    private void mostrarNotificacionMotivacional(Context context, String mensaje) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("from_motivational_notification", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_MOTIVACIONAL)
                .setSmallIcon(R.drawable.ic_heart)
                .setContentTitle("Mensaje Motivacional")
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(999, builder.build()); // ID fijo para motivacional
            Log.d("MotivationalReceiver", "Notificación motivacional mostrada: " + mensaje);
        } else {
            Log.w("MotivationalReceiver", "Sin permisos para mostrar notificación motivacional");
        }
    }
}
