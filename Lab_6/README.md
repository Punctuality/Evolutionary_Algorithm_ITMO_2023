# Лабораторная работа №5

**Дисциплина:** "Эволюционные вычисления"

**Дата:** 29/03/2023

**Выполнил:** Федоров Сергей, M4150 

**Название:** "РАСПРЕДЕЛЕННЫЕ ЭВОЛЮЦИОННЫЕ АЛГОРИТМЫ"

**Репозиторий с исходным кодом:** [Репозиторий](https://github.com/Punctuality/Evolutionary_Algorithm_ITMO_2023)

## Описания выполнения:

1. Выбор языка, фреймворка и подхода к решению задачи
2. Настройка модуля прогона по сетке
3. Итоговые результаты работы алгоритма
4. Ответы на поставленные вопросы

## Выбор языка, фреймворка и подхода к решению задачи

Продолжая с прошлой лабораторной работы я выбрал язык `Scala`, так как:

1. Могу использовать библиотеку `Cats` удобной работы с отлодженными вычислениями
2. Система типов помогает не допускать дополнительных ошибок при разработке
3. Переиспользовать код для настройки и поиска по сетке из прошлой лабораторной работы

Также стоит заметить что, для проведения тестов я заменил стандартную виртуальную машину `JVM` на `GraalVM`, что по моим замерам дало прирост в производительности в 2-3.5 раза.

Посколько темплейт для лабораторной работы был предоставлен в виде `Java` проекта, то пришлось переписать все предоставленные примеры алгоритмов, а так же переписать реализацию `MultiFitnessFunction`, естественно без изменения функционала.

### Настройка модуля прогона по сетке
#### PerfExperimentSetup, PerfExperimentResult, PerfExperimentRunner

В данной лабораторной работе использовался все тот же самый модуль прогона по сетке, с только лишь измененными типами входных и выходных данных.

```scala
// PerfExperimentSetup.scala
final case class PerfExperimentSetup(
  dimension: Int,
  populationSize: Int,
  generations: Int,
  algoType: PerfExperimentSetup.AlgoType,
  complexity: Int
)

given Show[AlgoType] with
  def show(algoType: AlgoType): String = algoType match
    case AlgoType.SingleThreaded => "SingleThreaded"
    case AlgoType.MasterSlave => "MasterSlave"
    case AlgoType.Islands(islandCount, epochLength, migrationCount) => 
      s"Islands($islandCount)"
```

```scala
// PerfExperimentResult.scala
final case class PerfExperimentResult(
  bestResult: Double,
  bestCandidate: MyCandidate,
  firstAchievedIter: Int,
  finishedEvalAt: Int,
  evalTime: Long
) extends Ordered[PerfExperimentResult]:
  
  extension (prev: Int)
      private infix def ifZero(next: => Int): Int = 
        if prev == 0 then next else prev

  override def compare(that: PerfExperimentResult): Int = 
    this.bestResult.compare(that.bestResult) ifZero
      this.finishedEvalAt.compare(that.finishedEvalAt) ifZero
      this.firstAchievedIter.compare(that.firstAchievedIter) ifZero
      this.evalTime.compare(that.evalTime)
```

В отличие от предыдущих лабораторных работ, в данном случае нету предположения что прогон каждого эксперимента будет происходить в отдельном изолированном потоке (поскольку это не так), поэтому на данный раз, что различные эксперименты, что их повторы происходят в последовательном режиме без параллелизма, тем самым освобождая ресурсы для потоков исполнения самого алгоритма.

```scala
// OMMITTED

        // setting up algo here

        Iterator
          .range(0, repeats)
          .map(iter => runExperiment(algo(iter), setup))
          .toList

      .flatMap(_.sequence)

// OMMITTED
```

### Итоговые результаты работы алгоритма

В итоге был проведен прогон при разных параметрах размерности, кол-ва поколений, размера популяции и типа работы алгоритма (в одном потоке, master-slave и островной на 2 и 8 островов). Результаты усреднялись по 10 запускам. Таблица всех результатов в отчете не предоставленна, так как там 864 строки, однако она доступна в репозитории.

Результаты по таблице требуемой в методичке, приведены ниже:



### Ответы на вопросы

1. Какая модель алгоритма лучше при каких условиях?



2. Как повлияет увеличение размерности проблемы на алгоритмы?



3. Как повлияет увеличение размера популяции на алгоритмы?



4. Есть ли ограничение для количества островов?

Ограничением на количество островов является размер популяции, так как на острове, мы наверняка не хотим чтобы не было ни одного кандидата (хотя конечно так можно сделать, в случае когда различные острова имеют разный характер мутации и кроссовера). Однако, стоит заметить что в случае если количество острово будет превышать количество физических ядер процессора/процессоров, то это не даст никакого прироста в производительности, так как некоторые острова будут работать в одном потоке исполнения, что наоборот только замедлит общую работу алгоритма. Хотя такое количество островов может быть полезно в случае если какая либо стадия работы алгоритма использует блокирующие операции, например запрос к БД для подсчета фитнесс-функции, тогда чтобы поток не находился в состояния ожидания, можно допустить больше чем один остров на одно физическое ядро.
