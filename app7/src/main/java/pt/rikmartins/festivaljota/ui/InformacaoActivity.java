package pt.rikmartins.festivaljota.ui;

import pt.rikmartins.festivaljota.R;
import android.os.Bundle;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;

public class InformacaoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_informacao);

		try {
			String app_ver = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0).versionName;
			((TextView) findViewById(R.id.nome_versao)).setText(app_ver);
		} catch (NameNotFoundException e) {
			((TableRow) findViewById(R.id.linha_versao)).setVisibility(View.GONE);
		}
	}
}
