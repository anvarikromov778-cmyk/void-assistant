#!/bin/bash
# Скрипт настройки проекта

echo "========================================"
echo "   VOID ASSISTANT PROJECT SETUP"
echo "========================================"

# Создание необходимых файлов
echo "[*] Создание структуры проекта..."

# Создание иконок (заглушки)
mkdir -p app/src/main/res/{drawable,drawable-hdpi,drawable-mdpi,drawable-xhdpi,drawable-xxhdpi,drawable-xxxhdpi}

# Создание файлов иконок
echo "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAAZdEVYdFNvZnR3YXJlAHBhaW50Lm5ldCA0LjAuMTM0A1t6AAABM0lEQVQ4T5WTzU7CQBSF70C3fAePv8GqyQuwaIEQEEQ0BCVElKooQkLBCJIgPio2IV0QYsCGbggsuEtiY7DxlN5i5wISw84kk3vnnPvNOJ1O+jNs/lXrYLfbL4JQ8DiO44DT6bwly0EQH4i/LY0eGJqKXruJSrEA4fEJwvU19I6K4UCzBGZAVWWI5SKe02kIqRSy8ThS4TCynIAv9RMGmi7Q8qna/YZKIX8QlPLPSNzeQK7XoTQaGI9GsNjv91vKJeEYEVMAWcpi0u9jOhphYRhWjgaTyYQ8CdZBOpGA8vICrdUyffnWLIErCQ5fZ9g+vcBuNGJ2eXkIfEnjz5VmCdKxGHrNJrRvA/a6Tew1G9h13rDrNFn7jW2rjo1axUrIQ0zeIyQIyMZi4InUfv8HHCWXmKd69m8AAAAASUVORK5CYII=" \
  | base64 -d > app/src/main/res/drawable/ic_mic.png

echo "[*] Установка Gradle Wrapper..."
gradle wrapper --gradle-version 8.1 --distribution-type all

echo "[*] Настройка разрешений..."
chmod +x gradlew

echo "[+] Настройка завершена!"
echo ""
echo "Следующие шаги:"
echo "1. Запустите сборку: ./gradlew build"
echo "2. Импортируйте проект в Android Studio"
echo "3. Настройте подпись APK для release сборки"
