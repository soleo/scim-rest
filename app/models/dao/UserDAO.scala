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
        val result = SQL("""
            | SELECT COUNT(*) as numMatches
            | FROM `users`
            | WHERE id={userId};
        """.stripMargin).on(
            "userId" -> user.id
        ).apply().head

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
      // Probably a good idea to run a transaction instead of a few sql queries
      // But going to do separate queries for now
     val name: Name = user.name.getOrElse( new Name(None, None, None, None, None, None))
    // val basicTraits: BasicTrait = user.basicTraits.getOrElse(new BasicTrait())
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
                      StringUtils.optionalString(row[String]("formattedName")), 
                      StringUtils.optionalString(row[String]("familyName")),
                      StringUtils.optionalString(row[String]("givenName")),
                      StringUtils.optionalString(row[String]("middleName")),
                      StringUtils.optionalString(row[String]("honorificPrefix")),
                      StringUtils.optionalString(row[String]("honorificSuffix"))
                      )
        val nameField = name match{
            case Name(None, None, None, None, None, None) => None
            case _ => Some(name)
        }
        val baseUser = BaseUser(
            row[String]("username"), 
            StringUtils.optionalString(row[String]("externalId")) ,
            nameField,
            StringUtils.optionalString(row[String]("displayName")),
            StringUtils.optionalString(row[String]("nickname")),
            StringUtils.optionalString(row[String]("profileURL")),
            StringUtils.optionalString(row[String]("title")),
            StringUtils.optionalString(row[String]("userType")),
            StringUtils.optionalString(row[String]("preferredLanguage")),
            StringUtils.optionalString(row[String]("locale")),
            StringUtils.optionalString(row[String]("timezone")),
            StringUtils.optionalBoolean(row[Boolean]("active")),
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
                      StringUtils.optionalString(row[String]("formattedName")), 
                      StringUtils.optionalString(row[String]("familyName")),
                      StringUtils.optionalString(row[String]("givenName")),
                      StringUtils.optionalString(row[String]("middleName")),
                      StringUtils.optionalString(row[String]("honorificPrefix")),
                      StringUtils.optionalString(row[String]("honorificSuffix"))
                      )
        val nameField = name match{
            case Name(None, None, None, None, None, None) => None
            case _ => Some(name)
        }
        val baseUser = BaseUser(
            row[String]("username"), 
            StringUtils.optionalString(row[String]("externalId")) ,
            nameField,
            StringUtils.optionalString(row[String]("displayName")),
            StringUtils.optionalString(row[String]("nickname")),
            StringUtils.optionalString(row[String]("profileURL")),
            StringUtils.optionalString(row[String]("title")),
            StringUtils.optionalString(row[String]("userType")),
            StringUtils.optionalString(row[String]("preferredLanguage")),
            StringUtils.optionalString(row[String]("locale")),
            StringUtils.optionalString(row[String]("timezone")),
            StringUtils.optionalBoolean(row[Boolean]("active")),
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
        
        
    }else{
        // patch some fields
    }
  }
  
  

}