package pt.rikmartins.festivaljota.provider;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import pt.rikmartins.festivaljota.R;
import pt.rikmartins.festivaljota.provider.Mostrador.Noticias;
import pt.rikmartins.festivaljota.provider.Mostrador.Sincronismos;
import pt.rikmartins.festivaljota.utilitarios.SitioNoticiasFestivalJota2013;
import pt.rikmartins.festivaljota.utilitarios.SitioNoticiasFestivalJota2013.NoticiaFestivalJota;
import pt.rikmartins.utilitarios.noticias.SitioNoticias.Noticia;

public class FestivalJotaProvider extends ContentProvider {

	private static final String ETIQUETA = "FestivalJotaProvider";

	private static HashMap<String, String> sNoticiasProjectionMap;
	private static HashMap<String, String> sSincronismosProjectionMap;

	private static final int ID_NOTICIA	= 1;
	private static final int CATEGORIA_NOTICIAS = 2;
	private static final int NOTICIAS = 3;
	private static final int ACTUALIZAR_NOTICIAS = 11;
	private static final int ACTUALIZAR_NOTICIAS_FORCAR = 12;
	
	private static final UriMatcher sUriMatcher;

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, Mostrador.NOME_BASEDADOS, null, Mostrador.VERSAO_BASEDADOS);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + Mostrador.NOME_TABELA_NOTICIAS + " (" + Noticias._ID
					+ " INTEGER PRIMARY KEY," + Noticias.COLUNA_CATEGORIA + " TEXT,"
					+ Noticias.COLUNA_TITULO + " TEXT," + Noticias.COLUNA_SUBTITULO + " TEXT,"
					+ Noticias.COLUNA_TEXTO + " TEXT," + Noticias.COLUNA_ENDERECO + " TEXT,"
					+ Noticias.COLUNA_ENDERECOIMAGEM + " TEXT," + Noticias.COLUNA_IMAGEM + " BLOB,"
					+ Noticias.COLUNA_ORDEM + " INTEGER" + ");");
			db.execSQL("CREATE TABLE " + Mostrador.NOME_TABELA_SINCRONISMO + " ("
					+ Sincronismos._ID + " INTEGER PRIMARY KEY," + Sincronismos.COLUNA_HORA
					+ " INTEGER" + ");");
			db.execSQL("CREATE TRIGGER IF NOT EXISTS insertNoticia "
					+ "BEFORE INSERT ON noticias FOR EACH ROW "
					+ "WHEN ((SELECT count() FROM noticias "
					+ "WHERE texto = NEW.texto and titulo = NEW.titulo) > 0) " + "BEGIN "
					+ "SELECT RAISE(IGNORE); " + "END;");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO: Se possível aquando de uma nova versão, a base de dados
			// deve ser convertida e não apagada
			Log.w(ETIQUETA, "Upgrading database from version " + oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + Mostrador.NOME_TABELA_NOTICIAS);
			db.execSQL("DROP TABLE IF EXISTS " + Mostrador.NOME_TABELA_SINCRONISMO);
			onCreate(db);
		}
	}

	private DatabaseHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}
	
	private static boolean emActualizacao = false;

	private Uri actualizarDados(boolean forcar) {
		Log.v(ETIQUETA, "forçar: " + forcar);
		
		if (emActualizacao)
			return null;
		try {
			emActualizacao = true;
			SQLiteDatabase baseDados = mOpenHelper.getWritableDatabase();
			Uri resultado = actualizarDados(forcar, baseDados);

			Log.d(ETIQUETA, "resultado: " + resultado);
			return resultado;
		} finally {
			emActualizacao = false;
		}
	}

	private Uri actualizarDados(boolean forcar, SQLiteDatabase baseDados) {
		SQLiteQueryBuilder qbSinc = new SQLiteQueryBuilder();
		qbSinc.setTables(Mostrador.NOME_TABELA_SINCRONISMO);

		Cursor cSinc = qbSinc.query(baseDados, new String[] { Sincronismos._ID, Sincronismos.COLUNA_HORA }, null,
				null, null, null, null, "1");

		Log.v(ETIQUETA, "Verificar se já passou o tempo definido na frequência de actualização");
		// Verificar se já passou o tempo definido na frequência de actualização
		if (cSinc.moveToFirst()) {
			Date horaSinc = new Date(cSinc.getLong(cSinc
					.getColumnIndexOrThrow(Sincronismos.COLUNA_HORA)));
			Log.d(ETIQUETA, "horaSinc: " + horaSinc);

			Date limite = new Date();
			if (forcar)
				limite.setTime(limite.getTime() - 60 * 1000); // Subtrair um
																// minuto
			else {
				String chaveFreqSincro = getContext().getResources().getString(R.string.definicoes_chave_frequencia_sincronizacao);
				int minutosPeriodo = Integer.parseInt(PreferenceManager
						.getDefaultSharedPreferences(getContext()).getString(chaveFreqSincro,
								"1440"));
				if(minutosPeriodo < 0){
					return null;
				}
				limite.setTime(limite.getTime() - minutosPeriodo * 60 * 1000); // Subtrair
																				// o
																				// tempo
																				// configurado
			}
			Log.d(ETIQUETA, "limite: " + limite);

			if (horaSinc.after(limite)){
				Log.v(ETIQUETA, "horaSinc depois do limite");
				return null;
			}
		}

		Log.v(ETIQUETA, "Verificar se há ligação à internet");

		// Verificar se há ligação à internet
		SitioNoticiasFestivalJota2013 sitioNoticias = new SitioNoticiasFestivalJota2013(getContext());
		try {
			ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(
					Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			if (!activeNetwork.isConnectedOrConnecting())
				return null;
		} catch (NullPointerException npex) {
			return null;
		}

		Log.v(ETIQUETA, "Tentar obter a página");
		// Tentar obter a página
		if (!sitioNoticias.obterPagina())
			return null;

		Log.v(ETIQUETA, "Verificar se há notícias novas");
		// Verificar se há notícias novas
		qbSinc.setTables(Mostrador.NOME_TABELA_NOTICIAS);
		Cursor cTotal = qbSinc.query(baseDados, new String[] { Noticias.COLUNA_TITULO,
				Noticias.COLUNA_TEXTO }, null, null, null, null, Noticias.ORDEM_DEFEITO);

		List<Noticia> noticias = sitioNoticias.getListaNoticias();

		Noticia novaNoticia = null;
		
		if (cTotal.getCount() == noticias.size()) {

			cTotal.moveToFirst();
			for (cTotal.moveToFirst(); !cTotal.isAfterLast(); cTotal.moveToNext()) {
				String bdTitulo = cTotal.getString(cTotal.getColumnIndex(Noticias.COLUNA_TITULO));
				String bdTexto = cTotal.getString(cTotal.getColumnIndex(Noticias.COLUNA_TEXTO));

				boolean encontrado = false;
				Noticia noticia = null;
				for (int i = 0; i < noticias.size(); i++) {
					noticia = noticias.get(i);
					if (bdTitulo.equals(noticia.getTitulo())) {
						if (bdTexto.equals(noticia.getTexto())) {
							encontrado = true;
							break;
						}
					}
				}
				if (!encontrado) {
					novaNoticia = noticia;
					break;
				}
				if (noticia != null)
					noticias.remove(noticia);
			}
			if (noticias.isEmpty()) {
				return null;
			}
		}

		Log.v(ETIQUETA, "É necessário actualizar");

		Log.v(ETIQUETA, "Actualizar hora da última obtenção");
		// Actualizar hora da última obtenção
		Long agora = Long.valueOf(System.currentTimeMillis());
		ContentValues horaObtencao = new ContentValues();
		horaObtencao.put(Sincronismos.COLUNA_HORA, agora);
		if ((cSinc != null) && (cSinc.moveToFirst())) {
			String onde = Sincronismos._ID + " = " + cSinc.getInt(cSinc.getColumnIndex(Sincronismos._ID));
			
			Log.v(ETIQUETA, "A actualizar hora de obtenção >> onde: " + onde + "; horaObtencao: " + horaObtencao);

			baseDados.update(Mostrador.NOME_TABELA_SINCRONISMO, horaObtencao, onde, null);
		} else {
			Log.v(ETIQUETA, "A actualizar hora de obtenção >> horaObtencao: " + horaObtencao);
			baseDados.insertOrThrow(Mostrador.NOME_TABELA_SINCRONISMO, null, horaObtencao);
		}

		Log.v(ETIQUETA, "Apagar todos os registos da tabela noticias");
		baseDados.delete(Mostrador.NOME_TABELA_NOTICIAS, null, null);

		Log.v(ETIQUETA, "Inserir informação obtida da página na base de dados");

		Uri resultado = null;
		int ordem = 1;

		String[] listaCategorias = new String[] {
				SitioNoticiasFestivalJota2013.CATEGORIA_DESTAQUES,
				SitioNoticiasFestivalJota2013.CATEGORIA_NOTICIAS };
		
		for (String categoria : listaCategorias) {
			for (Noticia noticiaOrig : sitioNoticias.getListaNoticias(categoria)) {
				NoticiaFestivalJota noticia = (NoticiaFestivalJota) noticiaOrig;
				
				ContentValues valores = new ContentValues();

				valores.put(Noticias.COLUNA_CATEGORIA, categoria);
				valores.put(Noticias.COLUNA_ORDEM, ordem);

				valores.put(Noticias.COLUNA_TITULO, noticia.getTitulo());
				valores.put(Noticias.COLUNA_SUBTITULO, noticia.getSubtitulo());
				valores.put(Noticias.COLUNA_TEXTO, noticia.getTexto());
				if (noticia.getEnderecoNoticia() != null) {
					valores.put(Noticias.COLUNA_ENDERECO, noticia
							.getEnderecoNoticia().toExternalForm());
				} else {
					// TODO: Verificar se é necessário fazer alguma coisa em
					// substituição
				}
				if (noticia.getEnderecoImagem() != null) {
					valores.put(Noticias.COLUNA_ENDERECOIMAGEM, noticia
							.getEnderecoImagem().toExternalForm());
				} else {
					// TODO: Verificar se é necessário fazer alguma coisa em
					// substituição
				}

				long id = baseDados.insert(Mostrador.NOME_TABELA_NOTICIAS,
						null, valores);
				
				if (((novaNoticia == null) && (resultado != null)) || ((novaNoticia != null) && novaNoticia.equals(noticiaOrig))) {
					resultado = Uri.withAppendedPath(Noticias.CONTENT_URI_ID,
							Long.toString(id));
				}

				ordem++;
			}
		}
		
//		for (Noticia destaque : sitioNoticias
//				.getListaNoticias(SitioNoticiasFestivalJota2013.CATEGORIA_DESTAQUES)) {
//			ContentValues valores = new ContentValues();
//
//			valores.put(Noticias.COLUNA_CATEGORIA,
//					SitioNoticiasFestivalJota2013.CATEGORIA_DESTAQUES);
//			valores.put(Noticias.COLUNA_ORDEM, ordem);
//
//			valores.put(Noticias.COLUNA_TITULO, destaque.getTitulo());
//			valores.put(Noticias.COLUNA_SUBTITULO, destaque.getSubtitulo());
//			valores.put(Noticias.COLUNA_TEXTO, destaque.getTexto());
//			valores.put(Noticias.COLUNA_ENDERECO, destaque.getEnderecoNoticia().toExternalForm());
//			valores.put(Noticias.COLUNA_ENDERECOIMAGEM, destaque.getEnderecoImagem()
//					.toExternalForm());
//
//			long id = baseDados.insert(Mostrador.NOME_TABELA_NOTICIAS, null, valores);
//
//			if (resultado == null) {
//				resultado = Uri.withAppendedPath(Noticias.CONTENT_URI_ID, Long.toString(id));
//			}
//
//			ordem++;
//		}
//		for (Noticia destaque : sitioNoticias
//				.getListaNoticias(SitioNoticiasFestivalJota2013.CATEGORIA_NOTICIAS)) {
//			ContentValues valores = new ContentValues();
//
//			valores.put(Noticias.COLUNA_CATEGORIA, SitioNoticiasFestivalJota2013.CATEGORIA_NOTICIAS);
//			valores.put(Noticias.COLUNA_ORDEM, ordem);
//
//			valores.put(Noticias.COLUNA_TITULO, destaque.getTitulo());
//			valores.put(Noticias.COLUNA_SUBTITULO, destaque.getSubtitulo());
//			valores.put(Noticias.COLUNA_TEXTO, destaque.getTexto());
//			valores.put(Noticias.COLUNA_ENDERECO, destaque.getEnderecoNoticia().toExternalForm());
//			valores.put(Noticias.COLUNA_ENDERECOIMAGEM, destaque.getEnderecoImagem()
//					.toExternalForm());
//
//			long id = baseDados.insert(Mostrador.NOME_TABELA_NOTICIAS, null, valores);
//
//			if (resultado == null) {
//				resultado = Uri.withAppendedPath(Noticias.CONTENT_URI, "id/" + id);
//			}
//
//			ordem++;
//		}

		return resultado;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		int correspondencia = sUriMatcher.match(uri);
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(Mostrador.NOME_TABELA_NOTICIAS);

		switch (correspondencia) {
		case NOTICIAS:
			qb.setProjectionMap(sNoticiasProjectionMap);
			break;

		case ID_NOTICIA:
			qb.setProjectionMap(sNoticiasProjectionMap);
			qb.appendWhere(Noticias._ID + " = " + uri.getPathSegments().get(2));
			break;

		case CATEGORIA_NOTICIAS:
			qb.setProjectionMap(sNoticiasProjectionMap);
			qb.appendWhere(Noticias.COLUNA_CATEGORIA + " = '" + uri.getPathSegments().get(2) + "'");
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = Mostrador.Noticias.ORDEM_DEFEITO;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		// c.setNotificationUri(getContext().getContentResolver(), Mostrador.Noticias.CONTENT_URI);

		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case NOTICIAS:
		case ID_NOTICIA:
		case CATEGORIA_NOTICIAS:
			return Noticias.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (initialValues != null) {
			throw new IllegalArgumentException("Não são permitidos valores iniciais");
		}

		Uri resultado = null;
		
		int correspondencia = sUriMatcher.match(uri);

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(Mostrador.NOME_TABELA_NOTICIAS);

		switch (correspondencia) {
		case ACTUALIZAR_NOTICIAS:
			resultado = actualizarDados(false);
			break;
		case ACTUALIZAR_NOTICIAS_FORCAR:
			resultado = actualizarDados(true);
			break;
		default:
			throw new IllegalArgumentException("URI desconhecido " + uri);
		}
		
		return resultado;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		throw new UnsupportedOperationException("Operação não suportada!");
//		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//		int count;
//		switch (sUriMatcher.match(uri)) {
//		case NOTICIAS:
//			count = db.delete(Mostrador.NOME_TABELA_NOTICIAS, where, whereArgs);
//			break;
//
//		case CATEGORIA_NOTICIAS:
//			String noteId = uri.getPathSegments().get(1);
//			count = db.delete(Mostrador.NOME_TABELA_NOTICIAS, Noticias._ID + "=" + noteId
//					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
//			break;
//
//		default:
//			throw new IllegalArgumentException("Unknown URI " + uri);
//		}
//
//		getContext().getContentResolver().notifyChange(uri, null);
//		return count;
	}

	@Override
	public int update(Uri uri, ContentValues valores, String where, String[] whereArgs) {
		if((!valores.containsKey(Noticias.COLUNA_IMAGEM)) || (valores.size() != 1)){
			throw new IllegalArgumentException("Apenas disponível para inserir imagens nos registos");
		}
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int contagem;
		switch (sUriMatcher.match(uri)) {
		case ID_NOTICIA:
			String idNoticia = uri.getPathSegments().get(2);
			contagem = db.update(Mostrador.NOME_TABELA_NOTICIAS, valores, Noticias._ID + " = " + idNoticia/* + " AND " + Noticias.COLUNA_IMAGEM + " = NULL"*/, null);
			break;

		default:
			throw new IllegalArgumentException("URI desconhecido " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return contagem;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Mostrador.AUTORIDADE, "noticias", NOTICIAS);
		sUriMatcher.addURI(Mostrador.AUTORIDADE, "noticias/categoria/*", CATEGORIA_NOTICIAS);
		sUriMatcher.addURI(Mostrador.AUTORIDADE, "noticias/id/#", ID_NOTICIA);
		sUriMatcher.addURI(Mostrador.AUTORIDADE, "actualizar/noticias", ACTUALIZAR_NOTICIAS);
		sUriMatcher.addURI(Mostrador.AUTORIDADE, "actualizar/noticias/forcar", ACTUALIZAR_NOTICIAS_FORCAR);
		
		sNoticiasProjectionMap = new HashMap<String, String>();
		sNoticiasProjectionMap.put(Noticias._ID, Noticias._ID);
		sNoticiasProjectionMap.put(Noticias.COLUNA_CATEGORIA, Noticias.COLUNA_CATEGORIA);
		sNoticiasProjectionMap.put(Noticias.COLUNA_TITULO, Noticias.COLUNA_TITULO);
		sNoticiasProjectionMap.put(Noticias.COLUNA_SUBTITULO, Noticias.COLUNA_SUBTITULO);
		sNoticiasProjectionMap.put(Noticias.COLUNA_TEXTO, Noticias.COLUNA_TEXTO);
		sNoticiasProjectionMap.put(Noticias.COLUNA_ENDERECO, Noticias.COLUNA_ENDERECO);
		sNoticiasProjectionMap.put(Noticias.COLUNA_ENDERECOIMAGEM, Noticias.COLUNA_ENDERECOIMAGEM);
		sNoticiasProjectionMap.put(Noticias.COLUNA_IMAGEM, Noticias.COLUNA_IMAGEM);
		sNoticiasProjectionMap.put(Noticias.COLUNA_ORDEM, Noticias.COLUNA_ORDEM);

		sSincronismosProjectionMap = new HashMap<String, String>();
		sSincronismosProjectionMap.put(Sincronismos._ID, Sincronismos._ID);
		sSincronismosProjectionMap.put(Sincronismos.COLUNA_HORA, Sincronismos.COLUNA_HORA);
	}

}
