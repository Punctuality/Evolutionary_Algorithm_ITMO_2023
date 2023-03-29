package lab6.eval.perf

import scala.annotation.migration
import cats.Show

final case class PerfExperimentSetup(
  dimension: Int,
  populationSize: Int,
  generations: Int,
  algoType: PerfExperimentSetup.AlgoType,
  complexity: Int
)

object PerfExperimentSetup:
  enum AlgoType:
    case SingleThreaded
    case MasterSlave
    case Islands(islandCount: Int, epochLength: Int, migrationCount: Int):
      override def toString(): String = 
        s"Islands($islandCount)"

  given Show[PerfExperimentSetup] with
    def show(setup: PerfExperimentSetup): String = 
      s"PerfExperimentSetup(dimension=${setup.dimension}, populationSize=${setup.populationSize}, generations=${setup.generations}, algoType=${setup.algoType}, complexity=${setup.complexity})"