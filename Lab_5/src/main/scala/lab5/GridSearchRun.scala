package lab5

import java.io.File
import lab5.eval.*
import lab5.eval.queens.*
import lab5.evoalgo.*
import cats.syntax.all.*
import cats.effect.*
import java.util.Random

import scala.deriving._
import scala.compiletime.*
import lab5.eval.queens.QueensSetup.*
import cats.effect.unsafe.IORuntimeConfig
import scala.concurrent.duration.Duration

object GridSearchRun extends IOApp:

  override protected def runtimeConfig: IORuntimeConfig =
    super.runtimeConfig.copy(cpuStarvationCheckInitialDelay = Duration.Inf)

  def run(args: List[String]): IO[ExitCode] = 
    
    given Random = new Random()

    val evaluator = QueensFitnessFunction()
    val experimentRunner: ExperimentRunner[IO, QueensSetup, List[QueensResult]] =
      QueensExperimentRunner(evaluator, 2)

    val headers = List(
      "Dimension",
      "PopulationSize",
      "Generations",
      "Crossover MaxLen",
      "Crossover Prob",
      "Mutations",
      "SelectionType",
      "EngineType",
      "Best result",
      "Mean best result",
      "Mean first achieved iter",
      "Mean finished eval at",
      "Mean eval ime"
    )

    import Numeric.Implicits.*
    extension [N: Numeric](n: List[N])
      def mean: Double = Math.round(n.sum.toDouble / n.size * 100.0) / 100.0

    def toCSVRow(params: QueensSetup, results: List[QueensResult]): List[String] = 
      val bestResult = results.min
      List(
        params.dimension,
        params.populationSize,
        params.generations,
        params.crossoverMaxLenProc,
        params.crossoverProb,
        params.mutations.map((mt, prob) => s"$mt[${(prob * 100).toInt}%]").mkString("+"),
        params.selectionType,
        params.engineType,
        bestResult.bestResult,
        results.map(_.bestResult).mean,
        results.map(_.firstAchievedIter).mean,
        results.map(_.finishedEvalAt).mean,
        results.map(_.evalTime).mean
      ).map(_.toString())

    val gridEvaluator: GridEvaluator[IO, QueensSetup, List[QueensResult]] =
      GridEvaluator(experimentRunner, headers, toCSVRow)

    val dimensions: List[Int] = List(
      8,
      50,
      70,
      200
      )
    val populationSizes: List[Int] = List(
      10,
      100,
      80,
      500
      )
    val generations: List[Int] = List(
      100,
      10000
      )
    val crossoverMaxLens: List[Int] = List(
      // 5,
      10, // crossovers: best
      // 50
      )
    val crossoverProbs: List[Double] = List(
      // 0.05,
      0.1, // crossovers: best
      // 0.5
      )
    val mutations: List[List[(QueensMutation.MutationType, Double)]] = List(
      // List((QueensMutation.MutationType.LocalSwap(1), 0.1)),
      // List((QueensMutation.MutationType.LocalSwap(1), 0.5)),
      // List((QueensMutation.MutationType.LocalSwap(1), 0.9)),
      // List((QueensMutation.MutationType.Swap, 0.1)),
      // List((QueensMutation.MutationType.Swap, 0.3)),
      List((QueensMutation.MutationType.Swap, 0.4)), // mutations, mutations_2: best
      // List((QueensMutation.MutationType.Swap, 0.5)),
      // List((QueensMutation.MutationType.Swap, 0.6)),
      // List((QueensMutation.MutationType.Swap, 0.7)),
      // List((QueensMutation.MutationType.Swap, 0.9)),
      // List((QueensMutation.MutationType.Insertion, 0.1)),
      // List((QueensMutation.MutationType.Insertion, 0.5)),
      // List((QueensMutation.MutationType.Insertion, 0.9)),
      // List((QueensMutation.MutationType.Swap, 0.1), (QueensMutation.MutationType.Inversion(5), 0.1)),
      // List((QueensMutation.MutationType.Swap, 0.5), (QueensMutation.MutationType.Inversion(5), 0.5)),
      // List((QueensMutation.MutationType.Swap, 0.9), (QueensMutation.MutationType.Inversion(5), 0.9)),
      // List((QueensMutation.MutationType.Inversion(5), 0.1)),
      // List((QueensMutation.MutationType.Inversion(5), 0.5)),
      // List((QueensMutation.MutationType.Inversion(5), 0.9)),
      // List((QueensMutation.MutationType.Scramble(5), 0.1)),
      // List((QueensMutation.MutationType.Scramble(5), 0.5)),
      // List((QueensMutation.MutationType.Scramble(5), 0.9))
    )
    val selectionTypes: List[SelectionType] = List(
      // QueensSetup.SelectionType.RouletteWheel,
      // QueensSetup.SelectionType.StochasticUniversalSampling,
      // QueensSetup.SelectionType.Rank,
      // QueensSetup.SelectionType.Tournament(0.6),
      // QueensSetup.SelectionType.Tournament(0.9),
      // QueensSetup.SelectionType.Truncation(0.3),
      // QueensSetup.SelectionType.Truncation(0.4),
      QueensSetup.SelectionType.Truncation(0.5), // selections, selections_2, selections_3, selections_4: best
      // QueensSetup.SelectionType.Truncation(0.6),
      // QueensSetup.SelectionType.Truncation(0.7),
      // QueensSetup.SelectionType.Truncation(0.9)
    )
    val engineTypes: List[EngineType] = List(
      // QueensSetup.EngineType.SteadyState,
      QueensSetup.EngineType.Generational // engines: best
    )
    val repeats = 10

    val variables = (
        dimensions,
        populationSizes,
        generations,
        crossoverMaxLens,
        crossoverProbs,
        mutations,
        selectionTypes,
        engineTypes,
        List(repeats)
      )
      
    val outputFile = File("results/general.csv")
    gridEvaluator.run(
      outputFile,
      variables
    ).as(ExitCode.Success)