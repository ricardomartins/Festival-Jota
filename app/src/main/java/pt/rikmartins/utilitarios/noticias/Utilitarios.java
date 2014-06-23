package pt.rikmartins.utilitarios.noticias;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.MalformedURLException;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Utilitarios { 

	public static Bitmap puxarImagem(String urlImagem)
			throws IOException, MalformedURLException {

		URL url;
		URLConnection coneccao;
		url = new URL(urlImagem); 
		coneccao = url.openConnection();
		// coneccao.setUseCaches(true);
		coneccao.connect();
		Object response = coneccao.getContent();
		if (response instanceof InputStream) {
			Bitmap bitmap = BitmapFactory.decodeStream((InputStream) response);
			return bitmap;
		}
		return null;
	}

	public static ResponseCache definirCache(final Context actPrin) {
		// TODO: Pode ser necessário criar filtros de o que guardar e talvez definir o limite de tempo que vai ficar guradado
		ResponseCache aResponseCache = new ResponseCache() {
			@Override
			public CacheResponse get(URI uri, String s,
					Map<String, List<String>> headers) throws IOException {

				File dirCache = actPrin.getCacheDir();

				final File file = new File(dirCache, escape(uri.getPath()));
				if (file.exists()) {
					return new CacheResponse() {
						@Override
						public Map<String, List<String>> getHeaders()
								throws IOException {
							return null;
						}

						@Override
						public InputStream getBody() throws IOException {
							return new FileInputStream(file);
						}
					};
				} else {
					return null;
				}
			}

			@Override
			public CacheRequest put(URI uri, URLConnection urlConnection)
					throws IOException {

				File dirCache = actPrin.getCacheDir();

				final File file = new File(dirCache, escape(urlConnection
						.getURL().getPath()));
				return new CacheRequest() {
					@Override
					public OutputStream getBody() throws IOException {
						return new FileOutputStream(file);
					}

					@Override
					public void abort() {
						file.delete();
					}
				};
			}

			private String escape(String url) {
				return url.replace("/", "-").replace(".", "-");
			}

		};
		ResponseCache.setDefault(aResponseCache);

		return aResponseCache;
	}
}
