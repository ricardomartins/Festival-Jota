package pt.rikmartins.festivaljota.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import pt.rikmartins.festivaljota.NoticiasFestivalJotaService;
import pt.rikmartins.festivaljota.R;
import pt.rikmartins.festivaljota.utilitarios.SitioNoticiasFestivalJota2013;

public class PrincipalActivity extends FragmentActivity{
	private static final String ETIQUETA = "PrincipalActivity";
	
	private static boolean estaVisivel = false;
	
	AppSectionsPagerAdapter mAppSectionsPagerAdapter;
	ViewPager mViewPager;
	DialogFragment erroLigacaoDialogFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(ETIQUETA, "onCreate");

		setContentView(R.layout.activity_principal);
		// setContentView(new ProgressBar(this));

		FragmentManager fm = getSupportFragmentManager();

		mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(fm);

		// Set up the ViewPager, attaching the adapter and setting up a listener
		// for when the
		// user swipes between sections.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mAppSectionsPagerAdapter);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						// When swiping between different app sections, select
						// the corresponding tab.
						// We can also use ActionBar.Tab#select() to do this if
						// we have a reference to the
						// Tab.
						// actionBar.setSelectedNavigationItem(position);
					}
				});

		Intent correServico = new Intent(this, NoticiasFestivalJotaService.class);
		
		String chaveFreqSincro = getResources().getString(
				R.string.definicoes_chave_frequencia_sincronizacao);
		int minutosPeriodo = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(
				this).getString(chaveFreqSincro, "1440"));
		Log.d(ETIQUETA, "minutosPeriodo: " + minutosPeriodo);
		if(minutosPeriodo == -1){
			correServico.putExtra(NoticiasFestivalJotaService.ARG_FORCAR_ACTUALIZACAO, true);
		}
		startService(correServico);
	}
	@Override
	protected void onResume() {
		super.onResume();
		Log.v(ETIQUETA, "onResume");
		setVisivel(true);
		receptor = new FestivalJotaPrincipalReceiver();
		IntentFilter filtro = new IntentFilter();
		filtro.addAction(NoticiasFestivalJotaService.ACCAO_NOTICIAS_OBTIDAS);
		filtro.addAction(NoticiasFestivalJotaService.ACCAO_FALHA_OBTENCAO_NOTICIAS);
		registerReceiver(receptor, filtro);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.v(ETIQUETA, "onPause");
		setVisivel(false);
		unregisterReceiver(receptor);
	}

	private static void setVisivel(boolean visivel){
		estaVisivel = visivel;
	}

	public static boolean getVisivel(){
		return estaVisivel;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opc_informacao:
			Intent informacao = new Intent(this, InformacaoActivity.class);
			startActivity(informacao); 
			return true;
		case R.id.opc_definicoes:
			Intent definicoes = new Intent(this, DefinicoesActivity.class);
			startActivity(definicoes); 
			return true;
		case R.id.opc_actualizar:
			Intent correServico = new Intent(this, NoticiasFestivalJotaService.class);
			correServico.putExtra(NoticiasFestivalJotaService.ARG_FORCAR_ACTUALIZACAO, true);
			startService(correServico);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public class AppSectionsPagerAdapter extends FragmentPagerAdapter {
		
		
		public AppSectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
	
		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0:
				return NoticiasFragment.newInstance(SitioNoticiasFestivalJota2013.CATEGORIA_DESTAQUES);
			case 1:
				return NoticiasFragment.newInstance(SitioNoticiasFestivalJota2013.CATEGORIA_NOTICIAS);
			case 2:
				return ExtrasFragment.newInstance();
			default:
				return null;
			}
		}
	
		@Override
		public int getCount() {
			return 3;
		}
	
		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return SitioNoticiasFestivalJota2013.CATEGORIA_DESTAQUES;
			case 1:
				return SitioNoticiasFestivalJota2013.CATEGORIA_NOTICIAS;
			case 2:
				return ExtrasFragment.TITULO_EXTRAS;
			default:
				return null;
			}
		}
	}

	public interface Actualizavel {
		public void onNoticiasObtidas();
		public void onFalhaObtencaoNoticias();
	}
	
	void onNoticiasObtidas() {
		for (Actualizavel ac : actualizaveis) {
			ac.onNoticiasObtidas();
		}
	}
	
	void onFalhaObtencaoNoticias(){
		for (Actualizavel ac : actualizaveis) {
			ac.onFalhaObtencaoNoticias();
		}
	}

	private List<Actualizavel> actualizaveis;

	@Override
	public void onAttachFragment(Fragment fragment) {
		if (actualizaveis == null)
			actualizaveis = new ArrayList<PrincipalActivity.Actualizavel>();

		try {
			Actualizavel ac = (Actualizavel) fragment;
			actualizaveis.add(ac);
		} catch (ClassCastException ccex) {
			// Não fazer nada
		}
	}

	private BroadcastReceiver receptor;
	
	public class FestivalJotaPrincipalReceiver extends BroadcastReceiver {
		private static final String ETIQUETA = "FestivalJotaPrincipalReceiver";

		public FestivalJotaPrincipalReceiver() {
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(ETIQUETA, intent.getAction() + " recebido");
			if (intent.getAction().equals(NoticiasFestivalJotaService.ACCAO_NOTICIAS_OBTIDAS)) {
				if(PrincipalActivity.getVisivel()){
					// O que fazer quando a base de dados foi actualizada
					onNoticiasObtidas();
				}
			} else if (intent.getAction().equals(NoticiasFestivalJotaService.ACCAO_FALHA_OBTENCAO_NOTICIAS)) {
				// O que fazer quando a base de dados não pôde ser actualizada
				// Se a base de dados estiver vazia e não estiver a mostrar nada deve mostrar mensagem de erro
				onFalhaObtencaoNoticias();
			}
		}
	}
}
