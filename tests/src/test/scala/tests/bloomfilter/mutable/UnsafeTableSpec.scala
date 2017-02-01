package tests.bloomfilter.mutable

import bloomfilter.mutable.UnsafeTable
import org.scalacheck.Test.Parameters
import org.scalacheck.commands.Commands
import org.scalacheck.{Gen, Prop, Properties}

class UnsafeTableSpec extends Properties("UnsafeTableSpec") {

  property("insert") = new UnsafeTableCommands().property()

  override def overrideParameters(p: Parameters): Parameters = {
    super.overrideParameters(p).withMinSuccessfulTests(1000)
  }

  class UnsafeTableCommands extends Commands {
    type Sut = UnsafeTable

    case class State(size: Long)

    override def canCreateNewSut(
        newState: State,
        initSuts: Traversable[State],
        runningSuts: Traversable[Sut]): Boolean =
      initSuts.isEmpty && runningSuts.isEmpty

    override def destroySut(sut: Sut): Unit =
      sut.dispose()

    override def genInitialState: Gen[State] =
      Gen.chooseNum[Long](1, /*Int.MaxValue * 2L*/ 1000).map(State)

    override def newSut(state: State): Sut =
      new UnsafeTable(state.size, 8)

    def initialPreCondition(state: State): Boolean = true

    def genCommand(state: State): Gen[Command] =
      for {
        index <- Gen.choose[Long](0, state.size)
        tag <- Gen.choose[Byte](Byte.MinValue, Byte.MaxValue)
      } yield commandSequence(SetItem(index, tag), GetItem(index, tag))


    case class SetItem(index: Long, tag: Long) extends UnitCommand {
      def run(sut: Sut): Unit = sut.synchronized(sut.insert(index, tag, kickout = false))
      def nextState(state: State): State = state
      def preCondition(state: State) = true
      def postCondition(state: State, success: Boolean): Prop = success
    }

    case class GetItem(index: Long, tag: Long) extends SuccessCommand {
      type Result = Boolean
      def run(sut: Sut): Boolean = sut.synchronized(sut.find(index, 0, tag))
      def nextState(state: State): State = state
      def preCondition(state: State) = true
      def postCondition(state: State, result: Boolean): Prop = result
    }

  }

}

