import akka.actor.{ActorSystem, Props}
import com.niuwa.streaming.TestActor
import com.niuwa.streaming.TestActor.CreateView
import org.apache.spark.SparkConf


object Test {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("testAkka")/*.setMaster("local[2]")*/
    val actorSystem = ActorSystem("testSystem")
    val actor1 = actorSystem.actorOf(Props(new TestActor(sparkConf)), "actor1")
//    actor1 ! CreateView(1)
  }

}
