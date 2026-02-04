package com.void.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VoiceService extends Service {
    
    private static final String TAG = "VoiceService";
    private static final String CHANNEL_ID = "voice_service_channel";
    private static final int NOTIFICATION_ID = 101;
    
    private SpeechRecognizer speechRecognizer;
    private AudioManager audioManager;
    private String wakeWord = "джарвис";
    private boolean isListening = false;
    
    // Карта команд
    private Map<String, Runnable> commands = new HashMap<>();
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Создание VoiceService");
        
        // Инициализация AudioManager
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        
        // Инициализация команд
        initCommands();
        
        // Инициализация распознавания речи
        initSpeechRecognizer();
        
        // Создание уведомления для foreground service
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
    }
    
    private void initCommands() {
        commands.put("привет", () -> showToast("Привет!"));
        commands.put("время", this::showTime);
        commands.put("дата", this::showDate);
        commands.put("громче", this::volumeUp);
        commands.put("тише", this::volumeDown);
        commands.put("фото", this::takePhoto);
        commands.put("позвони", () -> showToast("Кому позвонить?"));
        commands.put("сообщение", () -> showToast("Что отправить?"));
        commands.put("где я", this::getLocation);
        commands.put("выключись", this::stopSelf);
        commands.put("помощь", this::showHelp);
    }
    
    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d(TAG, "Готов к распознаванию");
                }
                
                @Override
                public void onBeginningOfSpeech() {
                    Log.d(TAG, "Начало речи");
                }
                
                @Override
                public void onRmsChanged(float rmsdB) {
                    // Уровень громкости
                }
                
                @Override
                public void onBufferReceived(byte[] buffer) {
                }
                
                @Override
                public void onEndOfSpeech() {
                    Log.d(TAG, "Конец речи");
                }
                
                @Override
                public void onError(int error) {
                    Log.e(TAG, "Ошибка распознавания: " + error);
                    // Перезапускаем прослушивание через 2 секунды
                    new Handler(Looper.getMainLooper()).postDelayed(
                        () -> startListening(), 2000);
                }
                
                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                    
                    if (matches != null && !matches.isEmpty()) {
                        String text = matches.get(0).toLowerCase();
                        Log.d(TAG, "Распознано: " + text);
                        
                        // Проверка слова активации
                        if (text.contains(wakeWord)) {
                            processCommand(text);
                        }
                    }
                    
                    // Продолжаем слушать
                    startListening();
                }
                
                @Override
                public void onPartialResults(Bundle partialResults) {
                }
                
                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });
            
            startListening();
        } else {
            Log.e(TAG, "Распознавание речи недоступно");
            stopSelf();
        }
    }
    
    private void startListening() {
        if (speechRecognizer != null && !isListening) {
            Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            
            speechRecognizer.startListening(recognizerIntent);
            isListening = true;
            Log.d(TAG, "Начинаю слушать...");
        }
    }
    
    private void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            Log.d(TAG, "Остановлено прослушивание");
        }
    }
    
    private void processCommand(String text) {
        String command = text.replace(wakeWord, "").trim();
        Log.d(TAG, "Обработка команды: " + command);
        
        // Ищем команду в карте
        for (Map.Entry<String, Runnable> entry : commands.entrySet()) {
            if (command.contains(entry.getKey())) {
                entry.getValue().run();
                return;
            }
        }
        
        // Если команда не найдена
        showToast("Не понял команду. Скажите 'помощь'");
    }
    
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    private void showTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new java.util.Date());
        showToast("Сейчас " + currentTime);
    }
    
    private void showDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String currentDate = sdf.format(new java.util.Date());
        showToast("Сегодня " + currentDate);
    }
    
    private void volumeUp() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        showToast("Громкость увеличена");
    }
    
    private void volumeDown() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        showToast("Громкость уменьшена");
    }
    
    private void takePhoto() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cameraIntent);
        showToast("Запускаю камеру");
    }
    
    private void getLocation() {
        showToast("Определяю местоположение...");
        // Реализация получения локации
    }
    
    private void showHelp() {
        StringBuilder helpText = new StringBuilder("Доступные команды: ");
        for (String cmd : commands.keySet()) {
            helpText.append(cmd).append(", ");
        }
        showToast(helpText.toString());
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Голосовой ассистент",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Служба голосового управления");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Void Assistant")
            .setContentText("Слушаю команды...")
            .setSmallIcon(R.drawable.ic_mic)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Сервис запущен");
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopListening();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        Log.d(TAG, "Сервис остановлен");
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
