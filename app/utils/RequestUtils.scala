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
  
  def addMetaData(user:User, request: RequestHeader): Option[Meta] = {
    val meta: Meta = user.meta match {
            case Some(m) => {
              val location = RequestUtils.baseURL(request) + "Users/" + user.id
              val version = RequestUtils.ETag(request)
              Meta(m.created, m.lastModified, version, Some(location))
            }
            case None => {
              val location = RequestUtils.baseURL(request)  + "Users/" + user.id
              val version = RequestUtils.ETag(request) 
              Meta(new Date, new Date, version, Some(location))
            }
    }
          
    Some(meta)
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