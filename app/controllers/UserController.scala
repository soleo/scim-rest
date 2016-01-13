package controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current

import play.api.db._

object UserController extends Controller {

  def findAll = Action { request =>
    Ok("")
    
  }

  def find(userId : String) = Action { request =>
    Ok("")
    
  }

  def add = Action { request =>
    Ok("")
    
  }

  def update(userId : String) = Action { request =>
    Ok("")
  }

  def remove(userId : String) = Action { request =>
    Ok("")
    
  }

}