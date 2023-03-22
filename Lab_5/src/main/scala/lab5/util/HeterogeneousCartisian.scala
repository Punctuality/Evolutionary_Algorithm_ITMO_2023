package lab5.util

object HeterogeneousCartisian:
  trait TupleCartesianProduct[IT <: Tuple, OT <: Tuple]:
      def apply(cc: IT): List[OT]

  given tupleCartesianProductEmpty: TupleCartesianProduct[EmptyTuple, EmptyTuple] with
    def apply(cc: EmptyTuple): List[EmptyTuple] = List(EmptyTuple)

  given tupleCartesianProductCons[EH, ET <: Tuple, IT <: Tuple](
    using tailProduct: TupleCartesianProduct[IT, ET]
  ): TupleCartesianProduct[List[EH] *: IT, EH *: ET] with
    def apply(t: List[EH] *: IT): List[EH *: ET] =
      val (head, tail) = (t.head, t.tail)
      for
        hElem <- head
        tElem <- tailProduct(tail)
      yield hElem *: tElem

  def heterogeneousCartisianProduct[IT <: Tuple, OT <: Tuple](
    variables: IT
  )(using cartProduct: TupleCartesianProduct[IT, OT]): List[OT] = cartProduct(variables)

  extension [IT <: Tuple, OT <: Tuple](variables: IT)(using cartProduct: TupleCartesianProduct[IT, OT])
    def product: List[OT] = heterogeneousCartisianProduct(variables)

