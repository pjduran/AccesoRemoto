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
    public static String formateada = "";
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
        String lectura = "";

        String fechaInst;
        String hora;
        String carga;
        String caudal;
        String volts;
        //String tab;
        t = (TextView) findViewById(R.id.textView1);
        String respu = GetPhp(registro,nombrePhp);
        Character tab=9;
        //tab = getString(R.string.tab);
        //String nombreAplicacion = getResources().getString(R.string.app_name);
        //lectura = getResources().getString(R.string.respuesta);
        //lectura += respuestaPhp;
        System.out.println(lectura);
        //System.out.println(respuestaPhp);
        if (respuestaPhp.length() > 15) {
//            formateada = lectura + "\n";
            formateada = "";
            //fechaInst=respuestaPhp.substring(0, 17);
            fechaInst=respuestaPhp.substring(0, 8);
            //p4.jLabFechaHora.setText(fechaInst);
            //p4.jTxtSerieEquipo.setText(serieEquipo);

            formateada += "\nFecha:" +tab + tab +tab + fechaInst + "\n"; //obtengo la fecha
            hora = respuestaPhp.substring(9, 14);
            formateada += "Hora:" +tab +tab + tab + tab + tab + hora + "\n"; //obtengo la hora
            int posIni = respuestaPhp.indexOf(124, 18);
            carga = respuestaPhp.substring(18, posIni);
            formateada += "Carga:" +tab +tab + tab + tab + carga + " cm\n";
            int posFin = respuestaPhp.indexOf(124, posIni + 1);
            caudal = respuestaPhp.substring(posIni + 1, posFin);
            formateada += "Caudal:" + tab +tab + caudal + " l/s\n";
            posIni = posFin;
            posFin = respuestaPhp.indexOf(124, posIni + 1);
            volts = respuestaPhp.substring(posIni + 1, posFin);
            formateada += "Bateria:" + tab +tab + volts+" V";
        }else{
            formateada = "";
            formateada = "¡Ese equipo no tiene registro de lectura actual!"
                    + "\n\nRespuesta de la Web: "+respuestaPhp;
        }
        //t.setText(respuestaPhp);
        t.setText("");
        t.setText(formateada);

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
