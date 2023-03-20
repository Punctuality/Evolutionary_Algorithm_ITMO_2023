package lab5.eval

import org.uncommons.watchmaker.framework.{PopulationData, TerminationCondition}

class CombinedTerminationCondition(
  combineType: CombinedTerminationCondition.CombineType,
  terminationCondition: TerminationCondition*
) extends TerminationCondition:
  override def shouldTerminate(populationData: PopulationData[_]): Boolean = 
    combineType match
      case CombinedTerminationCondition.CombineType.OR =>
        terminationCondition.exists(_.shouldTerminate(populationData))
      case CombinedTerminationCondition.CombineType.AND =>
        terminationCondition.forall(_.shouldTerminate(populationData))

object CombinedTerminationCondition:
  enum CombineType:
    case AND, OR