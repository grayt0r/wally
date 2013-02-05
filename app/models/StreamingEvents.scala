package models

import akka.actor._
import scala.concurrent.duration._

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

object EventStream {
  implicit val timeout = Timeout(1 second)
  lazy val default = Akka.system.actorOf(Props[EventStream])

  def register:scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {
    (default ? Register()).map {
      case Connected(enumerator) => 
        val iteratee = Iteratee.ignore[JsValue]
        (iteratee,enumerator)
    }
  }
}

class EventStream extends Actor {
  val (streamEnumerator, streamChannel) = Concurrent.broadcast[JsValue]

  def receive = {
    case Register() => {
      sender ! Connected(streamEnumerator)
      self ! FireEvent("test", 1, 2)
    }

    case FireEvent(event, lon, lat) => {
      notifyAll(event, lon, lat)
    }
  }
  
  def notifyAll(event: String, lon: Long, lat: Long) {
    val msg = JsObject(
      Seq(
        "event" -> JsString(event),
        "lon" -> JsNumber(lon),
        "lat" -> JsNumber(lat)
      )
    )
    streamChannel.push(msg)
  }
  
}

case class Register()
case class FireEvent(event: String, lon: Long, lat: Long)
case class Connected(enumerator:Enumerator[JsValue])