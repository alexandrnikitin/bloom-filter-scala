package tests.bloomfilter.mutable

import bloomfilter.mutable.UnsafeBitArray
import org.scalacheck.Test.Parameters
import org.scalacheck.commands.Commands
import org.scalacheck.{Gen, Prop, Properties}

class UnsafeBitArraySpec extends Properties("UnsafeBitArray") {

  property("set & get") = new UnsafeBitArrayCommands().property()

  override def overrideParameters(p: Parameters): Parameters = {
    super.overrideParameters(p).withMinSuccessfulTests(1000)
  }

  class UnsafeBitArrayCommands extends Commands {
    type Sut = UnsafeBitArray

    case class State(size: Long)

    override def canCreateNewSut(
        newState: State,
        initSuts: Traversable[State],
        runningSuts: Traversable[Sut]): Boolean =
      initSuts.isEmpty && runningSuts.isEmpty

    override def destroySut(sut: Sut): Unit =
      sut.dispose()

    override def genInitialState: Gen[State] =
      Gen.chooseNum[Long](1, Int.MaxValue * 2L).map(State)

    override def newSut(state: State): Sut =
      new UnsafeBitArray(state.size)

    def initialPreCondition(state: State): Boolean = true

    def genCommand(state: State): Gen[Command] =
      for {
        i <- Gen.choose[Long](0, state.size - 1)
      } yield commandSequence(SetItem(i), GetItem(i))

    case class SetItem(i: Long) extends UnitCommand {
      def run(sut: Sut): Unit = sut.synchronized(sut.set(i))
      def nextState(state: State): State = state
      def preCondition(state: State) = true
      def postCondition(state: State, success: Boolean) = success
    }

    case class GetItem(i: Long) extends SuccessCommand {
      type Result = Boolean
      def run(sut: Sut): Boolean = sut.synchronized(sut.get(i))
      def nextState(state: State): State = state
      def preCondition(state: State) = true
      def postCondition(state: State, result: Boolean): Prop = result
    }

  }

}

