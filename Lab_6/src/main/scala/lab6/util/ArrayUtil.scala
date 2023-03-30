package lab6.util

import java.util.Random

object ArrayUtil:
  extension [T](arr: Array[T])
    def swap(idx: Int, other: Array[T]): Unit =
      val tmp = arr(idx)
      arr(idx) = other(idx)
      other(idx) = tmp

    def swap(idx1: Int, idx2: Int): Unit =
      val tmp = arr(idx1)
      arr(idx1) = arr(idx2)
      arr(idx2) = tmp

    def scramble(rng: Random): Unit =
      for i <- arr.indices.reverse.dropRight(1) do
        val idx = rng.nextInt(i + 1)
        arr.swap(idx, i)