package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError
import play.api.mvc.{RequestHeader, AnyContent, Request}
import models.dao.GroupDAO
import models.Group
import utils._
import java.util.Date

object Member {
    implicit val memberFormat = Json.format[Member]
}

case class Member( value: String, display: Option[String])


object Group {
  implicit val groupFormat = Json.format[Group]
  
    def findAll(): List[Group] = {
        var groups: Option[List[Group]] = GroupDAO.findAll
       
        groups match{
            case Some(grps) => {
                import scala.collection.mutable.ListBuffer
                  
                var newGroupWithMembers = ListBuffer[Group]()
                for(grp <- grps) {
                     val members: Option[List[Member]] = GroupDAO.findMembersByGroupId(grp.id)
                     val newGroup = Group(grp.id, grp.displayName, members)
                     newGroupWithMembers += newGroup
                }
                newGroupWithMembers.toList
            }
            case None => {
                 List() 
            }
        }
    }
  
    def patch(group: Group): Group = {
        GroupDAO.patch(group)
    }
    
    def setMetaData(group: Group, request: RequestHeader): Meta = {
        val location = Utils.baseURL(request) +  "Groups/" + group.id
        val version  = Utils.generateETAG(Json.stringify(Json.toJson(group)))
        
        group.meta match {
            case Some(m) => Meta(m.created, m.lastModified, Some(version), Some(location))
            case None => Meta(new Date, new Date, Some(version), Some(location))
        }
    }
}

case class Group (
                 id: String,
                 displayName: String,
                 members: Option[List[Member]] = None,
                 meta: Option[Meta] = None
                 )
                 
