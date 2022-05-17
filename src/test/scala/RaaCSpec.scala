import cats.implicits._
import cats.data.NonEmptyList
import mx.cinvestav.domain.replica
class RaaCSpec extends munit .CatsEffectSuite {

  test("RaaC"){
    val r0Wh0 = replica.who.Node(id = "sn-1")

    val r0 = replica.Replica(
      who = r0Wh0, 
      what = replica.what.Files(
        fs = NonEmptyList.of(
          replica.what.File(filename = "f1",size=19,digest = None),
          replica.what.File(filename = "f2",size=10,digest = None),
        )
      ),
      where = replica.where.StorageNodeSubset(
        ids   = Set("sn-2","sn-3"),
        order = None
      ),
      how = replica.how.Context(
        replicationTechnique = replica.how.ReplicationTechnique.Active,
        replicationTransferType = replica.how.ReplicationTransferType.Push
      ), 
      when = replica.when.Reactive
    )

  }

}
