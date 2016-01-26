package models.dao

import models.Group
import models.Member
import anorm._
import anorm.SqlParser._

import play.api.db._
import play.api.Play.current

object GroupDAO {
  
  val groupParser = str("groupId") ~ str("displayName") map {
      case groupId  ~ displayName  => Group(groupId, displayName)
  }
  
  val memberParser =  str("userId") ~ get[Option[String]]("display") map {
      case userId  ~ display  => Member(userId, display)
  }
  
  def findMembersByGroupId(groupId: String) : Option[List[Member]] = {
       
       DB.withConnection { implicit c =>
          val members: List[Member] = SQL(
                                        """
                                            | SELECT userId, displayName AS display
                                            | FROM `groups_users` AS gu, `users` AS u
                                            | WHERE `gu`.`groupId` = {groupId}
                                            | AND `u`.`id` = `gu`.`userId`;
                                        """.stripMargin).on(
                                            "groupId" -> groupId
                                        ).as(memberParser.*)
            //println(members)
            if(members.isEmpty) None else Some(members)
       }
      
  }
  
  def findAll(): Option[List[Group]] = {
      
      DB.withConnection { implicit c =>
        val groups: List[Group] = SQL(
                                    """
                                        | SELECT id AS groupId, displayName
                                        | FROM `groups`
                                        | LIMIT 10;
                                    """.stripMargin).as(groupParser.*)
        //println(groups)
        
        if(groups.isEmpty) {
            None
        }else {
            
            Some(groups)
        }
      }
  }

  def patch(group: Group) = {

  }
  
  def findGroupsByUserId(userId: String): Option[List[Group]] = {
      DB.withConnection { implicit c =>
        val groups: List[Group] = SQL(
                                    """
                                        | SELECT groupId, displayName
                                        | FROM `groups` AS g, `groups_users` AS gu
                                        | WHERE `gu`.`userId` = {userId}
                                        | AND `g`.`id` = `gu`.`groupId`;
                                    """.stripMargin).on(
                                        "userId" -> userId
                                    ).as(groupParser.*)
        //println(groups)
       if(groups.isEmpty) None else Some(groups)
      }
  }
}