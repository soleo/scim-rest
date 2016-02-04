package utils

import play.api.mvc.{RequestHeader, AnyContent, Request}
import play.api.libs.json._
import play.api.libs.Codecs

import models.Meta
import models.User

object Utils {
  
    def generateETAG(wholeDoc: String): String = {
        // use all fields involved in the document and created a digest
        "W/\"" + Codecs.sha1(wholeDoc.toString) + "\""
    }
  
    def generateUUID() : String = java.util.UUID.randomUUID.toString

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
  
    def isEmpty(s: String): Boolean = {
        
        if (s.length == 0)  true else false
    }
    
    def removeEmptyString(s: Option[String]) : Option[String] = {
        s match{
            case Some(str) => 
                if(Utils.isEmpty(str)) None else Some(str)
            case None => None
        }    
    }
    
    def optionalString(s: String): Option[String] = {
        if(!Utils.isEmpty(s)) Some(s) else None
    }
    
    def optionalBoolean(bool: Boolean): Option[Boolean] = {
        if(bool) Some(true) else None
    }
}