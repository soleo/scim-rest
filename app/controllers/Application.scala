package controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current

import play.api.db._

class Application extends Controller {

  def index = Action {
    Ok(views.html.index(null))
  }

  def ping = Action {
    Ok("pong")
  }

}
