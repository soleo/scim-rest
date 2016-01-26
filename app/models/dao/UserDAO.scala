package models.dao

import models._
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db._
import utils._
import java.util.Date


object UserDAO {
  
  val emailParser = str("value") ~ str("emailType") ~get[Boolean]("isPrimary") map {
      case value ~ emailType ~ isPrimary  => Email(value, emailType, StringUtils.optionalBoolean(isPrimary))
  }
  
  def exists(user: User): Boolean =  {
      DB.withConnection { implicit c =>
        val userName = user.baseUser.userName
        val result = if(userName.length <= 0) {
            SQL("""
                | SELECT COUNT(*) as numMatches
                | FROM `users`
                | WHERE id={userId};
            """.stripMargin).on(
                "userId" -> user.id
            ).apply().head
        }else{
           SQL("""
            | SELECT COUNT(*) as numMatches
            | FROM `users`
            | WHERE username={userName};
            """.stripMargin).on(
                "userName" -> userName
            ).apply().head
        }

        result[Int]("numMatches") != 0
      }
  }
  
  def create(id: String, 
            user: BaseUser, 
            emails: Option[List[Email]], 
            phoneNumbers: Option[List[PhoneNumber]],
            ims: Option[List[Im]],
            photos: Option[List[Photo]],
            addresses: Option[List[Address]],
            groups: Option[List[Group]],
            entitlements: Option[List[Entitlement]],
            roles: Option[List[Role]],
            x509certs: Option[List[X509Certificate]]
            ) = {
    DB.withTransaction { implicit c =>
      
     val name: Name = user.name.getOrElse( Name(None, None, None, None, None, None))
   
     SQL(
     """
        | INSERT IGNORE INTO `users` (
        | `id`, `externalId`, `username`,
        | `formattedName`, `familyName`, `givenName`, `middleName`,
        | `honorificPrefix`, `honorificSuffix`, `displayName`, `nickname`,
        | `profileURL`, `title`, `userType`, `preferredLanguage`,
        | `locale`, `timezone`, `active`, `password`
        | )
        | VALUES
        | (
        | {userId}, {externalId}, {username},
        | {formattedName}, {familyName}, {givenName}, {middleName},
        | {honorificPrefix}, {honorificSuffix}, {displayName}, {nickname},
        | {profileURL}, {title}, {userType}, {preferredLanguage},
        | {locale}, {timezone}, {active}, {password}
        | );
     """.stripMargin).on(
        "userId" -> id,
        "externalId" -> user.externalId,
        "username" -> user.userName,
        "formattedName" -> name.formattedName.getOrElse(""),
        "familyName" -> name.familyName.getOrElse(""),
        "givenName" -> name.givenName.getOrElse(""),
        "middleName" -> name.middleName.getOrElse(""),
        "honorificPrefix" -> name.honorificPrefix.getOrElse(""),
        "honorificSuffix" -> name.honorificSuffix.getOrElse(""),
        "displayName" -> user.displayName.getOrElse(""),
        "nickname" -> user.nickName.getOrElse(""),
        "profileURL" -> user.profileUrl.getOrElse(""),
        "title" -> user.title.getOrElse(""),
        "userType" -> user.userType.getOrElse(""),
        "preferredLanguage" -> user.preferredLanguage.getOrElse(""),
        "locale" -> user.locale.getOrElse(""),
        "timezone" -> user.timezone.getOrElse(""),
        "active" -> user.active.getOrElse(false),
        "password" -> user.password.getOrElse("")
     ).executeInsert()
     
         // insert extra info
         emails match {
             case Some(emails) => {
                 for(email <- emails) {
                     println(email)
                     SQL("""
                        | INSERT IGNORE INTO `emails` (
                        | `userId`, `value`, `type`, `isPrimary` )
                        | VALUES(
                        | {userId}, {value}, {type}, {primary}
                        | )
                     """.stripMargin).on(
                         "userId" -> id,
                         "value"  -> email.value,
                         "type"   -> email.emailType,
                         "primary" -> email.primary.getOrElse(false)
                        ).executeInsert()
                 }
             }
             case None => println("No Emails Provided")
         }
        // TODO: Insert all others into other related tables
        
        //  user.addresses match {
        //      case Some(addresses) => {
        //          for(address <- addresses) {
        //              println(address)
        //               SQL("""
        //                 | INSERT IGNORE INTO `emails` (
        //                 | `userId`, `type`,`streetAddress`, `locality`, 
        //                 | `region`, `postalCode`, `country`, `formatted`, `isPrimary` )
        //                 | VALUES(
        //                 | {userId}, {type}, {streetAddress}, {locality}, 
        //                 | {region}, {postalCode}, {country}, {formatted}, {primary}
        //                 | )
        //              """.stripMargin).on(
        //                  "userId" -> user.id.getOrElse(""),
        //                  "type" -> address.addressType,
        //                  "streetAddress"  -> address.streetAddress,
        //                  "locality" -> address.locality,
        //                  "region" -> address.region,
        //                  "postalCode" -> address.postalCode,
        //                  "country" -> address.country,
        //                  "formatted" -> address.formatted,
        //                  "primary" -> address.primary.getOrElse(false)
        //                 ).executeInsert()
        //          }
        //      }
        //      case None => println("No Addresses Provided")
        //  }
         
         
    }
  }

  def delete(user: User) = {
    DB.withConnection { implicit c =>
      // Delete Anything related to that user
      SQL(
        """
          | DELETE FROM `users`
          | WHERE `id`={userId};
        """.stripMargin).on(
        "userId" -> user.id
      ).executeUpdate()

    }
  }
  
 def findAll(filter: Option[Seq[String]]): List[User] = {
    DB.withTransaction { implicit c =>
     var results = SQL(
        """
         | SELECT *
         | FROM `users`
         | LIMIT 10;
        """.stripMargin).on(
     //   "userId" -> user.id
     ).apply()

      results.map { row =>
        val name = Name(
                      StringUtils.removeEmptyString(row[Option[String]]("formattedName")), 
                      StringUtils.removeEmptyString(row[Option[String]]("familyName")),
                      StringUtils.removeEmptyString(row[Option[String]]("givenName")),
                      StringUtils.removeEmptyString(row[Option[String]]("middleName")),
                      StringUtils.removeEmptyString(row[Option[String]]("honorificPrefix")),
                      StringUtils.removeEmptyString(row[Option[String]]("honorificSuffix"))
                      )
        val nameField = name match{
            case Name(None, None, None, None, None, None) => None
            case _ => Some(name)
        }
        val baseUser = BaseUser(
            row[String]("username"), 
            StringUtils.removeEmptyString(row[Option[String]]("externalId")) ,
            nameField,
            StringUtils.removeEmptyString(row[Option[String]]("displayName")),
            StringUtils.removeEmptyString(row[Option[String]]("nickname")),
            StringUtils.removeEmptyString(row[Option[String]]("profileURL")),
            StringUtils.removeEmptyString(row[Option[String]]("title")),
            StringUtils.removeEmptyString(row[Option[String]]("userType")),
            StringUtils.removeEmptyString(row[Option[String]]("preferredLanguage")),
            StringUtils.removeEmptyString(row[Option[String]]("locale")),
            StringUtils.removeEmptyString(row[Option[String]]("timezone")),
            (row[Option[Boolean]]("active")),
            None// Leave password alone
            )
        val meta = Meta(
            row[Date]("created"),
            row[Date]("lastModified")
            )
            
        // get all emails belong to that user
        val emails: List[Email] = SQL(
                                    """
                                        | SELECT value, type as emailType, isPrimary
                                        | FROM `emails`
                                        | WHERE `userId` = {userId};
                                    """.stripMargin).on(
                                        "userId" -> row[String]("id")
                                    ).as(emailParser.*)
        //println(emails)
        val emailsOption = if(emails.isEmpty) None else Some(emails)
        // get all groups
        
        User(row[String]("id"), baseUser, emailsOption, None, None, None, None, None, None, None, None, Some(meta))
      }.force.toList
    }
 }
 
 def findOne(user: User): List[User] = {
    DB.withTransaction { implicit c =>
     // Find One and Map Anything related to that user
     var results = SQL(
        """
         | SELECT *
         | FROM `users`
         | WHERE `id`={userId}
         | LIMIT 1;
        """.stripMargin).on(
        "userId" -> user.id
     ).apply()

      results.map { row =>
        val name = Name(
                      StringUtils.removeEmptyString(row[Option[String]]("formattedName")), 
                      StringUtils.removeEmptyString(row[Option[String]]("familyName")),
                      StringUtils.removeEmptyString(row[Option[String]]("givenName")),
                      StringUtils.removeEmptyString(row[Option[String]]("middleName")),
                      StringUtils.removeEmptyString(row[Option[String]]("honorificPrefix")),
                      StringUtils.removeEmptyString(row[Option[String]]("honorificSuffix"))
                      )
        val nameField = name match{
            case Name(None, None, None, None, None, None) => None
            case _ => Some(name)
        }
        val baseUser = BaseUser(
            row[String]("username"), 
            StringUtils.removeEmptyString(row[Option[String]]("externalId")) ,
            nameField,
            StringUtils.removeEmptyString(row[Option[String]]("displayName")),
            StringUtils.removeEmptyString(row[Option[String]]("nickname")),
            StringUtils.removeEmptyString(row[Option[String]]("profileURL")),
            StringUtils.removeEmptyString(row[Option[String]]("title")),
            StringUtils.removeEmptyString(row[Option[String]]("userType")),
            StringUtils.removeEmptyString(row[Option[String]]("preferredLanguage")),
            StringUtils.removeEmptyString(row[Option[String]]("locale")),
            StringUtils.removeEmptyString(row[Option[String]]("timezone")),
            (row[Option[Boolean]]("active")),
            None// Leave password alone
            )
        val meta = Meta(
            row[Date]("created"),
            row[Date]("lastModified")
            )
        val emails: List[Email] = SQL(
                                    """
                                        | SELECT value, type as emailType, isPrimary
                                        | FROM `emails`
                                        | WHERE `userId` = {userId};
                                    """.stripMargin).on(
                                        "userId" -> row[String]("id")
                                    ).as(emailParser.*)
        //println(emails)
        val emailsOption = if(emails.isEmpty) None else Some(emails)
        User(row[String]("id"), baseUser, emailsOption, None, None, None, None, None, None, None, None, Some(meta))
      }.force.toList
     
    }
 }

  def updateOne(user: User, replace: Boolean = false) = {
    if(replace) {
        // replace whole user
        // Note: replace password if exsits, otherwise remain as old 
        // step 1: get user password field and id 
        // step 2: remove the whole user from database
        // step 3: add current data set to database
        DB.withTransaction { implicit c => {
                // Step 1
                val password = user.baseUser.password.getOrElse("")
                if (password.length == 0) {
                    // get old password, because we cannot wipe it out
                    val oldPassword:Option[String] = SQL(
                        """
                         | SELECT  password
                         | FROM `users`
                         | WHERE `id` = {userId}
                         | LIMIT 1;
                        """.stripMargin).on(
                        "userId" -> user.id
                     ).as(SqlParser.get[Option[String]]("password").single)
                     
                     user.baseUser.password = oldPassword
                }
                // Step 2
                SQL(
                    """
                      | DELETE FROM `users`
                      | WHERE `id`={userId};
                    """.stripMargin).on(
                    "userId" -> user.id
                ).executeUpdate()
                // Step 3
                //UserDAO.create(user.id, user.baseUser, user.emails, user.phoneNumbers, user.ims, user.photos,
                //            user.addresses, user.groups, user.entitlements, user.roles, user.x509Certificates)
                // a repeat of create function but maybe should be in a util function
                val name: Name = user.baseUser.name.getOrElse( Name(None, None, None, None, None, None))
                SQL(
                     """
                        | INSERT IGNORE INTO `users` (
                        | `id`, `externalId`, `username`,
                        | `formattedName`, `familyName`, `givenName`, `middleName`,
                        | `honorificPrefix`, `honorificSuffix`, `displayName`, `nickname`,
                        | `profileURL`, `title`, `userType`, `preferredLanguage`,
                        | `locale`, `timezone`, `active`, `password`
                        | )
                        | VALUES
                        | (
                        | {userId}, {externalId}, {username},
                        | {formattedName}, {familyName}, {givenName}, {middleName},
                        | {honorificPrefix}, {honorificSuffix}, {displayName}, {nickname},
                        | {profileURL}, {title}, {userType}, {preferredLanguage},
                        | {locale}, {timezone}, {active}, {password}
                        | );
                     """.stripMargin).on(
                        "userId" -> user.id,
                        "externalId" -> user.baseUser.externalId,
                        "username" -> user.baseUser.userName,
                        "formattedName" -> name.formattedName.getOrElse(""),
                        "familyName" -> name.familyName.getOrElse(""),
                        "givenName" -> name.givenName.getOrElse(""),
                        "middleName" -> name.middleName.getOrElse(""),
                        "honorificPrefix" -> name.honorificPrefix.getOrElse(""),
                        "honorificSuffix" -> name.honorificSuffix.getOrElse(""),
                        "displayName" -> user.baseUser.displayName.getOrElse(""),
                        "nickname" -> user.baseUser.nickName.getOrElse(""),
                        "profileURL" -> user.baseUser.profileUrl.getOrElse(""),
                        "title" -> user.baseUser.title.getOrElse(""),
                        "userType" -> user.baseUser.userType.getOrElse(""),
                        "preferredLanguage" -> user.baseUser.preferredLanguage.getOrElse(""),
                        "locale" -> user.baseUser.locale.getOrElse(""),
                        "timezone" -> user.baseUser.timezone.getOrElse(""),
                        "active" -> user.baseUser.active.getOrElse(false),
                        "password" -> user.baseUser.password.getOrElse("")
                     ).executeInsert()
                     user.emails match {
                         case Some(emails) => {
                             for(email <- emails) {
                                 println(email)
                                 SQL("""
                                    | INSERT IGNORE INTO `emails` (
                                    | `userId`, `value`, `type`, `isPrimary` )
                                    | VALUES(
                                    | {userId}, {value}, {type}, {primary}
                                    | )
                                 """.stripMargin).on(
                                     "userId" -> user.id,
                                     "value"  -> email.value,
                                     "type"   -> email.emailType,
                                     "primary" -> email.primary.getOrElse(false)
                                    ).executeInsert()
                             }
                         }
                        case None => println("No Emails Provided")
                    }
                    // Should have others inserted as well
            }
        }
        
    }else{
        // patch some fields
        // Not Done Yet.  
    }
  }
  
 
  
  

}