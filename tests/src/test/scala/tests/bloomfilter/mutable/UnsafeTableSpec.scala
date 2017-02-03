package tests.bloomfilter.mutable

import bloomfilter.mutable.UnsafeTable8Bit
import org.scalacheck.Test.Parameters
import org.scalacheck.commands.Commands
import org.scalacheck.{Gen, Prop, Properties}

class UnsafeTableSpec extends Properties("UnsafeTableSpec") {

  property("writeTag & readTag") = new UnsafeTableCommands().property()

  // TODO Sometimes fails when trying to add 5 elements to one bucket. It fails correctly. It shouldn't add 5 elemns. Scalacheck issue? Investigate
  property("insert & find") = new UnsafeTableInsertFindCommands().property()

  override def overrideParameters(p: Parameters): Parameters = {
    super.overrideParameters(p).withMinSuccessfulTests(1000)
  }

  class UnsafeTableCommands extends Commands {
    type Sut = UnsafeTable8Bit

    case class State(numberOfBuckets: Long, addedItems: Long)

    override def canCreateNewSut(
        newState: State,
        initSuts: Traversable[State],
        runningSuts: Traversable[Sut]): Boolean =
      (initSuts.isEmpty && runningSuts.isEmpty) ||
          newState.addedItems >= newState.numberOfBuckets || newState.addedItems >= 4


    override def destroySut(sut: Sut): Unit =
      sut.dispose()

    override def genInitialState: Gen[State] =
      Gen.chooseNum[Long](1, /*Int.MaxValue * 2L*/ 1000).map(State(_, 0))

    override def newSut(state: State): Sut =
      new UnsafeTable8Bit(state.numberOfBuckets)

    def initialPreCondition(state: State): Boolean = true

    def genCommand(state: State): Gen[Command] =
      for {
        index <- Gen.choose[Long](0, state.numberOfBuckets - 1)
        tagIndex <- Gen.choose[Int](0, 3)
        tag <- Gen.choose[Byte](0, Byte.MaxValue)
      } yield commandSequence(WriteTag(index, tagIndex, tag), ReadTag(index, tagIndex, tag))

    case class WriteTag(index: Long, tagIndex:Int, tag: Byte) extends UnitCommand {
      def run(sut: Sut): Unit = sut.synchronized(sut.writeTag(index, tagIndex, tag))
      def nextState(state: State): State =  state.copy(addedItems = state.addedItems + 1)
      def preCondition(state: State): Boolean = state.addedItems < state.numberOfBuckets || state.addedItems < 4
      def postCondition(state: State, success: Boolean): Prop = success
    }

    case class ReadTag(index: Long, tagIndex:Int, tag: Byte) extends SuccessCommand {
      type Result = Boolean
      def run(sut: Sut): Boolean = sut.synchronized(sut.readTag(index, tagIndex) == tag)
      def nextState(state: State): State = state
      def preCondition(state: State): Boolean = state.addedItems < state.numberOfBuckets || state.addedItems < 4
      def postCondition(state: State, result: Boolean): Prop = result
    }

  }

  class UnsafeTableInsertFindCommands extends Commands {
    type Sut = UnsafeTable8Bit

    case class State(numberOfBuckets: Long, addedItems: Long)

    override def canCreateNewSut(
        newState: State,
        initSuts: Traversable[State],
        runningSuts: Traversable[Sut]): Boolean =
      (initSuts.isEmpty && runningSuts.isEmpty) ||
          newState.addedItems >= newState.numberOfBuckets || newState.addedItems >= 4

    override def destroySut(sut: Sut): Unit =
      sut.dispose()

    override def genInitialState: Gen[State] =
      Gen.chooseNum[Long](1, /*Int.MaxValue * 2L*/ 1000).map(State(_, 0))

    override def newSut(state: State): Sut =
      new UnsafeTable8Bit(state.numberOfBuckets)

    def initialPreCondition(state: State): Boolean = true

    def genCommand(state: State): Gen[Command] =
      for {
        index <- Gen.choose[Long](0, state.numberOfBuckets - 1)
        tag <- Gen.choose[Byte](0, Byte.MaxValue)
      } yield commandSequence(Insert(index, tag), Find(index, tag))

    case class Insert(index: Long, tag: Byte) extends UnitCommand {
      def run(sut: Sut): Unit = sut.synchronized(sut.insert(index, tag))
      def nextState(state: State): State =  state.copy(addedItems = state.addedItems + 1)
      def preCondition(state: State): Boolean = state.addedItems < state.numberOfBuckets || state.addedItems < 4
      def postCondition(state: State, success: Boolean): Prop = success
    }

    case class Find(index: Long, tag: Byte) extends SuccessCommand {
      type Result = Boolean
      def run(sut: Sut): Boolean = sut.synchronized(sut.find(index, tag))
      def nextState(state: State): State = state
      def preCondition(state: State): Boolean = state.addedItems < state.numberOfBuckets || state.addedItems < 4
      def postCondition(state: State, result: Boolean): Prop = result
    }

  }

}
