package utils

import play.api.mvc.{RequestHeader, AnyContent, Request}
import models.Meta
import models.User
import java.util.Date
import play.api.libs.json._

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

  def ETag(implicit request: RequestHeader): Option[String] = {

    None
  }
  
  // add Location to response Header
  def addLocation(): Boolean = {
      true
  }
  
  def addMetaData(resourceName: String, id:String, meta: Option[Meta],  request: RequestHeader): Option[Meta] = {
    val finalMeta: Meta = meta match {
            case Some(m) => {
              val location = RequestUtils.baseURL(request) + resourceName + "/" + id
              val version = RequestUtils.ETag(request)
              Meta(m.created, m.lastModified, version, Some(location))
            }
            case None => {
              val location = RequestUtils.baseURL(request)  + resourceName +"/" + id
              val version = RequestUtils.ETag(request) 
              Meta(new Date, new Date, version, Some(location))
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