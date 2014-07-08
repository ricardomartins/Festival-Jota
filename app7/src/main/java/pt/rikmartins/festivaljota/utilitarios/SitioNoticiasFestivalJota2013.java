package pt.rikmartins.festivaljota.utilitarios;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import pt.rikmartins.festivaljota.provider.Mostrador.Noticias;
import pt.rikmartins.utilitarios.noticias.SitioNoticias;

public class SitioNoticiasFestivalJota2013 extends SitioNoticias {
	
	private static final String ETIQUETA = "SitioNoticiasFestivalJota2013";
	
	private URL oEndereco = null;
	// private FragmentPagerAdapter adaptadorFragmentos;
	// private FragmentManager gestorFragmentos;
	// /**
	// * Construtor da classe
	// *
	// * @param listaDestaques
	// * <code>AbsListView</code> contentora dos detalhes
	// * @param listaNoticias
	// * <code>AbsListView</code> contentora das notícias
	// */
	// public SitioNoticiasFestivalJota2013(FragmentPagerAdapter
	// adaptadorFragmentos, FragmentManager gestorFragmentos) {
	// Log.d("pt.rikmartins.festivaljota",
	// "Construtor do SitioNoticiasFestivalJota2013");
	//
	// this.adaptadorFragmentos = adaptadorFragmentos;
	// this.gestorFragmentos = gestorFragmentos;
	// }

	public static final String CATEGORIA_DESTAQUES = "Destaques";
	public static final String CATEGORIA_NOTICIAS = "Notícias";

	protected Context contexto;
	
	private SitioNoticiasFestivalJota2013(){
		super();
	}
	
	public SitioNoticiasFestivalJota2013(Context contexto){
		this.contexto = contexto;
	}
	
	@Override
	public URL getEndereco() {
		if (oEndereco == null) {
			try {
				oEndereco = new URL("http://www.festivaljota.com/");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return oEndereco;
	}

	@Override
	public boolean processarSitioNoticias(Document pagina) {

		Log.d("pt.rikmartins.festivaljota",
				"processarSitioNoticias do SitioNoticiasFestivalJota2013");

		if (pagina == null) {
			return false;
		}
		Element destaque = obterDestaque(pagina);
		adicionarNoticia(CATEGORIA_DESTAQUES,
				new NoticiaFestivalJota2013Destaque(destaque, this, contexto));

		Elements noticias = obterTodasNoticias(pagina);
		for (Element noticia : noticias) {
			adicionarNoticia(CATEGORIA_NOTICIAS, new NoticiaFestivalJota2013(
					noticia, this, contexto)); // Alterar isto para ser obtido do valor de
										// string (xml)
		}
		return true;
	}

	private Elements obterTodasNoticias(Document pagina) {
		return pagina.getElementById("colesq").getElementsByClass("destaque");
	}

	private Element obterDestaque(Document pagina) {
		Element destaque = pagina.getElementById("maindestaque");

		return destaque.parent();
	}

	public static abstract class NoticiaFestivalJota extends Noticia {
		
		protected Context contexto;

		public NoticiaFestivalJota(Context contexto) {
			super();
			this.contexto = contexto;
		}

		public NoticiaFestivalJota(Element html, SitioNoticias sitio, Context contexto) {
			super(html, sitio);
			this.contexto = contexto;
		}

		public NoticiaFestivalJota(Element html, Context contexto) {
			super(html);
			this.contexto = contexto;
		}

		public NoticiaFestivalJota(String umaNoticia, SitioNoticias sitio, Context contexto) {
			super(umaNoticia, sitio);
			this.contexto = contexto;
		}

		public NoticiaFestivalJota(String umaNoticia, Context contexto) {
			super(umaNoticia);
			this.contexto = contexto;
		}

		protected Uri enderecoProvider; // endereço no Provider
		
		public Uri getEnderecoProvider(){
			return enderecoProvider;
		}
		
		public void setEnderecoProvider(Uri enderecoProvider){
			this.enderecoProvider = enderecoProvider;
		}

		@Override
		public void processarImagem(Bitmap btm) {
			if ((contexto != null) && (btm != null)) {
				ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
				btm.compress(Bitmap.CompressFormat.PNG, 80, byteArrayBitmapStream);
				byte[] babtm = byteArrayBitmapStream.toByteArray();

				ContentValues cv = new ContentValues();
				cv.put(Noticias.COLUNA_IMAGEM, babtm);

				int resultado = contexto.getContentResolver().update(getEnderecoProvider(), cv,
						null, null);
				Log.v(ETIQUETA, "resultado do " + getEnderecoProvider().toString() + " = " + resultado);
			}
		}

		@Override
		public void posProcessarImagem(Bitmap btm, Object obj) {
			if ((btm != null) && (obj != null)) {
                ((ImageView) obj).setImageBitmap(btm);
            } else {
				if(obj != null){
					// Esconder obj
					((View) obj).setVisibility(View.GONE);
				}
			}
		}
	}
	
	public static class NoticiaFestivalJota2013 extends NoticiaFestivalJota {

		public NoticiaFestivalJota2013(Context contexto) {
			super(contexto);
		}

		public NoticiaFestivalJota2013(Element html, SitioNoticias sitio, Context contexto) {
			super(html, sitio, contexto);
		}

		public NoticiaFestivalJota2013(Element html, Context contexto) {
			super(html, contexto);
		}

		public NoticiaFestivalJota2013(String umaNoticia, SitioNoticias sitio, Context contexto) {
			super(umaNoticia, sitio, contexto);
		}

		public NoticiaFestivalJota2013(String umaNoticia, Context contexto) {
			super(umaNoticia, contexto);
		}

		@Override
		public Noticia preparaNoticia(Element html) {

			// SEM COLUNA_IMAGEM
			// <div class="destaque">
			// <div id="titulo"><a
			// href="/cgi-bin/getfromdb.pl?nid=EFuFlElEAugXvlmHlV">Emissão do
			// Programa 70x7 da RTP1 sobre a última edição do Festival Jota
			// ocorrida em Braga</a></div>
			// <div>A emissão do dia 29 de Julho do programa 70x7 foi dedicado
			// ao festival Jota. Este&nbsp;ano há mais. Será nos dias 19, 20 e
			// 21 de Julho de 2013,...</div>
			// </div>

			// COM COLUNA_IMAGEM
			// <div class="destaque">
			// <div class="foto"
			// style="background-image:url(/cgi-bin/gd_imager.pl?img=/_imgs/full/voluntariosp.JPG&amp;height=100&amp;width=100&amp;bleed=1)"></div>
			// <div id="titulo"><a
			// href="/cgi-bin/getfromdb.pl?nid=EFyVVAyEEZPuKfssAU">Queres ser
			// voluntário no Festival Jota?</a></div>
			// <div>Vem ajudar-nos no melhor festival de música de inspiração
			// cristã! A inscrição de Voluntários tem limite e obedece...</div>
			// </div>

			Element htmlTitulo = html.getElementById("titulo").child(0);
			this.titulo = Html.fromHtml(htmlTitulo.html()).toString();
			try {
				this.enderecoNoticia = sitio.getEndereco().toURI()
						.resolve(htmlTitulo.attr("href")).toURL();
			} catch (MalformedURLException e1) {
				this.enderecoNoticia = null;
				e1.printStackTrace();
			} catch (URISyntaxException e1) {
				this.enderecoNoticia = null;
				e1.printStackTrace();
			}

			Elements imagens = html.getElementsByClass("foto");
			if (!imagens.isEmpty()) {
				this.texto = Html.fromHtml(html.child(2).html()).toString();

				String imagemUrl = imagens.attr("style").split("[()]")[1];
				try {
					this.enderecoImagem = sitio.getEndereco().toURI()
							.resolve(imagemUrl).toURL();
				} catch (MalformedURLException e) {
					this.enderecoImagem = null;
					e.printStackTrace();
				} catch (URISyntaxException e) {
					this.enderecoImagem = null;
					e.printStackTrace();
				}
			} else {
				this.texto = Html.fromHtml(html.child(1).html()).toString();
			}

			return this;
		}
	}

	public static class NoticiaFestivalJota2013Destaque extends NoticiaFestivalJota {

		public NoticiaFestivalJota2013Destaque(Context contexto) {
			super(contexto);
		}

		public NoticiaFestivalJota2013Destaque(Element html, SitioNoticias sitio, Context contexto) {
			super(html, sitio, contexto);
		}

		public NoticiaFestivalJota2013Destaque(Element html, Context contexto) {
			super(html, contexto);
		}

		public NoticiaFestivalJota2013Destaque(String umaNoticia,
				SitioNoticias sitio, Context contexto) {
			super(umaNoticia, sitio, contexto);
		}

		public NoticiaFestivalJota2013Destaque(String umaNoticia, Context contexto) {
			super(umaNoticia, contexto);
		}

		@Override
		public Noticia preparaNoticia(Element html) {
			// SEM COLUNA_IMAGEM
			// <div class="destaque" id="maindestaque">
			// <div class="semfoto"></div>
			// <h3>Emissão do Programa 70x7 da RTP1 sobre a última edição do
			// Festival Jota ocorrida em Braga</h3>
			// <p>A emissão do dia 29 de Julho do programa 70x7 foi dedicado ao
			// festival Jota. Este&nbsp;ano há mais. Será nos dias 19, 20 e 21
			// de Julho de 2013, no espaço emblemático onde nasceu o Festival
			// Jota, em plena Serra da Estrela. A 6ª edição do Festival Jota
			// ocorrerá na vila do Paul, a vinte quilómetros da Covilhã, na
			// diocese da Guarda...</p>
			// <iframe src="http://www.youtube.com/embed/fAB5zA9cJEY"
			// allowfullscreen="" frameborder="0" height="285"
			// width="420"></iframe>
			// <p><a href="/cgi-bin/getfromdb.pl?nid=EFuFlElEAugXvlmHlV"
			// class="maisinfo"><img src="/imgs/seta.gif" border="0"> mais
			// informação</a></p>
			// </div>

			// COM COLUNA_IMAGEM
			// <div class="destaque" id="maindestaque">
			// <div class="foto"
			// style="background-image:url(/cgi-bin/gd_imager.pl?img=/_imgs/full/cartaz_festival_jota_2013final%20-%20Cpia.jpg&amp;height=160&amp;width=160&amp;bleed=1)"></div>
			// <h3>Um cartaz recheado de grandes nomes</h3>
			// <p>A sexta edição do Festival JOTA regressa ao espaço emblemático
			// onde nasceu, à vila do Paul, a vinte quilómetros da cidade da
			// Covilhã, em plena Serra da Estrela, nos dias 19, 20 e 21 de Julho
			// do corrente ano de 2013, com um grande cartaz. As noites deste
			// festival são dedicadas à música de inspiração cristã, e...</p>
			// <p><a href="/cgi-bin/getfromdb.pl?nid=EFyVZluFEABfuFDwNl"
			// class="maisinfo"><img src="/imgs/seta.gif" border="0"> mais
			// informação</a></p>
			// </div>

			// <div class="destaque" id="maindestaque">
			// <div class="foto"
			// style="background-image:url(/cgi-bin/gd_imager.pl?img=/_imgs/full/Luciano.jpg&amp;height=160&amp;width=160&amp;bleed=1)"></div>
			// <h3>Luciano San</h3>
			// <p>&nbsp; O Luciano é evangélico e vem do Brasil. Qual terá sido
			// o concerto mais marcante da sua carreira? Descobre a resposta a
			// esta pergunta na entrevista express concedida ao Festival Jota.
			// &nbsp; Uma palavra para definir cada um dos elementos do Luciano
			// e da banda que o vai acompanhar: Sintonia Momento marcante de um
			// concerto: Lançamento Momento mais alto da vida de Luciano:...</p>
			// <iframe src="http://www.youtube.com/embed/U9xZ5DCEoNU"
			// allowfullscreen="" frameborder="0" height="285"
			// width="450"></iframe>
			// <p><a href="/cgi-bin/getfromdb.pl?nid=EFZpFZFAAAfBbzdFrZ"
			// class="maisinfo"><img src="/imgs/seta.gif" border="0"> mais
			// informação</a></p>
			// </div>

			this.titulo = Html.fromHtml(
					html.getElementsByTag("h3").get(0).html()).toString();
			try {
				this.enderecoNoticia = sitio
						.getEndereco()
						.toURI()
						.resolve(
								html.getElementsByClass("maisinfo").get(0)
										.attr("href")).toURL();
			} catch (MalformedURLException e) {
				this.enderecoNoticia = null;
				e.printStackTrace();
			} catch (URISyntaxException e) {
				this.enderecoNoticia = null;
				e.printStackTrace();
			}
			this.texto = Html
					.fromHtml(html.getElementsByTag("p").get(0).html())
					.toString();

			if (html.getElementsByClass("semfoto").isEmpty()) {
				String imagemUrl = html.getElementsByClass("foto")
						.attr("style").split("[()]")[1];
				try {
					this.enderecoImagem = sitio.getEndereco().toURI()
							.resolve(imagemUrl).toURL();
				} catch (MalformedURLException e) {
					this.enderecoImagem = null;
					e.printStackTrace();
				} catch (URISyntaxException e) {
					this.enderecoImagem = null;
					e.printStackTrace();
				}
			}

			return this;
		}
	}
}
