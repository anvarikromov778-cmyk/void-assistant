package com.void.assistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private static final int PERMISSIONS_REQUEST_CODE = 1001;
    
    private Button startButton, stopButton;
    private TextView statusText;
    private Switch stealthSwitch, autoStartSwitch;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Инициализация UI элементов
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        statusText = findViewById(R.id.status_text);
        stealthSwitch = findViewById(R.id.stealth_switch);
        autoStartSwitch = findViewById(R.id.autostart_switch);
        
        // Настройка обработчиков кнопок
        startButton.setOnClickListener(v -> startAssistant());
        stopButton.setOnClickListener(v -> stopAssistant());
        
        // Проверка разрешений
        checkPermissions();
        
        // Проверка запущен ли сервис
        updateServiceStatus();
    }
    
    private void startAssistant() {
        // Запуск голосового сервиса
        Intent serviceIntent = new Intent(this, VoiceService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
        
        // Запуск фонового сервиса
        Intent backgroundIntent = new Intent(this, BackgroundService.class);
        startService(backgroundIntent);
        
        // Обновление UI
        statusText.setText("Статус: АКТИВЕН");
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        
        Toast.makeText(this, "Ассистент запущен", Toast.LENGTH_SHORT).show();
        
        // Если включен скрытный режим - сворачиваем приложение
        if (stealthSwitch.isChecked()) {
            moveTaskToBack(true);
        }
    }
    
    private void stopAssistant() {
        // Остановка сервисов
        stopService(new Intent(this, VoiceService.class));
        stopService(new Intent(this, BackgroundService.class));
        
        // Обновление UI
        statusText.setText("Статус: ВЫКЛЮЧЕН");
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        
        Toast.makeText(this, "Ассистент остановлен", Toast.LENGTH_SHORT).show();
    }
    
    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        
        String[] requiredPermissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }
        
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                permissionsNeeded.toArray(new String[0]),
                PERMISSIONS_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                        "Разрешение необходимо: " + permissions[i],
                        Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    
    private void updateServiceStatus() {
        // Проверка запущен ли VoiceService
        // В реальном приложении используйте ServiceConnection
        // Для простоты предположим, что сервис не запущен
        statusText.setText("Статус: ВЫКЛЮЧЕН");
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }
}
