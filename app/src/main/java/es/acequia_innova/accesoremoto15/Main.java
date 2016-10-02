package es.acequia_innova.accesoremoto15;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

//public class Main extends Activity {
public class Main extends Activity implements Response.Listener<StringRequest>, Response.ErrorListener{

    private String urlFija = "http://www.acequia-innova.es/misPhp/";
    private static String url = "";
    private String registro="";
    private String nombrePhp="";
    public static String respuestaPhp="";
    public TextView t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //LO PRIMERO SERIA CONFIRMAR LA EXISTENCIA DEL USUARIO Y SU CLAVE


        //LUEGO HAY QUE ENCONTRAR LOS EQUIPOS QUE LE PERTENECEN Y PONERLOS EN UN DESPLEGABLE


        //AL ELEGIR UNO, HAY QUE BUSCAR SUS DATOS ACTUALES Y MOSTRARLOS EN UN AREA DE TEXTO

        BajoDatosActuales("1507171821");


    }

    private void BajoDatosActuales (String serie){
        nombrePhp = "bajoDatosActuales.php";
        registro = "dato="+serie;
        t = (TextView) findViewById(R.id.textView1);
        String respu = GetPhp(registro,nombrePhp);

        //String nombreAplicacion = getResources().getString(R.string.app_name);
        String res = getResources().getString(R.string.respuesta);
        res += respuestaPhp;
        System.out.println(res);
        //System.out.println(respuestaPhp);
        t.setText(respuestaPhp);
    }

    private String GetPhp(String reg, String php){
        String respuesta="";
        url = urlFija+php+"?"+reg;
        System.out.println("URL enviada a la web: "+url);
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("onResponse()", "La respuesta es: "+ response);
                        respuestaPhp = response;
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("onErrorResponse()", "ERROR en GetPhp: "+ error);
                        respuestaPhp = error.toString();
            }
        });
//  Add the request to the RequestQueue.
        queue.add(stringRequest);
        //respuesta = respuestaPhp;
        return respuestaPhp;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d("onErrorResponse()", error.toString());
    }

    @Override
    public void onResponse(StringRequest response) {
        Log.d("onResponse()", response.toString());
    }

}
