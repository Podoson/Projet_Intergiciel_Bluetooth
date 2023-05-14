package com.example.bluetoothapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE_BT_CONNECT = 1;
    private static final int REQUEST_INTENT_ENABLE_BT = 2;
    private static final int REQUEST_PERMISSION_CODE_BT_ADVERTISE = 3;
    private static final int REQUEST_INTENT_DISCOVERABLE = 4;
    private static final int REQUEST_PERMISSION_CODE_BT_SCAN = 5;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    private static final String APP_NAME = "BTApp";
    private static final UUID MY_UUID = UUID.fromString("410aaa96-6d14-4084-b6ab-0203b7bf7344");

    SendReceive sendReceive;

    CheckBox enable_bt, visible_bt;
    Button search_bt, bt_scan, btn_send, btn_newBt;
    TextView name_bt, scanText, tv_status;
    ListView listView, listViewNewDevices;
    EditText et_msg;
    LinearLayout layout_lists;
    ScrollView layout_msg_et;

    Toolbar toolbar;

    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private List<BluetoothDevice> listPairedDevices = new ArrayList<>();

    private List<BluetoothDevice> listNewDevices = new ArrayList<>();
    ArrayList<String> listStringNewDevices = new ArrayList<>();
    ArrayAdapter<String> newDevicesAdapter;

    RecyclerView recyclerMsg;
    MessageAdapter messageAdapter;

    List<com.example.bluetoothapp.Models.Message> messageList = new ArrayList<>();
    int messageId = 0;

    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_CODE_BT_CONNECT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        return;
                }
                listStringNewDevices.add(device.getName());
                newDevicesAdapter.notifyDataSetChanged();
                listNewDevices.add(device);
            }
        }
    };

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enable_bt = findViewById(R.id.enable_bt);
        visible_bt = findViewById(R.id.visible_bt);
        search_bt = findViewById(R.id.search_bt);
        name_bt = findViewById(R.id.name_bt);
        listView = findViewById(R.id.list_view);
        listViewNewDevices = findViewById(R.id.list_view_newDevices);
        bt_scan = findViewById(R.id.bt_scan);
        btn_send = findViewById(R.id.btn_send);
        et_msg = findViewById(R.id.et_msg);
        tv_status = findViewById(R.id.tv_status);
        layout_msg_et = findViewById(R.id.layout_msg_et);
        toolbar = findViewById(R.id.toolbar);
        btn_newBt = findViewById(R.id.btn_newBt);
        layout_lists = findViewById(R.id.layout_lists);
        recyclerMsg = findViewById(R.id.recyclerMsg);


        setSupportActionBar(toolbar);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (bluetoothAdapter == null) {
            Toast.makeText(this, getResources().getString(R.string.bluetooth_not_supported), Toast.LENGTH_LONG).show();
            finish();
        } else {
            name_bt.setText(getLocalBluetoothName());

            if (bluetoothAdapter.isEnabled()) {
                enable_bt.setChecked(true);
            }

            // Start Discovery
            newDevicesAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, listStringNewDevices);
            listViewNewDevices.setAdapter(newDevicesAdapter);

            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(myReceiver, intentFilter);

            //  Enable
            enable_bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                            if (!isChecked) {

                                bluetoothAdapter.disable();
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.turned_off), Toast.LENGTH_LONG).show();
                            } else {
                                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(intent, REQUEST_INTENT_ENABLE_BT);
                            }
                        } else {
                            enable_bt.setChecked(false);
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_CODE_BT_CONNECT);
                        }
                    } else {
                        if (!isChecked) {

                            bluetoothAdapter.disable();
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.turned_off), Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(intent, REQUEST_INTENT_ENABLE_BT);
                        }
                    }
                }
            });

            //  Visible
            visible_bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
                            if (isChecked) {
                                Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                                startActivityForResult(getVisible, REQUEST_INTENT_DISCOVERABLE);

                            }
                        } else {
                            visible_bt.setChecked(false);
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, REQUEST_PERMISSION_CODE_BT_ADVERTISE);
                        }
                    } else {
                        if (isChecked) {
                            Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                            startActivityForResult(getVisible, REQUEST_INTENT_DISCOVERABLE);
                        }
                    }
                }
            });

            //  List
            search_bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list();
                }
            });

            //  Scan
            bt_scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ServerClass serverClass = new ServerClass();
                    serverClass.start();

                }
            });

            //  listView Click
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ClientClass clientClass = new ClientClass(listPairedDevices.get(position));
                    clientClass.start();
                    tv_status.setText("Connecting");
                }
            });

            // listView New Devices
            listViewNewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ClientClass clientClass = new ClientClass(listNewDevices.get(position));
                    clientClass.start();
                    tv_status.setText("Connecting");
                }
            });

            // send
            btn_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String string = et_msg.getText().toString();
                    if (string.trim().isEmpty()){
                        return;
                    }
                    com.example.bluetoothapp.Models.Message message = new com.example.bluetoothapp.Models.Message();
                    message.setMessage(string);
                    message.setCreatedAt(new Date());
                    message.setSender("me");
                    messageId += 1;
                    message.setId(messageId);
                    messageAdapter.addMessage(message);

                    sendReceive.write(string.getBytes());
                }
            });

            // new Bt
            btn_newBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_PERMISSION_CODE_BT_CONNECT);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                            return;

                    }
                    bluetoothAdapter.startDiscovery();
                }
            });

            populateRecycler();

        }


    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what) {
                case STATE_LISTENING:
                    tv_status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    tv_status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    tv_status.setText("Connected");
                    layout_lists.setVisibility(View.GONE);
                    layout_msg_et.setVisibility(View.VISIBLE);
                    populateRecycler();
                    break;
                case STATE_CONNECTION_FAILED:
                    tv_status.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuffer = (byte[]) msg.obj;
                    String tempMsg = new String(readBuffer, 0, msg.arg1);
                    com.example.bluetoothapp.Models.Message message = new com.example.bluetoothapp.Models.Message();
                    message.setMessage(tempMsg);
                    message.setCreatedAt(new Date());
                    message.setSender("sender");
                    messageId += 1;
                    message.setId(messageId);
                    messageAdapter.addMessage(message);

                    break;
            }

            return false;
        }
    });


    private void list() {
        listView.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {

                pairedDevices = bluetoothAdapter.getBondedDevices();

                ArrayList<String> list = new ArrayList();

                for (BluetoothDevice bt : pairedDevices) {
                    list.add(bt.getName());
                    listPairedDevices.add(bt);
                }

                Toast.makeText(this, getResources().getString(R.string.showing_devices), Toast.LENGTH_SHORT).show();
                ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, list);
                listView.setAdapter(adapter);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_CODE_BT_CONNECT);
            }
        } else {
            pairedDevices = bluetoothAdapter.getBondedDevices();

            ArrayList<String> list = new ArrayList();

            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName());
                listPairedDevices.add(bt);
            }

            Toast.makeText(this, getResources().getString(R.string.showing_devices), Toast.LENGTH_SHORT).show();
            ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, list);
            listView.setAdapter(adapter);
        }
    }


    public String getLocalBluetoothName() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {

                String name = bluetoothAdapter.getName();
                if (name == null) {
                    name = bluetoothAdapter.getAddress();
                }

                return name;
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_CODE_BT_CONNECT);
                return "name";
            }
        } else {

            String name = bluetoothAdapter.getName();
            if (name == null) {
                name = bluetoothAdapter.getAddress();
            }

            return name;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INTENT_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.turned_on), Toast.LENGTH_LONG).show();
            } else {
                enable_bt.setChecked(false);
            }
        } else if (requestCode == REQUEST_INTENT_DISCOVERABLE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.visible_for_min), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION_CODE_BT_CONNECT) {
            getLocalBluetoothName();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        ServerClass() {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_CODE_BT_CONNECT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return;
            }
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            BluetoothSocket socket = null;

            while (socket == null){
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();

                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if (socket != null){

                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    // write some code for send / receive
                    sendReceive = new SendReceive(socket);
                    sendReceive.start();

                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread{
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device1){
            device = device1;

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_CODE_BT_CONNECT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return;
            }
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_CODE_BT_CONNECT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return;
            }
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                sendReceive.start();


            }catch (IOException e){
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private void populateRecycler(){

        recyclerMsg.setHasFixedSize(false);
        recyclerMsg.setLayoutManager(new GridLayoutManager(MainActivity.this, 1));
        messageAdapter = new MessageAdapter(MainActivity.this, messageList);
        recyclerMsg.setAdapter(messageAdapter);
    }

    private class SendReceive extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;


        private SendReceive(BluetoothSocket bluetoothSocket) {
            this.bluetoothSocket = bluetoothSocket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn =  bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            inputStream = tempIn;
            outputStream = tempOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true){
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}