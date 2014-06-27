package pt.rikmartins.festivaljota;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import pt.rikmartins.utilitarios.noticias.SitioNoticias.Noticia;

public class NoticiaAdapter extends BaseAdapter {

	private Context contexto;
	private List<Noticia> dados = new ArrayList<Noticia>();
	private static LayoutInflater inflater = null;
	
	public NoticiaAdapter(Context contexto, List<Noticia> d) {
		this.contexto = contexto;
		dados = d;
		inflater = LayoutInflater.from(contexto);
	}

	@Override
	public int getCount() {
		return dados.size();
	}

	@Override
	public Object getItem(int posicao) {
		return posicao;
	}

	@Override
	public long getItemId(int posicao) {
		return posicao;
	}

	@Override
	public View getView(int posicao, View vista, ViewGroup pai) {
		View vi = vista;
		if (vista == null)
			vi = inflater.inflate(R.layout.view_noticia, null);

		TextView titulo = (TextView) vi.findViewById(R.id.titulo); // título
		TextView texto = (TextView) vi.findViewById(R.id.texto); // texto
		ImageView imagem = (ImageView) vi.findViewById(R.id.imagem); // imagem
		LinearLayout corpoNoticia = (LinearLayout) vi.findViewById(R.id.corpo_noticia);

		final Noticia aNoticia = dados.get(posicao);

		corpoNoticia.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean conectividade = true;
				try {
					ConnectivityManager cm = (ConnectivityManager) contexto
							.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
					if (!activeNetwork.isConnectedOrConnecting())
						conectividade = false;
				} catch (NullPointerException npex) {
					conectividade = false;
				}

				if (conectividade) {
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(aNoticia
							.getEnderecoNoticia().toExternalForm()));

					contexto.startActivity(i);
				} else {
					Toast toast = Toast.makeText(contexto.getApplicationContext(),
							"É necessária uma ligação à internet!", Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});

		// Setting all values in listview
		titulo.setText(aNoticia.getTitulo());
		texto.setText(aNoticia.getTexto());
		try {
			aNoticia.obterImagem(imagem);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return vi;
	}
}
