package com.example.asaem.dadm;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // Debugging
    private static final String TAG = "MainActivity";

    // permission request code
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 3;
    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 4;

    // layout
    Button setup;
    Button delete;
    Button report;

    // Macro message db member
    MacroMessageHandler mMacroMessageHandler= new MacroMessageHandler(MainActivity.this);
    MacroMessage mMacroMessage = new MacroMessage();
    private int macro_id = 0;
    private String defaultMessage = "운전중입니다\n 나중에 연락드리겠습니다";

    // Stack message list db member
    StackMessageHandler mStackMessageHandler;
    StackMessage mStackMessage;
    int stack_id = 0;

    // Report db member
    ReportHandler mReportHandler;
    Report mReport;
    private int report_id = 0;
    public String reportNumber = "01074300095";
    private String reportMessage = "교통 사고 발생";

    // BroadcastReceiver member
    BroadcastReceiver mMessageReceiver = new MessageReceiver();

    // BluetoothService member
    private BluetoothService mBluetoothService;

    // RaspberryThread member field
    // send data to raspberry pi, when receive sms or call
    Thread raspberryThread;
    boolean run_rasp = true;
    public String number = "";
    public String messageStr = "";
    public String msgDelimiter = "#";
    public String flag = "";
    static String CALL_STATE = "1#";
    static String SMS_STATE = "2#";

    // sendSmsThread member field
    // received data handle
    Thread sendSmsThread;
    boolean run_sms = true;
    String gestureTemp = "";
    static String AUTO_REPLY = "s1";
    static String SERIOUS_ACCIDENT = "e1";
    static String NORMAL_ACCIDENT = "e2";

    // call state handle
    public TelephonyManager tm;
    boolean ringState = false;

    // send sms receiver
    private BroadcastReceiver sendBroadcastReceiver;
    private BroadcastReceiver deliveryBroadcastReceiver;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    // called at start app
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up button event
        setup = (Button) findViewById(R.id.btn_setup);
        delete = (Button) findViewById(R.id.btn_delete);
        report = (Button) findViewById(R.id.btn_inputReport);

        setup.setOnClickListener(this);
        delete.setOnClickListener(this);
        report.setOnClickListener(this);

        // set up stack message list db
        mStackMessageHandler = new StackMessageHandler(this);
        mStackMessage = new StackMessage();
        if (stack_id == 0){
            mStackMessage.id = stack_id;
            mStackMessage.sender = "no sender";
            mStackMessage.message = "no message";
            stack_id = mStackMessageHandler.insertToStack(mStackMessage);
        }

        // set up report db
        mReportHandler = new ReportHandler(this);
        mReport = new Report();

        // check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                Log.d(TAG, "Manifest.permission.READ_PHONE_STATE");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                Log.d(TAG, "Manifest.permission.SEND_SMS");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
                Log.d(TAG, "Manifest.permission.READ_SMS");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_SMS},
                        MY_PERMISSIONS_REQUEST_READ_SMS);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
                Log.d(TAG, "Manifest.permission.RECEIVE_SMS");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECEIVE_SMS},
                        MY_PERMISSIONS_REQUEST_RECEIVE_SMS);
            }
        }

        // register BroadcastReceiver action
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(mMessageReceiver, filter);
        Log.d(TAG, "BroadcastReceiver registered");

        // BroadcastReceiver for send Sms
        sendBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getApplicationContext(), "전송 완료", Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getApplicationContext(), "전송 실패", Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getApplicationContext(), "서비스 지역이 아닙니다", Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getApplicationContext(), "무선(Radio)가 꺼져있습니다", Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getApplicationContext(), "PDU Null", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        // BroadcastReceiver for react of send Sms
        deliveryBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getApplicationContext(), "SMS 도착 완료", Toast.LENGTH_LONG).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getApplicationContext(), "SMS 도착 실패", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        // register BroadcastReceiver for send sms
        registerReceiver(sendBroadcastReceiver, new IntentFilter("SMS_SENT_ACTION"));
        registerReceiver(deliveryBroadcastReceiver, new IntentFilter("SMS_DELIVERED_ACTION"));

        // Bluetooth on
        mBluetoothService = new BluetoothService(this);
        mBluetoothService.checkBluetooth();

        // send data to raspberry pi, when receive sms or call
        raspberryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (run_rasp) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!("".equals(flag)) && !("".equals(number))) {
                        mBluetoothService.sendData(flag + number +  messageStr);
                        Log.d(TAG, "raspberry Thread " + flag + number + messageStr);
                        flag = "";
                    }
                }
            }
        });
        raspberryThread.start();

        // received data handle
        sendSmsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (run_sms) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    gestureTemp = mBluetoothService.getGesture();
                    if (!("".equals(gestureTemp))) {
                            Log.d(TAG, gestureTemp);
                            if (gestureTemp.equals(AUTO_REPLY)) { // received s1
                                if (number.equals("")) {
                                    Log.d(TAG, "send sms error, no number error");
                                }
                                else {
                                    Log.d("test1", "auto reply");
                                    mMacroMessage = mMacroMessageHandler.getMacroMessageById(macro_id);
                                    String macro = mMacroMessage.message;
                                    sendSMS(number, macro);
                                    Log.d(TAG, "send macro message, " + number + ", " + macro);
                                }
                            } else if (gestureTemp.equals(NORMAL_ACCIDENT)) { // received e2
                                String reportNum = reportNumber;
                                String reportMsg = "보험사\n" + reportMessage;
                                sendSMS(reportNum, reportMsg);
                                Log.d(TAG, "e2 send report message, "+reportNum+", "+reportMsg);
                            } else if (gestureTemp.equals(SERIOUS_ACCIDENT)) { // received e1
                                String reportNum = reportNumber;
                                String reportMsg = "119\n" + reportMessage;
                                sendSMS(reportNum, reportMsg);
                                Log.d(TAG, "e1 send report message, "+reportNum+", "+reportMsg);

                                if (report_id == -1){
                                    Toast.makeText(getApplicationContext(), "보호자 연락처가 없습니다.", Toast.LENGTH_LONG).show();
                                }
                                else{
                                    mReport = mReportHandler.getReportById(report_id);
                                    String reportNum2 = mReport.number;
                                    String reportMsg2 = "보호자\n" + mReport.message;
                                    sendSMS(reportNum2, reportMsg2);
                                    Log.d(TAG, "e1 send report message, "+reportNum2+", "+reportMsg2);
                                }
                            }
                        }
                    }
                    gestureTemp = ""; // initialize gesture
                }
        });
         sendSmsThread.start();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    // called at end app
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();

        // unregister BroadcastReceiver
        unregisterReceiver(mMessageReceiver);
        unregisterReceiver(sendBroadcastReceiver);
        unregisterReceiver(deliveryBroadcastReceiver);
        Log.d(TAG, "BroadcastReceiver unregistered");

        // Thread loop exit
        run_rasp = false;
        run_sms = false;

        // Bluetooth Thread and Socket close
        try {
            if (mBluetoothService.mThread != null) {
                mBluetoothService.mThread.interrupt();
            }
            if (mBluetoothService.mInputStream != null) {
                mBluetoothService.mInputStream.close();
            }
            if (mBluetoothService.mSocket != null) {
                mBluetoothService.mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // start StackActivity and app finish
        Intent intent = new Intent(MainActivity.this, StackActivity.class);
        startActivity(intent);
        finish();
    }

    // Check permission method
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "MY_PERMISSIONS_REQUEST_READ_PHONE_STATE");
                } else {
                    Toast.makeText(getApplicationContext(), "동의후 이용가능", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "MY_PERMISSIONS_REQUEST_SEND_SMS");
                } else {
                    Toast.makeText(getApplicationContext(), "동의후 이용가능", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_READ_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "MY_PERMISSIONS_REQUEST_READ_SMS");
                } else {
                    Toast.makeText(getApplicationContext(), "동의후 이용가능", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_RECEIVE_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "MY_PERMISSIONS_REQUEST_RECEIVE_SMS");
                } else {
                    Toast.makeText(getApplicationContext(), "동의후 이용가능", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Handled select bluetooth device
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BluetoothService.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    mBluetoothService.selectDevice();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "블루투스 연결을 취소했습니다.\n" +
                            "앱을 종료합니다.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
    * Button click event handle
    * */
    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick()");

        // setup button click
        if (view == findViewById(R.id.btn_setup)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.setup_macro);
            builder.setMessage(R.string.input_message);

            final EditText editText = new EditText(this);
            builder.setView(editText);

            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mMacroMessage.message = editText.getText().toString();
                    mMacroMessage.id = macro_id;

                    macro_id = mMacroMessageHandler.insertMacroMessage(mMacroMessage);
                    Toast.makeText(getApplicationContext(), "New Message Setup", Toast.LENGTH_LONG).show();

                    Log.d(TAG, "Macro message setup"+macro_id);
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(), "Cancel Setup Macro Message", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Macro message setup cancel");
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        } else if (view == findViewById(R.id.btn_delete)) { // delete button click
            // delete macro message
            mMacroMessageHandler.deleteMacroMessage(macro_id);

            // set up default macro message
            mMacroMessage.id = macro_id;
            mMacroMessage.message = defaultMessage;
            macro_id = mMacroMessageHandler.insertMacroMessage(mMacroMessage);

            Log.d(TAG, "Delete macro message");
            Toast.makeText(getApplicationContext(), "Delete Macro Message", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Macro message setup"+macro_id);
        } else if (view == findViewById(R.id.btn_inputReport)) { // report button click
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.input_report);
            builder.setMessage("보호자 번호를 입력하세요");

            final EditText editText1 = new EditText(this);
            builder.setView(editText1);

            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mReport.number = editText1.getText().toString();
                    mReport.message = reportMessage;
                    mReport.id = report_id;
                    Log.d(TAG, "report id: " + report_id);

                    if (report_id == 0) {
                        report_id = mReportHandler.insertReport(mReport);
                        Toast.makeText(getApplicationContext(), "New Report number Setup", Toast.LENGTH_LONG).show();
                    } else {
                        mReportHandler.updateReport(mReport);
                        Toast.makeText(getApplicationContext(), "Report number update", Toast.LENGTH_LONG).show();
                    }
                    Log.d(TAG, "report id: " + report_id);
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }
    }

    /*
    * send sms
    * @params: phone number of sms receiver, sms message
    * */
    public void sendSMS(String smsNumber, String smsText) {
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.asaem.dadm/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.asaem.dadm/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    /*
    * Call and Sms message BroadcastReceiver class
    * */
    public class MessageReceiver extends BroadcastReceiver {
        // debugging
        private static final String TAG = "MessageReceiver";

        public MessageReceiver() {
            super();
        }

        @Override
        public void onReceive(final Context context, Intent intent) {
            Log.d(TAG, "onReceive()");

            // get TelephonyManager
            tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            // receive call state
            tm.listen(new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    Log.d(TAG, "Phone call state changed");

                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        Log.d(TAG, "Phone call incoming sender: " + incomingNumber);
                        Toast.makeText(context, "Phone call incoming\nSender: " + incomingNumber, Toast.LENGTH_LONG).show();
                        number = "";
                        number = number + incomingNumber;
                        messageStr = "";
                        flag = "";
                        flag = flag + CALL_STATE;
                        ringState = true;
                        Log.d(TAG, number + ", " + incomingNumber);
                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            }, PhoneStateListener.LISTEN_CALL_STATE);

            // receive sms message
            if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                SmsMessage[] msgs;
                String sender = "";
                String message = "";
                String str = "";

                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];

                    for (int i = 0; i < pdus.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        sender = sender + msgs[i].getOriginatingAddress();
                        message = message + msgs[i].getDisplayMessageBody().toString();
                    }

                    str = str + "Sender: " + sender + "\nMessage: " + message;
                    Log.d(TAG, str);
                    Toast.makeText(context, str, Toast.LENGTH_LONG).show();

                    number = "";
                    number = number + sender;
                    messageStr = "";
                    messageStr = msgDelimiter + message;
                    flag = "";
                    flag = flag + SMS_STATE;
                    Log.d(TAG, number + ", " + sender);

                    mStackMessage.id = stack_id;
                    mStackMessage.sender = sender;
                    mStackMessage.message = message;

                    stack_id = mStackMessageHandler.insertToStack(mStackMessage);
                    Log.d(TAG, "Stack to Message List, id_stackMessage: " + stack_id);
                }
            }
        }
    }
}