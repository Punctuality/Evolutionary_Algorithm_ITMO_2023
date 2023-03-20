package lab5

import lab5.evoalgo.*
import cats.implicits.*

object Main extends App:
  val s = QueensSolution(Array(1, 0, 3, 2, 5, 4, 7, 6))
  val ff = QueensFitnessFunction()

  println(s"Solution: $s\n(${s.show})")
  println(s"Sum on 2 column: ${s.intersectionsOn(2)}")
  println(s"Fitness on example: ${ff.getFitness(s, null)}")

