package com.example.patryk.pobieranie;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MojaIntentService extends IntentService {
    private final int ROZMIAR_BLOKU=1024;
    public int mRozmiar=0;
    public int mPobranychBajtow=0;
    PostepInfo postepInfo = new PostepInfo();
    //akcje które potrafi wykonać usługa (może być więcej niż jedna)
    private static final String AKCJA_ZADANIE1 =
            "com.example.patryk.pobieranie.action.zadanie1";
    //tekstowe identyfikatory parametrów potrzebnych do
    //wykonania akji (może być więcej niż jeden)
    private static final String PARAMETR1 =
            "com.example.patryk.pobieranie.extra.parametr1";

    public static void uruchomUsluge(Context context, String parametr){
        Intent zamiar = new Intent(context, MojaIntentService.class);
        zamiar.setAction(AKCJA_ZADANIE1);
        zamiar.putExtra(PARAMETR1, parametr);
        context.startService(zamiar);

    }

    public MojaIntentService() {
        super("MojaIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            //sprawdzenie o jaką akcję chodzi
            if (AKCJA_ZADANIE1.equals(action)) {
                final String mAdres = intent.getStringExtra(PARAMETR1);
                wykonajZadanie(mAdres);
            } else {
                Log.e("intent_service","nieznana akcja");
            }
        }
        Log.d("intent_service","usługa wykonała zadanie");
    }

    private void wykonajZadanie(String mAdres) {
        //kod faktycznie wykonujący zadanie...
        InputStream strumienZSieci = null;
        FileOutputStream strumienDoPliku = null;
        HttpURLConnection polaczenie = null;
        try {

            URL url = new URL(mAdres);
            polaczenie = (HttpURLConnection) url.openConnection();
            polaczenie.connect();
            mRozmiar = polaczenie.getContentLength();
            File plikRoboczy = new File(url.getFile());
            File plikWyjsciowy = new File(
                    Environment.getExternalStorageDirectory() +
                            File.separator + plikRoboczy.getName());
            if (plikWyjsciowy.exists()) plikWyjsciowy.delete();
            DataInputStream czytnik = new DataInputStream(polaczenie.getInputStream());
            strumienDoPliku = new FileOutputStream(plikWyjsciowy.getPath());
            byte bufor[] = new byte[ROZMIAR_BLOKU];
            int pobrano = czytnik.read(bufor, 0, ROZMIAR_BLOKU);
            while (pobrano != -1) {
                strumienDoPliku.write(bufor, 0, pobrano);
                mPobranychBajtow += pobrano;
                pobrano = czytnik.read(bufor, 0, ROZMIAR_BLOKU);
                wyslijBroadcast(mPobranychBajtow,mRozmiar);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (polaczenie != null) polaczenie.disconnect();
        }
        }

    public final static String POWIADOMIENIE = "com.example.patryk.pobieranie.odbiornik";
    public final static String INFO = "info";
    private void wyslijBroadcast(int PobranychBajtow, int Rozmiar) {
        //utworzenie intencji powiadomienia i umieszczenie w niej danych
        Intent zamiar = new Intent(POWIADOMIENIE);
        postepInfo.mRozmiar=Rozmiar;
        postepInfo.mPobranychBajtow=PobranychBajtow;
        if(PobranychBajtow==Rozmiar){
            postepInfo.mWynik="Pobieranie zakonczone";
        }
        else{
            postepInfo.mWynik="Pobieranie w toku";
        }
        zamiar.putExtra(INFO,postepInfo);
        //wysłanie komunikatu
        sendBroadcast(zamiar);
    }
}
