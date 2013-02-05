package controllers

import play.api._
import play.api.mvc._

import play.api.libs.json._

import java.io.IOException

import com.maxmind.geoip._

import models._

object Application extends Controller {
  
  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def location(ip: String) = Action {
    val cl = new LookupService("data/GeoLiteCity.dat", LookupService.GEOIP_MEMORY_CACHE)
    val l1 = cl.getLocation(ip)

    val json = Json.obj("countryCode" -> l1.countryCode, "city" -> l1.city, "latitude" -> l1.latitude, "longitude" -> l1.longitude)
    val jsonStr = Json.stringify(json)

    cl.close()

    Ok(json)
  }

  def ws = WebSocket.async[JsValue] { request =>
    EventStream.register
  }

  def webhooks = Action(parse.json) { request =>
    val event = (request.body \ "event").as[String]
    val lon = (request.body \ "lon").as[Float]
    val lat = (request.body \ "lat").as[Float]

    EventStream.fire(event, lon, lat)

    Ok("BOOM") 
  } 
  
}