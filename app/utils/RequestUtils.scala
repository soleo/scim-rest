package utils

import play.api.mvc.{RequestHeader, AnyContent, Request}

object RequestUtils {
  def secure(implicit request: RequestHeader): Boolean = {
    request.secure || request.headers.get("x-forwarded-proto").getOrElse("").contains("https")
  }

  def baseURL(implicit request: RequestHeader): String = {
    var proto = "http"

    if (RequestUtils.secure) {
      proto += "s"
    }
    // return base URL
    proto + "://" + request.host + "/v1/"
  }

  def ETag(implicit request: RequestHeader): Option[String] = {

    None
  }
}