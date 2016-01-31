package utils

import play.api.mvc.{RequestHeader, AnyContent, Request}
import models.Meta
import models.User
import java.util.Date
import play.api.libs.json._
import play.api.libs.Codecs

object Utils {
  
  def generateETAG(jsValue: String): String = {
        // Use LastModified for digest 
        // A better way to do it should be use all fields involved in the document and created a digest
        
        "W/\"" + Codecs.sha1(jsValue.toString) + "\""
        
  }
  
  def generateUUID() : String = java.util.UUID.randomUUID.toString
  
  def getISO88FormatedDate = {
      
  }
  
  def secure(request: RequestHeader): Boolean = {
    request.secure || request.headers.get("x-forwarded-proto").getOrElse("").contains("https")
  }

  def baseURL(request: RequestHeader): String = {
    var proto = "http"

    if (secure(request)) {
      proto += "s"
    }
    // return base URL
    proto + "://" + request.host + "/v1/"
  }
  
  def resourceNotFoundMessage(id: String): JsObject = {
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