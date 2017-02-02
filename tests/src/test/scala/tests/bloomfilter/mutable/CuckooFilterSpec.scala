package tests.bloomfilter.mutable

import bloomfilter.CanGenerateHashFrom
import bloomfilter.mutable.CuckooFilter
import org.scalacheck.Test.Parameters
import org.scalacheck.commands.Commands
import org.scalacheck.{Arbitrary, Gen, Prop, Properties}

class CuckooFilterSpec extends Properties("CuckooFilter") {

  property("for Long") = new CuckooFilterCommands[Long].property()
  property("for String") = new CuckooFilterCommands[String].property()
  property("for Array[Byte]") = new CuckooFilterCommands[Array[Byte]].property()


  override def overrideParameters(p: Parameters): Parameters = {
    super.overrideParameters(p)//.withMinSuccessfulTests(1000)
  }

  class CuckooFilterCommands[T: Arbitrary](implicit canGenerateHash: CanGenerateHashFrom[T]) extends Commands {
    type Sut = CuckooFilter[T]

    case class State(expectedItems: Long, addedItems: Long)

    override def canCreateNewSut(
        newState: State,
        initSuts: Traversable[State],
        runningSuts: Traversable[Sut]): Boolean = {
      initSuts.isEmpty && runningSuts.isEmpty
    }

    override def destroySut(sut: Sut): Unit =
      sut.dispose()

    override def genInitialState: Gen[State] =
      Gen.chooseNum[Long](1, 100000).map(State(_, 0))

    override def newSut(state: State): Sut =
      CuckooFilter[T](state.expectedItems)

    def initialPreCondition(state: State): Boolean = true

    def genCommand(state: State): Gen[Command] =
      for {
        item <- Arbitrary.arbitrary[T]
      } yield commandSequence(AddItem(item), CheckItem(item), RemoveItem(item))

    case class AddItem(item: T) extends UnitCommand {
      def run(sut: Sut): Unit = sut.synchronized(sut.add(item))
      def nextState(state: State): State = state.copy(addedItems = state.addedItems + 1)
      def preCondition(state: State): Boolean = state.addedItems < state.expectedItems
      def postCondition(state: State, success: Boolean): Prop = success
    }

    case class RemoveItem(item: T) extends SuccessCommand {
      type Result = Boolean
      def run(sut: Sut): Boolean = sut.synchronized{
        sut.remove(item)
        !sut.mightContain(item)
      }
      def nextState(state: State): State = state.copy(addedItems = state.addedItems - 1)
      def preCondition(state: State): Boolean = state.addedItems < state.expectedItems
      def postCondition(state: State, success: Boolean): Prop = success
    }

    case class CheckItem(item: T) extends SuccessCommand {
      type Result = Boolean
      def run(sut: Sut): Boolean = sut.synchronized(sut.mightContain(item))
      def nextState(state: State): State = state
      def preCondition(state: State): Boolean = state.addedItems < state.expectedItems
      def postCondition(state: State, result: Boolean): Prop = result
    }

  }

}
