# Лабораторная работа №5

**Дисциплина:** "Эволюционные вычисления"

**Дата:** 22/03/2023

**Выполнил:** Федоров Сергей, M4150 

**Название:** "ПРОЕКТИРОВАНИЕ ЭВОЛЮЦИОННОГО АЛГОРИТМА ДЛЯ ЗАДАЧИ РАССТАНОВКИ ФЕРЗЕЙ"

**Репозиторий с исходным кодом:** [Репозиторий](https://github.com/Punctuality/Evolutionary_Algorithm_ITMO_2023)

## Описания выполнения:

1. Выбор языка, фреймворка и подхода к решению задачи
2. Определение внутренней структуры кандидата
3. Имплементация инициализации популяции
4. Имплементация операции кроссинговера
5. Имплементация операции мутации
6. Настройка модуля прогона по сетке
7. Поиск оптимальных параметров алгоритма
8. Итоговые результаты работы алгоритма
9. Ответы на поставленные вопросы

## Выбор языка, фреймворка и подхода к решению задачи

В данной лабораторной работе я решил использовать язык `Scala`, вместо `Java` по следующим причинам:

1. Это язык на котором я пишу больше всего, соответственно знаю я его лучше остальных.
2. `Scala` - `JVM` язык, а значит не будет проблем, во-первых использовать библиотеки написанные на `Java` и использованные в прошлых лабораторных, во-вторых использовать `JVM` инструменты для профилирования, в третьих то что можно будет портировать (учитывая low-level оптимизации) будет несложно сделать.
3. `Scala` - язык с очень мощной системой типов (что я люблю), что позволит мне избежать многих ошибок во время разработки, позволит сделать код более "удобным" для прогонов, а также я смогу использовать библиотеку контроля эффектов, что значительно облегчит процесс многопоточной разработки и достижения максимальной утилизации ресурсов процессора.
4. В `Scala` помимо, других оптимизаций, нас интересует то, как здесь работают примитивные типы данных, за счет которых достаточно часто получается и вовсе избежать автобоксинга, что в свою очередь значительно увеличивает производительность.

Фреймфорк я выбрал тот же самый, поскольку не надо было "перевыдумывать" тот велосипед что я уже сделал в прошлых лабораторных работах. Все что мне нужно было это добавить новый модуль с новым набором классов для данной задачи и переписать оставшиеся на идиоматичную `Scala`.

Из подходов к решению задачи я выбрал ГА, поскольку как показал быстренький ресерч, он дает хорошие результаты на данной задаче и тут больше всего настраиваемых параметров и вариантов по сравнению с другими подходами. Также я решил использовать подход "прогон по сетке" для настройки этих самых параметров алгоритма.

Также стоит заметить что, для проведения тестов я заменил стандартную виртуальную машину `JVM` на `GraalVM`, что по моим замерам дало прирост в производительности в 2-3.5 раза.

## Структура внутреннего представления кандидата
#### QueensSolution, QueensFitnessFunction

Внутреняя структура кандидата представлена в виде массива целочисленных значений, где каждая ячейка массива соответствует одному из ферзей в соответствующей колонке, а значение в ячейке - это номер строки в котором находится ферзь. Таким образом мы исключаем ситуации когда ферзи имеют конфликты по вертикали или горизонтали, экономим память, так как не храним пустые значения матрицы, используем максимально компактное представление в виде массива и можем переиспользовать операторы мутации и кроссовера из прошлых лабораторных работ, ведь это также задача на упорядочивание.

```scala
import cats.Show

// A solution is an array of integers, where each integer represents the row
// Graphical example:
// queens = 1 0 3 2 5 4 7 6
//   0 1 2 3 4 5 6 7
// 0 . # . . . . . .
// 1 # . . . . . . .
// 2 . . . # . . . .
// 3 . . # . . . . .
// 4 . . . . . # . .
// 5 . . . . # . .
// 6 . . . . . . . #
// 7 . . . . . . # .

class QueensSolution(val queens: Array[Int]) extends AnyVal:

  @deprecated("Using more optimized version in fitness-function")
  def intersectionsOn(column: Int): Int = 
    val mainDiagCoef = queens(column) - column
    val subDiagCoef = queens(column) + column

    queens
      .iterator
      .zipWithIndex
      .map:      
        case (_, `column`) => 0
        case (r, c) => 
          (if (r - c == mainDiagCoef) 1 else 0) + (if (r + c == subDiagCoef) 1 else 0)
      .sum

  override def toString: String = s"QuennsSolution(${queens.mkString(", ")})"

object QueensSolution:
// Show instance for custom display in linear form
  given Show[QueensSolution] with
    override def show(solution: QueensSolution): String = 
      solution.queens.zipWithIndex.map((r, idx) => s"$idx->$r").mkString("[", ", ", "]")

// Show instance for custom display in matrix form
  object MatrixLike:
    given Show[QueensSolution] with
      override def show(solution: QueensSolution): String = 
        val queens = solution.queens
        val dimension = queens.length
        val matrix = Array.fill(dimension, dimension)('.')
        queens.zipWithIndex.foreach((r, c) => matrix(r)(c) = '#')
        matrix.map(_.mkString(" ")).mkString("\n")
```

Фитнес функция для данной задачи является `isNatural=false` поскольку считает количество конфликтов, а не их отсутствие. Такой подход дает более гранулярную оценку состояния системы. Вычисление изначально было простое - суммируем количество конфликтов по каждому из ферзей. Также проверяем что в кандидате присутствуют все ферзи и не более, иначе это некорректное решение. Однако данный подход имеет сложность в `O(N^2)` что дает значительную накладку по времени при больших размерностях.

Для улучшения производительности я решил использовать 32-битное поразрядное дерево  для хранения коэффициентов главной и побочной диагоналей, что позволило уменьшить сложность до `O(N * log32(N))`, что в данной задаче с используемыми размерностями близко к `O(N)`. Такой подход позволил ускорить вычислния в **50 раз** при размерности в 200. 

Правда оптимизацию я использовал только на последнем этапе когда выполнял последний прогон, поэтому только там актуальная информация о производительности алгоритма текущей версии.

```scala
class QueensFitnessFunction extends FitnessEvaluator[QueensSolution]:

  private def countDiagonals(queens: Array[Int]): (IntMap[Int], IntMap[Int]) =
    queens
      .iterator
      .zipWithIndex
      .map:
         (r, c) => (r - c) -> (r + c)
      .foldLeft(IntMap.empty[Int], IntMap.empty[Int]):
        case ((mainDiags, subDiags), (main, sub)) =>
            mainDiags.updated(main, mainDiags.getOrElse(main, 0) + 1) ->
              subDiags.updated(sub, subDiags.getOrElse(sub, 0) + 1)

  override def getFitness(candidate: QueensSolution, population: util.List[_ <: QueensSolution]): Double =
    if candidate.queens.sum != candidate.queens.length * (candidate.queens.length - 1) / 2 then
      throw new IllegalArgumentException(s"Invalid solution: ${candidate.show}")
    else
      
      val (mainDiags, subDiags) = countDiagonals(candidate.queens)
      candidate
        .queens
        .iterator
        .zipWithIndex
        .foldLeft(0):
          case (acc, (r, c)) => 
            val main = r - c
            val sub = r + c
            acc + (mainDiags(main) - 1) + (subDiags(sub) - 1)
        .toDouble
  
  override val isNatural: Boolean = false
```

###  Имплементация инициализации популяции 
#### TspFactory

Начальная популяция инициализируется в случайном порядке:

```scala
object ArrayUtil:
  extension [T](arr: Array[T])
    def swap(idx1: Int, idx2: Int): Unit =
      val tmp = arr(idx1)
      arr(idx1) = arr(idx2)
      arr(idx2) = tmp

    def scramble(rng: Random): Unit =
      for i <- arr.indices.reverse.dropRight(1) do
        val idx = rng.nextInt(i + 1)
        arr.swap(idx, i)

class QueensFactory(dimension: Int) extends AbstractCandidateFactory[QueensSolution]:
  override def generateRandomCandidate(rng: Random): QueensSolution = 
    val queens = Array.range(0, dimension)
    queens.scramble(rng)
    QueensSolution(queens)
```

###  Имплементация операции кроссинговера
#### QueensCrossover

Операция кроссинговера выполнена как и в прошлой лабораторной работе в виде *ordered crossover*. При этом, теперь в качестве параметра указывается не максимальная длинна сегмента для обмена, а максимальный процент от общей размерности. Предпологается что таким образом параметр будет более универсальным и не будет зависеть от размерности задачи.

```scala
class QueensCrossover(maxLenSwappedProc: Int, crossoverProbability: Double) 
  extends AbstractCrossover[QueensSolution](1, Probability(crossoverProbability)):

  private def orderCrossover(from: Array[Int], to: Array[Int], rng: util.Random): Array[Int] =
    val toNew = to.clone

    val maxLenSwapped = (maxLenSwappedProc * from.length / 100.0).ceil.toInt
    val swapSegmentLen = rng.nextInt(maxLenSwapped) + 1
    val swapPoint = rng.nextInt(from.length - swapSegmentLen)
    val swapSegment = from.slice(swapPoint, swapPoint + swapSegmentLen)

    System.arraycopy(swapSegment, 0, toNew, swapPoint, swapSegmentLen)
    Arrays.sort(swapSegment)

    var j = 0
    var jumped = false
    for i <- 0 until to.length do
      if j >= swapPoint && !jumped then
          j += swapSegmentLen
          jumped = true
      if Arrays.binarySearch(swapSegment, to(i)) < 0 then
        toNew(j) = to(i)
        j += 1
    end for

    toNew

  override def mate(
    parent1: QueensSolution,
    parent2: QueensSolution,
    numberOfCrossoverPoints: Int,
    rng: util.Random
  ): util.List[QueensSolution] = 
    if parent1.queens.length != parent2.queens.length then
      throw new IllegalStateException("Input candidates cannot have different dimensions on crossover")

    var child1 = parent1.queens
    var child2 = parent2.queens
    Range(0, numberOfCrossoverPoints).foreach: _ =>
      val tmp = orderCrossover(child1, child2, rng)
      child1 = orderCrossover(child2, child1, rng)
      child2 = tmp

    ImmutableList.of(QueensSolution(child1), QueensSolution(child2))
```

###  Имплементация операции мутации
#### QueensMutation

Все операторы мутации из прошлой лабораторной работы были также сохранены и перенесены на `Scala`. Также был добавлен новый оператор мутации - `LocalSwap`, суть которого в локальной оптимизации: выбирается случайная точка и другая но в некотором малом радиусе от нее для перестановки. Предпологается что таким образом получится меньше ломать уже существующее хорошее решение и только улучшать его понемногу.

```scala
class QueensMutation(
  mutationType: QueensMutation.MutationType,
  mutationProbability: Double
) extends EvolutionaryOperator[QueensSolution]:
  import QueensMutation.MutationType.*

  private def mutate(candidate: QueensSolution, rng: Random): QueensSolution = 
    val newSolution = candidate.queens.clone()
    mutationType match
      case Swap => 
        val idx1 = rng.nextInt(candidate.queens.length)
        val idx2 = rng.nextInt(candidate.queens.length)
        newSolution.swap(idx1, idx2)

      case LocalSwap(maxRadius) => 
        val idx1 = rng.nextInt(candidate.queens.length)
        val radius = rng.nextInt(maxRadius) + 1
        val idx2 = (idx1 + radius) % candidate.queens.length
        newSolution.swap(idx1, idx2)

      case Insertion => 
        val idx1 = rng.nextInt(candidate.queens.length - 1)
        var idx2 = rng.nextInt(candidate.queens.length)
        while (idx1 == idx2)
          idx2 = rng.nextInt(candidate.queens.length)

        val (left, right) = (min(idx1, idx2), max(idx1, idx2))

        newSolution(left + 1) = candidate.queens(right)
        System.arraycopy(
          candidate.queens,
          left + 1,
          newSolution,
          left + 2,
          right - left - 1
        )

      case Scramble(maxLen) => 
        val scrambleSegmentLen = rng.nextInt(maxLen) + 1
        val scrambleStart = rng.nextInt(candidate.queens.length - scrambleSegmentLen)
        val scrambleSection = newSolution.slice(scrambleStart, scrambleStart + scrambleSegmentLen)
        scrambleSection.scramble(rng)

        System.arraycopy(scrambleSection, 0, newSolution, scrambleStart, scrambleSegmentLen)

      case Inversion(maxLen) => 
        val inversionSegmentLen = rng.nextInt(maxLen) + 1
        val inversionStart = rng.nextInt(candidate.queens.length - inversionSegmentLen)
        val inversionSection = newSolution.slice(inversionStart, inversionStart + inversionSegmentLen).reverse

        System.arraycopy(inversionSection, 0, newSolution, inversionStart, inversionSegmentLen)
    end match

    QueensSolution(newSolution)
    

  override def apply(selectedCandidates: util.List[QueensSolution], rng: Random): util.List[QueensSolution] = 
    val result = new util.ArrayList[QueensSolution](selectedCandidates.size)
    Range(0, selectedCandidates.size())
      .foreach: idx => 
        selectedCandidates.get(idx) match
          case candidate if rng.nextDouble() < mutationProbability => 
            result add mutate(candidate, rng)
          case same => result add same
    
    result

object QueensMutation:
  enum MutationType:
    case Swap
    case LocalSwap(maxRadius: Int)
    case Insertion
    case Scramble(maxLen: Int)
    case Inversion(maxLen: Int)
```

### Настройка модуля прогона по сетке
#### QueensSetup, QueensResult, QuuensExperimentRunner, GridEvaluator

В данном случае был перписан написанный мною модуль поиска по сетке из прошлой лабораторной работы. Изменения с прошлого раза:
1. Добавлена новая удобная возможность конфигурации эксперимента с прокеркой корректности настройки в *compilte-time*, за счет использования системы типов и гетерогенных структур данных.

```scala
object HeterogeneousCartisian:
  trait TupleCartesianProduct[IT <: Tuple, OT <: Tuple]:
      def apply(cc: IT): List[OT]

  given tupleCartesianProductEmpty: TupleCartesianProduct[EmptyTuple, EmptyTuple] with
    def apply(cc: EmptyTuple): List[EmptyTuple] = List(EmptyTuple)

  given tupleCartesianProductCons[EH, ET <: Tuple, IT <: Tuple](
    using tailProduct: TupleCartesianProduct[IT, ET]
  ): TupleCartesianProduct[List[EH] *: IT, EH *: ET] with
    def apply(t: List[EH] *: IT): List[EH *: ET] =
      val (head, tail) = (t.head, t.tail)
      for
        hElem <- head
        tElem <- tailProduct(tail)
      yield hElem *: tElem

  def heterogeneousCartisianProduct[IT <: Tuple, OT <: Tuple](
    variables: IT
  )(using cartProduct: TupleCartesianProduct[IT, OT]): List[OT] = cartProduct(variables)

  extension [IT <: Tuple, OT <: Tuple](variables: IT)(using cartProduct: TupleCartesianProduct[IT, OT])
    def product: List[OT] = heterogeneousCartisianProduct(variables)
```

Данный алгоритм, с некоторой "магией типов" (хочу заметить что никакой *runtime reflection* тут нету) будет производить декартово произведение параметров без потери информации о типах. Таким образом, мы можем собрать все параметры эксперимента в одну структуру данных, а затем пройтись по ней и запустить эксперименты. При этом, если мы забудем указать какой-то параметр, то компилятор нас об этом предупредит.

2. Выполнения экспериментов теперь происходит не просто на пуле потоков, а внутри системы контроля эффектов, которые в свою очередь выполняются на нитках, которые эффективно распределены на пуле потоков.

```scala
object Racing:
  def raceManyEffects[
    F[_]: Spawn: Monad,
    CC[_]: Traverse: MonoidK: Applicative, R
  ](effects: CC[F[R]]): F[CC[R]] =
    effects
      .traverse(_.start) // параллельный запуск нескольких попыток тут
        .flatMap: 
          _.foldLeftM(MonoidK[CC].empty[R]):
            case (acc, fiber) =>
              fiber
                .joinWith(CancellationException("Race many pipeline Canceled").raiseError[F, R])
                .map(result => MonoidK[CC].combineK(acc, Applicative[CC].pure(result)))

  extension [F[_]: Spawn: Monad, CC[_]: Traverse: MonoidK: Applicative, R](effects: CC[F[R]])
    def raceMany: F[CC[R]] = raceManyEffects(effects)

// QueensExperimentRunner.scala
// OMITTED

Iterator
    .fill(setup.repeats)(params)
    .map(runExperiment.tupled)
    .toList
    .raceMany

// OMMITTED
```

```scala
// OMMITED
val finished = Ref.unsafe[F, Int](0)
Stream
    .emits(setups.map(ms.fromProduct(_)))
    .zipWithIndex
    .parEvalMapUnbounded( (params, idx) => // параллельное выполнение экспериментов тут
        for
        results <- experimentRunner.run(params).map(params -> _)
        count <- finished.updateAndGet(1.+)
        _ <- Console[F].println(s"$count/${setups.length}: Finished experiment #$idx with params: ${params.show}")
        yield results
    )
    .map(rowLogger.tupled)
            
// OMMITED
```

3. Все операции ввода-вывода теперь вызываются в блокирующих потоках, чтобы не тормозить выполнение.

```scala
// OMMITTED
Monad[F].pure(variables.product)
    .flatTap:
    setups => Console[F].println(s"Running ${setups.length} experiments")
    
// OMMITTED

for
    results <- experimentRunner.run(params).map(params -> _)
    count <- finished.updateAndGet(1.+)
    _ <- Console[F].println(s"$count/${setups.length}: Finished experiment #$idx with params: ${params.show}")
yield results

// OMMITED

.through(Files[F].writeAll(
outputFile.toPath,
StandardOpenOption.CREATE :: Nil
))
```

4. Появилась поддержка выбора алгоритма селлекции, движка эволюции и множественных операторов мутации.

```scala
// OMMITTED

val operators: List[EvolutionaryOperator[QueensSolution]] = 
    QueensCrossover(setup.crossoverMaxLenProc, setup.crossoverProb) :: 
    setup.mutations.map:
        case (mutationType, mutationProb) => 
        QueensMutation(mutationType, mutationProb)

val pipeline = EvolutionPipeline(operators.asJava)

val selection: SelectionStrategy[Any] = setup.selectionType match
    case QueensSetup.SelectionType.RouletteWheel => RouletteWheelSelection()
    case QueensSetup.SelectionType.Tournament(prob) => TournamentSelection(Probability(prob))
    case QueensSetup.SelectionType.StochasticUniversalSampling => StochasticUniversalSampling()
    case QueensSetup.SelectionType.Rank => RankSelection()
    case QueensSetup.SelectionType.Truncation(ratio) => TruncationSelection(ratio)

val algorithm = setup.engineType match
    case EngineType.SteadyState => 
    SteadyStateEvolutionEngine(
        factory,
        pipeline,
        evaluator,
        selection,
        setup.populationSize,
        false,
        random
    )
    case EngineType.Generational =>
    GenerationalEvolutionEngine(
        factory,
        pipeline,
        evaluator,
        selection,
        random
    )

algorithm.setSingleThreaded(true) 
// Нужно чтобы не было дополнительных переключений контекста
// К тому же фитнес-фунция в данной задаче - достаточно быстро исполняется

// OMMITTED
```


### Поиск оптимальных параметров алгоритма

В данной случае если перебирать примерно все потенциально подходящие варианты алгоритма получается 46 тысяч различных комбинаций, поэтому было принято перебирать в несколько заходом по группе параметров, тем самом пологая что локальная оптимизация гиперпараметров будет коррелировать с глобальной. Группы параметров и результаты их прогона:

1. Кроссовер (crossovers.csv) = Лучшим оказался вариант на секции с максимальной длинной в 10% и вероятностью 10%
2. Мутации (mutations_#.csv) = Лучший вариант - мутация перестановкой (SWAP) с вероятностью 40%
3. Селекция (selections_#.csv) = Лучший вариант - truncate селекция с выборов 50% лучших особей
4. Движок эволюции (engines.csv) = Лучший вариант - GenerationalEvolutionEngine

Все эксперименты проводились по 20 раз, значения усреднялись. Убирались результаты не достигшие идеального решения и выбирался лучший параметр по совокупности значения среднего кол-ва поколений до завершения и среднего кол-ва итераций до достижения идеального решения.

### Итоговые результаты работы алгоритма

В итоге был проведен прогон при разных параметрах размерности, кол-ва поколений и размера популяции. Результаты усреднялись по 10 запускам. Данные результаты предоставлены ниже:

| Dimension | PopulationSize | Generations | Crossover MaxLen | Crossover Prob | Mutations | SelectionType   | EngineType   | Best result | Mean best result | Mean first achieved iter | Mean finished eval at | Mean eval ime |
| :-------- | :------------- | :---------- | :--------------- | :------------- | :-------- | :-------------- | :----------- | :---------- | :--------------- | :----------------------- | :-------------------- | :------------ |
| 8         | 10             | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 24.0                     | 39.9                  | 56.7          |
| 8         | 10             | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 7.0                      | 54.7                  | 59.1          |
| 8         | 100            | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 6.0                      | 2.0                   | 50.2          |
| 8         | 100            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 2.0                      | 4.2                   | 57.4          |
| 8         | 80             | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 5.0                      | 4.0                   | 54.4          |
| 8         | 80             | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 0.0                      | 3.9                   | 53.3          |
| 8         | 500            | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 2.0                      | 0.5                   | 63.6          |
| 8         | 500            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 0.0                      | 0.5                   | 60.0          |
| 50        | 10             | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 6.0         | 7.8              | 95.1                     | 87.1                  | 139.9         |
| 50        | 10             | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 1225.0                   | 1109.6                | 421.2         |
| 50        | 100            | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.8              | 77.4                     | 98.3                  | 494.9         |
| 50        | 100            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 173.0                    | 176.8                 | 741.6         |
| 50        | 80             | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.6              | 94.5                     | 99.1                  | 358.9         |
| 50        | 80             | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 186.0                    | 213.1                 | 856.4         |
| 50        | 500            | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.2              | 88.3                     | 97.2                  | 1616.0        |
| 50        | 500            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 79.0                     | 99.2                  | 1707.7        |
| 70        | 10             | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 16.0        | 16.6             | 89.0                     | 100.0                 | 46.4          |
| 70        | 10             | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 785.0                    | 1239.1                | 596.6         |
| 70        | 100            | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 4.0         | 4.0              | 96.0                     | 100.0                 | 429.6         |
| 70        | 100            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 149.0                    | 343.2                 | 1390.5        |
| 70        | 80             | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 6.0         | 7.2              | 87.4                     | 97.5                  | 345.3         |
| 70        | 80             | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 171.0                    | 307.9                 | 943.9         |
| 70        | 500            | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 2.0         | 2.2              | 98.2                     | 98.6                  | 2203.8        |
| 70        | 500            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 142.0                    | 197.1                 | 4029.2        |
| 200       | 10             | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 96.0        | 106.6            | 97.3                     | 99.9                  | 175.5         |
| 200       | 10             | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 2670.0                   | 4860.2                | 8616.2        |
| 200       | 100            | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 82.0        | 82.0             | 96.0                     | 98.8                  | 1732.6        |
| 200       | 100            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 518.0                    | 1074.2                | 17485.2       |
| 200       | 80             | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 80.0        | 82.2             | 97.3                     | 100.0                 | 1344.4        |
| 200       | 80             | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 889.0                    | 849.8                 | 11200.8       |
| 200       | 500            | 100         | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 72.0        | 76.0             | 96.0                     | 100.0                 | 8402.2        |
| 200       | 500            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 497.0                    | 521.3                 | 33024.6       |

Из интересных наблюдений стоит отметить:
1. Благодаря все оптимизациям и архитектурным решениям описанным выше, получается достичнуть отличной скорости работы алгоритма.
2. При выбранном подходе, время потраченное на разработку кода лабораторной работы (а это много), время потраченное на поиск лучшего (из моих) решения занял совсем чуть-чуть.

### Ответы на вопросы

1. Является ли задача оптимизационной или ограаниченной?

Оптимизационные задачи и ограниченные задачи относятся к разным аспектам одной и той же проблемы. Вот основные различия между ними:

        a. Оптимизационная задача- это задача, в которой требуется найти наилучшее решение из всех возможных решений, удовлетворяющих заданным условиям. Оптимизационные задачи фокусируются на минимизации или максимизации целевой функции.

        b. Ограниченная задача - это оптимизационная задача с дополнительными условиями или ограничениями, которым должны удовлетворять все допустимые решения.

Задача о расстановке ферзей не является типичной оптимизационной задачей, поскольку нет целевой функции для минимизации или максимизации. Вместо этого задача состоит в том, чтобы найти конфигурацию ферзей, удовлетворяющую всем ограничениям (не должно быть пересечений по горизонтали, вертикали или диагонали), что делает её ограниченной задачей, так как из всего пространства решений не все подходят.

Однако, задачу во время решения можно преобразовать в оптимизационную задачу, введя целевую функцию, которая измеряет, насколько хорошо промежуточное решение удовлетворяет ограничениям. В нашем случае, насколько много таких ограничений решение нарушило.

2. Как растет сложность задачи при увеличении размерности?

После оптимизауий алгоритмическая сложность подсчета фитнес-функции = `O(N * log32(N))`. Однако эта сложность не является сложностью всего алгоритма. Поэтому был проведен дополнительный прогон (`complexity_trend.csv`) для определения сложности всего алгоритма.

Результаты прогона:

| Dimension | PopulationSize | Generations | Crossover MaxLen | Crossover Prob | Mutations | SelectionType   | EngineType   | Best result | Mean best result | Mean first achieved iter | Mean finished eval at | Mean eval ime |
| :-------- | :------------- | :---------- | :--------------- | :------------- | :-------- | :-------------- | :----------- | :---------- | :--------------- | :----------------------- | :-------------------- | :------------ |
| 8         | 100            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 1.0                      | 2.2                   | 43.6          |
| 20        | 100            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 40.0                     | 59.3                  | 240.2         |
| 50        | 100            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 98.0                     | 167.4                 | 792.5         |
| 70        | 100            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 169.0                    | 233.1                 | 1130.1        |
| 90        | 100            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 430.0                    | 481.1                 | 2734.4        |
| 110       | 100            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 391.0                    | 638.8                 | 4552.3        |
| 130       | 100            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 855.0                    | 673.0                 | 6523.9        |
| 160       | 100            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 422.0                    | 827.9                 | 10002.0       |
| 200       | 100            | 10000       | 10               | 0.1            | Swap[40%] | Truncation(0.5) | Generational | 0.0         | 0.0              | 1056.0                   | 814.6                 | 12230.1       |

![time_trend](https://github.com/Punctuality/Evolutionary_Algorithm_ITMO_2023/blob/main/Lab_5/images/time_trend_graph.png?raw=true)

Как видно из построенной аппроксимации, примерная сложность алгоритма - `O(N^2 + N)` (описывает лучше чем просто N^2).