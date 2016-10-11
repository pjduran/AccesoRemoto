package es.acequia_innova.accesoremoto15;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.logging.Level;
import java.util.logging.Logger;

//public class Main extends Activity {
public class Main extends Activity implements Response.Listener<StringRequest>, Response.ErrorListener{


    private String urlFija = "http://www.acequia-innova.es/misPhp/";
    private static String url = "";
    //private String registro="";
    private String nombrePhp="";
    public static String respuestaPhp="";
    public static String respuestaPhpAnterior="";
    public static String serieAnterior = "";
    public static String formateada = "";
    public static String serie;
    public static TextView TvMensajes;
    public TextView t;
    private static boolean isAdminSist;
    private int numTit;
    private static int numUsu;
    private Spinner combo;
    private String [] nomEquip;  //array donde se guardan los nombres de equipo
    private static String [] serieEquip;//array donde se guardan las series
    private static String [][] eq; //aray doble con nombre y serie
    private static int numEqEncontrados;
    private ArrayAdapter adaptador;
    private boolean existeUsu;
    private static Button boton1;
    private static Button boton2;
    private static Button boton4;
    private static Button botonSalir;
    private static boolean encontreEquip;
    public static final String TAG = "***";
    public static boolean primeraVez = true;
    private static CheckBox esAdmin;


    //public final StringRequest stringRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TvMensajes = (TextView) findViewById(R.id.textView_Mensajes);
        //TvMensajes.setText(getResources().getString(R.string.ingreseUsuClave));
        TvMensajes.setText(R.string.ingreseUsuClave); //ESta forma es más simple que la anterior

        isAdminSist = true;
        esAdmin = (CheckBox) findViewById(R.id.checkBox) ;

        esAdmin.setEnabled(true);
        esAdmin.setPressed(true);
        esAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (esAdmin.isChecked())
                    isAdminSist = true;
                else
                    isAdminSist = false;
            }
        });

        //////////////////////
        ///ARMO EL BOTON DE BUSQUEDA DE USUARIOS
        ///////////
        boton1 = (Button) findViewById(R.id.butt_OK);
        boton2 = (Button) findViewById(R.id.butt_BuscoEquipos);
        boton4 = (Button) findViewById(R.id.butt_DatosActuales);
        botonSalir = (Button) findViewById(R.id.buttSalir) ;

        boton2.setEnabled(false); //Lo dejo inhabilitado hasta que encuentre el usuario
        boton4.setEnabled(false); //Lo dejo inhabilitado hasta que encuentre equipos

        boton1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LO PRIMERO SERIA CONFIRMAR LA EXISTENCIA DEL USUARIO Y SU CLAVE
                //Esto lo hace onClickOK()
                existeUsu = onClickOk();
                if (existeUsu) {
                    boton2.setEnabled(true);
                }
            }
        });

        boton2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hay que encontrar los equipos del usuario
                //LUEGO HAY QUE ENCONTRAR LOS EQUIPOS QUE LE PERTENECEN Y PONERLOS EN UN ARRAY String[]
                //Para que se vean en el Spinner
                //if (existeUsu) { //Si existe el usuario, hay que buscar sus equipos

                boton1.setEnabled(false);

                //do {
                encontreEquip = encuentroEquipos();
                if (encontreEquip && numEqEncontrados > 0) {
                    TvMensajes.setText("SE HAN ENCONTRADOS LOS EQUIPOS DE ESTE USUARIO. Son: " + numEqEncontrados);
                    //boton3.setEnabled(true);
                    boton2.setEnabled(false);
                    cargoEquipos();
                } else {
                    TvMensajes.setText("PULSE NUEVAMENTE");
                    Toast to = Toast.makeText(getApplicationContext(), "PULSE NUEVAMENTE", Toast.LENGTH_SHORT);
                    to.show();

                }
            }
        });


        boton4.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MOSTRAR LOS DATOS ACTUALES DEL EQUIPO
                boton2.setEnabled(false);
                BajoDatosActuales(serie);
            }
        });


        botonSalir.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Esto es para salir
                finish();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

/*
        boton3.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargoEquipos();
        }
*/

    }

    ///////////////////////////////////////
    //FUNCIONES PROPIAS
    /////////////////////

    public void cargoEquipos(){
        //YA TENGO EL ARRAY CON LOS EQUIPOS
        //DEBO CARGAR EL SPINNER
        combo = (Spinner) findViewById(R.id.spinner_Equipos);
        //Creamos el adaptador del spinner donde el tercer parámetro es el array donde van a ir los nombres
        adaptador = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, nomEquip);
        Log.d(TAG,"Se ha creado el adaptador del spinner");
        //Vinculamos el Spinner con su adaptador
        combo.setAdapter(adaptador);
        Log.d(TAG,"se ha vinculado el espiner con su adaptador");

        //Escuchador de selección de item del spinner
        combo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //AL ELEGIR UNO, HAY QUE BUSCAR SUS DATOS ACTUALES Y MOSTRARLOS EN UN AREA DE TEXTO
                Log.d(TAG,"spinner - onItemSelected");
                int indiceArray = combo.getSelectedItemPosition(); //no se si hay que restarle 1
                Log.d(TAG,"Indice del elemento seleccionado en el spinner: "+indiceArray);
                serie = serieEquip[indiceArray]; //"1507171821";
                Log.d(TAG,"SERIE DEL EQUIPO SELECCIONADO: "+serie);
                TvMensajes.setText("Pulse DATOS ACTUALES (2 veces)");
                boton4.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Toast to = Toast.makeText(getApplicationContext(), "La cagaste Burt Lancaster", Toast.LENGTH_LONG);
                //to.show();
            }
        });

    }

    public boolean onClickOk() {
        Log.d(TAG,"Entro a onClickOk()");
        boolean ok = false;
        EditText tUsu = (EditText) findViewById(R.id.editTxt_Usu);
        EditText tClave = (EditText) findViewById(R.id.editText_Clave);
        String usu, clave;
        usu = tUsu.getText().toString();
        clave = tClave.getText().toString();
        Log.d(TAG,"Usuario ingresado: "+usu);
        Log.d(TAG,"Clave ingresada: "+ clave);

        String mensError = getResources().getString(R.string.errorUsuClave);
        if (tUsu.getText().length() < 1 || tClave.getText().length() < 1) {
            TvMensajes.setText(mensError);
        }else{
            TvMensajes.setText("Usuario y Clave completados");
            //Aqui debo saber si existe esa combinación de Usuario y Clave y si es Administrador
            //o usuario final
            if(buscoUsuario(usu, clave)) {
                ok = true;
                //TvMensajes.setText("¡¡Yupi!!, Usuario y Clave Correctos");
                TvMensajes.setText("Pulse BUSCAR EQUIPOS");
                Toast t3 = Toast.makeText(getApplicationContext(),
                        "Usuario y Clave confirmados\n" +
                                "Pulse 2 veces el botón BUSCAR EQUIPOS", Toast.LENGTH_SHORT);
                t3.show();
            }else{
                TvMensajes.setText("No se ha encontrado ese Usuario y Clave\nRevise los datos y REINTENTE");
                Toast t1 = Toast.makeText(getApplicationContext(),
                        "No se ha encontrado ese Usuario y Clave\n" +
                                "Revise los datos y REINTENTE", Toast.LENGTH_SHORT);
                t1.show();

            }
        }

        return ok;
    }

    //Aqui hay que sustituir el combobox por una lista desplegable
    public boolean encuentroEquipos() {
        Log.d(TAG,"Entro a encuentroEquipos()");
        //debe encontrar los equipos de ese usuario o administrador
        boolean r=false;
        int cuento = 0;
        //do {
            if (isAdminSist) {
                if (buscoEquipAdmin())
                    r = true;
                else
                    r = false;
            } else {
                if (buscoEquiposUsu())
                    r = true;
                else
                    r = false;
            }
            cuento++;
        //}while(!r && cuento<2);
        /*
        if(primeraVez && !r){
            boton2.setPressed(true); // a ver si esto dispara el segundo clic
        }
        */
        return r;
    }

    /**
     * Obtiene un array de String doble a partir de una String obtenida por phpGet()
     * array de bytes donde las campos
     * tienen un largo VARIABLE y estan separadas por el caracter
     * @param b
     * @return
     */
    public String[][] obtengoLinEquip(String b, char d, int esp){
        int numLineas = cuentoLineas(b,d);
        Log.d(TAG,"Numero de celdas del StringBuffer: "+numLineas);
        int nL;
        if(numLineas%2==0)
            nL = numLineas/2;
        else
            nL = numLineas/2+1;
        Log.d(TAG,"Numero de equipos: "+nL);
        numEqEncontrados=nL;
        String[][] lineas = new String[2][nL];
        String lin = ""; //aqui guardo cada una de las campos separadas por un caracter
        int j=0;
        int celda=0; //0: serie; 1: nombre del equipo
        char c;
        //obtengo las cadenas con la linea de cada variable
        for (int k = 0; k < b.length(); k++) {
	//		System.out.print(b[k]);
            c = b.charAt(k);
            if (c != d) {//si el byte leido no es el limitador de campos, acumulo las letras
                lin = lin + c;
            } else {
                if (celda == 0) { //si la celda es 0
                    lineas[celda][j] = lin.trim(); //guardo la celda uno: nombre
                    celda = 1; //cambio a cero
                } else { //si la celda es cero
                    lineas[celda][j] = lin.trim(); //guardo la celda cero: serie
                    celda = 0; //cambio a 1 para la proxima vez
                    Log.d(TAG,"Linea " + j + " : "
                            + lineas[0][j] + "  " + lineas[1][j]);
                    j++;
                }
                k = k + esp;
                lin = "";
            }
        }
//		log.grabo("salgo de obtengo campos; nro campos obtenidas: "+j);
        return lineas;
    }

    /* Cuenta el numero de caracteres "<" en el byte[] enviado como parámetro
    * @param b (lo leido de la web), direc(caracter separador de campos)
    * @return
    * int con el numero de campos
    */
    public int cuentoLineas(String b, char d){
        int numL=0;
        char c;
        for (int j=0;j<b.length();j++){
            c= b.charAt(j);
            if (c==d)
                numL++;
        }
        Log.d(TAG,"Número de unidades separadas por el caracter "+d+" : "+numL);
        return numL;
    }


    private boolean buscoEquipAdmin() {
        //ResultSet rs = null;
        char d = ';';
        int numCar = 0;

        //aqui debo armar la consulta con phpGet(reg, php)
        String reg = "dato=" + numTit;
        Log.d(TAG,"buscoEquiposAdmin() - registro: "+reg);
        String sb = phpGet(reg, "buscoEquiposAdmin3.php");

        Log.d(TAG,"Respuesta de buscoEquiposAdmin3.php: "+sb);


        ////////////////
        //Log.d(TAG,"Respuesta del buscoEquiposUsu.php: "+sb);
        if(respuestaPhp!=null && !(respuestaPhp.contains("false")) &&!(respuestaPhp.contains("error"))){
            eq = obtengoLinEquip(respuestaPhp, d, numCar);
            nomEquip = new String[numEqEncontrados]; //defino los arrays con nombres y series
            serieEquip = new String[numEqEncontrados];
            Log.d(TAG,"Número de lineas en matriz eq: "+numEqEncontrados);
            for(int j=0;j<numEqEncontrados;j++){
                nomEquip[j]=eq[1][j];
                serieEquip[j]=eq[0][j];
            }
            return true;
        }else{
            return false;
        }
    }

    //Aqui hay que sustituir el combobox por una lista desplegable
    private boolean  buscoEquiposUsu() {
        Log.d(TAG,"Entro a buscoEquiposUsu()");
        //String sb = null;
        char d = ';';
        int numCar = 0;
        //aqui debo armar la consulta con phpGet(reg, php)
        String reg = "dato=" + numUsu;
        String sb = phpGet(reg, "buscoEquiposUsu.php");

        Log.d(TAG,"Respuesta del buscoEquiposUsu.php: "+respuestaPhp.toString());
        if(respuestaPhp!=null && !(respuestaPhp.toString().contains("false")) && !(respuestaPhp.toString().contains("error"))){
            eq = obtengoLinEquip(respuestaPhp, d, numCar);
            nomEquip = new String[numEqEncontrados];
            serieEquip = new String[numEqEncontrados];
            Log.d(TAG,"Número de lineas en matriz eq: "+numEqEncontrados);
            for(int j=0;j<numEqEncontrados;j++){
                nomEquip[j]=eq[1][j];
                serieEquip[j]=eq[0][j];
            }
            return true;
        }else{
            Log.d(TAG,"Algo salio mal en buscoEquiposUsu");
            //Toast t0 = Toast.makeText(getApplicationContext(), "Algo salio mal en buscoEquiposUsu", Toast.LENGTH_LONG);
            //t0.show();
            return false;
        }
    }



    public synchronized boolean buscoUsuario(String usuario, String clave){
        //debo saber si busco un administrador o usuario final
        Log.d(TAG,"Entro a buscoUsuario()");
        //isAdminSist=isAdmin(usuario, clave);
        if (!isAdminSist){ //si no es Administrador busco un usuario final
            if(buscoUsuFinal(usuario, clave)){
                return true;
            }else{
                return false;
            }
        }else{//si es un administrador lo busco
            if(buscoAdmSist(usuario, clave)){
                return true;
            }else{
                return false;
            }
        }
    }

    /**
     *
     * @param pUf
     * @return
     */
    private boolean buscoAdmSist(String pUf, String clave) {
        Log.d(TAG,"Entro a buscoAdmSist()");
        //debo invocar un php que verifique si existe el Administrador de Sist
        //y la clave es correcta.
        boolean ret = false;
        String php = "buscoAdmSis.php";
        String usuario = pUf;
        String reg = "dato="+usuario+","+clave;
        String sbErr = "Error al buscar usuario y clave";
        String sb= phpGet(reg, php);

        //if(sb!=null){
        if(respuestaPhp.length()>23){
            sbErr = sb;
            //aqui debo obtener el nro de Admin del Sist
//            String cad = sbUltReg.toString();
            String cad = sbErr;
            Log.d(TAG,"buscoAdminSist - Cadena recibida del php: "+cad);
            String num = cad.substring(cad.indexOf("Nro Admin Sist:")+15, cad.indexOf('|')).trim();
            Log.d(TAG,"Numero extraido "+num);
            try {
                numTit = Integer.parseInt(num);
//            pUf.jListNomUsu.setListData(sbErr);
                if (numTit > 0) {
                    ret = true;
                    String nom = "Bienvenido " + cad.substring(cad.indexOf("Nombre:") + 8);
                } else if (numTit == 0) {
                    //ret = false;
                    sbErr = "ERROR: Usuario/clave no existen";
                } else {
                    //ret = false;
                    sbErr = "ERROR: No se transmitieron bien los datos de usuario y clave";
                }
            }catch (NumberFormatException e){
                Log.d(TAG,"NumberFormatException en BuscoAdmSist(): "+e.getMessage());
            }
        }else{
            sbErr = "ERROR: Sin Respuesta de la BD";
            //ret = false;
        }
        return ret;
    }


    /**
     * Mira en la BD si el usuario es un Administrador o no y si coincide la clave
     * @param pUf
     * @return
     */
    public boolean isAdmin(String pUf, String clave) {
        Log.d(TAG,"Entro a isAdmin()");
        //debo invocar un php que verifique si existe el Administrador de Sist
        //y la clave es correcta.
        boolean ret=false;
        String php = "buscoAdmSis.php";
        //String clave = new String(pUf.passwClave.getPassword());
        String usuario = pUf;
        //String usuario = pUf.txtUsuario.getText();
        String reg = "dato="+usuario+","+clave;
        //respuestaPhp="          ";
        String sb=phpGet(reg, php);
        //sb = respuestaPhp;
        System.out.println();
        Log.d(TAG,"isAdmin - Respuesta de phpGet sb: "+sb+"  o respuestaPhp: "+respuestaPhp);
        if (respuestaPhp.length()>20) {
            //sbErr = sb;
            //aqui debo obtener el nro de Admin del Sist
//            String cad = sbUltReg.toString();
            //String cad = sbErr;
            String cad = respuestaPhp;
            Log.d(TAG,"isAdmin()- Cadena recibida del php: " + cad);
            String num = cad.substring(cad.indexOf("Nro Admin Sist:") + 15, cad.indexOf('|')).trim();
            Log.d(TAG,"Numero extraido " + num);
            try {
            numTit = Integer.parseInt(num);
//            pUf.jListNomUsu.setListData(sbErr);
            if (numTit > 0) {
                ret = true;
            } else if (numTit == 0) {
                ret = false;
            } else {
                ret = false;
            }
            }catch (NumberFormatException e){
                Log.d(TAG,"NumberFormatException en isAdmin(): "+e.getMessage());
            }
        }else{
            ret = false;
            String sbErr = "Error al buscar usuario y clave";
            Log.d(TAG,"isAdmin - "+sbErr);
            //Toast t2 = Toast.makeText(getApplicationContext(),sbErr, Toast.LENGTH_LONG);
            //t2.show();
        }
        return ret;
    }

    private boolean buscoUsuFinal(String pUf, String clave){
        Log.d(TAG,"Entro a buscoUsuFinal()");
        //debo invocar un php que verifique si existe el Administrador de Sist
        //y la clave es correcta.
        boolean ret=false;
        String php = "buscoUsuario.php";
        //String clave = new String(pUf.passwClave.getPassword());
        //String clave = clave;
        //String usuario = pUf.txtUsuario.getText();
        String usuario = pUf;

        String reg = "dato="+usuario+","+clave;
        String sbErr = "";
        //String sbErr = "Error al buscar usuario y clave";
        phpGet(reg, php);
        String sb= respuestaPhp;
        if(respuestaPhp.length()>23){
            sbErr = respuestaPhp;
            //aqui debo obtener el nro de usuario
//            String cad = sbUltReg.toString();
            String cad = respuestaPhp;
            Log.d(TAG,"buscoUsuarioFinal() - Cadena recibida del php: "+cad);
            String num = cad.substring(cad.indexOf("Nro:")+4, cad.indexOf('|')).trim();
            Log.d(TAG,"Numero extraido "+num);
            try{
            numUsu= Integer.parseInt(num);
            if(numUsu>0){
                ret = true;
                String nom = "Bienvenido "+cad.substring(cad.indexOf("Nombre:")+8,cad.lastIndexOf('|'));
                //pUf.jLabComentarios1.setText(nom);
                Log.d(TAG,"Numero del padre del usuario: "+  numTit);
                //Toast t4 = Toast.makeText(getApplicationContext(), nom, Toast.LENGTH_LONG);
                //t4.show();
            }else if (numUsu==0){
                sbErr = "ERROR: Usuario/clave no existen";
                //pUf.jLabComentarios1.setText("ERROR: Usuario/clave no existen");
//            pUf.jListNomUsu.setListData(sbErr);
            }else{
                sbErr = "ERROR: No se transmitieron bien los datos de usuario y clave";
                //pUf.jLabComentarios1.setText("ERROR: No se transmitieron bien los datos de usuario y clave");
            }
            }catch (NumberFormatException e){
                Log.d(TAG,"NumberFormatException en buscoUsuFinal(): "+e.getMessage());
            }

        }else{
            sbErr = "ERROR: Sin Respuesta de la BD: "+respuestaPhp;
            //pUf.jLabComentarios1.setText("ERROR: Sin Respuesta de la BD");
//             pUf.jListNomUsu.setListData(sbErr);
        }
        //HAY QUE HACER ALGO CON LOS MENSAJES nom y sbError
        if (sbErr.length() > 1) {
            Log.d(TAG,"buscoUsuarioFinal() - "+sbErr);
            //Toast t3 = Toast.makeText(getApplicationContext(), sbErr, Toast.LENGTH_LONG);
            //t3.show();
        }
        return ret;
    }

    public static void delay(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void BajoDatosActuales (String serie){
        Log.d(TAG,"Entro a BajoDatosActuales()");
        nombrePhp = "bajoDatosActuales.php";
        String reg = "dato="+serie;
        String lectura = "";

        String fechaInst;
        String hora;
        String carga;
        String caudal;
        String volts;
        //String tab;
        t = (TextView) findViewById(R.id.textView_Mensajes);
        String respu =phpGet(reg,nombrePhp);
        Log.d(TAG,"respuestaPhp: "+ respuestaPhp+"   resp Anterior: "+respuestaPhpAnterior);
        Log.d(TAG,"Serie: "+serie+"  Serie anterior: "+serieAnterior);
        if((respuestaPhp.contains(respuestaPhpAnterior) && (!serie.contains(serieAnterior))) || primeraVez){
            //Si la respuesta es igual a la anterior pero no coincide la serie anterior con la actual
            //hay que volver a pulsar el botón
            Toast to = Toast.makeText(getApplicationContext(), "PULSE NUEVAMENTE", Toast.LENGTH_SHORT);
            to.show();
            formateada = "¡¡PULSE NUEVAMENTE!!";
            System.out.println(formateada);
            primeraVez=false;
        }else {
            serieAnterior = serie; //Esto es para que avise cuando se cambia de equipo
            respuestaPhpAnterior = respuestaPhp;
            Character tab = 9;
            primeraVez=true;

            Log.d(TAG,"Datos Actuales leidos: " + respu);
            Log.d(TAG,"Posicion del caracter ';' es: " + respu.indexOf(';'));
            if (respu.indexOf(';') < 0 && respu.length() > 15) {
                Log.d(TAG,"Los datos leidos son >15 chars y no tienen ';'");
                formateada = "";
                //fechaInst=respuestaPhp.substring(0, 17);
                fechaInst = respu.substring(0, 8);
                //p4.jLabFechaHora.setText(fechaInst);
                //p4.jTxtSerieEquipo.setText(serieEquipo);

                formateada += "Fecha:" + tab + tab + tab + fechaInst + "\n"; //obtengo la fecha
                hora = respu.substring(9, 14);
                formateada += "Hora:" + tab + tab + tab + tab + tab + hora + "\n"; //obtengo la hora
                int posIni = respu.indexOf(124, 18);
                carga = respu.substring(18, posIni);
                formateada += "Carga:" + tab + tab + tab + tab + carga + " cm\n";
                int posFin = respu.indexOf(124, posIni + 1);
                caudal = respu.substring(posIni + 1, posFin);
                formateada += "Caudal:" + tab + tab + caudal + " l/s\n";
                posIni = posFin;
                posFin = respu.indexOf(124, posIni + 1);
                volts = respu.substring(posIni + 1, posFin);
                formateada += "Bateria:" + tab + tab + volts + " V";

            } else {

                formateada = "";
                if (respu.indexOf(';') > 0)
                    formateada = "¡¡REINTENTE!!";
                else
                    formateada = "¡Ese equipo no tiene registro de lectura actual!"
                            + "\n\nRespuesta de la Web: " + respu;
            }
        }
        //t.setText(respuestaPhp);
        t.setText("");
        t.setText(formateada);

    }

    private boolean isNetworkConnected(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null || !info.isConnected() || !info.isAvailable()) {
            return false;
        }
        return true;
    }

    private String phpGet(String reg, String php) {
        //respuestaPhp=""; // Borro todo lo anterior por las dudas
        url = urlFija + php + "?" + reg;
        Log.d(TAG,"url usada en el php "+php+" : "+url);
        int cuento=0;

        //Primero verifico si ha conexion a internet
        if (isNetworkConnected(getApplicationContext())) {
            Log.d(TAG,"Hay conexion a Internet");
        } else {
            String sinInternet = "No hay conexion a Internet\nReintente o SALIR";
            System.out.println(sinInternet);
            respuestaPhp="";

            Toast t3 = Toast.makeText(getApplicationContext(), sinInternet, Toast.LENGTH_SHORT);
            t3.show();
            return respuestaPhp;
        }
        // Instanciamos el RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest;
            // Request a string response from the provided URL.
            stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("onResponse()", "La respuesta de phpGet es: " + response);
                            respuestaPhp = response;
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("onErrorResponse()", "ERROR en GetPhp: " + error);
                    respuestaPhp = error.toString();
                }
            });
            queue.add(stringRequest);
        Log.d(TAG,"Respuesta del php "+php+", variable respuestaPhp :"+respuestaPhp);
        //respuestaPhpAnterior=respuestaPhp;
        return respuestaPhp;
    }


    /*
    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
    */
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d("onErrorResponse()", error.toString());
    }

    @Override
    public void onResponse(StringRequest response) {
        Log.d("onResponse()", response.toString());
    }

}
