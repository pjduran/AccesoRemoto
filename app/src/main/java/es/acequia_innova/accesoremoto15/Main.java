package es.acequia_innova.accesoremoto15;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    private static Spinner combo;
    private static String [] nomEquip;
    private static String [] serieEquip;
    private static String [][] eq;
    private static int numEqEncontrados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TvMensajes =  (TextView) findViewById(R.id.textView_Mensajes);
        //TvMensajes.setText(getResources().getString(R.string.ingreseUsuClave));
        TvMensajes.setText(R.string.ingreseUsuClave); //ESta forma es más simple que la anterior

        //////////////////////
        ///ARMO EL BOTON
        ///////////
        Button boton = (Button) findViewById(R.id.butt_OK);
        boton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                //LO PRIMERO SERIA CONFIRMAR LA EXISTENCIA DEL USUARIO Y SU CLAVE
                //Esto lo hace onClickOK()
                boolean existeUsu = onClickOk();

                //LUEGO HAY QUE ENCONTRAR LOS EQUIPOS QUE LE PERTENECEN Y PONERLOS EN UN ARRAY String[]
                //Para que se vean en el Spinner
                if (existeUsu){ //Si existe el usuario, hay que buscar sus equipos


                }

            }
        });

        ///////////////////////
        ////ARMO EL SPINNER DESPLEGABLE Y SU METODO DE CAPTURA
        ////////////
        combo = (Spinner) findViewById(R.id.spinner_Equipos);
        //Creamos el adaptador del spinner donde el tercer parámetro es el array donde van a ir los nombres
        ArrayAdapter adaptador = new ArrayAdapter(this,android.R.layout.simple_spinner_item, nomEquip);

        //Creo que primero habría que llenar el array String [] nomEquip

        //Vinculamos el Spinner con su adaptador
        combo.setAdapter(adaptador);

        combo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Aqui adentro habría que
                //AL ELEGIR UNO, HAY QUE BUSCAR SUS DATOS ACTUALES Y MOSTRARLOS EN UN AREA DE TEXTO
                int indiceArray = combo.getSelectedItemPosition(); //no se si hay que restarle 1
                /*
                serie = serieEquip[]; //"1507171821";
                BajoDatosActuales(serie);
                */
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast to = Toast.makeText(getApplicationContext(), "La cagaste Burt Lancaster", Toast.LENGTH_LONG);
                to.show();
            }
        });

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

    //Aqui hay que sustituir el combobox por una lista desplegable
    public static boolean encuentroEquipos(Spinner jCBequipos) {
        System.out.println("Entro a encuentroEquipos()");
        //debe encontrar los equipos de ese usuario o administrador
        if(isAdminSist){
            if(buscoEquipAdmin(jCBequipos))
                return true;
            else
                return false;
        }else{
            if(buscoEquiposUsu(jCBequipos))
                return true;
            else
                return false;
        }
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
        System.out.println("Numero de celdas del StringBuffer: "+numLineas);
        int nL;
        if(numLineas%2==0)
            nL = numLineas/2;
        else
            nL = numLineas/2+1;
        System.out.println("Numero de equipos: "+nL);
        numEqEncontrados=nL;
        String[][] lineas = new String[2][nL];
        String lin = ""; //aqui guardo cada una de las campos separadas por un caracter
        int j=0;
        int celda=0; //0: serie; 1: nombre del equipo
        char c;
        //obtengo las cadenas con la linea de cada variable
        for (int k = 0; k < b.length(); k++) {
//			System.out.print(b[k]);
            c = b.charAt(k);
            if (c != d) {//si el byte leido no es el limitador de campos, acumulo las letras
                lin = lin + c;
            } else {
                if (celda == 0) { //si la celda es 1
                    lineas[celda][j] = lin.trim(); //guardo la celda uno: nombre
                    celda = 1; //cambio a cero
                } else { //si la celda es cero
                    lineas[celda][j] = lin.trim(); //guardo la ceelda cero: serie
                    celda = 0; //cambio a 1 para la proxima vez
//                    System.out.println("Linea " + j + " : "
//                            + lineasConH[0][j] + "  " + lineasConH[1][j]);
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
        System.out.println("Número de unidades separadas por el caracter "+d+" : "+numL);
        return numL;
    }

    //Aqui hay que sustituir el combobox por una lista desplegable
    private boolean buscoEquipAdmin(Spinner jCBequipos) {
        //ResultSet rs = null;
        char d = ';';
        int numCar = 0;
        String[] equip;
        //aqui debo armar la consulta con phpGet(reg, php)
        String reg = "dato=" + numTit;
        String sb = phpGet(reg, "buscoEquiposAdmin3.php");
        System.out.println("Respuesta del buscoEquiposAdmin3.php: "+sb);
//        if(!(sbUltReg.toString().contains("false")) &&!(sbUltReg.toString().contains("error"))){
//            equip = obtengoCampos(sbUltReg, direc, numCar);

        ////////////////
        System.out.println("Respuesta del buscoEquiposUsu.php: "+sb);
        if(sb!=null && !(sb.contains("false")) &&!(sb.contains("error"))){
            eq = obtengoLinEquip(sb, d, numCar);
            equip = new String[numEqEncontrados];
            System.out.println("Número de lineas en matriz eq: "+numEqEncontrados);
            for(int j=0;j<numEqEncontrados;j++){
                equip[j]=eq[1][j];
            }
            ////////////
            llenoComboEquip(jCBequipos, equip);
            return true;
        }else{
            return false;
        }
    }

    //Aqui hay que sustituir el combobox por una lista desplegable
    private boolean  buscoEquiposUsu(JComboBox jCBequipos) {
        System.out.println("Entro a buscoEquiposUsu()");
        String sb = null;
        String[] equip;
        char d = ';';
        int numCar = 0;
        //aqui debo armar la consulta con phpGet(reg, php)
        String reg = "dato=" + numUsu;
        sb = phpGet(reg, "buscoEquiposUsu.php");
        System.out.println("Respuesta del buscoEquiposUsu.php: "+sb.toString());
        if(sb!=null && !(sb.toString().contains("false")) &&!(sb.toString().contains("error"))){
            eq = obtengoLinEquip(sb, d, numCar);
            equip = new String[numEqEncontrados];
            System.out.println("Número de lineas en matriz eq: "+numEqEncontrados);
            for(int j=0;j<numEqEncontrados;j++){
                equip[j]=eq[1][j];
            }
            llenoComboEquip(jCBequipos, equip);
            return true;
        }else{
            System.out.println("Algo salio mal en buscoEquiposUsu");
            return false;
        }
    }

    //Aqui hay que sustituir el combobox por una lista desplegable
    private static void llenoComboEquip(Spinner jCBequipos, String[] eq) {
        DefaultComboBoxModel modeloCombo = new DefaultComboBoxModel();//esto es el modelo
        modeloCombo.addElement("Seleccione un Equipo");//es el primer registro q mostrara el combo
        jCBequipos.setModel(modeloCombo);//con esto lo agregamos al objeto al jcombobox
        for (int j = 0; j < eq.length; j++) {
            modeloCombo.addElement(eq[j]);
            jCBequipos.setModel(modeloCombo);
        }
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
