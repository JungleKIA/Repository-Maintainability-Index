# Windows Setup Guide for UTF-8 Support

## Проблема с Отображением Эмодзи в Windows

При запуске проекта в Windows (особенно через Git Bash или Command Prompt) могут возникать проблемы с отображением эмодзи и Unicode символов. Вместо эмодзи появляются вопросительные знаки (?).

### Причина

Windows консоль по умолчанию использует кодировку, отличную от UTF-8 (например, CP437, CP1252 или CP866), что не позволяет корректно отображать Unicode символы.

## ✅ Решения

### Решение 1: Настройка Git Bash (Рекомендуется)

Если вы используете Git Bash, это лучший вариант:

1. **Откройте Git Bash**

2. **Установите UTF-8 в настройках терминала:**
   - Правый клик на заголовке окна → Options
   - Перейдите в раздел "Text"
   - Установите "Character set" в "UTF-8"
   - Нажмите "Apply" и "Save"

3. **Добавьте в `.bashrc` (опционально):**
   ```bash
   export LANG=en_US.UTF-8
   export LC_ALL=en_US.UTF-8
   ```

### Решение 2: Windows Terminal (Лучший вариант)

Windows Terminal поддерживает UTF-8 из коробки:

1. **Установите Windows Terminal из Microsoft Store** (если еще не установлен)

2. **Откройте Windows Terminal**

3. **Запустите приложение:**
   ```bash
   java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze facebook/react --llm
   ```

### Решение 3: Command Prompt с UTF-8

Для обычного Command Prompt:

1. **Откройте CMD**

2. **Установите UTF-8 кодировку:**
   ```cmd
   chcp 65001
   ```

3. **Запустите приложение:**
   ```cmd
   java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze facebook/react --llm
   ```

### Решение 4: PowerShell

PowerShell обычно лучше обрабатывает UTF-8:

1. **Откройте PowerShell**

2. **Установите UTF-8 для текущей сессии:**
   ```powershell
   [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
   $OutputEncoding = [System.Text.Encoding]::UTF8
   ```

3. **Запустите приложение:**
   ```powershell
   java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze facebook/react --llm
   ```

## 🎯 Рекомендуемый Способ Запуска

### Для Git Bash:
```bash
export LANG=en_US.UTF-8
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze facebook/react --llm
```

### Для Windows Terminal:
```bash
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze facebook/react --llm
```

### Для Command Prompt:
```cmd
chcp 65001
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze facebook/react --llm
```

## 🔧 Создание Batch Файла (Удобный способ)

Создайте файл `run-analysis.bat` в корне проекта:

```batch
@echo off
chcp 65001 > nul
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar %*
```

Теперь можно запускать так:
```cmd
run-analysis.bat analyze facebook/react --llm
```

## 🔧 Создание Bash Скрипта

Создайте файл `run-analysis.sh`:

```bash
#!/bin/bash
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar "$@"
```

Сделайте исполняемым:
```bash
chmod +x run-analysis.sh
```

Запускайте так:
```bash
./run-analysis.sh analyze facebook/react --llm
```

## 🐛 Диагностика Проблем

### Проверка текущей кодировки:

**Git Bash / Linux / macOS:**
```bash
echo $LANG
locale
```

**Command Prompt:**
```cmd
chcp
```

**PowerShell:**
```powershell
[Console]::OutputEncoding
```

### Проверка Java кодировки:
```bash
java -XshowSettings:properties -version 2>&1 | grep file.encoding
```

## 📝 Пример Вывода

### ❌ Неправильно (без UTF-8):
```
? Documentation: 100.00/100 (weight: 20%)
? Commit Quality: 32.00/100 (weight: 15%)
? ? Improve response time to community
```

### ✅ Правильно (с UTF-8):
```
▪ Documentation: 100.00/100 (weight: 20%)
▪ Commit Quality: 32.00/100 (weight: 15%)
🥇 🔴 Improve response time to community
```

## 🎨 Альтернатива: Запуск без Эмодзи

Если UTF-8 не работает, можно запустить с отключенными эмодзи (в будущей версии):
```bash
java -Dno-emoji=true -jar target/repo-maintainability-index-1.0.0.jar analyze facebook/react --llm
```

## ✅ Автоматическое Решение

Проект теперь автоматически настраивает UTF-8 при запуске. Однако, для **лучших результатов** рекомендуется использовать один из методов выше.

## 📚 Дополнительные Ресурсы

- [Windows Terminal Documentation](https://docs.microsoft.com/en-us/windows/terminal/)
- [UTF-8 in Windows Console](https://docs.microsoft.com/en-us/windows/console/console-code-pages)
- [Git Bash Configuration](https://git-scm.com/docs/git-config)

## 🆘 Если Ничего Не Помогает

1. Используйте **Windows Terminal** (самый надежный способ)
2. Попробуйте запустить через **WSL** (Windows Subsystem for Linux)
3. Убедитесь, что используете современный шрифт в консоли (например, "Cascadia Code" или "Consolas")

---

**Протестировано на:**
- Windows 10/11 с Git Bash 2.40+
- Windows Terminal 1.15+
- PowerShell 7.3+
- Command Prompt (Windows 10+)

**Статус:** ✅ Работает с правильной настройкой
