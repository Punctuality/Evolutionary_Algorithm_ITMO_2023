# Лабораторная работа №4

**Дисциплина:** "Эволюционные вычисления"

**Дата:** 15/03/2023

**Выполнил:** Федоров Сергей, M4150 

**Название:** "Генетический алгоритм для задачи коммивояжёра"

**Репозиторий с исходным кодом:** [Репозиторий](https://github.com/Punctuality/Evolutionary_Algorithm_ITMO_2023)

## Описания выполнения:

1. Скачать репозиторий с исходным кодом заглушек проекта: [Репозиторий](https://gitlab.com/itmo_ec_labs/lab3)
2. Настроить среду исполнения для запуска проекта
3. Определение внутренней структуры кандидата
4. Парсинг TSP файлов
5. Имплементация инициализации популяции
6. Имплементация операции кроссинговера
7. Имплементация операции мутации
8. Настройка модуля прогона по сетке
9. Поиск оптимальных параметров алгоритма
10. Итоговые результаты работы алгоритма
11. Ответы на поставленные вопросы

### Скачивание репозитория

Для скачивания репозитория используется команда `git submodule add` для скачивания репозитория.

```bash
git submodule add https://gitlab.com/Punctuality/evolutionary-algorithm-itmo-2023-lab-4 Lab_4/code
```

Стоит так-же сказать что из-за того что репозиторий находится в GitLab, GitHub репозиторий, где хранится вся информация о лабораторной работе, не дает возможности по щелчку на ссылку перейти в репозиторий. Поэтому стоит использовать ссылку указанную выше в команда добавления подмодуля.

## Структура внутреннего представления кандидата
#### TspSolution

Стоит отметить что внутреннее представление кандидата было получено не в одной итерации и с множественныии оптимизациями. Основной задачей для себя я поставил максимальную производительность операций над кандидатами и минимальная нагрузка на память (кол-во занимаемой памяти и нагрузка на GC). По итогу различных экспериментов остановился на оптимальном варианте в ввиде массива индексов городов.

```java
public class TspSolution {
    final int[] orderedSolution;

    TspSolution(int[] orderedSolution) {
        this.orderedSolution = orderedSolution;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int idx : orderedSolution) {
            sb.append(idx);
            sb.append('>');
        }
        sb.append(orderedSolution[0]);
        return sb.toString();
    }
}
```

Никаких дополнительных обьявлений в классе TspSolution я не проводил. Возможно стоило бы уделить больше времени компоновке кода и инкапсуляции операций над кандидатами внутри данного класса, но так как в операторах мутации и кроссовера используются различные операции над массивами (и они постоянно менялись) я это не делал. Обоснование почему был выбран именно такой вариант представления кандидата:

1. По мере того как будет увеличиваться размерность проблемы (кол-во городов) будет увеличиваться и кол-во элементов массива. Выбирая стандартный Java массив вместо `ArrayList` или любой другой имплементации `java.util.List` мы избегаем необходимость в автобоксинге всех элементов массива из `int` в `Interger`, а как следствие уменьшаем нагрузку на GC и увеличиваем производительность. Тут стоит заметить что использовали бы мы другой JVM язык, такой как Scala, так проблемы не было бы.
2. Может показаться что помимо операции мутации Swap другие типы операций более эффективно выполнялись бы на списках где элементы связаны ссылками на вершины, по типу LinkedList, посколько нету необходимости в их копированиях, однако это не так, в связи с тем что нам все равно необходимо создавать копии всех кандидатов на каждой итерации, а также во время операции взятия подсписка из-за того что метод `.subList` возвращает `AbstractList<T>` вместо изначального типа коллекций и обычным тайп-кастом не обойтись. 
3. Была предпринята попытка нивелировать проблемы из пункта 2 использованиям коллекции `TreeList<Integer>` из библиотеки `apache-commons.collections`, поскольку такой тип структуры предоставляет отличие характеристики производительности при всех операциях необходимых в данной задаче. Но даже это не помогло получить произодительность сравнимую с решением на обычных массивах.
4. Все операции копирования массивов где копируется не весь массив а только его часть, были заменены на использование `System.arraycopy` для увеличения производительности.
5. Операция поиска индекса элемента в массиве была заменена на бинарный поиск (вместо использования таких коллекций как HashSet), что также увеличило производительность.
6. По итогу производительность (по сравнению с лучшим решением на `List`) по CPU была лучше в 11 раз, а по памяти расход на запуск на 20000 поколений при размерности популяции 200 был в 7 раза меньше (700 Мб против 9.8 Гб аллокаций).

## Парсинг TSP файлов
#### TspReader, lab4.codec

Не буду вставлять сниппет кода парсинга TSP файлов в отчет, потому как там получилось откровенно не мало, потому как  стоит заметить, что в данном случае я реализовал считывание плюс-минус полной спецификации TSP файлов с поддержкой различных типо расстояний: эвклидовое, манхеттенское, географическое, максимальное и округленное. Поддерживается считывание расстояний из матрицы расстояний. Также поддерживается работа с 2D, 3D и Гео координатами.

Итоговый результат работы парсера записывается в экземпляр класса `TspDescription` который содержит в себе:

```java
public class TspDescription {
    public String name;
    public TspType type;
    public List<String> comments;
    public int dimension;
    public TspEdgeType edgeWeightType;
    public TspDistanceMatrix edgeWeightSection;
    public TspNode[] nodeCoordSection;

...
}
```

Стоит отметить что поле `nodeCoordSection`, изначально было представлено в виде `HashMap<Integer, TspNode>`, но во время дальнейших оптимизаций было заменено на обычный массив, поскольку в данном случае нам не нужна была быстрая вставка элементов, а только быстрый доступ по индексу. И да, судя по последующему профилированию это дало прирост в производительности на этапе оценки популяции в `TspFitnessFunction`.

Стоит заметить также что в случае парсинга файлов и затем последующей операции оценки значения фитнес-функции всегда проверяется целостность данных. Так например в `TspFitnessFunction` используется формула арифметический прогрессии и счетчик, который к концу оценки должен будет равняться нулю в случае если предоставленные данные корректны (нет дупликатов).

```java
double fitness = 0;
int checker = this.problemDescription.dimension * (this.problemDescription.dimension + 1) / 2;
for (int i = 1; i <= this.problemDescription.dimension; i++) {
    int fst = solution.orderedSolution[i - 1]; // This way we achieve full route cycle
    int snd = solution.orderedSolution[i == this.problemDescription.dimension ? 0 : i];

    checker -= fst;

    fitness += this.problemDescription.getDistanceBetween(fst, snd);
}
if (checker != 0) {
    throw new IllegalStateException("Solution does not contain only unique elems");
}
```

###  Имплементация инициализации популяции 
#### TspFactory

Особого внимания заострять здесь не требует: создается кандидат со случайно расставленныит вершинами и добавляется в популяцию. Единственное что можно ответить - многи операции присутствующие в `Collections`, не имеют аналогов в стандартном `Arrays`, поэтому например операцию перемешивания как и некоторые другие операции приходится реализовывать самостоятельно. В данном случае все такие операции реализованы в `ArrayUtil.java`.

```java
public class ArrayUtil {
    public static void swapElement(int[] a1, int[] a2, int idx) {
        int tmp = a1[idx];
        a1[idx] = a2[idx];
        a2[idx] = tmp;
    }

    public static void swapElement(int[] a, int idx1, int idx2) {
        int tmp = a[idx1];
        a[idx1] = a[idx2];
        a[idx2] = tmp;
    }

    public static void scrambleArray(int[] arr, Random random) {
        for (int i = arr.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            swapElement(arr, index, i);
        }
    }

    public static void reverse(int[] arr) {
        for (int i = 0; i < arr.length / 2; i++) swapElement(arr, i, arr.length - i - 1);
    }
}
```

###  Имплементация операции кроссинговера
#### TspCrossover

Операция кроссинговера выполняется согласно описанию операции ordered crossoover из методички к лабораторной работе. В качестве параметров оператор получает вероятность совершения кроссинговера и максимальную длинну переносимого сегмента. Непосредственная реализация переноса сегмента, со всеми оптимизациями,предоставлена ниже:

```java
int[] orderCrossover(int[] from, int[] to, Random random) {
    int[] toNew = to.clone();

    int swapSegmentLen = random.nextInt(maxLenSwapped) + 1;
    int swapPoint = random.nextInt(from.length - swapSegmentLen);
    int[] swapSegment = new int[swapSegmentLen];
    System.arraycopy(from, swapPoint, swapSegment, 0, swapSegmentLen);

    System.arraycopy(swapSegment, 0, toNew, swapPoint, swapSegmentLen);

    Arrays.sort(swapSegment);

    boolean jumped = false;
    for (int i = 0, j = 0; i < to.length; i++) {
        if (j >= swapPoint && !jumped) {
            j += swapSegmentLen;
            jumped = true;
        }
        if (Arrays.binarySearch(swapSegment, to[i]) < 0) {
            toNew[j++] = to[i];
        }
    }

    return toNew;
}
```

###  Имплементация операции мутации
#### TspMutation

Оператор мутации принимает вероятность мутации и описанный тип мутации `MutationType`, который описывает четыре возможных мутации (перемешивание, обмен, инверсия и вставка). Отдельные мутации также имеют параметр максимального сегмента который они затрагивают

```java
public enum MutationType {
    SWAP(1),
    INSERTION(1),
    SCRAMBLE(1),
    INVERSION(1);

    int maxLen;
    ...
}
```

Все мутации реализованы в соответствии с описанием в методичке к лабораторной работе.

```java
public TspSolution mutate(TspSolution candidate, Random random) {
    int[] newSolution = candidate.orderedSolution.clone();
    switch (this.type) {
        case SWAP -> {
            int idx1 = random.nextInt(candidate.orderedSolution.length);
            int idx2 = random.nextInt(candidate.orderedSolution.length);
            while (idx1 == idx2) {
                idx2 = random.nextInt(candidate.orderedSolution.length);
            }

            ArrayUtil.swapElement(newSolution, idx1, idx2);
        }
        case INSERTION -> {
            int idx1 = random.nextInt(candidate.orderedSolution.length - 1);
            int idx2 = random.nextInt(candidate.orderedSolution.length);
            while (idx1 == idx2) {
                idx2 = random.nextInt(candidate.orderedSolution.length);
            }
            if (idx1 > idx2) {
                int tmp = idx1;
                idx1 = idx2;
                idx2 = tmp;
            }

            newSolution[idx1 + 1] = newSolution[idx2];
            System.arraycopy(candidate.orderedSolution, idx1 + 1, newSolution, idx1 + 2, idx2 - idx1 - 1);
        }
        case SCRAMBLE -> {
            int scrambleSegmentLen = random.nextInt(this.type.getMaxLen()) + 1;
            int scrambleStart = random.nextInt(newSolution.length - scrambleSegmentLen);

            int[] scrambleSection = new int[scrambleSegmentLen];
            System.arraycopy(newSolution, scrambleStart, scrambleSection, 0, scrambleSegmentLen);

            ArrayUtil.scrambleArray(scrambleSection, random);

            System.arraycopy(scrambleSection, 0, newSolution, scrambleStart, scrambleSegmentLen);
        }
        case INVERSION -> {
            int inversionSegmentLen = random.nextInt(this.type.getMaxLen()) + 1;
            int inversionStart = random.nextInt(newSolution.length - inversionSegmentLen);

            int[] insertionSection = new int[inversionSegmentLen];
            System.arraycopy(newSolution, inversionStart, insertionSection, 0, inversionSegmentLen);

            ArrayUtil.reverse(insertionSection);

            System.arraycopy(insertionSection, 0, newSolution, inversionStart, inversionSegmentLen);
        }
    }
    return new TspSolution(newSolution);
}
```

### Настройка модуля прогона по сетке
#### GridEvaluator, ExperimentUnit, ExperimentResult, CombinedTerminationCondition

В данном случае был переиспользован написанный мною модуль GridSearch из прошлой лабораторной работы. Необходимость в нем есть для:
1. Проведения экспериментов с различными параметрами алгоритма, что в данном случае необходимо так как операторы кроссовера и мутации имеют множетсво различных параметров.
2. Эффективного исполнения программы, так как в моем случае я могу себе позволить проводить прогоны на 16 потоках одновременно.

Особых изменений в модуле не происходило, кроме перенастройки pipeline'a исполнения и добавления новых параметров. Список всех параметров:

```java
GridEvaluator<ExperimentResult[], ExperimentUnit> evaluator = new GridEvaluator<>(
        outputFile,
        experimentCompiler,
        rowLogger,
        paramNames,
        resultNames,
        problems,
        evolutionTypes,
        populationSizes,
        generations,
        mutationProbs,
        mutationTypes,
        maxLenCrossedOver,
        crossoverProbs
);
```

Также был исправлена проблема с потоками из прошлой лабораторной работы, что привело к корректной их утилизации и 100% (скорее ближе к 92%, из-за тротлинга) загруженности процессора.

Скриншоты из JVisualVM:
![cpu_monitor](https://github.com/Punctuality/Evolutionary_Algorithm_ITMO_2023/blob/main/Lab_4/images/cpu_monitor.jpg?raw=true)
![thread_monitor](https://github.com/Punctuality/Evolutionary_Algorithm_ITMO_2023/blob/main/Lab_4/images/thread_monitor.jpg?raw=true)

Еще стоит заметить что был добавлен композитный тип `CombinedTerminationCondition` который позволяет объединять различные условия остановки в одно. Это позволяет в случае достижения глобального оптимума более не проводить вычисления впустую (в данном случае с каждой проблемой нам заранее известны оптимальные решения):

```java
public class CombinedTerminationCondition implements TerminationCondition {

    public enum CombineType {
        AND,
        OR;
    }

    List<TerminationCondition> conditionList;
    CombineType combineType;

    public CombinedTerminationCondition(CombineType combineType, TerminationCondition... conditionList){
        this.conditionList = Arrays.stream(conditionList).toList();
        this.combineType = combineType;
    }

    long countTerminated(PopulationData<?> populationData) {
        return conditionList.stream().filter(cond -> cond.shouldTerminate(populationData)).count();
    }

    @Override
    public boolean shouldTerminate(PopulationData<?> populationData) {
        switch (combineType) {
            case OR -> {
                return countTerminated(populationData) > 0;
            }
            case AND -> {
                return countTerminated(populationData) == conditionList.size();
            }
        }
        return false;
    }
}
```

### Поиск оптимальных параметров алгоритма

В данном случае был проведено не мало экспериментов с различными параметрами алгоритма (это заняло львиную долю времени и потому лаба сдана в самый дедлайн). Некоторые прожуточные результаты сохранены в файлах в папке `results`. Изначальный поиск (самый широкий разброс параметров) имел примерно 11 тысяч различных комбинаций параметров, однако благодаря оптимизациям для параллельной обработки - время работы алгоритма было в приемлимых значениях (около 2-3 часов).

В конце концов был выполнен прогон по самым удачным комбинациям параметров на различных проблемах и стратегиях эволюции. Результаты храняться в файле `final_results.csv` и указаны ниже.

### Итоговые результаты работы алгоритма

| Problem      | Evolution type | Population size | Generations | Mutation probability | Mutation type | Crossover Max len | Crossover probability | Best fitness       | Finished eval at | First generation of best | Mean time (ms) |
| :----------- | :------------- | :-------------- | :---------- | :------------------- | :------------ | :---------------- | :-------------------- | :----------------- | :--------------- | :----------------------- | :------------- |
| XQF131(564)  | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 5                 | 0.01                  | 726.6873878721707  | 79999            | 73452                    | 69851.5        |
| XQF131(564)  | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 5                 | 0.03                  | 695.2426516763185  | 79999            | 78470                    | 63279.5        |
| XQF131(564)  | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 100               | 0.01                  | 774.2946476891029  | 79999            | 79365                    | 61425.4        |
| XQF131(564)  | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 100               | 0.03                  | 716.1624887722176  | 79999            | 76836                    | 64432.4        |
| XQF131(564)  | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 120               | 0.01                  | 763.8392234428673  | 79999            | 50334                    | 61952.8        |
| XQF131(564)  | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 120               | 0.03                  | 739.750815259488   | 79999            | 41353                    | 71734.4        |
| XQF131(564)  | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 5                 | 0.01                  | 781.6475702204865  | 79999            | 79825                    | 68404.7        |
| XQF131(564)  | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 5                 | 0.03                  | 704.3242476207624  | 79999            | 76927                    | 70023.9        |
| XQF131(564)  | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 100               | 0.01                  | 722.9755992804977  | 79999            | 66446                    | 63282.8        |
| XQF131(564)  | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 100               | 0.03                  | 731.3375969974472  | 79999            | 55820                    | 64204.3        |
| XQF131(564)  | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 120               | 0.01                  | 742.9204957515237  | 79999            | 65842                    | 69495.3        |
| XQF131(564)  | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 120               | 0.03                  | 721.6495013635393  | 79999            | 44185                    | 64944.1        |
| XQF131(564)  | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 5                 | 0.01                  | 697.4079038834267  | 79999            | 40868                    | 55140.3        |
| XQF131(564)  | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 5                 | 0.03                  | 713.7031976821996  | 79999            | 25321                    | 56194.0        |
| XQF131(564)  | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 100               | 0.01                  | 733.5747561235465  | 79999            | 67259                    | 56668.6        |
| XQF131(564)  | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 100               | 0.03                  | 668.1994586066513  | 79999            | 60435                    | 63006.7        |
| XQF131(564)  | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 120               | 0.01                  | 702.1781250549992  | 79999            | 45801                    | 56465.1        |
| XQF131(564)  | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 120               | 0.03                  | 718.3222471506551  | 79999            | 75788                    | 59817.4        |
| XQF131(564)  | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 5                 | 0.01                  | 759.4371018523532  | 79999            | 76538                    | 62538.3        |
| XQF131(564)  | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 5                 | 0.03                  | 746.3119440130381  | 79999            | 78078                    | 64823.7        |
| XQF131(564)  | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 100               | 0.01                  | 700.1154755663608  | 79999            | 41046                    | 65896.8        |
| XQF131(564)  | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 100               | 0.03                  | 730.5179667281625  | 79999            | 79022                    | 68740.2        |
| XQF131(564)  | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 120               | 0.01                  | 734.4330700782926  | 79999            | 64519                    | 67715.0        |
| XQF131(564)  | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 120               | 0.03                  | 735.4263592385715  | 79999            | 52329                    | 72125.0        |
| XQL662(2513) | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 5                 | 0.01                  | 9210.613537352709  | 79999            | 79970                    | 253575.6       |
| XQL662(2513) | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 5                 | 0.03                  | 9061.564926449448  | 79999            | 79810                    | 253214.2       |
| XQL662(2513) | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 100               | 0.01                  | 9010.262730621831  | 79999            | 79957                    | 244435.2       |
| XQL662(2513) | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 100               | 0.03                  | 9025.662122685397  | 79999            | 79691                    | 306511.4       |
| XQL662(2513) | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 120               | 0.01                  | 9041.591442940424  | 79999            | 79839                    | 284312.5       |
| XQL662(2513) | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 120               | 0.03                  | 9477.509995620641  | 79999            | 79137                    | 297802.9       |
| XQL662(2513) | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 5                 | 0.01                  | 9341.46647066139   | 79999            | 79720                    | 281870.8       |
| XQL662(2513) | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 5                 | 0.03                  | 9116.721749857024  | 79999            | 79110                    | 289027.1       |
| XQL662(2513) | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 100               | 0.01                  | 9471.702901290344  | 79999            | 79996                    | 291897.9       |
| XQL662(2513) | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 100               | 0.03                  | 9377.871419058723  | 79999            | 79864                    | 310243.8       |
| XQL662(2513) | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 120               | 0.01                  | 9283.292993210078  | 79999            | 79609                    | 303102.3       |
| XQL662(2513) | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 120               | 0.03                  | 9513.048554593686  | 79999            | 79935                    | 308638.9       |
| XQL662(2513) | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 5                 | 0.01                  | 7871.388924172555  | 79999            | 79939                    | 287105.0       |
| XQL662(2513) | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 5                 | 0.03                  | 7831.249357575448  | 79999            | 79785                    | 292475.9       |
| XQL662(2513) | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 100               | 0.01                  | 7630.0528077229865 | 79999            | 79734                    | 292495.5       |
| XQL662(2513) | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 100               | 0.03                  | 7860.577980174606  | 79999            | 79975                    | 310607.0       |
| XQL662(2513) | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 120               | 0.01                  | 7814.3745678030755 | 79999            | 79812                    | 291217.3       |
| XQL662(2513) | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 120               | 0.03                  | 7986.464392699952  | 79999            | 79973                    | 303674.7       |
| XQL662(2513) | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 5                 | 0.01                  | 7938.227000314909  | 79999            | 79983                    | 289324.6       |
| XQL662(2513) | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 5                 | 0.03                  | 7973.0055823584435 | 79999            | 79825                    | 290956.2       |
| XQL662(2513) | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 100               | 0.01                  | 8025.73565088669   | 79999            | 79993                    | 298215.9       |
| XQL662(2513) | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 100               | 0.03                  | 8066.474433336069  | 79999            | 79935                    | 302517.2       |
| XQL662(2513) | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 120               | 0.01                  | 7845.661298086573  | 79999            | 79560                    | 293047.1       |
| XQL662(2513) | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 120               | 0.03                  | 7968.721839047754  | 79999            | 79747                    | 307361.3       |
| PBM436(1443) | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 5                 | 0.01                  | 3551.681005620805  | 79999            | 79570                    | 184990.4       |
| PBM436(1443) | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 5                 | 0.03                  | 3401.5331738024297 | 79999            | 79920                    | 188532.1       |
| PBM436(1443) | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 100               | 0.01                  | 3462.0730427334975 | 79999            | 79564                    | 189458.4       |
| PBM436(1443) | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 100               | 0.03                  | 3790.2158999702383 | 79999            | 78768                    | 198593.3       |
| PBM436(1443) | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 120               | 0.01                  | 3523.843044430987  | 79999            | 79903                    | 185840.2       |
| PBM436(1443) | STEADY_STATE   | 200             | 80000       | 0.7                  | INSERTION(1)  | 120               | 0.03                  | 3725.5578049080623 | 79999            | 79936                    | 196849.5       |
| PBM436(1443) | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 5                 | 0.01                  | 3780.7796394169    | 79999            | 79943                    | 184468.5       |
| PBM436(1443) | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 5                 | 0.03                  | 3668.631255530927  | 79999            | 79550                    | 187589.6       |
| PBM436(1443) | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 100               | 0.01                  | 3731.103371707323  | 79999            | 79855                    | 186937.7       |
| PBM436(1443) | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 100               | 0.03                  | 3593.808471050529  | 79999            | 79764                    | 201602.7       |
| PBM436(1443) | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 120               | 0.01                  | 3663.2836166675042 | 79999            | 79973                    | 194252.1       |
| PBM436(1443) | STEADY_STATE   | 200             | 80000       | 0.9                  | INSERTION(1)  | 120               | 0.03                  | 3604.51480009759   | 79999            | 79891                    | 206073.6       |
| PBM436(1443) | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 5                 | 0.01                  | 3132.1616229062215 | 79999            | 79905                    | 188083.4       |
| PBM436(1443) | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 5                 | 0.03                  | 3134.349712413486  | 79999            | 79509                    | 189868.4       |
| PBM436(1443) | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 100               | 0.01                  | 3124.32173667313   | 79999            | 79336                    | 191260.8       |
| PBM436(1443) | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 100               | 0.03                  | 3186.7693166651707 | 79999            | 79963                    | 199138.4       |
| PBM436(1443) | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 120               | 0.01                  | 3153.1762399609897 | 79999            | 79983                    | 190682.5       |
| PBM436(1443) | GENERATIONAL   | 200             | 80000       | 0.7                  | INSERTION(1)  | 120               | 0.03                  | 3005.676020356488  | 79999            | 79506                    | 199174.8       |
| PBM436(1443) | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 5                 | 0.01                  | 3272.61572902036   | 79999            | 79835                    | 189083.2       |
| PBM436(1443) | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 5                 | 0.03                  | 3319.211273440612  | 79999            | 79752                    | 195029.7       |
| PBM436(1443) | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 100               | 0.01                  | 3112.462172239994  | 79999            | 79658                    | 192808.4       |
| PBM436(1443) | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 100               | 0.03                  | 3105.0046595226954 | 79999            | 79896                    | 201085.6       |
| PBM436(1443) | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 120               | 0.01                  | 3159.69239022268   | 79999            | 79843                    | 193019.2       |
| PBM436(1443) | GENERATIONAL   | 200             | 80000       | 0.9                  | INSERTION(1)  | 120               | 0.03                  | 3328.784448395313  | 79999            | 79829                    | 203093.9       |
|              |                |                 |             |                      |               |                   |                       |                    |                  |                          |                |

Из интересных наблюдений стоит отметить:
1. По итогу наблюдений за различными гиперпараметрами, операции мутации типа Insertion оказались наиболее эффективными. Видимо потому что привносят маленький изменения в уже существующие решения, что позволяет избежать хаотичного изменения кандидатов.
2. Операция кроссинговера также была эффективна, либо при замене сегментов маленького размера, либо большого, но не среднего. Видимо также потому что сохраняла большинство уже хороших решений.
3. Удалось найти оптимальные значения гиперпараметров для каждой из операций, о чем свидетельствует то что даже при большом количестве поколений, алгоритм не переставал находить хорошие решения и лучшие решения на поздних этапах.

### Ответы на вопросы

1. Можно ли определить, что полученное решение является глобальным оптимумом?

Да, если мы знаем значение глобального оптимума из условия задачи, то можем сравнить с текущим значением фитнесс-функции. Однако, в реальном мире, где такой информации нет, нельзя подтвердить, что полученное решение является глобальным оптимумом.

2. Можно ли допускать невалидные решения (с повторением городов). Если да, то как обрабатывать такие решения и как это повлияет на производительность алгоритма?

Нельзя допускать невалидные решения. Одна из задач при написании операторов мутации и кроссинговера - это генерировать валидных кандидатов. А задача фитнесс-функции еще и валидировать предоставленные решения. Один из таких способов - я описал выше. В задаче комивояжера, где по крайней мере в данном случае мы считаем линейные евклидовые расстояния, по идее, невалидные решения не должны быть эффективными. На производительность, такие решение может повлиять, ведь в таком случае мы вольны строить маршруты бесконечной длинны.

3. Как изменится задача, если убрать условие необходимости возврата в конечную точку?

С точки зрения кода - не сильно, необходимо будет убрать лишь одно условие в фитнесс-функции. С точки зрения постановки задачи, поскольку нет необходимости возвращаться в конечную точку, это позволяет исключить дополнительные шаги, которые могут быть затрачены на возврат в начальную точку.