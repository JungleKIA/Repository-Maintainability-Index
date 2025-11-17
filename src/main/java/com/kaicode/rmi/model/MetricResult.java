package com.kaicode.rmi.model;

import java.util.Objects;

/**
 * Результаты расчета конкретной метрики поддерживаемости репозитория.
 * <p>
 * Этот класс содержит полную информацию о результате вычисления одной метрики,
 * включая оценку, вес, описание и дополнительные детали. Используется всеми
 * реализациями {@link com.kaicode.rmi.metrics.MetricCalculator} для упаковки
 * результатов анализа в структурированный формат.
 * <p>
 * Результаты метрики включают:
 * <ul>
 *   <li>Имя метрики для идентификации</li>
 *   <li>Оценку в диапазоне 0-100 (выше = лучше)</li>
 *   <li>Вес важности в диапазоне 0.0-1.0</li>
 *   <li>Краткое описание того, что измеряет метрика</li>
 *   <li>Подробные детали расчетов и объяснения</li>
 * </ul>
 * <p>
 * Экземпляры неизменяемы и потокобезопасны.
 * Идентичность основана на имени, оценке и весе метрики.
 * <p>
 * Использование:
 * <pre>{@code
 * MetricResult result = MetricResult.builder()
 *     .name("Commit Quality")
 *     .score(85.5)
 *     .weight(0.8)
 *     .description("Quality of commit messages")
 *     .details("Analyzed 50 commits, 85% have proper formatting")
 *     .build();
 * }</pre>
 *
 * @since 1.0
 * @see com.kaicode.rmi.metrics.MetricCalculator
 * @see com.kaicode.rmi.service.MaintainabilityService
 */
public class MetricResult {
    private final String name;
    private final double score;
    private final double weight;
    private final String description;
    private final String details;

    /**
     * Приватный конструктор для создания неизменяемых экземпляров MetricResult.
     * <p>
     * Вызывается исключительно {@link Builder#build()} для создания
     * проверенных, неизменяемых объектов результатов метрики.
     * Все финальные поля присваиваются из проверенного состояния билдера.
     *
     * @param builder билдер с проверенными значениями полей
     */
    private MetricResult(Builder builder) {
        this.name = builder.name;
        this.score = builder.score;
        this.weight = builder.weight;
        this.description = builder.description;
        this.details = builder.details;
    }

    /**
     * Возвращает имя метрики.
     * <p>
     * Человеко-читаемое имя, которое идентифицирует метрику.
     * Используется для отображения в отчетах и интерфейсе.
     *
     * @return имя метрики, никогда не null
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает оценку метрики.
     * <p>
     * Значение оценки в диапазоне 0-100, где большее значение
     * указывает на лучшее состояние метрики. 100 = идеально.
     *
     * @return оценка в диапазоне 0.0 до 100.0
     */
    public double getScore() {
        return score;
    }

    /**
     * Возвращает вес важности метрики.
     * <p>
     * Вес показывает относительную важность этой метрики в общем
     * расчете поддерживаемости. Диапазон 0.0-1.0, где 1.0 наиболее важно.
     *
     * @return вес метрики в диапазоне 0.0 до 1.0
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Возвращает краткое описание метрики.
     * <p>
     * Описание объясняет, что именно эта метрика измеряет
     * и какие аспекты качества репозитория оценивает.
     *
     * @return описание метрики, может быть null
     */
    public String getDescription() {
        return description;
    }

    /**
     * Возвращает подробные детали расчетов метрики.
     * <p>
     * Содержит дополнительную информацию о том, как была рассчитана
     * оценка, какие данные анализировались и конкретные результаты.
     *
     * @return детали расчетов, могут быть null
     */
    public String getDetails() {
        return details;
    }

    /**
     * Вычисляет взвешенную оценку метрики.
     * <p>
     * Взвешенная оценка = оценка × вес. Этот показатель отражает
     * вклад данной метрики в общую оценку поддерживаемости.
     * Используется для агрегации результатов всех метрик.
     *
     * @return взвешенная оценка ({@code score × weight})
     */
    public double getWeightedScore() {
        return score * weight;
    }

    /**
     * Сравнивает этот результат метрики с другим объектом на равенство.
     * <p>
     * Два объекта MetricResult считаются равными, если они имеют одинаковые
     * значения имени, оценки и веса. Другие поля (описание, детали) не
     * учитываются для поддержки стабильности идентичности при изменении
     * деталей расчетов.
     *
     * @param o объект для сравнения
     * @return true если объекты представляют один и тот же результат метрики
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricResult that = (MetricResult) o;
        return Double.compare(that.score, score) == 0 &&
                Double.compare(that.weight, weight) == 0 &&
                Objects.equals(name, that.name);
    }

    /**
     * Возвращает значение хэш-кода для этого результата метрики.
     * <p>
     * Хэш-код вычисляется на основе имени, оценки и веса,
     * в соответствии с реализацией метода equals.
     *
     * @return хэш-код для этого результата метрики
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, score, weight);
    }

    /**
     * Создает новый экземпляр Builder для конструирования объектов MetricResult.
     * <p>
     * Этот фабричный метод предоставляет точку входа для паттерна builder,
     * позволяя fluent построение неизменяемых экземпляров MetricResult
     * с правильной валидацией.
     *
     * @return новый экземпляр Builder для цепочного вызова методов
     * @since 1.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Класс Builder для конструирования неизменяемых экземпляров MetricResult.
     * <p>
     * Предоставляет fluent API для установки всех полей результатов метрики
     * перед созданием финального неизменяемого экземпляра. Builder валидирует
     * обязательные поля (имя) и диапазоны значений во время сборки.
     * <p>
     * Поле имени обязательно, другие поля опциональны.
     * Неустановленные опциональные поля получат значения по умолчанию.
     *
     * @since 1.0
     */
    public static class Builder {
        private String name;
        private double score;
        private double weight;
        private String description;
        private String details;

        /**
         * Устанавливает имя метрики (обязательное поле).
         * <p>
         * Человеко-читаемое имя должно однозначно идентифицировать метрику.
         * Это поле является обязательным и не может быть null.
         *
         * @param name имя метрики, должно быть не null и не пустым
         * @return этот экземпляр builder для цепочного вызова
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Устанавливает оценку метрики.
         * <p>
         * Значение должно быть в диапазоне 0.0 до 100.0, где большее значение
         * указывает на лучшее состояние метрики. 100.0 = идеально.
         * Значение проверяется во время сборки.
         *
         * @param score оценка метрики в диапазоне 0.0 до 100.0
         * @return этот экземпляр builder для цепочного вызова
         */
        public Builder score(double score) {
            this.score = score;
            return this;
        }

        /**
         * Устанавливает вес важности метрики.
         * <p>
         * Вес показывает относительную важность метрики в общем расчете.
         * Должен быть в диапазоне 0.0 до 1.0, где 1.0 означает максимальную важность.
         * Значение проверяется во время сборки.
         *
         * @param weight вес метрики в диапазоне 0.0 до 1.0
         * @return этот экземпляр builder для цепочного вызова
         */
        public Builder weight(double weight) {
            this.weight = weight;
            return this;
        }

        /**
         * Устанавливает краткое описание метрики (опциональное поле).
         * <p>
         * Описание должно объяснять, что измеряет эта метрика
         * и какие аспекты качества репозитория оцениваются.
         *
         * @param description краткое описание метрики, может быть null
         * @return этот экземпляр builder для цепочного вызова
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Устанавливает подробные детали расчетов (опциональное поле).
         * <p>
         * Дополнительная информация о том, как была рассчитана оценка,
         * какие данные анализировались и конкретные результаты анализа.
         *
         * @param details детали расчетов, могут быть null
         * @return этот экземпляр builder для цепочного вызова
         */
        public Builder details(String details) {
            this.details = details;
            return this;
        }

        /**
         * Собирает и валидирует новый экземпляр MetricResult.
         * <p>
         * Создает неизменяемый MetricResult из текущего состояния builder.
         * Обязательные поля (имя) проверяются на наличие, а числовые поля
         * проверяются на допустимые диапазоны.
         * <p>
         * Возвращаемый экземпляр потокобезопасен и может безопасно передаваться.
         *
         * @return новый неизменяемый экземпляр MetricResult
         * @throws NullPointerException если имя равно null
         * @throws IllegalArgumentException если оценка вне диапазона 0-100
         *                                  или вес вне диапазона 0.0-1.0
         */
        public MetricResult build() {
            Objects.requireNonNull(name, "name must not be null");
            if (score < 0 || score > 100) {
                throw new IllegalArgumentException("score must be between 0 and 100");
            }
            if (weight < 0 || weight > 1) {
                throw new IllegalArgumentException("weight must be between 0 and 1");
            }
            return new MetricResult(this);
        }
    }
}
