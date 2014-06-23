package pt.rikmartins.utilitarios.noticias;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public abstract class SitioNoticias {
	private static final String ETIQUETA = "SitioNoticias";

	
	private Map<String, List<Noticia>> asCategorias;
	private String categoriaActiva;

	/**
	 * Função chamada após obtenção da página, deve implementar o preenchimento
	 * do atributo <code>asCategorias</code>.
	 * 
	 * @param pagina
	 *            <code>Document</code> que contém a totalidade da página obtida
	 *            da internet
	 */
	public abstract boolean processarSitioNoticias(Document pagina);

	public abstract URL getEndereco();

	/**
	 * @param forcarObtencao
	 *            quando a página já foi obtida, <code>forcarObtencao</code> a
	 *            <code>true</code> força a reobtenção da página
	 */
	public final boolean obterPagina(boolean forcarObtencao) {
		if (estaPreenchido() && !forcarObtencao)
			return false;
		asCategorias = new HashMap<String, List<Noticia>>();
		Document pagina;
		try {
			pagina = Jsoup.connect(getEndereco().toExternalForm()).get();
		} catch (IOException e) {
			e.printStackTrace();
			pagina = null;
		}
		if (pagina != null) {
			return processarSitioNoticias(pagina);
		}
		return false;
	}

	public final boolean obterPagina() {
		return obterPagina(false);
	}

//	private class ObtensorPagina extends AsyncTask<URL, Void, Document> {
//		private SitioNoticias sitioNoticias;
//
//		public ObtensorPagina(SitioNoticias sitioNoticias) {
//			this.sitioNoticias = sitioNoticias;
//		}
//
//		@Override
//		protected Document doInBackground(URL... urls) {
//			Document pagina;
//			for (URL url : urls) {
//				try {
//					pagina = Jsoup.connect(url.toExternalForm()).get();
//					return pagina;
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Document result) {
//			super.onPostExecute(result);
//			sitioNoticias.processarSitioNoticias(result);
//		}
//	}

	public final SitioNoticias adicionarCategoria(String nomeCategoria) {
		List<Noticia> aNovaLista = new ArrayList<Noticia>();
		asCategorias.put(nomeCategoria, aNovaLista);

		categoriaActiva = nomeCategoria;
		return this;
	}

	public final SitioNoticias renomearCategoria(String nomeCategoria,
			String novoNome) {
		List<Noticia> aLista = asCategorias.get(nomeCategoria);
		asCategorias.put(novoNome, aLista);

		categoriaActiva = novoNome;
		return this;
	}

	public final SitioNoticias renomearCategoria(String novoNome) {
		return renomearCategoria(categoriaActiva, novoNome);
	}

	public final SitioNoticias removerCategoria(String categoria) {
		asCategorias.remove(categoria);

		return this;
	}

	public final SitioNoticias adicionarNoticia(String categoria,
			Noticia noticia) {
		if (!asCategorias.containsKey(categoria)) {
			adicionarCategoria(categoria);
		}
		asCategorias.get(categoria).add(noticia);

		categoriaActiva = categoria;
		return this;
	}

	public final SitioNoticias adicionarNoticia(Noticia noticia) {
		return adicionarNoticia(categoriaActiva, noticia);
	}

	public final SitioNoticias removerNoticia(String categoria, Noticia noticia) {
		asCategorias.get(categoria).remove(noticia);

		categoriaActiva = categoria;
		return this;
	}

	public final SitioNoticias removerNoticia(Noticia noticia) {
		return removerNoticia(categoriaActiva, noticia);
	}

	public final SitioNoticias removerNoticia(String categoria,
			int indiceNoticia) {
		asCategorias.get(categoria).remove(indiceNoticia);

		categoriaActiva = categoria;
		return this;
	}

	public final SitioNoticias removerNoticia(int indiceNoticia) {
		return removerNoticia(categoriaActiva, indiceNoticia);
	}

	public final List<Noticia> getListaNoticias(String categoria) {
		return asCategorias.get(categoria);
	}
	
	public final List<Noticia> getListaNoticias() {
		List<Noticia> resultado = new ArrayList<Noticia>();
		for(List<Noticia> listaNoticias : asCategorias.values()){
			resultado.addAll(listaNoticias);
		}
		return resultado;
	}
	
	public final Set<String> getListaCategorias(){
		return asCategorias.keySet();
	}

	public final boolean estaPreenchido() {
		if (asCategorias == null || asCategorias.isEmpty()) {
			return false;
		}
		for (List<Noticia> lNoticias : asCategorias.values()) {
			if (!lNoticias.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public static abstract class Noticia {
		protected Boolean valida = false;

		protected SitioNoticias sitio;
		// protected String enderecoDoSitio = "";

		protected String original = "";

		protected String titulo = "";
		protected String subtitulo = "";
		protected String texto = "";
		protected URL enderecoNoticia; // endereço absoluto
		protected URL enderecoImagem; // endereço absoluto

		protected Bitmap imagem = null;

		public Noticia() {
		}

		public Noticia(Element html) {
			preparaNoticia(html);
			this.valida = true;
		}

		public Noticia(String umaNoticia) {
			preparaNoticia(umaNoticia);
			this.valida = true;
		}

		public Noticia(Element html, SitioNoticias sitio) {
			this.sitio = sitio;
			preparaNoticia(html);
			this.valida = true;

		}

		public Noticia(String umaNoticia, SitioNoticias sitio) {
			this.sitio = sitio;
			preparaNoticia(umaNoticia);
			this.valida = true;
		}

		public abstract Noticia preparaNoticia(Element html);

		public Noticia preparaNoticia(String umaNoticia) {
			return preparaNoticia((Element) Jsoup.parse(umaNoticia));
		}

		// Obtensores directos
		public String getOriginal() {
			return this.original;
		}

		public Boolean getValida() {
			return this.valida;
		}

		public URL getEnderecoDoSitio() {
			return this.sitio.getEndereco();
		}

		public String getTitulo() {
			return this.titulo;
		}

		public void setTitulo(String titulo) {
			this.titulo = titulo;
		}

		public String getSubtitulo() {
			return this.subtitulo;
		}

		public void setSubtitulo(String subtitulo) {
			this.subtitulo = subtitulo;
		}

		public URL getEnderecoNoticia() {
			return this.enderecoNoticia;
		}

		public void setEnderecoNoticia(URL enderecoNoticia) {
			this.enderecoNoticia = enderecoNoticia;
		}

		public void setEnderecoNoticia(String enderecoNoticia) throws MalformedURLException {
			this.enderecoNoticia = new URL(enderecoNoticia);
		}

		public URL getEnderecoImagem() {
			return this.enderecoImagem;
		}

		public void setEnderecoImagem(URL enderecoImagem) {
			this.enderecoImagem = enderecoImagem;
		}

		public void setEnderecoImagem(String enderecoImagem) throws MalformedURLException {
			this.enderecoImagem = new URL(enderecoImagem);
		}

		public String getTexto() {
			return this.texto;
		}
		
		public void setTexto(String texto) {
			this.texto = texto;
		}

		public Bitmap getImagem() {
			return imagem;
		}
		
		public void setImagem(Bitmap imagem){
			this.imagem = imagem;
		}

		public void obterImagem(Object obj) throws MalformedURLException {
			if (imagem != null && obj != null) {
				posProcessarImagem(imagem, obj);
			} else {
				ObtensorImagem oi = new ObtensorImagem(obj, this);
				oi.execute(new URL[] { enderecoImagem });
			}
		}

		public abstract void processarImagem(Bitmap btm);
		
		public abstract void posProcessarImagem(Bitmap btm, Object obj);

		private int estadoObtensorImagem = ObtensorImagem.ESTADO_OBTENSOR_IMAGEM_PARADO;
		
		public int getEstadoObtensorImagem(){
			return estadoObtensorImagem;
		}
		
		public class ObtensorImagem extends AsyncTask<URL, Void, Bitmap> {
			private static final String ETIQUETA = "ObtensorImagem";
			
			public final static int ESTADO_OBTENSOR_IMAGEM_PARADO = 0;
			public final static int ESTADO_OBTENSOR_IMAGEM_A_OBTER = 1000;


			private Object obj;
			private Noticia noticia;

			public ObtensorImagem(Object obj, Noticia noticia) {
				this.obj = obj;
				this.noticia = noticia;
			}

			@Override
			protected Bitmap doInBackground(URL... urls) {
				try {
					for (URL url : urls) {
						while (noticia.getEstadoObtensorImagem() != ESTADO_OBTENSOR_IMAGEM_PARADO){
							Thread.sleep(200);
							Log.v(ETIQUETA, "À espera");
						}
						noticia.estadoObtensorImagem = ESTADO_OBTENSOR_IMAGEM_A_OBTER;
						
						if (noticia.getImagem() != null) {
							return noticia.getImagem();
						}
						URLConnection conn = url.openConnection();
						Bitmap btm = BitmapFactory.decodeStream(conn.getInputStream());

						noticia.processarImagem(btm);

						if (btm != null) {
							noticia.imagem = btm;
						}
						return btm;
					}
				} catch (Exception ex) {
				} finally {
					noticia.estadoObtensorImagem = ESTADO_OBTENSOR_IMAGEM_PARADO;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Bitmap btm) {
				super.onPostExecute(btm);
				if (obj != null) {
					noticia.posProcessarImagem(btm, obj);
				}
			}
		}
	}
}
