package ca.lakeeffect.scoutingapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity{

    //TODO: Redo text sizes

    List<Counter> counters = new ArrayList<>();
    List<CheckBox> checkboxes = new ArrayList<>();
    List<RadioGroup> radiogroups = new ArrayList<>();
    List<Button> buttons = new ArrayList<>();
    List<SeekBar> seekbars = new ArrayList<>();

//    Button submit;

    TextView timer;
    TextView robotNumText; //robotnum and round

    int robotNum = 2708;
    int round = 0;

    static long start;

    InputPagerAdapter pagerAdapter;
    ViewPager viewPager;

    BluetoothSocket bluetoothsocket;
    OutputStream out;
    InputStream in;
    ArrayList<String> pendingmessages = new ArrayList<>();
    boolean connected;

    Button moreOptions;

    Thread bluetoothConnectionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs1 = getSharedPreferences("theme", MODE_PRIVATE);
        switch(prefs1.getInt("theme", 0)){
            case 0:
                setTheme(R.style.AppTheme);
                break;
            case 1:
                setTheme(R.style.AppThemeLight);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alert();
        //add all buttons and counters etc.

        SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
        for(int i=0;i<prefs.getInt("messageAmount",0);i++){
            if(prefs.getString("message"+i,null) == null){
                SharedPreferences.Editor editor = prefs.edit();
                for(int s=i;s<prefs.getInt("messageAmount",0)-1;s++) {
                    editor.putString("message" + s, prefs.getString("message" + (s+1), ""));
                }
                editor.putInt("messageAmount", prefs.getInt("messageAmount",0)-1);
                editor.commit();
            }else {
                pendingmessages.add(prefs.getString("message" + i, ""));
            }
        }


        Button moreOptions = (Button) findViewById(R.id.moreOptions);
                        moreOptions.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PopupMenu menu = new PopupMenu(MainActivity.this, v, Gravity.CENTER_HORIZONTAL);
                                menu.getMenuInflater().inflate(R.menu.more_options, menu.getMenu());
                                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    public boolean onMenuItemClick(MenuItem item) {
                                        if(item.getItemId() == R.id.reset){
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("Confirm")
                                                    .setMessage("Continuing will reset current data.")
                                                    .setPositiveButton("Continue", new DialogInterface.OnClickListener(){
                                                        public void onClick(DialogInterface dialog, int which){
                                                            reset();

                                                        }
                                                    })
                                                    .setNegativeButton("Cancel", null)
                                                    .create()
                                                    .show();
                                        }
                                        if(item.getItemId() == R.id.changeNum){
                                            alert();
                                        }

                                        if(item.getItemId() == R.id.changeTheme) {
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("Confirm")
                                                    .setMessage("Continuing will reset current data.")
                                                    .setPositiveButton("Continue", new DialogInterface.OnClickListener(){
                                                        public void onClick(DialogInterface dialog, int which){
                                                            Intent intent = new Intent(MainActivity.this, StartActivity.class);
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .setNegativeButton("Cancel", null)
                                                    .create()
                                                    .show();
                                        }
                        Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                menu.show();
            }});

//        counters.add((Counter) findViewById(R.id.goalsCounter));

        //setup scrolling viewpager
        viewPager = (ViewPager) findViewById(R.id.scrollingview);
        pagerAdapter = new InputPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);


//        NumberPicker np = (NumberPicker) findViewm counters
//        np.setWrapSelectorWheel(false);ById(R.id.numberPicker);
//
//        np.setMinValue(0);
//        np.setMaxValue(20);    //maybe switch fro
//        np.setValue(0);

        //add onClickListeners

//        checkboxes.add((CheckBox) findViewById(R.id.scaleCheckBox));

//        submit = (Button) findViewById(R.id.submitButton);

//        timer = (TextView) findViewById(R.id.timer);
        robotNumText = (TextView) findViewById(R.id.robotNum);

        robotNumText.setText("Round: " + round + "  Robot: " + robotNum);

//        submit.setOnClickListener(this);

        //bluetooth stuff
//        setupBluetoothConnections();
//        registerBluetoothListeners();


        start = System.nanoTime();
    }

    public void setupBluetoothConnections(String address){
        final BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
//        Set<BluetoothDevice> pairedDevices = ba.getBondedDevices();
//        final BluetoothDevice[] devices = pairedDevices.toArray(new BluetoothDevice[0]);
        try {
//            int which = -1;
//            for(int i=0;i<devices.length;i++){
//                if(devices[i].getName().equals("2708 Server")){
//                    which = i;
//                    break;
//                }
//            }

            int which = 1;
            if(which != -1){
                System.out.println("Starting rfcomm ");


//                bluetoothsocket = devices[which].createRfcommSocketToServiceRecord(UUID.fromString("6ba6afdc-6a0a-4b1d-a2bf-f71ac108b636"));
                bluetoothsocket = ba.getRemoteDevice(address).createRfcommSocketToServiceRecord(UUID.fromString("6ba6afdc-6a0a-4b1d-a2bf-f71ac108b636"));


                bluetoothConnectionThread = new Thread(){
                    public void run(){
                        try {
                            Log.d("Uh Oh", "CONNECTINGJADLKJASDKLJ");
                            try {
                                System.out.println("Starting Connection ");
                                bluetoothsocket.connect();
                                System.out.println("Ended onnection");

                            }catch(IOException e){
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                                this.run();
                                return;
                            }
                            out = bluetoothsocket.getOutputStream();
                            in = bluetoothsocket.getInputStream();
                            connected = true;
                            Log.d("Uh Oh", "CONNECTED");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((TextView) ((RelativeLayout) findViewById(R.id.statusLayout)).findViewById(R.id.status)).setText("CONNECTED");
                                    ((TextView) ((RelativeLayout) findViewById(R.id.statusLayout)).findViewById(R.id.status)).setTextColor(Color.argb(255,0,255,0));
                                    Toast.makeText(MainActivity.this, "connected!",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            try {
                                Thread.sleep(2400);   //TODO DELET THIS IF NOT NESSECARY
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Log.d("Starting", "Starting pending messages..."+pendingmessages.size());
                            while(pendingmessages.size()>0){
                                for(String message: pendingmessages){
                                    Log.d("Starting", "Startingasasas pending messages...");
                                    out.write(message.getBytes(Charset.forName("UTF-8")));
                                    byte[] bytes = new byte[1000];
                                    int amount = in.read(bytes);
                                    Log.d("Starting", "Passed in read");
                                    if(amount>0)  bytes = Arrays.copyOfRange(bytes, 0, amount);//puts data into bytes and cuts bytes
                                    else break;
                                    if(new String(bytes, Charset.forName("UTF-8")).equals("done")){
                                        pendingmessages.remove(message);
                                        int loc = getLocationInSharedMessages(message);
                                        if(loc != -1){
                                            SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("message"+loc, null);
                                            editor.apply();
                                        }
                                        break;
                                    }
                                }//TODO TEST IF THIS WORKS
                                Log.d("Uh Oh", "Uh oh sadjkhasdkjhasdkjhsadkadshkjsad");
                                if(!connected) break;
                            }

                            //                    while(bluetoothsocket.isConnected(){
                            //                        Log.d("SDsddsdssd","fasdfdfdfsd)fsdfsdfsddfsfdsfd");
                            //                        try {
                            //                            Thread.sleep(200);
                            //                        } catch (InterruptedException e) {
                            //                            e.printStackTrace();
                            //                        }
                            //                    }
                            //                    runOnUiThread(new Runnable() {
                            //                        @Override
                            //                        public void run() {
                            //                            ((TextView) findViewById(R.id.status)).setText("DISCONNECTED");
                            //                            ((TextView) findViewById(R.id.status)).setTextColor(Color.argb(255,255,0,0));
                            //                        }
                            //                    });
                            //                    run();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                bluetoothConnectionThread.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void registerBluetoothListeners(){
        BroadcastReceiver bState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("SDAsadsadsadsad","iouweroiurweoiurewoirweuoiweru");
                String action = intent.getAction();
                switch (action){
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        connected = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) ((RelativeLayout) findViewById(R.id.statusLayout)).findViewById(R.id.status)).setText("DISCONNECTED");
                                ((TextView) ((RelativeLayout) findViewById(R.id.statusLayout)).findViewById(R.id.status)).setTextColor(Color.argb(255,255,0,0));
                            }
                        });
//                        if(bluetoothConnectionThread == null) setupBluetoothConnections();
                        Thread thread1 = new Thread(bluetoothConnectionThread);
                        thread1.start();
                        break;
//                    case BluetoothDevice.ACTION_ACL_CONNECTED:
//                        try {
//                            out = bluetoothsocket.getOutputStream();
//                            in = bluetoothsocket.getInputStream();
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    ((TextView) findViewById(R.id.status)).setText("CONNECTED");
//                                    ((TextView) findViewById(R.id.status)).setTextColor(Color.argb(255,0,255,0));
//                                    Toast.makeText(MainActivity.this, "connected!",
//                                            Toast.LENGTH_LONG).show();
//                                }
//                            });
//                            while(!pendingmessages.isEmpty()){
//                                for(String message: pendingmessages){
//
//                                    byte[] bytes = new byte[1000000];
//                                    int amount = in.read(bytes);
//                                    if(amount>0)  bytes = Arrays.copyOfRange(bytes, 0, amount);//puts data into bytes and cuts bytes
//                                    else continue;
//                                    if(new String(bytes, Charset.forName("UTF-8")).equals("done")){
//                                        pendingmessages.remove(message);
//                                        break;
//                                    }
//                                }//TODO TEST IF THIS WORKS
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        break;
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bState,filter);
    }

    public void saveData(){
        //TODO: make radio button output legible
//        PercentRelativeLayout layout = (PercentRelativeLayout) viewPager.findViewWithTag("page1");
//        for(int i=0;i<layout.getChildCount();i++){
//            if(layout.getChildAt(i) instanceof CheckBox) {
//                try {
//                    out.write(Byte.valueOf("," + String.valueOf( ( (CheckBox) layout.getChildAt(i)).isChecked())));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        return;

        File sdCard = Environment.getExternalStorageDirectory();
//        File dir = new File (sdCard.getPath() + "/ScoutingData/");

        File file = new File(sdCard.getPath() + "/ScoutingData/" + robotNum + ".txt");

        try {
            file.getParentFile().mkdirs();
            if(!file.exists()) {
                file.createNewFile();
            }


            FileOutputStream f = new FileOutputStream(file, true);

            OutputStreamWriter out = new OutputStreamWriter(f);

            final StringBuilder data = new StringBuilder();

            //The Labels
//            data.append("Date and Time Of Match, Round, ");

            DateFormat dateFormat = new SimpleDateFormat("dd HH mm ss");
            Date date = new Date();

            data.append("\n" + "start " + round + " " + dateFormat.format(date) + "\n");



            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TableLayout layout = (TableLayout) pagerAdapter.autoPage.getView().findViewById(R.id.autopagetablelayout);
    //            PercentRelativeLayout layout = (PercentRelativeLayout) findViewById(R.layout.autopage);
            data.append("auto");
            for(int i=0;i<layout.getChildCount();i++){
                for(int s = 0; s<((TableRow) layout.getChildAt(i)).getChildCount(); s++) {
                    if (((TableRow) layout.getChildAt(i)).getChildAt(s) instanceof RadioGroup) {
                        int pressed = -1;
                        for(int r=0;r<((RadioGroup) ((TableRow) layout.getChildAt(i)).getChildAt(s)).getChildCount();r++){
                            if(((RadioButton) ((RadioGroup) ((TableRow) layout.getChildAt(i)).getChildAt(s)).getChildAt(r)).isChecked()){
                                pressed = 1-r;
                            }
                        }
                        data.append("," + pressed);

                    }
                    else if (((TableRow) layout.getChildAt(i)).getChildAt(s) instanceof Counter) {
                        data.append("," + String.valueOf(((Counter) ((TableRow) layout.getChildAt(i)).getChildAt(s)).count));
                    }
                }
            }

            layout = (TableLayout) pagerAdapter.teleopPage.getView().findViewById(R.id.teleoptablelayout);
            data.append("\nteleop");
            for(int i=0;i<layout.getChildCount();i++){
                for(int s = 0; s<((TableRow) layout.getChildAt(i)).getChildCount(); s++) {
                    if (((TableRow) layout.getChildAt(i)).getChildAt(s) instanceof Counter) {
                        data.append("," + String.valueOf(((Counter) ((TableRow) layout.getChildAt(i)).getChildAt(s)).count));
                    }
                    if (((TableRow) layout.getChildAt(i)).getChildAt(s) instanceof HigherCounter) {
                        data.append("," + String.valueOf(((HigherCounter) ((TableRow) layout.getChildAt(i)).getChildAt(s)).count));
                    }
                }
            }
            DisplayMetrics m = getResources().getDisplayMetrics();
            PercentRelativeLayout v = null;
            if(m.widthPixels/m.density >= 600) v = ((PercentRelativeLayout) ((ScrollView) pagerAdapter.endgamePage.getView()).getChildAt(0));
            else v = ((PercentRelativeLayout) pagerAdapter.endgamePage.getView());
            data.append("\nendgame");
            for(int i=0; i<v.getChildCount(); i++){
                if(v.getChildAt(i) instanceof RadioGroup){
                    int pressed = -1;
                    for(int r=0;r<((RadioGroup) v.getChildAt(i)).getChildCount();r++){
                        if(((RadioButton) ((RadioGroup) v.getChildAt(i)).getChildAt(r)).isChecked()){
                            pressed = r;
                        }
                    }
                    data.append("," + pressed);
                }
                if(v.getChildAt(i) instanceof EditText){
                    data.append("," + ((EditText) v.getChildAt(i)).getText().toString().replace(",", "."));
                }
            }


            data.append("\nend");//make sure full message has been sent

            out.append(data.toString());
            out.close();

            f.close();


            Thread thread = new Thread(){
                public void run(){
                    while(true) {
                        System.out.println("aaaa");
                        byte[] bytes = new byte[1000];
                        try {
                            if(!connected){
                                pendingmessages.add(robotNum + ":" + data.toString());
                                SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("message"+prefs.getInt("messageAmount",0), robotNum + ":" + data.toString());
                                editor.putInt("messageAmount", prefs.getInt("messageAmount",0)+1);
                                editor.apply();
                                return;
                            }
                            int amount = in.read(bytes);
                            if (new String(bytes, Charset.forName("UTF-8")).equals("done")) {
                                return;
                            }
                            if(!connected){
                                pendingmessages.add(robotNum + ":" + data.toString());
                                SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("message"+prefs.getInt("messageAmount",0), robotNum + ":" + data.toString());
                                editor.putInt("messageAmount", prefs.getInt("messageAmount",0)+1);
                                editor.apply();
                                return;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            if(bluetoothsocket != null && bluetoothsocket.isConnected()){
                System.out.println("aaaa");
                System.out.println("aaaa");
                System.out.println("aaaa");
                System.out.println("aaaa");
                System.out.println("aaaa");
                System.out.println("aaaa");
                System.out.println("aaaa");
                this.out.write((robotNum + ":" + data.toString()).getBytes(Charset.forName("UTF-8")));
                thread.start();
            }else{
                pendingmessages.add(robotNum + ":" + data.toString());
                SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("message"+prefs.getInt("messageAmount",0), robotNum + ":" + data.toString());
                editor.putInt("messageAmount", prefs.getInt("messageAmount",0)+1);
                editor.apply();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int getLocationInSharedMessages(String message){
        SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
        for(int i=0;i<prefs.getInt("messageAmount",0);i++) {
            if(prefs.getString("message" + i, "").equals(message)){
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                    Toast.makeText(MainActivity.this, "The app has to save items to the external storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed(){
        return;
    }

    public void reset(){
        //setup scrolling viewpager
        alert();

//        viewPager = (ViewPager) findViewById(R.id.scrollingview);
        viewPager.setAdapter(pagerAdapter);
//        viewPager.setOffscreenPageLimit(3);
//        viewPager.getAdapter().notifyDataSetChanged();

        ((RadioGroup) pagerAdapter.autoPage.getView().findViewById(R.id.autoBaselineGroup)).clearCheck();
        ((RadioGroup) pagerAdapter.autoPage.getView().findViewById(R.id.autoGearGroup)).clearCheck();
        ((RadioGroup) pagerAdapter.endgamePage.getView().findViewById(R.id.endgameClimbGroup)).clearCheck();
        ((EditText) pagerAdapter.endgamePage.getView().findViewById(R.id.endgameComments)).setText("");
    }

    public void alert(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(R.layout.dialog)
                .setTitle("Enter Info")
                .setPositiveButton(android.R.string.yes,  null)
                .setCancelable(false)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
             @Override
             public void onShow(final DialogInterface dialog) {
                 ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                         LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog, null);
                         EditText robotNumin = (EditText) ((AlertDialog) dialog).findViewById(R.id.editText);
                         EditText roundin = (EditText) ((AlertDialog) dialog).findViewById(R.id.editText2);
                         try {
                             robotNum = Integer.parseInt(robotNumin.getText().toString());
                             round = Integer.parseInt(roundin.getText().toString());
                         } catch (NumberFormatException e) {
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     Toast.makeText(MainActivity.this, "Invalid Data! Only numbers please",
                                             Toast.LENGTH_LONG).show();
                                 }
                             });
                             return;
                         }
                         robotNumText = (TextView) findViewById(R.id.robotNum);
                         robotNumText.setText("Robot: " + robotNum + " " + "Round: " + round);
                         dialog.dismiss();
                     }
                 });

             }
         });
        dialog.show();
    }


}
