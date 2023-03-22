package lab5.eval

trait ExperimentRunner[F[_], S, R]:
  def run(setup: S): F[R]