package com.example.asaem.dadm;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Songbum on 2016-11-30.
 */
public class BluetoothService {
    // Debugging
    private static final String TAG = "BluetoothService";

    // Bluetooth on/off state of Device
    static final int REQUEST_ENABLE_BT = 1;

    // Paired device list
    int mPairedDeviceCount = 0;
    Set<BluetoothDevice> mDevices;

    // Bluetooth adapter object
    BluetoothAdapter mBluetoothAdapter;

    // Device to bluetooth connect
    BluetoothDevice mRemoteDevice;

    // Bluetooth socket and In&OutStream for data communication
    BluetoothSocket mSocket = null;
    OutputStream mOutputStream = null;
    InputStream mInputStream = null;
    char mCharDelimiter = '\n';

    // Thread for read data from raspberry pi
    Thread mThread = null;
    byte[] readBuffer;
    int readBufferPosition;

    // Context of MainActivity
    static Context mContext;

    // gesture sensor data received from raspberry pi
    private String gesture = "";

    /*
    Constructor of BluetoothService class
    @parameter : UI Activity calling this class
    */
    public BluetoothService(Context context){
        mContext = context;
    }

    /*
    When launch this app, called this method
    Bluetooth module to enable
    If device not supports Bluetooth module -> app exit
    */
    public void checkBluetooth(){
        /* getDefaultAdapter()
        Get bluetooth adapter for enable bluetooth
        If device not supports bluetooth module -> return null
        */
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null){
            Toast.makeText(mContext, "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
            ((Activity) mContext).finish();
        }
        else{
            /*BluetoothAdapter.isEnabled()
            check for bluetooth module of device is enabled
            return : enable -> true,  disable -> false
            */
            if (!mBluetoothAdapter.isEnabled()){
                Toast.makeText(mContext, "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_LONG).show();
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity)mContext).startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
            }
            else{
                selectDevice();
            }
        }
    }

    /*
    Select device of paired device lists for connect
    */
    public void selectDevice(){
        mDevices = mBluetoothAdapter.getBondedDevices();
        mPairedDeviceCount = mDevices.size();

        if (mPairedDeviceCount == 0){ // no paired device
            Toast.makeText(mContext, "페어링된 장치가 없습니다.\n" +
                    "먼저 장치를 페어링 한 후 다시 실행해주세요.", Toast.LENGTH_LONG).show();
            ((Activity) mContext).finish();
        }

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setTitle("연결할 장치 선택");

        List<String> list = new ArrayList<String>();
        for (BluetoothDevice device : mDevices){
            list.add(device.getName());
        }
        list.add("취소");

        final CharSequence[] items = list.toArray(new CharSequence[list.size()]);
        list.toArray(new CharSequence[list.size()]);

        mBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == mPairedDeviceCount){ // select cancel
                    Toast.makeText(mContext, "연결할 장치를 선택하지 않았습니다.\n" +
                            "앱을 종료합니다.", Toast.LENGTH_LONG).show();
                    ((Activity) mContext).finish();
                }
                else { // select device
                    connectToSelectedDevice(items[which].toString());
                }
            }
        });
        mBuilder.setCancelable(false); // disable back button
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    } // selectDevice()

    /*
    Connect to remote device via bluetooth
    get In&OutStream from socket for data communicate
    */
    void connectToSelectedDevice(String selectedDeviceName){
        mRemoteDevice = getDeviceFromBondedList(selectedDeviceName);
        // UUID of Serial Port Protocol
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {
            // Socket create and connect with remote device
            mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect();

            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();

            // ready for receive data
            beginListenForData();
        } catch (IOException e){
            Toast.makeText(mContext, "블루투스 연결 중 오류가 발생했습니다.\n" +
                    "앱을 종료합니다.", Toast.LENGTH_LONG).show();
            ((Activity) mContext).finish();
        }
    }

    /*
   return object of device
   */
    BluetoothDevice getDeviceFromBondedList(String name){
        BluetoothDevice selectedDevice = null;
        for (BluetoothDevice device : mDevices){
            if (name.equals(device.getName())){
                selectedDevice = device;
                break;
            }
        }
        return  selectedDevice;
    }

    /*
   start thread for receive data
   */
    void beginListenForData(){
        final Handler handler = new Handler();

        readBufferPosition = 0;
        readBuffer = new byte[1024];

        // thread for receive data
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()){
                    try {
                        int byteAvailable = mInputStream.available();
                        if (byteAvailable > 0){
                            byte[] packetBytes = new byte[byteAvailable];
                            int bytes;
                            // read(buf[]) : read data as buf size from InputStream, empty stream : read -1
                            bytes = mInputStream.read(packetBytes);
                            for (int i=0; i<byteAvailable; i++){
                                byte b = packetBytes[i];
                                if (b == mCharDelimiter){
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // assign received data to String var, gesture
                                            gesture = data.trim();
                                        }
                                    });
                                }
                                else{
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException e){
                        Toast.makeText(mContext, "데이터 수신 중 오류가 발생했습니다.\n" +
                                "앱을 종료합니다.", Toast.LENGTH_LONG).show();
                        ((Activity) mContext).finish();
                    }
                }
            }
        });
        mThread.start();
    }

    /*
    Send message to remote device
    */
    public void sendData(String msg){
        byte[] buf = msg.getBytes();
        try {
            mOutputStream.write(buf);
        }catch (IOException e){
            Toast.makeText(mContext, "데이터 전송중 오류 발생",Toast.LENGTH_LONG).show();
            ((Activity)mContext).finish();
        }
    }

    /*
    send gesture data to MainActivity
    */
    public String getGesture(){
        if ("s1".equals(gesture) || "s1\n".equals(gesture)){
            gesture= "";
            return "s1";
        }
        if ("e1".equals(gesture) || "s1\n".equals(gesture)){
            gesture= "";
            return "e1";
        }
        if ("e2".equals(gesture) || "e2\n".equals(gesture)){
            gesture= "";
            return "e2";
        }
        else{
            gesture= "";
            return "";
        }
    }
}