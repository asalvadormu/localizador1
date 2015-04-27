package com.samuan.localizacion1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    private ServicioLocalizador elservicio;
    private boolean enlazado=false;
    private boolean locActiva=true;

    private Intent intentServicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("ACTIVIDAD", "arracando");

        //arrancar servicio que comprueba si estamos fuera de zona segura
        intentServicio = new Intent(this, ServicioLocalizador.class);
       // startService(intent);

        enlazar();

        Log.i("ACTIVIDAD", "servicio arrancado");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

   /*******************    METODOS PARA ENLAZADO CON EL SERVICIO   **************************/
    private ServiceConnection conexion=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            elservicio=((ServicioLocalizador.LocalBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            elservicio=null;
        }
    };

    void enlazar(){
        bindService(new Intent(MainActivity.this,ServicioLocalizador.class), conexion, Context.BIND_AUTO_CREATE );
        enlazado=true;
    }

    void desenlazar(){
        unbindService(conexion);
        enlazado=false;
    }


    @Override
    public void onStop(){
        super.onStop();
        if(elservicio!=null)         desenlazar();
    }

    public void onclick(View v){
        int cual=  v.getId();
        switch(cual){
            case R.id.button0:
                Log.i("LOCALIZACION1","SERVICIO click boton0 ");
                //conectar localizador
                if(elservicio!=null) {
                    if(!locActiva) {
                        elservicio.startLocationUpdates();
                        locActiva=true;
                    }
                }
                break;
            case R.id.button1:
                Log.i("LOCALIZACION1","SERVICIO click boton1 ");
                //iniciar servicio
                startService(intentServicio);
                break;
            case R.id.button2:
                Log.i("LOCALIZACION1","SERVICIO click boton2 ");
                //parar servicio
                boolean que=stopService(intentServicio);
                Log.i("LOCALIZACION1","SERVICIO servicio parado? "+que);
                break;
            case R.id.button3: Log.i("LOCALIZACION1","SERVICIO click boton3 ");
                //desconectar localizador
                if(elservicio!=null) {
                    if(locActiva) {
                        elservicio.stopLocationUpdates();
                        locActiva=false;
                    }
                }
                break;
        }
    }

}
