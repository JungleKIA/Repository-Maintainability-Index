# Windows/GitBash Setup Guide for UTF-8 Unicode Support

## Проблема / Problem

При запуске приложения на Windows в GitBash Unicode символы отображаются некорректно:

❌ **Неправильно:**
```
ΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉ
  Repository Maintainability Index Report
ΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉΓòÉ

ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
  Detailed Metrics
ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ

Γû¬ Documentation: 80,00/100
```

✅ **Правильно:**
```
═══════════════════════════════════════════════════════════════
  Repository Maintainability Index Report
═══════════════════════════════════════════════════════════════

───────────────────────────────────────────────────────────────
  Detailed Metrics
───────────────────────────────────────────────────────────────

▪ Documentation: 80.00/100 (weight: 20%)
```

## Решение / Solution

Приложение **автоматически** настраивает UTF-8 кодировку при запуске.

### Автоматическая настройка (встроена в приложение)

Приложение автоматически выполняет следующие шаги:

1. **Установка системных свойств Java**
   - `file.encoding=UTF-8`
   - `sun.jnu.encoding=UTF-8`
   - `console.encoding=UTF-8`

2. **Настройка Windows Console (только на Windows)**
   - Выполняет `chcp 65001` для установки кодовой страницы UTF-8

3. **Перенастройка потоков вывода**
   - Заменяет `System.out` и `System.err` на UTF-8 потоки
   - Включает autoFlush для корректной работы в GitBash

4. **Настройка логирования**
   - Конфигурирует Logback для UTF-8
   - Настраивает java.util.logging для UTF-8

5. **Восстановление поврежденных символов (mojibake)**
   - Автоматически исправляет искаженные символы, если они появились

### Ручная настройка (если нужна)

Если автоматическая настройка не сработала, выполните следующие шаги:

#### 1. Настройте GitBash

Добавьте в `~/.bashrc`:

```bash
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
```

Примените изменения:

```bash
source ~/.bashrc
```

#### 2. Настройте шрифт в GitBash

1. Правый клик на заголовок окна GitBash → **Options**
2. **Text** → **Font**: выберите один из:
   - **Cascadia Code** (рекомендуется)
   - **Consolas**
   - **JetBrains Mono**
3. **Text** → **Locale**: `en_US`
4. **Text** → **Character set**: `UTF-8`
5. **Apply** → **Save**

#### 3. Запустите приложение

```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze prettier/prettier
```

Или с явной установкой кодировки:

```bash
java -Dfile.encoding=UTF-8 -jar target/repo-maintainability-index-1.0.0.jar analyze prettier/prettier
```

## Тестирование / Testing

### Проверка окружения

```bash
# Проверьте локаль
echo $LANG
# Должно быть: en_US.UTF-8 или аналогичное

# Проверьте кодировку терминала
locale charmap
# Должно быть: UTF-8
```

### Тест Unicode символов

Запустите тестовый скрипт:

```bash
./test-unicode.sh
```

Вы должны увидеть корректно отображенный бокс:

```
┌─────────────────────┐
│  Unicode Test Box   │
│  ═══════════════   │
│  ▪ Item 1           │
│  ▪ Item 2           │
└─────────────────────┘
```

### Тест приложения

```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze prettier/prettier
```

Проверьте что символы `═`, `─`, `▪` отображаются корректно.

## Альтернативы / Alternatives

### Вариант 1: Windows Terminal

Windows Terminal имеет лучшую встроенную поддержку UTF-8:

1. Установите из Microsoft Store: **Windows Terminal**
2. Откройте GitBash в Windows Terminal
3. Запустите приложение

### Вариант 2: PowerShell

```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
java -jar target/repo-maintainability-index-1.0.0.jar analyze prettier/prettier
```

### Вариант 3: Command Prompt

```cmd
chcp 65001
java -jar target/repo-maintainability-index-1.0.0.jar analyze prettier/prettier
```

## Техническая документация / Technical Documentation

Для разработчиков, которые хотят понять как работает реализация:

📖 См. [docs/UTF8-ENCODING-IMPLEMENTATION.md](docs/UTF8-ENCODING-IMPLEMENTATION.md)

Этот документ содержит:
- Архитектуру решения (4 слоя защиты)
- Детали реализации каждого слоя
- Алгоритм восстановления mojibake
- Объяснение почему это работает для GitBash
- Рекомендации по тестированию

## Файлы проекта / Project Files

Ключевые файлы, связанные с UTF-8:

- `src/main/java/com/kaicode/rmi/Main.java` - Точка входа, вызывает setupUTF8ConsoleStreams()
- `src/main/java/com/kaicode/rmi/util/EncodingHelper.java` - Основная логика UTF-8
- `src/main/java/com/kaicode/rmi/util/ReportFormatter.java` - Применяет cleanTextForWindows()
- `src/main/java/com/kaicode/rmi/util/LLMReportFormatter.java` - Применяет cleanTextForWindows()
- `src/main/resources/logback.xml` - UTF-8 для логирования
- `src/test/java/com/kaicode/rmi/util/EncodingHelperTest.java` - 34 теста UTF-8 функциональности
- `docs/UTF8-ENCODING-IMPLEMENTATION.md` - Техническая документация
- `test-unicode.sh` - Скрипт для проверки Unicode

## Troubleshooting

### Все еще вижу искаженные символы

**Причина:** Шрифт не поддерживает box-drawing символы

**Решение:** Установите шрифт Cascadia Code или Consolas

### GitBash показывает "?" вместо символов

**Причина:** Локаль не установлена в UTF-8

**Решение:**
```bash
export LANG=en_US.UTF-8
```

### Работает в Linux, не работает в Windows

**Причина:** Windows использует другую кодовую страницу по умолчанию

**Решение:** Приложение автоматически выполняет `chcp 65001`, но вы можете
выполнить это вручную перед запуском

### IntelliJ IDEA показывает искаженные символы

**Решение:**
1. File → Settings → Editor → File Encodings
2. Global Encoding: **UTF-8**
3. Project Encoding: **UTF-8**
4. Default encoding for properties files: **UTF-8**

## Статус / Status

✅ **Реализовано и протестировано**

- [x] Автоматическая настройка UTF-8 при запуске
- [x] Поддержка Windows Command Prompt
- [x] Поддержка Windows PowerShell
- [x] Поддержка GitBash на Windows
- [x] Поддержка Linux terminals
- [x] Поддержка macOS terminals
- [x] Автоматическое восстановление mojibake
- [x] 250 unit тестов (все проходят)
- [x] 90% instruction coverage
- [x] 79% branch coverage
- [x] Документация для пользователей
- [x] Техническая документация для разработчиков

## Контакты / Contacts

Если у вас возникли проблемы с отображением Unicode:

1. Проверьте что локаль установлена в UTF-8: `echo $LANG`
2. Проверьте что шрифт поддерживает box-drawing символы
3. Запустите `test-unicode.sh` для диагностики
4. См. техническую документацию: `docs/UTF8-ENCODING-IMPLEMENTATION.md`

---

**Важно:** Приложение специально использует Unicode символы вместо ASCII для
лучшего визуального представления. Мы НЕ заменяем их на ASCII (как `===` или `---`),
потому что правильная настройка кодировки обеспечивает корректное отображение
на всех современных терминалах.
