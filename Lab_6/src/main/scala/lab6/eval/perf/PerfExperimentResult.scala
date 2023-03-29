package lab6.eval.perf

import lab6.evoalgo.MyCandidate

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
