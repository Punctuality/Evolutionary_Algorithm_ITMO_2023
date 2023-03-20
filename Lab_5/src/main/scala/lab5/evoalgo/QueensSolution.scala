package lab5.evoalgo

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
  given Show[QueensSolution] with
    override def show(solution: QueensSolution): String = 
      solution.queens.zipWithIndex.map((r, idx) => s"$idx->$r").mkString("[", ", ", "]")
