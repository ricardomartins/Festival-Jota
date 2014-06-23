package pt.rikmartins.festivaljota.ui;

import pt.rikmartins.festivaljota.R;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link ExtrasFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link ExtrasFragment#newInstance} factory method
 * to create an instance of this fragment.
 * 
 */
public class ExtrasFragment extends Fragment {

	public static final String TITULO_EXTRAS = "Extras";
	
	public static ExtrasFragment newInstance() {
		ExtrasFragment fragment = new ExtrasFragment();
//		Bundle args = new Bundle();
//		args.putString(ARG_PARAM1, param1);
//		args.putString(ARG_PARAM2, param2);
//		fragment.setArguments(args);
		return fragment;
	}

	public ExtrasFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		if (getArguments() != null) {
//			mParam1 = getArguments().getString(ARG_PARAM1);
//			mParam2 = getArguments().getString(ARG_PARAM2);
//		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_extras,
				container, false);
		
		((ImageView) rootView.findViewById(R.id.logoWeb)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.festivaljota.com/"));
				v.getContext().startActivity(i);
			}
		});
		((ImageView) rootView.findViewById(R.id.logoFacebook)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/festivaljota"));
				v.getContext().startActivity(i);
			}
		});
		((ImageView) rootView.findViewById(R.id.logoTwitter)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/festivaljota"));
				v.getContext().startActivity(i);
			}
		});
		((ImageView) rootView.findViewById(R.id.logoYoutube)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/user/festivaljota"));
				v.getContext().startActivity(i);
			}
		});

		((ImageView) rootView.findViewById(R.id.logoNavegar)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:ll=40.19484,-7.629771"));
				try{
					v.getContext().startActivity(i);
				}catch(ActivityNotFoundException anfEx){
					Toast toast = Toast.makeText(getActivity(),
							"Sem aplicações de navegação.", Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});

		return rootView;
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

}
