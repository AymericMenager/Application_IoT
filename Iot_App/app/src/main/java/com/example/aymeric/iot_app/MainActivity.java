package com.example.aymeric.iot_app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

//Import des librairies Bluetooth
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

//Import des librairies MQTT
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;


// SOURCE BLUETOOTH https://www.youtube.com/watch?v=DPbCFAQCFhE&feature=youtu.be

/*********** QUE FAIT CETTE ACTIVITY ?*************
 *
 * 1. ELLE SCAN LES APPAREILS EN BLUETOOTH DISPONIBLE ET LES AFFICHE A L'ECRAN
 * 2. ELLE SE CONNECTE AU SERVEUR MQTT POUR ENVOYER UN MESSAGE (QUELLE LAMPE ON A SELECTIONNE ?)
 * 3. QUAND ON CLIQUE SUR UN APPAREIL DISPONIBLE, ON VA VERS LA SECONDE ACTIVITY*/

/*******PROBLEME**********
 * J'arrive pas à dire au serveur "j'ai cliqué sur CETTE lampe
 * On verra au lab quand on aura le matos, là je teste avec la télé 4k du voisin...
 */


public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ACCESS_COARSE_LOCATION = 1;
    public static final int REQUEST_ENABLE_BLUETOOTH = 11;
    private ListView devicesList;
    private Button scanningBtn;
    private Button connect;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> listAdapter;


    //Déclaration d'un client, permettant de se connecter au serveur et d'envoyer un message
    String clientId = MqttClient.generateClientId();
    final MqttAndroidClient client =
            new MqttAndroidClient(MainActivity.this, "tcp://m15.cloudmqtt.com:13274",
                    clientId);

    // On utilie le topic "foo/bar"
    String topic = "foo/bar";
    int qos = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // we get bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        devicesList = findViewById(R.id.activity_main_devicesList);
        scanningBtn = findViewById(R.id.activity_main_scanningBtn);
        connect = findViewById(R.id.connect);

        //On crée un "array" (une liste) pour afficher les appareils dispo à l'écran
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        devicesList.setAdapter(listAdapter);

        //we check bluetoothstate
        checkBluetoothState();

        //we register a dedicated receiver for some Bluetooth actions
        // On peut trouver les appareils...
        registerReceiver(deviceFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        //... Commencer la recherche d'appareils...
        registerReceiver(deviceFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        //... Et stopper la recherche
        registerReceiver(deviceFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        scanningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (bluetoothAdapter!=null && bluetoothAdapter.isEnabled()) {
                    //On check si on peut avoir accès à la position du téléphone
                    if (checkCoarseLocationPermission()) {
                        listAdapter.clear();
                        bluetoothAdapter.startDiscovery();
                    }

                } else {
                    checkBluetoothState();
                }

            }
        });

        // Détecte le clique sur un élément de la liste des appareils trouvés
        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //On passe à la prochaine activity
                Intent parameterIntent = new Intent(MainActivity.this, ParametersActivity.class);
                startActivity(parameterIntent);

                // ENVOI DU PREMIER MESSAGE : QUELLE LAMPE MODIFIER
                String topic = "foo/bar";
                String payload = "LAMPE";
                Toast.makeText(MainActivity.this, "Sending message...", Toast.LENGTH_SHORT).show();
                byte[] encodedPayload = new byte[0];
                try {
                    encodedPayload = payload.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    client.publish(topic, message);
                } catch (UnsupportedEncodingException | MqttException e) {
                    e.printStackTrace();
                }

            }
        });

        // On se connecte au serveur
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
                    options.setUserName("ldwqzejh");
                    options.setPassword("PkkXIJ8X5fdM".toCharArray());
                    IMqttToken token = client.connect(options);
                    token.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                            subscribe();
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            // Something went wrong e.g. connection timeout or firewall problems
                            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        //we check permission a start of the app
        checkCoarseLocationPermission();

    }

    // Bon je vous avoue, tout ça j'ai pas tout compris...
    // En gros, ça check la position de l'appareil et tout, ça fait des trucs de Bluetooth quoi
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(deviceFoundReceiver);
    }

    private boolean checkCoarseLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_COARSE_LOCATION);
            return false;
        } else {
            return true;
        }
    }

    // Pas la peine de trop rentrer dans les détails de ça, en gros ça check si ton tel peut supporter le Bluetooth,
    // si il est activé et tout
    private void checkBluetoothState() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on your device !", Toast.LENGTH_SHORT).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {
                if (bluetoothAdapter.isDiscovering()) {
                    Toast.makeText(this, "Device discovering process ...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show();
                    scanningBtn.setEnabled(true);
                }
            } else {
                Toast.makeText(this, "You need to enable Bluetooth", Toast.LENGTH_SHORT).show();
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            checkBluetoothState();
        }
    }

    // Fonction qui regarde si l'app peut avoiraccès à la position de l'appareil
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Access coarse location allowed. You can scan Bluetooth devices", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Access coarse location forbidden. You can't scan Bluetooth devices", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    // we need to implement our receiver to get devices detected
    // SCAN LES APPAREILS AUX ALENTOURS
    private final BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Si on a trouvé un appareil...
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //... On le met dans la liste
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //... Avec son nom et son adresse
                listAdapter.add(device.getName() + "\n" + device.getAddress());
                listAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // Quand on a fini de scanner, y a marquer SCANNER sur le bouton
                scanningBtn.setText("SCANNER");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                // Quand on est en trzinde scanner, y a marqué SCANNING IN PROGRESS sur le bouton
                scanningBtn.setText("SCANNING IN PROGRESS...");
            }
        }
    };

    // Fonction qui permet de "subscribe" (bien vu Billy)
    public void subscribe(){

        try {
            //On subscribe au topic "foo/bar"
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                //Show smth
                @Override
                // Si ça marche
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    Toast.makeText(MainActivity.this, "Subscribe !", Toast.LENGTH_SHORT).show();
                }

                @Override
                // Si ça marche pas (mais di tute façon ci juste ! #Mechkour)
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    Toast.makeText(MainActivity.this, "Failed to subscribe !", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

}


