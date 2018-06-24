package com.example.patryk.pobieranie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class MainActivity extends AppCompatActivity{

    Button pobierzInformacjeButton, pobierzPlikButton;
    EditText adresPole;
    String adresText="";
    ProgressBar progress;
    TextView pobranoBajtow;
    TextView status;
    int postep=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress=findViewById(R.id.progressBar);
        pobranoBajtow=findViewById(R.id.textView8);
        status=findViewById(R.id.textView4);
        adresPole=(EditText)findViewById(R.id.adres);
        adresPole.setText("https://www.kernel.org/pub/linux/kernel/v2.6/longterm/v2.6.34/linux-2.6.34.14.tar.xz");
        adresPole.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        adresText=adresPole.getText().toString();
                    }
                }
        );
        pobierzInformacjeButton=(Button)findViewById(R.id.button);
        pobierzInformacjeButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(adresText.equals("")){
                            Toast.makeText(MainActivity.this,"Uzupełnij pole Adres!",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            pobierzInformacje();
                        }
                    }
                }
        );
        pobierzPlikButton=(Button)findViewById(R.id.button2);
        pobierzPlikButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(adresText.equals("")){
                            Toast.makeText(MainActivity.this,"Uzupełnij pole Adres!",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            requestPermission();
                            pobierzPilk();
                        }
                    }
                }
        );
    }

    public void pobierzInformacje(){
        ZadanieAsynchroniczne zadanie = new ZadanieAsynchroniczne();
        zadanie.execute(adresText);
    }

    public void pobierzPilk(){
        MojaIntentService.uruchomUsluge(
                MainActivity.this, //kontekst
                adresText);
    }

    private class ZadanieAsynchroniczne extends AsyncTask<String, Integer, String[]> {
        // obowiązkowa, wywoływana w osobnym wątku
        HttpURLConnection polaczenie = null;
        int mRozmiar;
        String mTyp;
        TextView rozmiarPliku;
        TextView typPliku;
        TextView adres;
        String[] napis = new String[2];

        @Override
        protected String[] doInBackground(String... params) {
            //do parametr params traktuje się jak tablicę np.

            //wykonanie zadania...
            //w trakcie wykonania zadania można wysłać informację o postępie
            //argumentem publishProgress też jest Integer.. params – stąd „dziwny” argument
            //po zakończeniu zadania zwracamy wynik

            HttpURLConnection polaczenie = null;
            {
                try {

                    URL url = new URL(adresText);
                    polaczenie = (HttpURLConnection) url.openConnection();
                    polaczenie.setRequestMethod("GET");
                    polaczenie.setDoOutput(true);
                    mRozmiar = polaczenie.getContentLength();   //!!!!!!!!!!!!
                    mTyp = polaczenie.getContentType();
                    napis[0] = Integer.toString(mRozmiar);
                    napis[1] = mTyp;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (polaczenie != null) polaczenie.disconnect();
                }

                return napis;
            }
        }
        // opcjonalna, wywoływana w wątku GUI
        @Override
        protected void onProgressUpdate(Integer... values) {
            //aktualizacja informacji o postępie
        }
        // opcjonalna, wywoływana w wątku GUI
        @Override
        protected void onPostExecute(String[] result) {
            rozmiarPliku=(TextView)findViewById(R.id.rozmiar);
            typPliku=(TextView)findViewById(R.id.typ);
            //wyświetla wyniki
            rozmiarPliku.setText(napis[0]);
            typPliku.setText(napis[1]);
        }

    }
    private static final int REQUEST_WRITE_PERMISSION = 786;

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        }
    }

    private BroadcastReceiver mOdbiorcaRozgloszen = new BroadcastReceiver() {

        @Override
        //obsługa odebrania komunikatu
        public void onReceive(Context context, Intent intent) {
            Bundle tobolek = intent.getExtras();
            PostepInfo postepInfo = tobolek.getParcelable(MojaIntentService.INFO);
            postep=(int)((postepInfo.mPobranychBajtow*100)/postepInfo.mRozmiar);
            progress.setProgress(postep);
            pobranoBajtow.setText(Long.toString(postepInfo.mPobranychBajtow));
            status.setText(postepInfo.mWynik);
            //kod obsługujący komunikat...
        }
    };
    @Override //zarejestrowanie odbiorcy
    protected void onResume() {
        super.onResume();
        registerReceiver(mOdbiorcaRozgloszen, new IntentFilter(
                MojaIntentService.POWIADOMIENIE));
    }
    @Override //wyrejestrowanie odbiorcy
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mOdbiorcaRozgloszen);
    }
}
