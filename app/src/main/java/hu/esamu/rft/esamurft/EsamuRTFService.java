package hu.esamu.rft.esamurft;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import hu.esamu.rft.esamurft.utility.ConfigHelper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import hu.esamu.rft.esamurft.ProtoPicture.Picture;

/**
 * This service handles the TCP connection of our application.
 * You can reach this service with bounding.
 * @see Service
 * @link https://developer.android.com/guide/components/bound-services.html
 * @author Jakab Ádám
 * */
public class EsamuRTFService extends Service {
    /**
     * Some constraints for the debugging and intent filtering.
     */
    private static final String TAG = "EsamuRTFService";
    private static final String EXTRA_SERVER_MESSAGE = "hu.esamu.rft.esamurft.server_message";
    private static final String EXTRA_NETWORK_NOT_AVAILABLE = "hu.esamu.rft.esamurft.network_not_available";
    private static final String EXTRA_SERVER_NOT_RUNNING = "hu.esamu.rft.esamurft.server_not_running";
    private static final String EXTRA_CANT_SEND_MESSAGE = "hu.esamu.rft.esamurft.cant_send_message";

    /**
     * @see IBinder
     * */
    private final IBinder mBinder = new EsamuRTFServiceBinder();

    /**
     * These fields are the server IP and port.
     * You can edit these values in 'res/raw/config.properties"
     * @see ConfigHelper
     * */
    private String serverIp;
    private int serverPort;

    /**
     * Server socket and streams for communication.
     * These must be volatile because of the separate threads.
     * */
    private volatile Socket client = null;
    private volatile static ObjectOutputStream out = null;
    private volatile static ObjectInputStream in = null;

    public class EsamuRTFServiceBinder extends Binder {
        public EsamuRTFService getService(){
            return EsamuRTFService.this;
        }
    }

    @Override
    public void onCreate(){
        serverIp = ConfigHelper.getConfigValue(this, "server_ip");
        serverPort = Integer.parseInt(ConfigHelper.getConfigValue(this, "server_port"));
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * Starts the TCP connection with the server.
     * If it fails, it send an intent to the server with the proper extra.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(isNetworkAvailableAndConnected()) {
            Log.e(TAG, "SERVERIP: " + serverIp + ", SERVERPORT: " + serverPort);
            new Thread(new TCPClientThread()).start();
            return START_STICKY;
        } else {
            Intent response = new Intent(EXTRA_NETWORK_NOT_AVAILABLE);
            response.putExtra(EXTRA_NETWORK_NOT_AVAILABLE, "Nem lehet csatlakozni a hálózathoz. Nincs bekapcsolva a wifi?");
            LocalBroadcastManager.getInstance(EsamuRTFService.this).sendBroadcast(response);
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * The activity calls this method when the user presses the send button.
     * Sends a message to the server via the {@link ObjectOutputStream}.
     * @param message A message to the server.
     * */
    public void sendMessage(Object message){
        class TCPClientWriteThread implements Runnable {
            private Object message;
            private TCPClientWriteThread(Object msg) {
                message = msg;
            }
            @Override
            public void run() {
                try {
                    out.writeObject(message);
                    out.flush();
                } catch (NullPointerException e_null) {
                    Intent response = new Intent(EXTRA_CANT_SEND_MESSAGE);
                    response.putExtra(EXTRA_CANT_SEND_MESSAGE, "Ha nincs szerver, üzenetet sem tudsz küldeni.");
                    LocalBroadcastManager.getInstance(EsamuRTFService.this).sendBroadcast(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        new Thread(new TCPClientWriteThread(message)).start();
    }

    /**
     * The activity calls this method when the user presses the send button.
     * Sends a message to the server via the {@link ObjectOutputStream}.
     * @param message A message to the server.
     * */
    public void sendMessage(ProtoPicture.Picture message){
        class TCPClientWriteThread implements Runnable {
            private ProtoPicture.Picture message;
            private TCPClientWriteThread(ProtoPicture.Picture msg) {
                message = msg;
            }
            @Override
            public void run() {
                try {
                    out.writeObject(message.toByteArray());
                    out.flush();
                } catch (NullPointerException e_null) {
                    Intent response = new Intent(EXTRA_CANT_SEND_MESSAGE);
                    response.putExtra(EXTRA_CANT_SEND_MESSAGE, "Ha nincs szerver, üzenetet sem tudsz küldeni.");
                    LocalBroadcastManager.getInstance(EsamuRTFService.this).sendBroadcast(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        new Thread(new TCPClientWriteThread(message)).start();
    }


    /**
     * TCP Client class.
     * On run it will setup the socket connection and starts the input stream
     * to read the incomming objects from the server.
     * */
    private class TCPClientThread implements Runnable {
        @Override
        public void run() {
            try{
                client = new Socket(serverIp, serverPort);
                out = new ObjectOutputStream(client.getOutputStream());
                in = new ObjectInputStream(client.getInputStream());
                Log.d(TAG, "Connected to " + serverIp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Object serverIn;
                while(true){
                    if((serverIn = in.readObject()) != null){
                        if(serverIn instanceof String){
                            Intent response = new Intent(EXTRA_SERVER_MESSAGE);
                            response.putExtra(EXTRA_SERVER_MESSAGE, (String)serverIn);
                            LocalBroadcastManager.getInstance(EsamuRTFService.this).sendBroadcast(response);
                            Log.d(TAG, (String)serverIn);
                        }
                    } else {
                        break;
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e_null_pointer){
                Intent response = new Intent(EXTRA_SERVER_NOT_RUNNING);
                response.putExtra(EXTRA_SERVER_NOT_RUNNING, "A megadott IP-címen nem érhető el szerver.");
                LocalBroadcastManager.getInstance(EsamuRTFService.this).sendBroadcast(response);
            }

        }
    }

    /**
     * Checks the networks state.
     * If the wifi is enabled and connected to the network returns true,
     * else returns false.
     * */
    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        return isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
    }
}
