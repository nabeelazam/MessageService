package com.messageservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //startMessageService();

        // Destroy activity
        finish();
    }

    private void startMessageService() {
        Intent serviceIntent = new Intent(MainActivity.this, MessageService.class);
        startService(serviceIntent);
        finish();
    }

}
