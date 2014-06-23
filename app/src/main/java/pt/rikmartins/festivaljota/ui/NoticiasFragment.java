package pt.rikmartins.festivaljota.ui;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import pt.rikmartins.festivaljota.NoticiaAdapter;
import pt.rikmartins.festivaljota.R;
import pt.rikmartins.festivaljota.provider.Mostrador.Noticias;
import pt.rikmartins.festivaljota.ui.PrincipalActivity.Actualizavel;
import pt.rikmartins.festivaljota.utilitarios.SitioNoticiasFestivalJota2013;
import pt.rikmartins.festivaljota.utilitarios.SitioNoticiasFestivalJota2013.NoticiaFestivalJota;
import pt.rikmartins.utilitarios.noticias.SitioNoticias;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Use the {@link NoticiasFragment#newInstance} factory
 * method to create an instance of this fragment.
 * 
 */
public class NoticiasFragment extends Fragment implements LoaderCallbacks<Cursor>, Actualizavel{
	private static final String ETIQUETA = "NoticiasFragment";

	private static final String ARG_CATEGORIA = "categoria";
	
	private String categoriaNoticia;
	
	private NoticiaAdapter noticiaAdapter;
	
	private ControladorEmptyListView controladorEmptyGridNoticias;
	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param categoriaNoticia
	 *            Parameter 1.
	 * @return A new instance of fragment NoticiasFragment.
	 */
	
	public static NoticiasFragment newInstance(String categoriaNoticia) {
		NoticiasFragment fragment = new NoticiasFragment();
		Bundle args = new Bundle();
		args.putString(ARG_CATEGORIA, categoriaNoticia);
		fragment.setArguments(args);
		return fragment;
	}

	public NoticiasFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(ETIQUETA, "onCreate");
		if (getArguments() != null) {
			categoriaNoticia = getArguments().getString(ARG_CATEGORIA);
		}
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.v(ETIQUETA, "onActivityCreated");

		getLoaderManager().initLoader(0, null, this);
	}

	private ProgressBar barraProgresso;
	private LinearLayout avisoDados;
	private ListView listViewNoticias;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.v(ETIQUETA, "onCreateView");

		View rootView = inflater.inflate(R.layout.fragment_noticias,
				container, false);

		barraProgresso = (ProgressBar) rootView.findViewById(R.id.barraProgresso);
		avisoDados = (LinearLayout) rootView.findViewById(R.id.avisoDados);
		listViewNoticias = ((ListView) rootView.findViewById(R.id.listViewNoticias));

		controladorEmptyGridNoticias = new ControladorEmptyListView(listViewNoticias, barraProgresso, avisoDados);

		Log.d("pt.rikmartins.festivaljota", "onCreate do PrincipalActivity: " + rootView.toString());
		
		return rootView;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.v(ETIQUETA, "onCreateLoader");
		Uri uriBase;
		uriBase = Uri.withAppendedPath(Noticias.CONTENT_URI_CATEGORIA, categoriaNoticia);
		
		String[] projeccao = new String[]
				{Noticias._ID,
				 Noticias.COLUNA_CATEGORIA, 
				 Noticias.COLUNA_TITULO, 
				 Noticias.COLUNA_SUBTITULO, 
				 Noticias.COLUNA_TEXTO, 
				 Noticias.COLUNA_ENDERECOIMAGEM, 
				 Noticias.COLUNA_ENDERECO, 
				 Noticias.COLUNA_IMAGEM};
		return new CursorLoader(getActivity(), uriBase, projeccao, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor c) {
		List<SitioNoticias.Noticia> dados = new ArrayList<SitioNoticias.Noticia>();

		if (c.moveToFirst()) {
			controladorEmptyGridNoticias.onConteudoPresente();
			for (; !c.isAfterLast(); c.moveToNext()) {
				NoticiaFestivalJota noticia = null;
				if (categoriaNoticia.equals(SitioNoticiasFestivalJota2013.CATEGORIA_DESTAQUES)) {
					noticia = new SitioNoticiasFestivalJota2013.NoticiaFestivalJota2013Destaque(getActivity());
				} else {
					noticia = new SitioNoticiasFestivalJota2013.NoticiaFestivalJota2013(getActivity());
				}

				noticia.setTitulo(c.getString(c.getColumnIndex(Noticias.COLUNA_TITULO)));
				noticia.setSubtitulo(c.getString(c.getColumnIndex(Noticias.COLUNA_SUBTITULO)));
				noticia.setTexto(c.getString(c.getColumnIndex(Noticias.COLUNA_TEXTO)));

				byte[] mancha = c.getBlob(c.getColumnIndex(Noticias.COLUNA_IMAGEM));
				if (mancha != null) {
					Bitmap btm = BitmapFactory.decodeByteArray(mancha, 0, mancha.length);
					if (btm != null) {
						noticia.setImagem(btm);
					}
				}
				
				try {
					noticia.setEnderecoNoticia(c.getString(c
							.getColumnIndex(Noticias.COLUNA_ENDERECO)));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					noticia.setEnderecoImagem(c.getString(c
							.getColumnIndex(Noticias.COLUNA_ENDERECOIMAGEM)));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					noticia.setEnderecoProvider(Uri.withAppendedPath(Noticias.CONTENT_URI_ID,
							String.valueOf(c.getInt(c.getColumnIndex(Noticias._ID)))));
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dados.add(noticia);
			}
		} else {
			controladorEmptyGridNoticias.onConteudoAusente();
		}

		// noticiaAdapter = new NoticiaCursorAdapter(getActivity(), c, 0);
		noticiaAdapter = new NoticiaAdapter(getActivity(), dados);

		((ListView) getView().findViewById(R.id.listViewNoticias)).setAdapter(noticiaAdapter);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// Não faz nada para já
	}

	@Override
	public void onNoticiasObtidas() {
		Log.v(ETIQUETA, "onNoticiasObtidas");
		getLoaderManager().restartLoader(0, null, this);
		if(controladorEmptyGridNoticias != null)
			controladorEmptyGridNoticias.onObtencaoSucedida();
	}

	@Override
	public void onFalhaObtencaoNoticias() {
		Log.v(ETIQUETA, "onFalhaObtencaoNoticias");
		if(controladorEmptyGridNoticias != null)
			controladorEmptyGridNoticias.onObtencaoFalhada();
	}

	private class ControladorEmptyListView {
		private boolean obtencaoFalhada = false;
		private boolean conteudoAusente = false;
		
		private ListView oListView;
		
		private View normalView, conteudoIndisponivelView;
		
		public ControladorEmptyListView(){
			this.oListView = null;
			this.normalView = null;
			this.conteudoIndisponivelView = null;
		}
		
		public ControladorEmptyListView(ListView oListView, View normal, View conteudoIndisponivel){
			Log.d("pt.rikmartins.festivaljota", "construtor do ControladorEmptyListView");
			this.oListView = oListView;
			this.normalView = normal;
			this.conteudoIndisponivelView = conteudoIndisponivel;
			mudaEmptyViewListViewNoticias(getEmptyViewCorrecta());
		}
		
		public void setListView(ListView oListView){
			this.oListView = oListView;
			mudaEmptyViewListViewNoticias(getEmptyViewCorrecta());
		}
		
		public void setNormalView(View normal){
			this.normalView = normal;
			mudaEmptyViewListViewNoticias(getEmptyViewCorrecta());
		}
		
		public void setConteudoIndisponivelView(View conteudoIndisponivel){
			this.conteudoIndisponivelView = conteudoIndisponivel;
			mudaEmptyViewListViewNoticias(getEmptyViewCorrecta());
		}
		
		public View getEmptyViewCorrecta(){
			if(obtencaoFalhada && conteudoAusente){
				return conteudoIndisponivelView;
			} else {
				return normalView;
			}
		}
		
		public void onObtencaoFalhada(){
			if(obtencaoFalhada == true){
				return;
			}
			obtencaoFalhada = true;
			mudaEmptyViewListViewNoticias(getEmptyViewCorrecta());
		}
		
		public void onObtencaoSucedida(){
			if(obtencaoFalhada == false){
				return;
			}
			obtencaoFalhada = false;
			mudaEmptyViewListViewNoticias(getEmptyViewCorrecta());
		}
		
		public void onConteudoAusente(){
			if(conteudoAusente == true){
				return;
			}
			conteudoAusente = true;
			mudaEmptyViewListViewNoticias(getEmptyViewCorrecta());
		}

		public void onConteudoPresente(){
			if(conteudoAusente == false){
				return;
			}
			conteudoAusente = false;
			mudaEmptyViewListViewNoticias(getEmptyViewCorrecta());
		}

		private View mudaEmptyViewListViewNoticias(View novo){
			if(oListView == null)
				return null;
			View actual = oListView.getEmptyView();
			
			if(actual != null){
				actual.setVisibility(View.GONE);
			}
			oListView.setEmptyView(novo);
			
			return novo;
		}
	}
	
}
