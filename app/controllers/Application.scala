package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current

class Application extends Controller {

  def index = Action {
    Ok(views.html.index(null))
  }

  def ping = Action {
    Ok("pong")
  }

}
