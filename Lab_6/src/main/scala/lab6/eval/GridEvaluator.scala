package lab6.eval

import java.io.File
import scala.deriving.Mirror

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import cats.effect.syntax.all.*
import fs2.io.file.*
import fs2.*
import java.nio.file.StandardOpenOption
import lab6.util.HeterogeneousCartisian.*
import lab6.eval.ExperimentRunner

class GridEvaluator[F[_]: Async: Files: Console, S: Show, R](
  experimentRunner: ExperimentRunner[F, S, R],
  headers: List[String],
  rowLogger: (S, R) => List[String]
):
  def run[T <: Tuple, CT <: Tuple](outputFile: File, variables: T)(using
    tcp: TupleCartesianProduct[T, CT],
    ms: Mirror.ProductOf[S],
    mct: Mirror.ProductOf[CT] { type MirroredElemTypes = ms.MirroredElemTypes }
  ): F[Unit] = 
    Monad[F].pure(variables.product)
      .flatTap:
        setups => Console[F].println(s"Running ${setups.length} experiments")
      .flatMap: setups =>
        val finished = Ref.unsafe[F, Int](0)
        (Stream(headers) ++
          Stream
            .emits(setups.map(ms.fromProduct(_)))
            .zipWithIndex
            .parEvalMapUnbounded( (params, idx) =>
              for
                results <- experimentRunner.run(params).map(params -> _)
                count <- finished.updateAndGet(1.+)
                _ <- Console[F].println(s"$count/${setups.length}: Finished experiment #$idx with params: ${params.show}")
              yield results
            )
            .map(rowLogger.tupled)
          )
          .map(_.mkString(","))
          .intersperse("\n")
          .through(text.utf8Encode)
          .through(Files[F].writeAll(
            outputFile.toPath,
            StandardOpenOption.CREATE :: Nil
          ))
          .compile
          .drain
  