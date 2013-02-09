package models

import com.maxmind.geoip._

object GeoIP {
  val lookupService = new LookupService("data/GeoLiteCity.dat", LookupService.GEOIP_MEMORY_CACHE)

  def locate(ip: String): (Float, Float) = {
    lookupService.getLocation(ip) match {
      case l: Location => (l.longitude, l.latitude)
      case _ => null
    }
  }
  
}