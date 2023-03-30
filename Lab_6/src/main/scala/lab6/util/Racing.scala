package lab6.util

import cats.*
import cats.syntax.all.*
import cats.effect.*
import cats.effect.syntax.all.*
import java.util.concurrent.CancellationException

object Racing:
  def raceManyEffects[
    F[_]: Spawn: Monad,
    CC[_]: Traverse: MonoidK: Applicative, R
  ](effects: CC[F[R]]): F[CC[R]] =
    effects
      .traverse(_.start)
        .flatMap: 
          _.foldLeftM(MonoidK[CC].empty[R]):
            case (acc, fiber) =>
              fiber
                .joinWith(CancellationException("Race many pipeline Canceled").raiseError[F, R])
                .map(result => MonoidK[CC].combineK(acc, Applicative[CC].pure(result)))

  extension [F[_]: Spawn: Monad, CC[_]: Traverse: MonoidK: Applicative, R](effects: CC[F[R]])
    def raceMany: F[CC[R]] = raceManyEffects(effects)
