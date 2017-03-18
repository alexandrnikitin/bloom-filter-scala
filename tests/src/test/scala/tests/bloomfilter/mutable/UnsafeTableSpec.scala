package tests.bloomfilter.mutable

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import bloomfilter.mutable.UnsafeTable8Bit
import org.scalacheck.Test.Parameters
import org.scalacheck.commands.Commands
import org.scalacheck.{Arbitrary, Gen, Prop, Properties}
import org.scalatest.{Matchers, PrivateMethodTester}

class UnsafeTableSpec extends Properties("UnsafeTableSpec") with Matchers with PrivateMethodTester{

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

  property("supports java serialization") = {
    val gen = for{
      numBuckets <- Gen.posNum[Int]
      numPopulated <- Gen.choose(0, numBuckets)
      m <- Gen.mapOfN(numPopulated, Gen.zip(Gen.choose(0, numBuckets - 1), Arbitrary.arbByte.arbitrary))
    } yield {
      numBuckets -> m
    }
    val ptrAccessor = PrivateMethod[Long]('ptr)
    def ptrOf( unsaffeTable8Bit : UnsafeTable8Bit) = unsaffeTable8Bit invokePrivate ptrAccessor()
    Prop.forAllNoShrink(gen){ case (numBuckets, tags) =>
        val sut = new UnsafeTable8Bit(numberOfBuckets = numBuckets)
        try {
          for {
            (idx, tag) <- tags
          } {
            sut.insert(idx, tag)
          }
          val bos = new ByteArrayOutputStream
          val oos = new ObjectOutputStream((bos))
          oos.writeObject(sut)
          oos.close()
          val bis = new ByteArrayInputStream(bos.toByteArray)
          val ois = new ObjectInputStream(bis)
          val deserialized = ois.readObject()
          ois.close()

          deserialized should not be null
          deserialized should be (a[UnsafeTable8Bit])
          val sut2 = deserialized.asInstanceOf[UnsafeTable8Bit]
          ptrOf(sut2) should not be 0
          ptrOf(sut2) should not equal ptrOf(sut)
          try {
            for{
              idx <- 0 until numBuckets
              tagIdx <- 0 until UnsafeTable8Bit.TagsPerBucket
            }{
              sut.readTag(idx, tagIdx) shouldEqual sut2.readTag(idx, tagIdx)
            }
            Prop.passed
          } finally sut2.dispose()
        } finally sut.dispose()
    }
  }

}
