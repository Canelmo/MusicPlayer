package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    Button playBtn;
    //    Button next;
    SeekBar positionBar;
    SeekBar volumeBar;
    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;
    MediaPlayer mp;
    //    private Button start = null;
    private Button button = null;
    //    private Button stop = null;
//    private EditText editText = null;
//    private Connection mConnect = null;
    boolean f = true;
    static int m = 0;
    private static String host = null;
    int totalTime;
    private static String name = "Яна";
    private static DataOutputStream output = null;
    public static Socket socket = null;
    public static float loudness;

    static boolean pushed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        start = (Button) findViewById(R.id.start);
        button = (Button) findViewById(R.id.button);
//        stop = (Button) findViewById(R.id.stop);
//        editText = (EditText) findViewById(R.id.editText);

//        start.setEnabled(true);
//        stop.setEnabled(false);
        ((Button) findViewById(R.id.buttonStop)).setEnabled(false);
//        playBtn = (Button) findViewById(R.id.playBtn);
        elapsedTimeLabel = (TextView) findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = (TextView) findViewById(R.id.remainingTimeLabel);

        // Media Player
        mp = MediaPlayer.create(this, R.raw.yana);

        mp.setLooping(true);
        mp.seekTo(0);
        mp.setVolume(0.5f, 0.5f);
        totalTime = mp.getDuration();

        // Position Bar
        positionBar = (SeekBar) findViewById(R.id.positionBar);
        positionBar.setMax(totalTime);
        positionBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mp.seekTo(progress);
                            positionBar.setProgress(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
        // Volume Bar
//        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
//        volumeBar.setOnSeekBarChangeListener(
//                new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        float volumeNum = progress / 100f;
//                        loudness = volumeNum;
//                        mp.setVolume(volumeNum, volumeNum);
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//
//                    }
//                }
//        );

        // Thread (Update positionBar & timeLabel)
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mp != null) {
                    try {
                        Message msg = new Message();
                        msg.what = mp.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update positionBar.
            positionBar.setProgress(currentPosition);

            // Update Labels.
            String elapsedTime = createTimeLabel(currentPosition);
            elapsedTimeLabel.setText(elapsedTime);

            String remainingTime = createTimeLabel(totalTime - currentPosition);
            remainingTimeLabel.setText("- " + remainingTime);
        }
    };

    public String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }

//    public void playBtnClick(View view) {
//
//        if (!mp.isPlaying()) {
//            // Stopping
//            mp.start();
//            sendMessage(name + ":" + ConnectionUtil.CLIENT_START);
//            button.setBackgroundResource(R.drawable.button);
//            button.setBackgroundResource(R.drawable.button_pressed);
//
//        } else {
//            // Playing
//            mp.pause();
//            sendMessage(name + ":" + ConnectionUtil.CLIENT_STOP);
//            playBtn.setBackgroundResource(R.drawable.play);
//            playBtn.setBackgroundResource(R.drawable.stop);
//        }
//
//    }

    //Pressed Button
    public void onSendClick(View view) {
                if (pushed) {
                    sendMessage(name + ":" + ConnectionUtil.CLIENT_STOP);
                    mp.pause();
                    button.setBackgroundResource(R.drawable.button);
                    pushed = false;

                } else {
                    sendMessage(name + ":" + ConnectionUtil.CLIENT_START);
                    mp.start();
                    button.setBackgroundResource(R.drawable.button_pressed);
                    pushed = true;
                }

            }

//    Next Button
    public void nextClick(View view) {
        if (mp.isPlaying()) {
            sendMessage(name + ":" + ConnectionUtil.CLIENT_STOP);
            mp.pause();
            button.setBackgroundResource(R.drawable.button);
            pushed = false;
        }
        if (m == 0) {
            mp = MediaPlayer.create(this, R.raw.yana2);
            m++;
        } else {
            mp = MediaPlayer.create(this, R.raw.yana);
            m--;
        }
        mp.setLooping(true);
        mp.seekTo(0);
        totalTime = mp.getDuration();
        positionBar.setMax(totalTime);
        sendMessage(name + ":" + ConnectionUtil.CLIENT_START);
        mp.start();
        button.setBackgroundResource(R.drawable.button_pressed);
        pushed = true;
    }

    private void sendMessage(final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (output != null) {
                    try {
                        output.writeUTF(msg);
                        output.flush();
                    } catch (IOException e) {
                        System.out.println("Send message failed");
                        closeSocket();
                    }
                }
            }
        }).start();
    }

    private void closeSocket() {
        try {
            if (output != null) {
                output.close();
                output = null;
            }
            if (socket != null) {
                socket.close();
                output = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onCloseClick(View view) {
        ((Button) findViewById(R.id.buttonStart)).setEnabled(true);
        ((Button) findViewById(R.id.buttonStop)).setEnabled(false);
        sendMessage(name + ":" + ConnectionUtil.CLIENT_EXIT);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                } catch (IOException ex) {
//                    closeSocket();
//                }
//            }
//        }).start();
        //closeSocket();
    }

    public void onOpenClick(View v) {
        EditText editText = (EditText) findViewById(R.id.editText);
        host = editText.getText().toString();
        ((Button) findViewById(R.id.buttonStart)).setEnabled(false);
        ((Button) findViewById(R.id.buttonStop)).setEnabled(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(host, ConnectionUtil.port);
                    output = new DataOutputStream(socket.getOutputStream());
                    sendMessage(name + ":" + ConnectionUtil.CLIENT_ALIVE);
                } catch (IOException ex) {
                    closeSocket();
                }
            }
        }).start();
    }
}
