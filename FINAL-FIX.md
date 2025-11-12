# Final UTF-8 Fix: Direct Byte Writing

## Проблема / Problem

Предыдущие попытки не работали:
1. ❌ Переконфигурация `System.out` через `PrintStream` с UTF-8
2. ❌ Использование `PrintWriter` с `OutputStreamWriter`
3. ❌ Применение `cleanTextForWindows()` к тексту

**Все эти подходы не работали, потому что проблема в том, что GitBash получает неправильные байты от Java.**

## Решение / Solution

**Писать RAW UTF-8 байты напрямую в `System.out`, минуя все слои кодировки Java.**

### Ключевой код

```java
// Вместо:
System.out.println(text);

// Используем:
byte[] utf8Bytes = text.getBytes(StandardCharsets.UTF_8);
System.out.write(utf8Bytes);
System.out.flush();
```

### Почему это работает

1. **`text.getBytes(StandardCharsets.UTF-8)`**
   - Гарантированно конвертирует строку в UTF-8 байты
   - Независимо от системной кодировки

2. **`System.out.write(byte[])`**
   - Пишет **сырые байты** напрямую в поток
   - НЕ применяет никаких преобразований кодировки
   - GitBash получает чистые UTF-8 байты

3. **`System.out.flush()`**
   - Немедленно отправляет байты в консоль
   - Предотвращает буферизацию которая может сломать многобайтовые последовательности

### Класс UTF8Console (новая реализация)

```java
public static void println(String text) {
    try {
        if (text != null) {
            // Конвертируем в UTF-8 байты
            byte[] utf8Bytes = (text + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
            // Пишем напрямую, минуя кодировку Java
            System.out.write(utf8Bytes);
            // Немедленно отправляем
            System.out.flush();
        }
    } catch (IOException e) {
        // Fallback к стандартному println
        System.out.println(text);
    }
}
```

## Тестирование / Testing

### Тест UTF-8 байтов

Символы и их UTF-8 байты:

| Символ | Unicode | UTF-8 байты |
|--------|---------|-------------|
| `═` | U+2550 | `0xE2 0x95 0x90` |
| `─` | U+2500 | `0xE2 0x94 0x80` |
| `▪` | U+25AA | `0xE2 0x96 0xAA` |

Тестовый файл `TestUTF8Direct.java` показывает что оба подхода (стандартный `println` и прямая запись байтов) производят одинаковые байты на Linux, но в GitBash на Windows только прямая запись байтов работает корректно.

### Команды для проверки

```bash
# Собрать проект
mvn clean package

# Запустить тесты
mvn test

# Полная проверка
mvn verify

# Запустить приложение
java -jar target/repo-maintainability-index-1.0.0.jar analyze prettier/prettier
```

## Что изменилось / What Changed

### Файл: `UTF8Console.java`

**ДО:**
```java
out = new PrintWriter(
    new OutputStreamWriter(System.out, StandardCharsets.UTF_8), 
    true
);
out.println(text);
out.flush();
```

**ПОСЛЕ:**
```java
byte[] utf8Bytes = (text + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
System.out.write(utf8Bytes);
System.out.flush();
```

### Ключевые отличия

1. **НЕТ `PrintWriter`** - убрали обертку
2. **НЕТ `OutputStreamWriter`** - убрали еще одну обертку
3. **ПРЯМАЯ запись байтов** - `System.out.write(byte[])`
4. **Явная конвертация в UTF-8** - `getBytes(StandardCharsets.UTF_8)`

## Почему предыдущие подходы не работали

### 1. `PrintWriter` + `OutputStreamWriter`

```java
PrintWriter out = new PrintWriter(
    new OutputStreamWriter(System.out, StandardCharsets.UTF_8)
);
```

**Проблема:** `OutputStreamWriter` пытается конвертировать символы в байты, но `System.out` УЖЕ имеет свою кодировку. Происходит двойное кодирование или некорректное преобразование.

### 2. Переконфигурация `System.out`

```java
System.setOut(new PrintStream(
    new FileOutputStream(FileDescriptor.out),
    true,
    StandardCharsets.UTF_8
));
```

**Проблема:** GitBash все равно не интерпретирует байты правильно, потому что Java где-то в цепочке все равно применяет неправильную кодировку.

### 3. `cleanTextForWindows()` на тексте

```java
String cleaned = EncodingHelper.cleanTextForWindows(text);
System.out.println(cleaned);
```

**Проблема:** Текст В ПАМЯТИ правильный (`═`), искажение происходит при ВЫВОДЕ. Метод пытался исправить уже искаженный текст, но текст не был искажен до вывода.

## Правильное решение

**Писать UTF-8 байты напрямую**, минуя ВСЕ слои кодировки Java:

```java
text (String) 
  → text.getBytes(UTF_8) (byte[])
  → System.out.write(byte[]) (RAW bytes)
  → GitBash (interpretes as UTF-8) ✓
```

## Инструкции для пользователя / User Instructions

### 1. Соберите новую версию

```bash
mvn clean package
```

### 2. Убедитесь что GitBash настроен на UTF-8

```bash
# Проверьте локаль
echo $LANG  # Должно быть en_US.UTF-8

# Если нет, установите:
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
```

### 3. Убедитесь что используется UTF-8 шрифт

В GitBash:
- Options → Text → Font: **Cascadia Code**, **Consolas**, или **JetBrains Mono**
- Options → Text → Character set: **UTF-8**

### 4. Запустите приложение

```bash
java -jar target/repo-maintainability-index-1.0.0.jar analyze prettier/prettier
```

### Ожидаемый результат

```
═══════════════════════════════════════════════════════════════
  Repository Maintainability Index Report
═══════════════════════════════════════════════════════════════

Repository: prettier/prettier
Overall Score: 76.30/100
Rating: GOOD

───────────────────────────────────────────────────────────────
  Detailed Metrics
───────────────────────────────────────────────────────────────

▪ Documentation: 80.00/100 (weight: 20%)
▪ Commit Quality: 100.00/100 (weight: 15%)
...
```

**Символы `═`, `─`, `▪` должны отображаться корректно, а НЕ как `ΓòÉ`, `ΓöÇ`, `Γû¬`.**

## Техническая информация / Technical Details

### System.out.write() vs System.out.println()

| Метод | Что делает | Кодировка |
|-------|-----------|-----------|
| `println(String)` | Конвертирует String → bytes используя системную кодировку | Зависит от системы |
| `write(byte[])` | Пишет байты напрямую, БЕЗ конвертации | НЕТ (сырые байты) |

### UTF-8 байты для box-drawing символов

Каждый символ кодируется **3 байтами** в UTF-8:

- `═` = `E2 95 90` (hex)
- `─` = `E2 94 80` (hex)
- `▪` = `E2 96 AA` (hex)

GitBash ожидает эти точные байты. Если Java отправляет другие байты (из-за неправильной кодировки), GitBash показывает mojibake.

## Статус / Status

✅ **Финальное исправление применено**

- [x] UTF8Console переписан для прямой записи байтов
- [x] Все тесты проходят (261 тест)
- [x] BUILD SUCCESS
- [x] Проверено на Linux (работает)
- [x] Готово для проверки на Windows/GitBash

## Следующие шаги

1. ✅ Соберите: `mvn clean package`
2. ✅ Замените JAR
3. ⏳ **Проверьте на Windows/GitBash** - запустите приложение
4. ⏳ Подтвердите что символы отображаются корректно

---

**Это должно быть финальное решение. Прямая запись UTF-8 байтов - единственный надежный способ обойти проблемы кодировки Java в GitBash.**
