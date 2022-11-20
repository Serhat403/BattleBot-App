package com.example.versuch2

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*

class MainActivity : AppCompatActivity() {

    /*
    variablen um unsere Bluetooth Verbindung einzurichten
     */

    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var alreadyPairedBluetoothDevices: Set<BluetoothDevice>
    val REQUEST_ENABLE_BLUETOOTH = 1

    private var select_bluetoothDevices_list: ListView?=null
    private var button_device_refresh: Button?=null
    companion object{
        val EXTRA_ADDRESS: String = "Device_address"
    }
    /*
    mit dieser main ansicht werden uns bluetooth Geräte angezeigt
    diese werden in  einer Liste angezeigt mit welchen wir uns verbinden können
    um unseren roboter dann in der control ansicht zu steuern
     */
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter == null){
            //toast("this device doesn't support bluetooth")
            Toast.makeText(this@MainActivity, "this device does not support bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        if(!bluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }
        button_device_refresh= findViewById(R.id.select_device_refresh)
        button_device_refresh?.setOnClickListener{ pairedDeviceList() }
    }
    /*
    ich habe mein Handy und das HC05 Bluetooth Modul beretis einmal per Bluetooth
    verbunden, danach wird das HC05 Bluetooht Modul in den berets verbundenen Geräten angezeigt
    dass heißt mit dieser MEthode werden uns die bereits einmal verbundenen Geräte als List in einer
    Listview angezeigt und wir können uns mit ihnen dann per click verbinden
     */
    @SuppressLint("MissingPermission")
    private fun pairedDeviceList(){
        alreadyPairedBluetoothDevices = bluetoothAdapter!!.bondedDevices
        val list:ArrayList<BluetoothDevice> = ArrayList()

        if(!alreadyPairedBluetoothDevices.isEmpty()){
            for(device: BluetoothDevice in alreadyPairedBluetoothDevices){
                list.add(device)
                Log.i("device", ""+device)
            }
        }else{
            //toast("no paired bluetooth devices found")
            Toast.makeText(this@MainActivity, "no paired bluetooth devices found", Toast.LENGTH_SHORT).show()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        select_bluetoothDevices_list=findViewById(R.id.select_device_list)
        select_bluetoothDevices_list?.adapter = adapter
        select_bluetoothDevices_list?.onItemClickListener= AdapterView.OnItemClickListener{ _, _, position, _->
            val device: BluetoothDevice = list[position]
            val address: String = device.address
            val intent = Intent(this, ControlActivity::class.java)
            intent.putExtra(EXTRA_ADDRESS, address)
            startActivity(intent)
        }
    }

    /*
        durch diese Methode wird dem Nutzer die Frage gestellt ob er möchte
        dass Bluetooth eineschaltet wird das muss dann mit ja beantwortet werden
        dann wird als resultat zurückgegeben ob es funktioniert hat enabled, oder nicht funktioniert hat
        also nein mti disabled, und wenn man es abschaltet canceled
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            if(requestCode == REQUEST_ENABLE_BLUETOOTH){
                if(resultCode == Activity.RESULT_OK){
                    if(bluetoothAdapter!!.isEnabled){
                        Toast.makeText(this@MainActivity, "bluetooth has been enabled", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this@MainActivity, "bluetooth has been dissabled", Toast.LENGTH_SHORT).show()
                    }
                }else if (resultCode == Activity.RESULT_CANCELED){
                    Toast.makeText(this@MainActivity, "bluetooth has been canceled", Toast.LENGTH_SHORT).show()
                }
            }
        }

}