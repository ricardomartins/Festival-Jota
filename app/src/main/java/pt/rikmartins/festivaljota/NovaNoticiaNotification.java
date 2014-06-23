package pt.rikmartins.festivaljota;

import pt.rikmartins.festivaljota.ui.PrincipalActivity;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

/**
 * Helper class for showing and canceling new message notifications.
 * <p>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class NovaNoticiaNotification {
	/**
	 * The unique identifier for this type of notification.
	 */
	private static final String ETIQUETA_NOTIFICACAO = "NovaNoticiaFestivalJota";
	
	/**
	 * Shows the notification, or updates a previously shown notification of
	 * this type, with the given parameters.
	 * <p>
	 * TODO: Customize this method's arguments to present relevant content in
	 * the notification.
	 * <p>
	 * TODO: Customize the contents of this method to tweak the behavior and
	 * presentation of new message notifications. Make sure to follow the <a
	 * href="https://developer.android.com/design/patterns/notifications.html">
	 * Notification design guidelines</a> when doing so.
	 * 
	 * @see #cancel(Context)
	 */
	public static void notify(final Context context, final Bitmap imagemNoticia, final String tituloNoticia, final String textoNoticia) {
		final Resources res = context.getResources();

		final boolean notificar = PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(res.getString(R.string.definicoes_chave_notificacoes_nova_noticia),
						true);
		if (!notificar)
			return;
		
		final String toque = PreferenceManager.getDefaultSharedPreferences(context).getString(res.getString(R.string.definicoes_chave_toque_notificacao), null);
		final boolean vibrar = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(res.getString(R.string.definicoes_chave_vibracao_notificacao), false);		

		final String titulo = res.getString(R.string.nova_noticia_titulo);
		final String texto = res.getString(R.string.nova_noticia_texto, tituloNoticia);
		final String previsao = res.getString(R.string.nova_noticia_previsao);
		
		final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)

				// Set appropriate defaults for the notification light, sound,
				// and vibration.
				.setDefaults(Notification.DEFAULT_ALL)

				.setOnlyAlertOnce(true)
				// Set required fields, including the small icon, the
				// notification title, and text.
				.setSmallIcon(R.drawable.ic_stat_new_message)
				.setContentTitle(titulo)
				.setContentText(texto)

				// All fields below this line are optional.

				// Use a default priority (recognized on devices running Android
				// 4.1 or later)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)

				// Set ticker text (preview) information for this notification.
				.setTicker(previsao)

				// Set the pending intent to be initiated when the user touches
				// the notification.
				.setContentIntent(
						PendingIntent.getActivity(context, 0,
								new Intent(context, PrincipalActivity.class),
								PendingIntent.FLAG_UPDATE_CURRENT))

				// Automatically dismiss the notification when it is touched.
				.setAutoCancel(true);

		if(toque != null){
			notificationBuilder.setSound(Uri.parse(toque));
		}
		
		if(vibrar)
			notificationBuilder.setVibrate(new long[] {0, 150, 100, 150, 100, 150, 100, 150});
		else
			notificationBuilder.setVibrate(new long[] {0});
		
		if (imagemNoticia != null) {
			// Provide a large icon, shown with the notification in the
			// notification drawer on devices running Android 3.0 or later.
			notificationBuilder.setLargeIcon(imagemNoticia);
		}

		notify(context, notificationBuilder.build());
	}

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	private static void notify(final Context contexto, final Notification notificacao) {
		final NotificationManager nm = (NotificationManager) contexto
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			nm.notify(ETIQUETA_NOTIFICACAO, 0, notificacao);
		} else {
			nm.notify(ETIQUETA_NOTIFICACAO.hashCode(), notificacao);
		}
	}

	/**
	 * Cancels any notifications of this type previously shown using
	 * {@link #notify(Context, String, int)}.
	 */
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public static void cancel(final Context contexto) {
		final NotificationManager nm = (NotificationManager) contexto
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			nm.cancel(ETIQUETA_NOTIFICACAO, 0);
		} else {
			nm.cancel(ETIQUETA_NOTIFICACAO.hashCode());
		}
	}
}