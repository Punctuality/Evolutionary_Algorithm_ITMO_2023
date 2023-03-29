package lab6.evoalgo

import org.uncommons.watchmaker.framework.EvolutionaryOperator
import java.{util => ju}

class MyMutation(minBound: Double, maxBound: Double, maxDiff: Double, changeProb: Double) extends EvolutionaryOperator[MyCandidate]:

  private inline def mutateOneElem(elem: Double)(using rng: ju.Random): Double =
    val diff = rng.nextDouble() * maxDiff
    val sign = if rng.nextBoolean() then 1 else -1
    val newElem = elem + diff * sign
    if newElem < minBound then minBound
    else if newElem > maxBound then maxBound
    else newElem

  private def mutate(previous: MyCandidate)(using rng: ju.Random): MyCandidate =
    var wasCloned = false
    Iterator
      .range(0, previous.length)
      .map(_ -> (rng.nextDouble() <= changeProb)).foldLeft(previous):
        case (candidate, (idx, true)) if !wasCloned => 
          wasCloned = true
          val cloned = candidate.clone
          cloned(idx) = mutateOneElem(candidate(idx))
          cloned
        case (candidate, (idx, true)) => 
          candidate(idx) = mutateOneElem(candidate(idx)); candidate
        case (candidate, (_, false)) => candidate
    

  override def apply(selectedCandidates: ju.List[MyCandidate], rng: ju.Random): ju.List[MyCandidate] = 
    given ju.Random = rng
    val mutated = ju.ArrayList[MyCandidate](selectedCandidates.size())
    selectedCandidates.forEach(candidate => mutated.add(mutate(candidate)))
    mutated
