package com.example.laboratorio05;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import static android.Manifest.permission.POST_NOTIFICATIONS;
public class MedicationReminderReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MedicationReceiver", "Notificación recibida");

        // Obtener datos del medicamento desde el Intent
        String nombreMedicamento = intent.getStringExtra("medicamento_nombre");
        String tipoMedicamento = intent.getStringExtra("medicamento_tipo");
        String dosisMedicamento = intent.getStringExtra("medicamento_dosis");
        int frecuenciaHoras = intent.getIntExtra("medicamento_frecuencia", 8);

        mostrarNotificacionMedicamento(context, nombreMedicamento, tipoMedicamento, dosisMedicamento);
        reprogramarSiguienteNotificacion(context, intent, frecuenciaHoras);
    }

    private void mostrarNotificacionMedicamento(Context context, String nombre, String tipo, String dosis) {
        // Intent para abrir MainActivity al presionar la notificación
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("from_notification", true);
        intent.putExtra("medicamento_nombre", nombre);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String channelId = getChannelByMedicationType(tipo);

        int iconoMedicamento = getIconByMedicationType(tipo);

        String accionSugerida = getActionByMedicationType(tipo, dosis);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(iconoMedicamento)
                .setContentTitle("Recordatorio: " + nombre)
                .setContentText(accionSugerida)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setCategory(NotificationCompat.CATEGORY_REMINDER);

        // Mostrar notificación
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Verificar permisos antes de mostrar (Android 13+)
        if (ActivityCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            int notificationId = generateNotificationId(nombre);
            notificationManager.notify(notificationId, builder.build());
            Log.d("MedicationReceiver", "Notificación mostrada para: " + nombre);
        } else {
            Log.w("MedicationReceiver", "Sin permisos para mostrar notificaciones");
        }
    }

    private void reprogramarSiguienteNotificacion(Context context, Intent originalIntent, int frecuenciaHoras) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long nextNotificationTime = System.currentTimeMillis() + (frecuenciaHoras * 60 * 60 * 1000L);

        String nombre = originalIntent.getStringExtra("medicamento_nombre");
        int requestCode = generateNotificationId(nombre);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                originalIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextNotificationTime,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        nextNotificationTime,
                        pendingIntent
                );
            }

            Log.d("MedicationReceiver", "Próxima notificación programada para: " + nombre +
                    " en " + frecuenciaHoras + " horas");

        } catch (Exception e) {
            Log.e("MedicationReceiver", "Error al reprogramar notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private String getChannelByMedicationType(String tipo) {
        if (tipo == null) return "channel_pastilla";

        switch (tipo.toLowerCase()) {
            case "pastilla":
                return "channel_pastilla";
            case "jarabe":
                return "channel_jarabe";
            case "ampolla":
                return "channel_ampolla";
            case "capsula":
            case "cápsula":
                return "channel_capsula";
            default:
                return "channel_pastilla";
        }
    }

    private int getIconByMedicationType(String tipo) {
        if (tipo == null) return R.drawable.ic_pill;

        switch (tipo.toLowerCase()) {
            case "pastilla":
                return R.drawable.ic_pill;
            case "jarabe":
                return R.drawable.ic_syrup;
            case "ampolla":
                return R.drawable.ic_injection;
            case "capsula":
            case "cápsula":
                return R.drawable.ic_capsule;
            default:
                return R.drawable.ic_pill;
        }
    }

    // Crear acción sugerida según tipo y dosis
    private String getActionByMedicationType(String tipo, String dosis) {
        if (tipo == null || dosis == null) return "Tomar medicamento";

        switch (tipo.toLowerCase()) {
            case "pastilla":
                return "Tomar " + dosis;
            case "jarabe":
                return "Tomar " + dosis;
            case "ampolla":
                return "Aplicar " + dosis;
            case "capsula":
            case "cápsula":
                return "Tomar " + dosis;
            default:
                return "Tomar " + dosis;
        }
    }

    private int generateNotificationId(String medicamentoNombre) {
        return medicamentoNombre != null ? medicamentoNombre.hashCode() : 0;
    }


}
