package utils

import play.api.mvc.{RequestHeader, AnyContent, Request}
import models.Meta
import models.User
import java.util.Date
import play.api.libs.json._
import play.api.libs.Codecs

object RequestUtils {
  def secure(request: RequestHeader): Boolean = {
    request.secure || request.headers.get("x-forwarded-proto").getOrElse("").contains("https")
  }

  def baseURL(request: RequestHeader): String = {
    var proto = "http"

    if (RequestUtils.secure(request)) {
      proto += "s"
    }
    // return base URL
    proto + "://" + request.host + "/v1/"
  }
  
  def generateETAG(meta: Meta): Option[String] = {
        // Use LastModified for digest
        val lastModified = meta.lastModified
        Some("W/\"" + Codecs.sha1(lastModified.toString) + "\"")
  }
  
  // add Location to response Header
  def getLocation(meta: Option[Meta]): String = {
        meta match {
            case Some(m) => {
                m.location.getOrElse("")
            } 
            case None => {
                ""
            }
        }
     
  }
  
  def addMetaData(resourceName: String, id:String, meta: Option[Meta],  request: RequestHeader): Option[Meta] = {
    val finalMeta: Meta = meta match {
            case Some(m) => {
              val location = RequestUtils.baseURL(request) + resourceName + "/" + id
              val version = RequestUtils.generateETAG(m)
              Meta(m.created, m.lastModified, version, Some(location))
            }
            case None => {
              val location = RequestUtils.baseURL(request)  + resourceName +"/" + id
              val today = new Date
              val version = RequestUtils.generateETAG(Meta(today, today, None, None)) 
              Meta(today, today, version, Some(location))
            }
    }
          
    Some(finalMeta)
  }
  
  
  def notFoundMessage(id: String): JsObject = {
      Json.obj(
          "Errors" -> Json.arr(
            Json.obj(
              "description" -> JsString("Resource " + id +" not found"),
              "code" -> 404  	  
            )
          )
      )
  }
}