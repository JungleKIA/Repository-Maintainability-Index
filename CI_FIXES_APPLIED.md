# Исправления CI Pipeline

## Проблема
После первых изменений CI все еще падал:
- ❌ Build and Test - падение после 2 секунд
- ❌ Generate SBOM - падение после 2 секунд

## Причина
Быстрое падение (2 секунды) указывает на проблемы с Maven lifecycle и выполнением команд.

## Применённые исправления

### 1. Build and Test Job
**Было:**
```yaml
- run: mvn clean compile -B
- run: mvn test -B
- run: mvn jacoco:report -B
- run: mvn jacoco:check -B
```

**Стало:**
```yaml
- run: mvn clean test -B
- run: mvn jacoco:check -B
```

**Почему**: 
- `mvn test` автоматически компилирует код
- JaCoCo report генерируется автоматически во время test phase
- Меньше отдельных команд = меньше вероятность ошибок

### 2. SBOM Generation Job
**Было:**
```yaml
- run: mvn cyclonedx:makeBom -B
- uses: actions/upload-artifact@v3
  with:
    path: target/bom.json
```

**Стало:**
```yaml
- run: mvn clean compile -B
- run: mvn cyclonedx:makeBom -B
- uses: actions/upload-artifact@v3
  with:
    path: target/bom.*
  if: always()
```

**Почему**:
- Плагин CycloneDX требует скомпилированные классы для анализа зависимостей
- `target/bom.*` ловит все форматы (json, xml)
- `if: always()` загружает артефакты даже при ошибке (для отладки)

### 3. POM.xml - CycloneDX Plugin
**Было:**
```xml
<goal>makeAggregateBom</goal>
```

**Стало:**
```xml
<goal>makeBom</goal>
```

**Почему**: CI использует команду `mvn cyclonedx:makeBom`, поэтому goal должен быть `makeBom`, а не `makeAggregateBom`.

## Дополнительное исправление #2

### Проблема: Jobs все еще падали после 2-3 секунд
После первых исправлений CI все еще падал очень быстро.

### Причина
Использовались устаревшие версии GitHub Actions и неправильный синтаксис для actions/upload-artifact@v4.

### Применённые исправления

**1. Обновление actions/upload-artifact с v3 на v4:**
- v4 требует обязательный параметр `retention-days`
- Изменен синтаксис размещения `if: always()`

**Было (v3):**
```yaml
- uses: actions/upload-artifact@v3
  with:
    name: sbom
    path: target/bom.*
  if: always()
```

**Стало (v4):**
```yaml
- if: always()
  uses: actions/upload-artifact@v4
  with:
    name: sbom
    path: target/bom.*
    retention-days: 90
```

**2. Исправление синтаксиса cache:**
```yaml
# Было:
cache: maven

# Стало:
cache: 'maven'
```

**3. Добавлены retention-days для всех артефактов:**
- test-results: 30 дней
- coverage-report: 30 дней
- sbom: 90 дней
- application-jar: 90 дней

## Результат
После этих исправлений CI должен пройти успешно:
- ✅ Build and Test - компиляция и тесты в одной команде
- ✅ Generate SBOM - компиляция перед генерацией SBOM
- ✅ Security Scanning - уже работает
- ✅ Code Quality - уже работает

## Файлы изменены
1. `.github/workflows/ci.yml` - упрощены Maven команды, обновлены actions до v4
2. `pom.xml` - исправлен goal CycloneDX плагина
3. `CI_FIX_SUMMARY.md` - обновлена документация

---
**Дата**: 2025-11-09
**Последнее обновление**: 2025-11-09 (3-е исправление)
**Статус**: Готово к проверке ✅
