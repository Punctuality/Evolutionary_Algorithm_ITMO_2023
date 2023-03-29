package lab6

import cats.effect.*
import cats.effect.unsafe.IORuntimeConfig

import java.{util => ju}
import scala.concurrent.duration.*

import lab6.eval.perf.*
import lab6.eval.GridEvaluator
import java.io.File

object ExperimentRun extends IOApp:
  override protected val computeWorkerThreadCount: Int = 2

  override protected def runtimeConfig: IORuntimeConfig =
      super.runtimeConfig.copy(cpuStarvationCheckInitialDelay = Duration.Inf)

  override def run(args: List[String]): IO[ExitCode] = 
    
    given ju.Random = new ju.Random(42)

    val experimentRunner = PerfExperimentRunner[IO](repeats = 10)

    val headers = List(
          "Dimension",
          "PopulationSize",
          "Generations",
          "EngineType",
          "Complexity",
          "Best result",
          "Mean best result",
          "Mean first achieved iter",
          "Mean finished eval at",
          "Mean eval ime"
        )

    import Numeric.Implicits.*
    extension [N: Numeric](n: List[N])
      def mean: Double = Math.round(n.sum.toDouble / n.size * 100.0) / 100.0

    def toCSVRow(params: PerfExperimentSetup, results: List[PerfExperimentResult]): List[String] = 
      val bestResult: PerfExperimentResult = results.min
      List(
        params.dimension,
        params.populationSize,
        params.generations,
        params.algoType.toString(),
        params.complexity,
        bestResult.bestResult,
        results.map(_.bestResult).mean,
        results.map(_.firstAchievedIter).mean,
        results.map(_.finishedEvalAt).mean,
        results.map(_.evalTime).mean
      ).map(_.toString())

    val gridEvaluator = GridEvaluator[IO, PerfExperimentSetup, List[PerfExperimentResult]](
      experimentRunner,
      headers,
      toCSVRow
    )

    val dimensions = List(2, 5, 10)

    val populationSizes = List(10, 50, 100)

    val generations = List(100, 200, 500, 1000)

    val algoTypes = List(
      PerfExperimentSetup.AlgoType.SingleThreaded,
      PerfExperimentSetup.AlgoType.MasterSlave,
      PerfExperimentSetup.AlgoType.Islands(islandCount = 2, epochLength = 100, migrationCount = 1),
      PerfExperimentSetup.AlgoType.Islands(islandCount = 8, epochLength = 100, migrationCount = 1),
    )

    val complexities = List(0, 1, 2, 3, 4, 5)

    val variables = (
      dimensions,
      populationSizes,
      generations,
      algoTypes,
      complexities
    )

    val outputFile = File("results/general.csv")
    gridEvaluator.run(
      outputFile,
      variables
    ).as(ExitCode.Success)
