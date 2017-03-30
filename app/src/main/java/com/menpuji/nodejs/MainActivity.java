package com.menpuji.nodejs;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.menpuji.nodejs.ShellUtils.execCommand;

public class MainActivity extends AppCompatActivity {
    private String params = "https://app.menpuji.com base64/eyJzdGFydElkIjoidWN0cW1wbGUzdWZxcmZ0Ym11ZWlzYWN2ZiIsInN0b3JlSWQiOiIzM3cxM29xMmJyNDAwczAwa3N3a3NnczA0IiwiZGV2aWNlIjp7ImlkIjoiMzQ6OTc6RjY6MjQ6REU6OTkiLCJob3N0IjoiMTcyLjE2LjI1NC43NiJ9LCJ0ZXJtaW5hbCI6eyJpZCI6IjM0ODZmYzU1YTY0NTQ0MjA4NzU0ZTYwYTA1ZmMxN2FmIiwibmFtZSI6IlBPUy0xODIifSwibGRjIjpmYWxzZSwib3B0aW9uIjp7fX0=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File ldcDir = getDir("ldc", MODE_PRIVATE);
        File ldcFile = new File(ldcDir, "ldc.zip");
        try {
            InputStream inputStream = getAssets().open("ldc.zip");
            OutputStream outputStream = new FileOutputStream(ldcFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        unzip(ldcFile.getAbsolutePath(), ldcDir.getAbsolutePath());
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_ldc:
                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void run() {
                        String ldcPath = getFilesDir().getParentFile().getAbsolutePath() + "/app_ldc/ldc";
                        System.out.println(new File(ldcPath + "/node").exists());
                        System.out.println(new File(ldcPath + "/start.js").exists());
                        String command = "cd " + ldcPath;
                        String command2 = "chmod -R 755 ./node";
                        String command3 = "./node ./start.js " + params;
                        ShellUtils.CommandResult commandResult = execCommand(new String[]{command, command2, command3}, false);
                        System.out.println(commandResult.result + ":" + commandResult.successMsg + ":" + commandResult.errorMsg);
                    }
                }).start();
                break;
            case R.id.btn_stop_ldc:
                if (ShellUtils.currentProcess != null) {
                    ShellUtils.currentProcess.destroy();
                }
                break;
            default:
                break;
        }
    }

    public static void unzip(String warPath, String unzipPath) {
        File warFile = new File(warPath);
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(warFile));
            ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.JAR, bufferedInputStream);
            JarArchiveEntry entry;
            while ((entry = (JarArchiveEntry) in.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(unzipPath, entry.getName()).mkdir();
                } else {
                    OutputStream out = FileUtils.openOutputStream(new File(unzipPath, entry.getName()));
                    IOUtils.copy(in, out);
                    out.close();
                }
            }
            in.close();
        } catch (ArchiveException | IOException e) {
            e.printStackTrace();
        }
    }
}
