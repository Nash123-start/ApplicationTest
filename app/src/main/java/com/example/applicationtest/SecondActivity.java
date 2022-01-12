package com.example.applicationtest;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.microsoft.intune.mam.client.app.MAMComponents;
import com.microsoft.intune.mam.client.identity.MAMPolicyManager;
import com.microsoft.intune.mam.policy.MAMEnrollmentManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class SecondActivity extends AppCompatActivity {
    private MAMEnrollmentManager mEnrollmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);
        mEnrollmentManager = MAMComponents.get(MAMEnrollmentManager.class);
        boolean isOffline = MAMComponents.isAppOffline();
        Log.d("zpb", "isOffline1" + isOffline);
        boolean isManager = MAMComponents.isManagedApp(this);
        Log.d("zpb", "isManager1=" + isManager);
        MAMPolicyManager.getPolicy(this).getNotificationRestriction().getCode();
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                50);
        findViewById(R.id.logoutOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmWritePermission();
                saveFile();
            }
        });

        findViewById(R.id.read).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readFile();
            }
        });

    }


    private void confirmWritePermission() {
        if (PermissionChecker.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    50);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 50) {
            saveFile();
        }
    }

    private void saveFile() {
        Thread saveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (PermissionChecker.checkSelfPermission(SecondActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                    Log.d("zpb", "no permission");
                    return;
                }

                // Now try to write the document to their device
                try {
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "saveFile.txt";
                    File exportFile = new File(filePath);
                    if (!exportFile.exists()) {
                        exportFile.createNewFile();
                    }
                    final PrintWriter writer = new PrintWriter(exportFile);
                    writer.append("Save my data to file");
                    writer.flush();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SecondActivity.this, "read success", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    Log.d("zpb", "io exception =" + (e.getMessage()));
                }

            }
        });
        saveThread.start();
    }


    private void readFile() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (PermissionChecker.checkSelfPermission(SecondActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                    Log.d("zpb", "no permission");
                    return;
                }


                try {
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "saveFile.txt";
                    File exportFile = new File(filePath);
                    if (!exportFile.exists()) {
                        return;
                    }
                    FileInputStream inputStream = new FileInputStream(exportFile);
                    InputStreamReader reader = new InputStreamReader(inputStream);
                    BufferedReader bf = new BufferedReader(reader);
                    final String text = bf.readLine();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SecondActivity.this, "read success text = " + text, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    Log.d("zpb", "io exception =" + (e.getMessage()));
                }
            }
        });
        thread.start();
    }

}
