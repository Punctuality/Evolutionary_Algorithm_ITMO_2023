package lab5.eval.queens

import lab5.evoalgo.QueensMutation
import org.uncommons.watchmaker.framework.SelectionStrategy
import org.uncommons.watchmaker.framework.AbstractEvolutionEngine
import cats.Show

final case class QueensSetup(
  dimension: Int,
  populationSize: Int,
  generations: Int,
  crossoverMaxLenProc: Int,
  crossoverProb: Double,
  mutations: List[(QueensMutation.MutationType, Double)],
  selectionType: QueensSetup.SelectionType,
  engineType: QueensSetup.EngineType,
  repeats: Int
)

object QueensSetup:
  enum SelectionType:
    case RouletteWheel, StochasticUniversalSampling, Rank
    case Tournament(prob: Double)
    case Truncation(ratio: Double)

  enum EngineType:
    case SteadyState, Generational

  given Show[QueensSetup] with
    def show(setup: QueensSetup): String =
      import setup.*
      val ps = List(
        dimension,
        populationSize,
        generations,
        s"$crossoverMaxLenProc %",
        crossoverProb,
        mutations.map { case (t, p) => s"$t($p)" }.mkString("+"),
        selectionType,
        engineType,
        repeats
      )
      s"QueensSetup(${ps.mkString(", ")})"