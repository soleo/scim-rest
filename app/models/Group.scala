package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError
import play.api.mvc.{RequestHeader, AnyContent, Request}
import models.dao.GroupDAO
import utils._
import java.util.Date

object Member {
    implicit val memberFormat = Json.format[Member]
}

case class Member( value: String, display: Option[String], operation: Option[String] = None)


object Group {
    
    implicit val groupReadFormat: Reads[Group] = new Reads[Group] {
        override def reads(json: JsValue): JsResult[Group] = {
            for {
            
               members   <- (json \ "members").validateOpt[List[Member]]
               meta      <- (json \ "meta").validateOpt[Meta]
            }yield{
                Group("", "", members, meta)
            }
        }
    }
    
    implicit val groupWrites = Json.writes[Group]
    
  
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

    def patchMembers(groupId: String, members: Option[List[Member]]) : Option[Group] = {
        val group = Group(groupId, "", members) 
        val grp = GroupDAO.patchMembers(group)
        grp match {
            case None => None
            case Some(g) =>
            {
                val mbs: Option[List[Member]] = GroupDAO.findMembersByGroupId(g.id)
                val newGroup = Group(g.id, g.displayName, mbs)
                Some(newGroup)
            }
        }
    }
    
    def replaceMembers(groupId: String, members:Option[List[Member]]) : Group = {
        val group = Group(groupId, "", members) 
        val grp = GroupDAO.replaceMembers(group)
        // get new members out
        val mbs: Option[List[Member]] = GroupDAO.findMembersByGroupId(grp.id)
        Group(grp.id, grp.displayName, mbs)
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
                 
