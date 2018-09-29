package com.niuwa.bigdata.controller

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import akka.pattern.ask
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn

object WebServer3 {

  case class Bid(userId: String, offer: Int)
  case object GetBids
  case class Bids(bids: List[Bid])

  class Auction extends Actor with ActorLogging {
    var bids = List.empty[Bid]

    override def receive: Receive = {
      case bid @ Bid(userId, offer) =>
        bids = bids :+ bid
        log.info(s"Bid complete: $userId, $offer")
      case GetBids => sender() ! Bids(bids)
      case _ => log.info("Invalid message")
    }
  }

  // 来自 spray-json
  implicit val bidFormat = jsonFormat2(Bid)
  implicit val bidsFormat = jsonFormat1(Bids)

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // 这个在最后的 future flatMap/onComplete 里面会用到
    implicit val executionContext = system.dispatcher

    val auction = system.actorOf(Props[Auction], "acution")

    val route =
      path("auction") {
        put {
          parameter("bid".as[Int], "user") { (bid, user) =>
            auction ! Bid(user, bid)
            complete((StatusCodes.Accepted, "bid placed"))
          }
        }
      } ~
        get {
          implicit val timeout: Timeout = 5.second

          // query the actor for the current auction status
          val bids: Future[Bids] = (auction ? GetBids).mapTo[Bids]
          complete(bids)
        }

        val bindingFuture = Http().bindAndHandle(route, "192.168.101.162", 8080)
        println(s"Server online at http://192.168.101.162:8080/\nPress RETURN to stop...")
        StdIn.readLine()
        bindingFuture
          .flatMap(_.unbind())
          .onComplete( _ => system.terminate())


  }
}
