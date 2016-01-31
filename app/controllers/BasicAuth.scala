package controllers

import play.api.mvc._
import scala.concurrent.Future
import models.User
//http://owainlewis.com/articles/play-framework-api-authentication
class AuthenticatedRequest[A](val user: String, request: Request[A]) 
  extends WrappedRequest[A](request)

object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {
  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
    request.headers.get("Authorization") flatMap { authHeader =>
      val (email, pass) = decodeBasicAuth(authHeader)
      if (User.authenticate(email, pass))
        User.findOneByEmail(email) map { user =>
          block(new AuthenticatedRequest(user, request))
        }
      else None
    } getOrElse unauthorized
  }

  private val unauthorized =
    Future.successful(
      Results.Unauthorized
        .withHeaders("WWW-Authenticate" -> "Basic realm=Unauthorized"))

  private [this] def decodeBasicAuth(authHeader: String): (String, String) = {
    val baStr = authHeader.replaceFirst("Basic ", "")
    val decoded = new sun.misc.BASE64Decoder().decodeBuffer(baStr)
    val Array(email, password) = new String(decoded).split(":")
    (email, password)
  }
}