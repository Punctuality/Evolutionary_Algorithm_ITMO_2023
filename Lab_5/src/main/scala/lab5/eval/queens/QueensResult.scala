package lab5.eval.queens

import lab5.evoalgo.QueensSolution

final case class QueensResult(
  bestResult: Double,
  bestCandidate: QueensSolution,
  firstAchievedIter: Int,
  finishedEvalAt: Int,
  evalTime: Long
) extends Ordered[QueensResult]:

  extension (prev: Int)
    private infix def ifZero(next: => Int): Int = 
      if prev == 0 then next else prev

  override def compare(that: QueensResult): Int = 
    this.bestResult.compare(that.bestResult) ifZero
      this.finishedEvalAt.compare(that.finishedEvalAt) ifZero
      this.firstAchievedIter.compare(that.firstAchievedIter) ifZero
      this.evalTime.compare(that.evalTime)