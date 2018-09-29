package com.niuwa.streaming

import akka.actor.ActorSystem
import akka.util.Timeout
import com.niuwa.streaming.TestActor.CreateView

import scala.concurrent.duration._

object AkkaSparkSQL {
  private implicit val timeout = Timeout(2 seconds)
  private implicit val system = ActorSystem("LocalSystem")

  def main(args: Array[String]): Unit = {

    val remoteDb = system.actorSelection(s"akka.tcp://testSystem@192.168.100.230:2552/user/actor1")
    val remoteDb2 = system.actorSelection(s"akka.tcp://testSystem@192.168.100.230:2552/user/actor2")
    remoteDb ! CreateView(3)
    remoteDb ! "select * from test1"
    remoteDb ! "select numavg(id) from test1"

//    remoteDb2 ! CreateView(10)
//    remoteDb2 ! "select * from test1"
//    remoteDb ! "select * from rds.p2p_dictionary"


  }
}

