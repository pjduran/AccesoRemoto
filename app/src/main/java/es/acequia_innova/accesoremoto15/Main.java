package es.acequia_innova.accesoremoto15;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    public static String serie;
    public static TextView TvMensajes;
    public TextView t;
    private static boolean isAdminSist;
    private int numTit;
    private static int numUsu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TvMensajes =  (TextView) findViewById(R.id.textView_Mensajes);
        //TvMensajes.setText(getResources().getString(R.string.ingreseUsuClave));
        TvMensajes.setText(R.string.ingreseUsuClave); //ESta forma es más simple que la anterior

        Button boton = (Button) findViewById(R.id.butt_OK);
        boton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LO PRIMERO SERIA CONFIRMAR LA EXISTENCIA DEL USUARIO Y SU CLAVE
                //Esto lo hace onClickOK()
                boolean existeUsu = onClickOk();

                //LUEGO HAY QUE ENCONTRAR LOS EQUIPOS QUE LE PERTENECEN Y PONERLOS EN UN DESPLEGABLE
            }
        });


        //AL ELEGIR UNO, HAY QUE BUSCAR SUS DATOS ACTUALES Y MOSTRARLOS EN UN AREA DE TEXTO
        //serie = "1507171821";
        //BajoDatosActuales(serie);

    }


    public boolean onClickOk() {
        System.out.println("Entro a onClickOk()");
        boolean ok = false;
        EditText tUsu = (EditText) findViewById(R.id.editTxt_Usu);
        EditText tClave = (EditText) findViewById(R.id.editText_Clave);
        String usu, clave;
        usu = tUsu.getText().toString();
        clave = tClave.getText().toString();
        System.out.println("Usuario ingresado: "+usu);
        System.out.println("Clave ingresada: "+ clave);

        String mensError = getResources().getString(R.string.errorUsuClave);
        if (tUsu.getText().length() < 1 || tClave.getText().length() < 1) {
            TvMensajes.setText(mensError);
        }else{
            TvMensajes.setText("Usuario y Clave completados");
            //Aqui debo saber si existe esa combinación de Usuario y Clave y si es Administrador
            //o usuario final
            if(buscoUsuario(usu, clave)) {
                ok = true;
                TvMensajes.setText("YUPII, usuario y clave correctos");
            }else{
                TvMensajes.setText("No existe esa combinacion de Usuario y Clave");
            }
        }

        return ok;
    }

    public synchronized boolean buscoUsuario(String usuario, String clave){
        //debo saber si busco un administrador o usuario final
        System.out.println("Entro a buscoUsuario()");
        isAdminSist=isAdmin(usuario, clave);
        if (!isAdminSist){ //si no es Administrador busco un usuario final
            if(buscoUsuFinal(usuario, clave)){
                return true;
            }else{
                return false;
            }
        }else{ //si es un administrador lo busco
//            isAdminSist=true;
            if(buscoAdmSist(usuario, clave)){
                return true;
            }else
                return false;
        }
    }

    /**
     *
     * @param pUf
     * @return
     */
    private boolean buscoAdmSist(String pUf, String clave) {
        System.out.println("Entro a buscoAdmSist()");
        //debo invocar un php que verifique si existe el Administrador de Sist
        //y la clave es correcta.
        boolean ret = false;
        String php = "buscoAdmSis.php";
        //String clave = new String(pUf.passwClave.getPassword());
        String usuario = pUf;
        //String usuario = pUf.txtUsuario.getText();
        String reg = "dato="+usuario+","+clave;
        String sbErr = "Error al buscar usuario y clave";
        String sb=phpGet(reg, php);
        if(sb!=null){
            sbErr = sb;
            //aqui debo obtener el nro de Admin del Sist
//            String cad = sbUltReg.toString();
            String cad = sbErr;
            System.out.println("buscoAdminSist - Cadena recibida del php: "+cad);
            String num = cad.substring(cad.indexOf("Nro Admin Sist:")+15, cad.indexOf('|')).trim();
            System.out.println("Numero extraido "+num);
            numTit= Integer.parseInt(num);
//            pUf.jListNomUsu.setListData(sbErr);
            if(numTit>0){
                ret=true;
                String nom = "Bienvenido "+cad.substring(cad.indexOf("Nombre:")+8);
                //pUf.jLabComentarios1.setText(nom);
//            pUf.setVisible(false);
            }else if (numTit==0){
                sbErr = "ERROR: Usuario/clave no existen";
                //pUf.jLabComentarios1.setText("ERROR: Usuario/clave no existen");
//            pUf.jListNomUsu.setListData(sbErr);
            }else{
                sbErr = "ERROR: No se transmitieron bien los datos de usuario y clave";
                //pUf.jLabComentarios1.setText("ERROR: No se transmitieron bien los datos de usuario y clave");
            }
        }else{
            sbErr = "ERROR: Sin Respuesta de la BD";
            //pUf.jLabComentarios1.setText("ERROR: Sin Respuesta de la BD");
//             pUf.jListNomUsu.setListData(sbErr);
        }
        return ret;
    }


    /**
     * Mira en la BD si el usuario es un Administrador o no y si coincide la clave
     * @param pUf
     * @return
     */
    public boolean isAdmin(String pUf, String clave) {
        System.out.println("Entro a isAdmin()");
        //debo invocar un php que verifique si existe el Administrador de Sist
        //y la clave es correcta.
        boolean ret=false;
        String php = "buscoAdmSis.php";
        //String clave = new String(pUf.passwClave.getPassword());
        String usuario = pUf;
        //String usuario = pUf.txtUsuario.getText();
        String reg = "dato="+usuario+","+clave;
        String sbErr = "Error al buscar usuario y clave";
        String sb = phpGet(reg, php);
        if (sb.length()>23) {
            sbErr = sb;
            //aqui debo obtener el nro de Admin del Sist
//            String cad = sbUltReg.toString();
            String cad = sbErr;
            System.out.println("isAdmin()- Cadena recibida del php: " + cad);
            String num = cad.substring(cad.indexOf("Nro Admin Sist:") + 15, cad.indexOf('|')).trim();
            System.out.println("Numero extraido " + num);
            numTit = Integer.parseInt(num);
//            pUf.jListNomUsu.setListData(sbErr);
            if (numTit > 0) {
                ret = true;
            } else if (numTit == 0) {
                ret = false;
            } else {
                ret = false;
            }
        }else{
            ret = false;
        }
        //HAY QUE HACER ALGO CON EL MENSAJE sbError
        return ret;
    }

    private boolean buscoUsuFinal(String pUf, String clave){
        System.out.println("Entro a buscoUsuFinal()");
        //debo invocar un php que verifique si existe el Administrador de Sist
        //y la clave es correcta.
        boolean ret=false;
        String php = "buscoUsuario.php";
        //String clave = new String(pUf.passwClave.getPassword());
        //String clave = clave;
        //String usuario = pUf.txtUsuario.getText();
        String usuario = pUf;
//        String reg = "dato="+usuario;
        String reg = "dato="+usuario+","+clave;
        String sbErr = "Error al buscar usuario y clave";
        String sb=phpGet(reg, php);
        if(sb.length()>23){
            sbErr = sb;
            //aqui debo obtener el nro de usuario
//            String cad = sbUltReg.toString();
            String cad = sbErr;
            System.out.println("buscoUsuarioFinal() - Cadena recibida del php: "+cad);
            String num = cad.substring(cad.indexOf("Nro:")+4, cad.indexOf('|')).trim();
            System.out.println("Numero extraido "+num);
            numUsu= Integer.parseInt(num);

            if(numUsu>0){
                ret = true;
                String nom = "Bienvenido "+cad.substring(cad.indexOf("Nombre:")+8,cad.lastIndexOf('|'));
                //pUf.jLabComentarios1.setText(nom);
//                Web.numTit = Integer.parseInt(cad.substring(cad.indexOf("Num Admin")+15, cad.indexOf('|')).trim());
                System.out.println("Numero del padre del usuario: "+  numTit);
            }else if (numUsu==0){
                sbErr = "ERROR: Usuario/clave no existen";
                //pUf.jLabComentarios1.setText("ERROR: Usuario/clave no existen");
//            pUf.jListNomUsu.setListData(sbErr);
            }else{
                sbErr = "ERROR: No se transmitieron bien los datos de usuario y clave";
                //pUf.jLabComentarios1.setText("ERROR: No se transmitieron bien los datos de usuario y clave");
            }
        }else{
            sbErr = "ERROR: Sin Respuesta de la BD";
            //pUf.jLabComentarios1.setText("ERROR: Sin Respuesta de la BD");
//             pUf.jListNomUsu.setListData(sbErr);
        }
        //HAY QUE HACER ALGO CON LOS MENSAJES nom y sbError
        return ret;
    }


    private void BajoDatosActuales (String serie){
        System.out.println("Entro a BajoDatosActuales()");
        nombrePhp = "bajoDatosActuales.php";
        registro = "dato="+serie;
        String lectura = "";

        String fechaInst;
        String hora;
        String carga;
        String caudal;
        String volts;
        //String tab;
        t = (TextView) findViewById(R.id.textView3);
        String respu = phpGet(registro,nombrePhp);
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

    private String phpGet(String reg, String php){
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
