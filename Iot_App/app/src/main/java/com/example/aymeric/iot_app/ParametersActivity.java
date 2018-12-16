package com.example.aymeric.iot_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

//Import des librairies MQTT
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

//Import des librairies MQTT
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;

/********* QUE FAIT CETTE ACTIVITY ?********/
 /**
 * 1. ELLE PERMET DE MODIFIER LES PARAMETRES DE LA LAMPE
 * 2. ELLE SE CONNECTE AU SERVEUR (oui encore, et d'ailleurs c'est un pb...)
 * 3. ELLE ENVOIE LES MODIFICATIONS AU SERVEUR*/

/********* PROBLEMES **************/
/**
 * 1. PROBLEME DE CONNECTION : On a créer un client dans la première activity et tout, ok !
 * MAIS ! J'arrive pas à garder ce même client, je suis obligé d'en créer un nouveau, donc de me reconnecter au serveur
 * avec ce client. Cela pose problème car j'envoie donc deux messages différents au serveur (un pour la lampe, l'autre pour ses états)
 *
 * 2. PROBLEME D'ERGONOMIE : J'arrive pas à ne pas créer le bouton "SEND DATA" pour envoyer les données... Normalement ce qui serait
 * cool ce serait d'envoyer en temps réel les modifications sans qu'on ait à le faire. Mais bon en soit c'est pas trop grave*/


public class ParametersActivity extends AppCompatActivity {

    private Switch switch1;
    private SeekBar briSeekBar;
    private SeekBar hueSeekBar;
    private TextView brighvalue;
    private TextView huevalue;
    private Button connect;
    private Button sendData;
    private double bri =1;
    private double hue = 1;

    String clientId = MqttClient.generateClientId();
    final MqttAndroidClient client =
            new MqttAndroidClient(ParametersActivity.this, "tcp://m15.cloudmqtt.com:13274",
                    clientId);

    String topic = "foo/bar";
    int qos = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);

        switch1 = findViewById(R.id.switch1);
        briSeekBar = findViewById(R.id.seekBar);
        hueSeekBar = findViewById(R.id.seekBar3);
        brighvalue = findViewById(R.id.textView3);
        huevalue = findViewById(R.id.textView4);
        connect = findViewById(R.id.activity_parameter_connect);
        sendData = findViewById(R.id.activity_parameter_sendData);

        sendData.setEnabled(false);

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    Toast.makeText(getApplicationContext(), "On", Toast.LENGTH_SHORT).show();
                    switch1.setText("On");

                    // ENVOI "ON"
                    /*String topic = "foo/bar";
                    String payload = "on : true";
                    byte[] encodedPayload = new byte[0];
                    try {
                        encodedPayload = payload.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        client.publish(topic, message);
                    } catch (UnsupportedEncodingException | MqttException e) {
                        e.printStackTrace();
                    }*/


                }else{
                    Toast.makeText(getApplicationContext(), "Off", Toast.LENGTH_SHORT).show();
                    switch1.setText("Off");

                    //ENVOI OFF
                   /* String topic = "foo/bar";
                    String payload = "on : false";
                    byte[] encodedPayload = new byte[0];
                    try {
                        encodedPayload = payload.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        client.publish(topic, message);
                    } catch (UnsupportedEncodingException | MqttException e) {
                        e.printStackTrace();
                    }*/

                }
            }
        });

        briSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                bri = progress*2.55;
                brighvalue.setText("" + (int) bri);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                brighvalue.setText("" + (int)bri);
                Toast.makeText(ParametersActivity.this, "Brightness : "+(int)bri, Toast.LENGTH_SHORT).show();

                //ENVOI LA VALEUR DE BRI
               /* String topic = "foo/bar";
                String payload = "bri : ";
                byte[] encodedPayload = new byte[0];
                try {
                    encodedPayload = payload.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    client.publish(topic, message);
                } catch (UnsupportedEncodingException | MqttException e) {
                    e.printStackTrace();
                }*/
            }
        });

        hueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                hue = progress * 3.6;
                huevalue.setText(""+ (int)hue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                huevalue.setText(""+ (int)hue);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                huevalue.setText(""+ (int)hue);
                Toast.makeText(ParametersActivity.this, "HUE : "+(int)hue, Toast.LENGTH_SHORT).show();

                //ENVOI LA VALEUR DE HUE
                //String topic = "foo/bar";
                /*String payload = "on : true";
                byte[] encodedPayload = new byte[0];
                try {
                    encodedPayload = payload.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    client.publish(topic, message);
                } catch (UnsupportedEncodingException | MqttException e) {
                    e.printStackTrace();
                }*/

            }
        });

        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(ParametersActivity.this, "Envoi des données...", Toast.LENGTH_SHORT).show();
                //ENVOI DES DONNES
                String topic = "foo/bar";
                String payload = "on : "+ switch1.isChecked()+", bri : "+(int)bri +", hue : "+(int)hue;
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



        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData.setEnabled(true);
                try {
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
                    options.setUserName("ldwqzejh");
                    options.setPassword("PkkXIJ8X5fdM".toCharArray());
                    IMqttToken token = client.connect(options);
                    token.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Toast.makeText(ParametersActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                            subscribe();
                            Toast.makeText(ParametersActivity.this, "Nice !", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            // Something went wrong e.g. connection timeout or firewall problems
                            Toast.makeText(ParametersActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void subscribe(){

        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                //Show smth
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    Toast.makeText(ParametersActivity.this, "Subscribe !", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    Toast.makeText(ParametersActivity.this, "Failed to subscribe !", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
}
