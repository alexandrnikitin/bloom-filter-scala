package tests.bloomfilter.mutable

import bloomfilter.CanGenerateHashFrom
import bloomfilter.mutable.BloomFilter
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop.forAll
import org.scalacheck.Test.Parameters
import org.scalacheck.commands.Commands
import org.scalacheck.{Arbitrary, Gen, Prop, Properties}

class BloomFilterSpec extends Properties("BloomFilter") {

  property("for Long") = new BloomFilterCommands[Long].property()
  property("for String") = new BloomFilterCommands[String].property()
  property("for Array[Byte]") = new BloomFilterCommands[Array[Byte]].property()


  override def overrideParameters(p: Parameters): Parameters = {
    super.overrideParameters(p).withMinSuccessfulTests(1000)
  }

  class BloomFilterCommands[T: Arbitrary](implicit canGenerateHash: CanGenerateHashFrom[T]) extends Commands {
    type Sut = BloomFilter[T]

    case class State(expectedItems: Long, addedItems: Long)

    override def canCreateNewSut(
        newState: State,
        initSuts: Traversable[State],
        runningSuts: Traversable[Sut]): Boolean = {
      initSuts.isEmpty && runningSuts.isEmpty ||
          newState.addedItems > newState.expectedItems ||
          newState.addedItems > 100
    }

    override def destroySut(sut: Sut): Unit =
      sut.dispose()

    override def genInitialState: Gen[State] =
      Gen.chooseNum[Long](1, Int.MaxValue).map(State(_, 0))

    override def newSut(state: State): Sut =
      BloomFilter[T](state.expectedItems, 0.01)

    def initialPreCondition(state: State): Boolean = true

    def genCommand(state: State): Gen[Command] =
      for {
        item <- Arbitrary.arbitrary[T]
      } yield commandSequence(AddItem(item), CheckItem(item))

    case class AddItem(item: T) extends UnitCommand {
      def run(sut: Sut): Unit = sut.synchronized(sut.add(item))
      def nextState(state: State) = state.copy(addedItems = state.addedItems + 1)
      def preCondition(state: State) = true
      def postCondition(state: State, success: Boolean) = success
    }

    case class CheckItem(item: T) extends SuccessCommand {
      type Result = Boolean
      def run(sut: Sut): Boolean = sut.synchronized(sut.mightContain(item))
      def nextState(state: State) = state
      def preCondition(state: State) = true
      def postCondition(state: State, result: Boolean): Prop = result
    }

  }

  private val elemsToAddGen = for {
    numberOfElemsToAdd <- Gen.chooseNum[Int](1, 1000)
    elemsToAdd <- Gen.listOfN(numberOfElemsToAdd, arbitrary[Long])
  } yield elemsToAdd

  // TODO fix elemsToAddGen.filter() below, why Gen.listOfN above generates empty lists?
  property("approximateElementCount") = forAll(elemsToAddGen.filter(x => x.size > 10 && x.toSet.size > 10)) { elemsToAdd: List[Long] =>
    val bf = BloomFilter[Long](elemsToAdd.size * 10, 0.0001)
    elemsToAdd.foreach(bf.add)
    val numberOfUnique = elemsToAdd.toSet.size
    math.abs(bf.approximateElementCount() - numberOfUnique) < numberOfUnique * 0.1
  }

}
