package controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import play.api.libs.json._
import play.api.db._
import models.Group
import models.Meta

class GroupController extends Controller {
    val schemas =  Json.obj("schemes" -> Json.arr("urn:scim:schemas:core:1.0"))
    
    def findAll = AuthenticatedAction { implicit request =>
    
        val groups = Group.findAll
        var total: Int = 0
        var resources = Json.arr()
        
        for(group <- groups){
            
            val meta = Group.setMetaData(group, request)
            var groupJsonObj = Json.toJson(group).as[JsObject] ++ Json.obj("meta" -> Json.toJson(meta).as[JsObject])
            resources = resources :+ groupJsonObj
            // Increase Count Now
            total += 1
        }
                
        Ok(
            Json.obj(
                "totalResults" -> total,
                "schemes" -> Json.arr("urn:scim:schemas:core:1.0"),
                "Resources" -> resources
            )
        )
    
    }

   def update(groupId : String) = AuthenticatedAction { implicit request =>
        request.body.asJson.map { 
            implicit json =>
            json.validate[Group].map {
                case group => 
                    group.meta match {
                        case None => 
                            //let's create a new member or remove some members
                            println("Create New Member")
                            val groupObj = Group.patchMembers(groupId, group.members) 
                            groupObj match {
                                case Some(grp) => 
                                    //TODO: post fetching operation, such as add meta data back, add schemas back
                                    val meta = Group.setMetaData(grp, request)
                                    var response = Json.toJson(grp).as[JsObject] ++ Json.obj("meta"-> Json.toJson(meta).as[JsObject])
                                    response = response ++ schemas
                                    
                                    val ETag = meta.version.getOrElse("")
                                    val location = meta.location.getOrElse("")
                                    
                                    Ok(response).withHeaders(
                                        ETAG     -> ETag,
                                        LOCATION -> location
                                    )
                                case None => 
                                    val meta = Group.setMetaData(group, request)
                                    val ETag = meta.version.getOrElse("")
                                    val location = meta.location.getOrElse("")
                                    
                                    NoContent withHeaders(
                                        ETAG -> ETag,
                                        LOCATION -> location
                                    )
                            }
                        
                        case Some(m) => 
                            // probably need to replace all members
                            if(m.attributes.getOrElse(List()).contains("members")) {
                                
                                var groupObj = Group.replaceMembers(groupId, group.members)
                                val meta = Group.setMetaData(groupObj, request)
                                
                                var response = Json.toJson(groupObj).as[JsObject] ++ Json.obj("meta"-> Json.toJson(meta).as[JsObject])
                                response = response ++ schemas
                                
                                val ETag = meta.version.getOrElse("")
                                val location = meta.location.getOrElse("")
                                
                                Ok(response).withHeaders(
                                    ETAG     -> ETag,
                                    LOCATION -> location
                                )
                            }else{
                                NotImplemented
                            }
                    }
            }.recoverTotal {
                e => BadRequest(JsError.toJson(e))
            }
        }.getOrElse {
            BadRequest(Json.obj("error" -> JsString("Expecting Json data")))
        }
    }

}