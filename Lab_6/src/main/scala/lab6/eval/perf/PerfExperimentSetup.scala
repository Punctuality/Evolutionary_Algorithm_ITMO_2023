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
    case Islands(islandCount: Int, epochLength: Int, migrationCount: Int)

  given Show[AlgoType] with
    def show(algoType: AlgoType): String = algoType match
      case AlgoType.SingleThreaded => "SingleThreaded"
      case AlgoType.MasterSlave => "MasterSlave"
      case AlgoType.Islands(islandCount, epochLength, migrationCount) => 
        s"Islands($islandCount)"

  given Show[PerfExperimentSetup] with
    def show(setup: PerfExperimentSetup): String = 
      s"PerfExperimentSetup(dimension=${setup.dimension}, populationSize=${setup.populationSize}, generations=${setup.generations}, algoType=${setup.algoType}, complexity=${setup.complexity})"