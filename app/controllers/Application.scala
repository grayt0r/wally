package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

import models._

object Application extends Controller {
  
  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def ws = WebSocket.async[JsValue] { request =>
    EventStream.register
  }

  def webhooks = Action(parse.json) { request =>
    val event = (request.body \ "event").as[String]
    val ip = (request.body \ "ip").as[Option[String]]
    
    val (lon, lat) = ((request.body \ "lon").as[Option[Float]], (request.body \ "lat").as[Option[Float]]) match {
      case (l1: Some[Float], l2: Some[Float]) => (l1.get, l2.get)
      case _ => {
        GeoIP.locate(ip getOrElse "") match {
          case (l1: Float, l2: Float) => (l1, l2)
          // TODO: Uncommenting means webhooks needs a result type - how?
          //case _ => return BadRequest("Longitude and Latitude are both required")
        }
      }
    }
    
    EventStream.fire(event, lon, lat)
    Ok
  } 
  
}