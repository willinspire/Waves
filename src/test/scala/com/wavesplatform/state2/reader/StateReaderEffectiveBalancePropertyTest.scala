package com.wavesplatform.state2.reader

import com.wavesplatform.state2.diffs.{ENOUGH_AMT, assertDiffAndState}
import com.wavesplatform.{NoShrink, TransactionGen, WithDB}
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, PropSpec}
import scorex.lagonaki.mocks.TestBlock
import scorex.transaction.GenesisTransaction


class StateReaderEffectiveBalancePropertyTest extends PropSpec
  with PropertyChecks with Matchers with TransactionGen with NoShrink with WithDB {
  val setup: Gen[(GenesisTransaction, Int, Int, Int)] = for {
    master <- accountGen
    ts <- positiveIntGen
    genesis: GenesisTransaction = GenesisTransaction.create(master, ENOUGH_AMT, ts).right.get
    emptyBlocksAmt <- Gen.choose(1, 10)
    atHeight <- Gen.choose(0, 20)
    confirmations <- Gen.choose(1, 20)
  } yield (genesis, emptyBlocksAmt, atHeight, confirmations)


  property("No-interactions genesis account's effectiveBalance doesn't depend on depths") {
    forAll(setup) { case ((genesis: GenesisTransaction, emptyBlocksAmt, atHeight, confirmations)) =>
      val genesisBlock = TestBlock.create(Seq(genesis))
      val nextBlocks = List.fill(emptyBlocksAmt - 1)(TestBlock.create(Seq.empty))
      assertDiffAndState(db, genesisBlock +: nextBlocks, TestBlock.create(Seq.empty)) { (_, newState) =>
        newState.effectiveBalanceAtHeightWithConfirmations(genesis.recipient, atHeight, confirmations).get shouldBe genesis.amount
      }
    }
  }
}