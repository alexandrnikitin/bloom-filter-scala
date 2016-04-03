package bloomfilter.mutable.tests

import bloomfilter.mutable.BloomFilter
import org.scalacheck.commands.Commands
import org.scalacheck.{Gen, Prop, Properties}

class BloomFilterSpec extends Properties("BloomFilter") {

  property("mightContain") = BloomFilterCommands.property()

  object BloomFilterCommands extends Commands {
    type Sut = BloomFilter[Long]

    case class State(expectedItems: Long)

    override def canCreateNewSut(
        newState: State,
        initSuts: Traversable[State],
        runningSuts: Traversable[Sut]): Boolean = {
      initSuts.isEmpty && runningSuts.isEmpty
    }

    override def destroySut(sut: Sut): Unit =
      sut.dispose()

    override def genInitialState: Gen[State] =
      Gen.chooseNum[Long](1, Int.MaxValue).map(State)

    override def newSut(state: State): Sut =
      BloomFilter[Long](state.expectedItems, 0.01)

    def initialPreCondition(state: State): Boolean = true

    def genCommand(state: State): Gen[Command] =
      for {
        item <- Gen.choose[Long](Long.MinValue, Long.MaxValue)
      } yield commandSequence(AddItem(item), CheckItem(item))

    case class AddItem(item: Long) extends UnitCommand {
      def run(sut: Sut): Unit = sut.synchronized(sut.add(item))
      def nextState(state: State) = state
      def preCondition(state: State) = true
      def postCondition(state: State, success: Boolean) = success
    }

    case class CheckItem(item: Long) extends SuccessCommand {
      type Result = Boolean
      def run(sut: Sut): Boolean = sut.synchronized(sut.mightContain(item))
      def nextState(state: State) = state
      def preCondition(state: State) = true
      def postCondition(state: State, result: Boolean): Prop = result
    }

  }

}
