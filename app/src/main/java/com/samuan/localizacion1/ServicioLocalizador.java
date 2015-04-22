package com.samuan.localizacion1;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;


/**
 * Created by SAMUAN on 16/04/2015.
 */
public class ServicioLocalizador extends Service implements  ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    GoogleApiClient cliente;
    LocationRequest locationRequest;
    private boolean localizadorActivo=true;

    Location zonasegura;
    private double latitud=  37.930801;
    private double longitud= -4.675326;

    private boolean SMSMandado=false;

    private HandlerThread hiloAparte;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("ServicioLocalizador", "SERVICIO creando");

        buildGoogleApiClient();
        cliente.connect();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); //en milisegundos.
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    /**
     * Activa el servicio.
     * Activar servicio de localización. Tener en cuenta reinicios.
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        hiloAparte=new HandlerThread("hilosensor");
        hiloAparte.start();


        //iniciar el servicio de localización
        //manda eventos, así que empezar nuevo hilo y ordenar el procesamiento de eventos en hilo nuevo.

        Log.i("SERVICIO","onStartCommand");

        if(cliente.isConnected()) Log.i("SERVICIO","cliente conectado");

        if(cliente.isConnected() && localizadorActivo){
            startLocationUpdates();
        }else{
            Log.i("SERVICIO","SERVICIO nanai");
        }

        return START_STICKY; //se arranca solo?
    }

    @Override
    public void onDestroy() {
        Log.i("SERVICIO","onDestroy");
        //parar servicio de localización.
        stopLocationUpdates();
    }

    /**** Métodos para enlazar la actividad con el servicio ****************/

    public class LocalBinder extends Binder {
        ServicioLocalizador getService(){
            return ServicioLocalizador.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //dar la posibilidad de enlazar desde la activity para poder parar? el servicio o
        //reiniciarlo?
        return mBinder;
    }

    private final IBinder mBinder=new LocalBinder();


    /******* MÉTODOS DE LOCALIZACIÓN ***********/

    @Override
    public void onLocationChanged(Location location) {

        String mensaje="";
        mensaje += Double.toString(location.getLatitude())+" ";
        mensaje += Double.toString(location.getLongitude())+" ";
        mensaje += Double.toString(   location.getTime())+" ";
        mensaje += Double.toString( location.getAccuracy() );

        Log.i("LOCATION","SERVICIO "+mensaje);

        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String fecha=s.format(location.getTime());
        Log.i("TIME","SERVICIO "+fecha);

        if(estaFueraZonaSegura(location)){
              Log.i("SERVICIO","SERVICIO fuera de zona");
        } else{
               Log.i("SERVICIO","SERVICIO dentro de zona");
        }



        //aqui se obtiene la última posición
        //calcular diferencia entre ultima posicion e inicial.
        //Comprobar tiempo entre la última y la inicial .?

      /*  if(estaFueraZonaSegura(location)){
             //indicar sms mandado. //repetir //anular
            if(!SMSMandado){
                //mandar sms
                SMSMandado=true; //se puede poner a false despues de un tiempo.
            }
        }*/

    }

    /**** Métodos de ConnectionCallbacks y OnConnectionFailedListener ****/

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("SERVICIO","SERVICIO onconnected");
        zonasegura=LocationServices.FusedLocationApi.getLastLocation( cliente );
        if(zonasegura==null){
            Log.i("SERVICIO","SERVICIO Lugar es null");
        }else{

        }



        /*if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        }*/

        if(localizadorActivo){
            startLocationUpdates();
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("SERVICIO","SERVICIO onConntectionSuspended "+i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("SERVICIO","SERVICIO onConnectionFailed "+connectionResult.toString());
    }

    /********* Métodos mios ***************/

    protected synchronized void buildGoogleApiClient(){
        cliente = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void startLocationUpdates(){
        Log.i("SERVICIO","SERVICIO startLocationUpdates");
        LocationServices.FusedLocationApi.requestLocationUpdates(cliente, locationRequest, this, hiloAparte.getLooper());
        Log.i("SERVICIO","SERVICIO arrancado updates");
    }

    public void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(cliente,this);

    }

    /**
     * Calcula la distancia en metros desde el lugar actual hasta la posición indicada
     * como zona segura.
     *
     * @param lugar La posición actual.
     * @return Distancia en metros.
     */
    private float calcularDistancia(Location lugar){
        float[] distancia=new float[1];
        Location.distanceBetween(latitud,longitud,lugar.getLatitude(),lugar.getLongitude(),distancia);
        return distancia[0];
    }

    /**
     * Indica si la distancia al punto seguro es mayor que la distancia de seguridad.
     *
     * @param lugar Posición actual
     * @return true si está fuera de la zona segura.
     */
    private boolean estaFueraZonaSegura(Location lugar){
        float distancia=calcularDistancia(lugar);
        Log.i("SERVICIO","SERVICIO distancia entre posiciones "+distancia);
        if(distancia>500){
            return true;
        }else{
            return false;
        }
    }



}
