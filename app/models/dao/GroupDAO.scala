package models.dao

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

import models.Group
import models.Member

object GroupDAO {

  val groupParser = str("groupId") ~ str("displayName") map {
    case groupId ~ displayName => Group(groupId, displayName)
  }

  val memberParser = str("userId") ~ get[Option[String]]("display") map {
    case userId ~ display => Member(userId, display)
  }

  def findMembersByGroupId(groupId: String): Option[List[Member]] = {

    DB.withConnection { implicit c =>
      val members: List[Member] = SQL(
        """
                                            | SELECT userId, displayName AS display
                                            | FROM `groups_users` AS gu, `users` AS u
                                            | WHERE `gu`.`groupId` = {groupId}
                                            | AND `u`.`id` = `gu`.`userId`;
                                        """.stripMargin).on(
          "groupId" -> groupId).as(memberParser.*)
      //println(members)
      if (members.isEmpty) None else Some(members)
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

      if (groups.isEmpty) None else Some(groups)
    }
  }

  def findOne(group: Group): Group = {
    DB.withConnection { implicit c =>
      SQL(
        """
                | SELECT id AS groupId, displayName
                | FROM `groups`
                | WHERE `id`={groupId}
                | LIMIT 1;
                """.stripMargin).on(
          "groupId" -> group.id).as(groupParser.single)
    }
  }

  def patchMembers(group: Group): Option[Group] = {
    DB.withTransaction {
      implicit c =>
        // Step 1: loop through each member to do update for them sequtially
        var isNew: Boolean = false
        group.members match {
          case None => {
            isNew = false
          }
          case Some(ms) =>
            for (member <- ms) {
              if (member.operation.getOrElse("").contains("delete")) {
                SQL(
                  """
                              | DELETE FROM `groups_users`
                              | WHERE `groupId`={groupId} AND `userId`={userId};
                            """.stripMargin).on(
                    "groupId" -> group.id,
                    "userId" -> member.value).executeUpdate()
                isNew = true
              } else {
                // new member?
                val total: Int = SQL("""
                                | SELECT COUNT(*) as numMatches
                                | FROM `groups_users`
                                | WHERE `groupId`={groupId} AND `userId`={userId};
                            """.stripMargin).on(
                  "groupId" -> group.id,
                  "userId" -> member.value).as(SqlParser.int("numMatches").single)
                println("count total " + total)
                if (total == 0) {
                  SQL(
                    """
                                  | INSERT IGNORE INTO `groups_users` (`groupId`, `userId`)
                                  | VALUES ({groupId},{userId});
                                """.stripMargin).on(
                      "groupId" -> group.id,
                      "userId" -> member.value).executeInsert()
                  isNew = true
                }
              }
            }
        }
        // Step 2: return group there are new changes made
        if (isNew) {
          val grp: Group = SQL(
            """
                                | SELECT id AS groupId, displayName
                                | FROM `groups`
                                | WHERE `id` = {groupId}
                                | LIMIT 1;
                                """.stripMargin).on("groupId" -> group.id).as(groupParser.single)
          Some(grp)
        } else {
          None
        }
    }
  }

  def replaceMembers(group: Group): Group = {
    DB.withTransaction {
      implicit c =>
        // Step 1: remove all current members
        // Step 2: if there are new members , add them now
        // Step 3: return group
        SQL(
          """
              | DELETE FROM `groups_users`
              | WHERE `groupId`={groupId};
            """.stripMargin).on(
            "groupId" -> group.id).executeUpdate()

        group.members match {
          case Some(members) =>
            for (member <- members) {
              SQL("""
                            | INSERT IGNORE INTO `groups_users` (
                            | `userId`, `groupId`)
                            | VALUES(
                            | {userId}, {groupId}
                            | );
                            """.stripMargin).on(
                "userId" -> member.value,
                "groupId" -> group.id).executeInsert()
            }

          case None => println("Do Nothing for members")
        }

        SQL(
          """
                | SELECT id AS groupId, displayName
                | FROM `groups`
                | WHERE `id`={groupId}
                | LIMIT 1;
                """.stripMargin).on(
            "groupId" -> group.id).as(groupParser.single)
    }
  }

  def findGroupsByUserId(userId: String): Option[List[Group]] = {
    DB.withConnection {
      implicit c =>
        val groups: List[Group] = SQL(
          """
                                        | SELECT groupId, displayName
                                        | FROM `groups` AS g, `groups_users` AS gu
                                        | WHERE `gu`.`userId` = {userId}
                                        | AND `g`.`id` = `gu`.`groupId`;
                                    """.stripMargin).on(
            "userId" -> userId).as(groupParser.*)
        //println(groups)
        if (groups.isEmpty) None else Some(groups)
    }
  }
}