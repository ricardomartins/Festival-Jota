package pt.rikmartins.festivaljota;

import java.util.Calendar;

import pt.rikmartins.festivaljota.provider.Mostrador;
import pt.rikmartins.festivaljota.provider.Mostrador.Noticias;
import pt.rikmartins.festivaljota.ui.PrincipalActivity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.CursorLoader;
import android.util.Log;

public class NoticiasFestivalJotaService extends IntentService {
	
	private static final String ETIQUETA = "NoticiasFestivalJotaService";
	
	public static final String ACCAO_NOTICIAS_OBTIDAS = "pt.rikmartins.festivaljota.noticias.OBTIDAS";
	public static final String ACCAO_FALHA_OBTENCAO_NOTICIAS = "pt.rikmartins.festivaljota.noticias.NAO_OBTIDAS";
	
	public static final String ARG_FORCAR_ACTUALIZACAO = "forcar_actualizacao";
	public static final String ARG_URI_NOTICIA = "uri_noticia";
	
	public NoticiasFestivalJotaService() {
		super(ETIQUETA);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(ETIQUETA, "onHandleIntent Intent extras: " + intent.getExtras());
		
		Uri resultado;
		Bundle extras = intent.getExtras();
		
		if((extras != null) && (extras.containsKey(ARG_FORCAR_ACTUALIZACAO)) && (extras.getBoolean(ARG_FORCAR_ACTUALIZACAO))){
			resultado = getContentResolver().insert(Mostrador.Noticias.CONTENT_URI_ACTUALIZAR_FORCAR, null);
		} else {
			resultado = getContentResolver().insert(Mostrador.Noticias.CONTENT_URI_ACTUALIZAR, null);
		}
		Log.v(ETIQUETA, "resultado " + resultado);
		
		Intent broadcast;
		if(resultado != null){
			broadcast = new Intent(ACCAO_NOTICIAS_OBTIDAS);
			broadcast.putExtra(ARG_URI_NOTICIA, resultado.toString());
		} else {
			broadcast = new Intent(ACCAO_FALHA_OBTENCAO_NOTICIAS);
		}
		sendBroadcast(broadcast);
	}
	
	public static class NoticiasFestivalJotaServiceReceiver extends BroadcastReceiver {
		private static final String ETIQUETA = "NoticiasFestivalJotaServiceReceiver";

		public NoticiasFestivalJotaServiceReceiver() {
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(ETIQUETA, intent.getAction() + " recebido");
			if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				ConnectivityManager gestorConectividade = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo informacaoRede = gestorConectividade.getActiveNetworkInfo();

				if ((informacaoRede != null) && (informacaoRede.isConnectedOrConnecting()) && gestorConectividade.getBackgroundDataSetting()) {
					iniciaAlarme(context);
				} else {
					terminaAlarme(context);
				}
			} else if (intent.getAction().equals(ACCAO_NOTICIAS_OBTIDAS)) {
				if (!PrincipalActivity.getVisivel()) {
					// Cria notificação

					Uri noticiaResultado = Uri.parse(intent.getStringExtra(ARG_URI_NOTICIA));
					
					CursorLoader cl = new CursorLoader(context, noticiaResultado, 
							new String[] {Noticias.COLUNA_TITULO, Noticias.COLUNA_TEXTO, Noticias.COLUNA_IMAGEM}, null, null, null);
					Cursor cur = cl.loadInBackground();
					if(cur.moveToFirst()){
						Bitmap imagemNoticia = null; // TODO: Obter da base de dados
						String tituloNoticia = cur.getString(cur.getColumnIndex(Noticias.COLUNA_TITULO));
						String textoNoticia = cur.getString(cur.getColumnIndex(Noticias.COLUNA_TEXTO));
						
						NovaNoticiaNotification.notify(context, imagemNoticia, tituloNoticia, textoNoticia);
					}
				}
			}
		}

		public static void iniciaAlarme(Context context) {
			Calendar cal = Calendar.getInstance();

			String chaveFreqSincro = context.getResources().getString(
					R.string.definicoes_chave_frequencia_sincronizacao);
			int minutosPeriodo = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(
					context).getString(chaveFreqSincro, "1440"));
			if (minutosPeriodo > 0) {
				Intent correServico = new Intent(context, NoticiasFestivalJotaService.class);
				PendingIntent pintent = PendingIntent.getService(context, 0, correServico, 0);

				AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				alarm.setInexactRepeating(AlarmManager.RTC, cal.getTimeInMillis(),
						minutosPeriodo * 60 * 1000, pintent);
				Log.v(ETIQUETA, "iniciaAlarme " + context);
			}
		}
		
		public static void terminaAlarme(Context context) {
			Log.v(ETIQUETA, "terminaAlarme " + context);

			Intent correServico = new Intent(context, NoticiasFestivalJotaService.class);
			PendingIntent pintent = PendingIntent.getService(context, 0, correServico, 0);

			AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarm.cancel(pintent);
		}
		
	}
}
