package com.example.versuch2

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.*

class ControlActivity : AppCompatActivity() {

    companion object{

        /*
        jeder arduino hat ein UUID, diese dient zur identifikation
        also das ist eine einheitliche Nummer die kann man sich aus dem Internet raussuchen
         */

        var general_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        /*
        die variablen um bluetooth zum laufen zu kriegen
         */
        var bluetoothSocket: BluetoothSocket? = null
        lateinit var progress: ProgressDialog
        lateinit var bluetoothAdapter: BluetoothAdapter
        var isConnected: Boolean = false
        lateinit var deviceAddress: String
    }

    /*
    variablen für unsere einzelnen buttons
     */

    var control_led_on: Button? = null
    var control_led_off: Button? = null
    var control_left : Button? = null
    var control_right : Button? = null
    var control_forward : Button? = null
    var control_backward : Button? = null

    val REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        durch diese funktion verstecken wir die anzeige bar oben
         */

        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        setContentView(R.layout.activity_control)
        /*
        wir weisen unseren variablen die einzelnen buttons hinzu
        damit wir mit ihnen arbeiten könnnen
         */
        control_led_on = findViewById(R.id.buttonOn);
        control_led_off = findViewById(R.id.buttonOff)
        control_left = findViewById(R.id.buttonLeft)
        control_right = findViewById(R.id.buttonRight)
        control_forward = findViewById(R.id.buttonForward)
        control_backward = findViewById(R.id.buttonBackward)
        deviceAddress = intent.getStringExtra(MainActivity.EXTRA_ADDRESS).toString()

        connectionToHC05(this).execute()

        var switchLeft = true
        var switchRight = true
        /*
        diese Methoden haben folgenden Sinn, sie schicken an den Arduino über
        dass HC05 Bluetooth Modul Befehle welche dafür sorgen dass der
        Roboter forwärts, rückwärts, links,recht, led on, led off auführen,
        im Arduino Code ist eine SwitchCase Abfragen welches abprüft welche aktion ausgeführt werden
        soll, sobald man den Button nicht mehr drückt, wird "z" gesendet was den Roboter in den
        Ruhestand versetzt
         */

        control_led_on?.setOnClickListener{ sendDataToDevice("1") }
        control_led_off?.setOnClickListener{ sendDataToDevice("0") }

        control_left?.setOnTouchListener { view, motionEvent ->
            if(motionEvent == null) false
            val action = motionEvent.action
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                sendDataToDevice("4")
            } else {
                sendDataToDevice("z")
            }
            switchLeft = !switchLeft
            true
        }
        control_right?.setOnTouchListener { view, motionEvent ->
            if(motionEvent == null) false
            if(!(view is Button)) false
            val action = motionEvent.action
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                sendDataToDevice("5")
            }else {
                sendDataToDevice("z")
            }
            switchRight = !switchRight
            true
        }
        control_forward?.setOnTouchListener { view, motionEvent ->
            if(motionEvent == null) false
            val action = motionEvent.action
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                sendDataToDevice("2")
            } else {
                sendDataToDevice("z")
            }
            switchLeft = !switchLeft
            true
        }
        control_backward?.setOnTouchListener { view, motionEvent ->
            if(motionEvent == null) false
            if(!(view is Button)) false
            val action = motionEvent.action
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                sendDataToDevice("3")
            }else {
                sendDataToDevice("z")
            }
            switchRight = !switchRight
            true
        }


        if(checkCallingOrSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT),REQUEST_BLUETOOTH_CONNECT_PERMISSION)
        }

    }

    /*
    diese Mehode sendet die Daten also 1,2,3, an den Arduino
    welches dann die jeweilige Aktion ausführt
    wir schicken eine Bytestrom hin, der Arduino empfangt den Bytestrom per Serialread
    in einer variable und dann im switchCase kann er verwendet werden
     */

    private fun sendDataToDevice(input: String){
        if(bluetoothSocket != null){
            try{
                val singleByte = input.toByteArray()
                bluetoothSocket!!.outputStream.write(singleByte)
            }catch(e: IOException){
                e.printStackTrace()
            }
        }
    }
    /*
    habe ich am ende nicht gebraucht
     */
    private fun disconnect(){
        if(bluetoothSocket != null){
            try {
                bluetoothSocket!!.close()
                bluetoothSocket = null
                isConnected = false
            }catch(e: IOException){
                e.printStackTrace()
            }
        }
        finish()
    }

    private class connectionToHC05(c: Context) : AsyncTask<Void, Void, String>(){
        private var connectSucess: Boolean = true
        private val context: Context

        init{
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progress = ProgressDialog.show(context, "Connecting...", "please wait")
        }

        @SuppressLint("MissingPermission")
        override fun doInBackground(vararg p0: Void?): String {
            try{
                if(bluetoothSocket == null || !isConnected){
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(general_UUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    bluetoothSocket!!.connect()

                }
            }catch (e: IOException){
                connectSucess = false
                e.printStackTrace()
            }
            return null.toString()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!connectSucess){
                Log.i("data", "couldn't connect")
            }else{
                isConnected = true
            }
            progress.dismiss()
        }
    }
    /*
    so diese Methode zeigt die Fehlermeldung: Erlaubnis erteil oder nicht erteilt
    dass hat damit zu tun wenn wir eine Bluetooth verbindugn mit dem HC05 Modul erstellen wollen
    und es aus irgendeinem Grund nicht funktioniert
    dann kommem halt dann diese Meldungen
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            REQUEST_BLUETOOTH_CONNECT_PERMISSION -> {
                val index = permissions.indexOfFirst { it.equals(android.Manifest.permission.BLUETOOTH_CONNECT) }
                if(index == -1){
                    Toast.makeText(this, "Permission Bluetooth not found", Toast.LENGTH_SHORT).show()
                    return
                }
                val permission = permissions[index]
                val result = grantResults[index]

                if(result == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission Bluetooth granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Bluetooth not granted", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}