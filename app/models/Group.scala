package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError
import models.dao.GroupDAO
import models.Group

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
    
}

case class Group (
                 id: String,
                 displayName: String,
                 members: Option[List[Member]] = None,
                 var meta: Option[Meta] = None
                 )
                 
