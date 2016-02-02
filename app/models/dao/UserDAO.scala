package models.dao

import models._
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db._
import utils._
import java.util.Date
import parsing._
import scala.language.implicitConversions

object UserDAO {
  
  val emailParser = get[Option[String]]("value") ~ get[Option[String]]("emailType") ~ get[Boolean]("isPrimary") map {
      case value ~ emailType ~ isPrimary  => Email(value, emailType, Utils.optionalBoolean(isPrimary))
  }

  val userParser  = {
                       str("id") ~ str("username") ~ get[Option[String]]("formattedName") ~ 
                       get[Option[String]]("familyName") ~ get[Option[String]]("givenName") ~
                       get[Option[String]]("middleName") ~ get[Option[String]]("honorificPrefix") ~ 
                       get[Option[String]]("honorificSuffix") ~
                       get[Option[String]]("externalId") ~ get[Option[String]]("displayName") ~
                       get[Option[String]]("nickname") ~ get[Option[String]]("profileURL") ~
                       get[Option[String]]("title") ~ get[Option[String]]("userType") ~
                       get[Option[String]]("preferredLanguage") ~ get[Option[String]]("locale") ~
                       get[Option[String]]("timezone") ~ get[Option[Boolean]]("active") ~
                       get[Date]("created") ~ get[Date]("lastModified") map {
                         case id ~ username ~ formattedName ~ familyName ~ givenName ~ middleName ~ honorificPrefix ~ honorificSuffix ~
                             externalId ~ displayName ~ nickname ~ profileURL ~ title ~ userType ~ preferredLanguage ~ locale ~ timezone ~
                                active ~ created ~ lastModified => 
                            val name = Name(formattedName, familyName, givenName, middleName, honorificPrefix, honorificSuffix)
                            val nameField = name match {
                                case Name(None, None, None, None, None, None) => None
                                case _ => Some(name)
                            }
                            val baseUser = BaseUser(username, externalId, nameField, displayName,
                                nickname, profileURL, title, userType, preferredLanguage, locale, timezone,
                                active, None/*Leave password alone*/)
                            val meta = Meta(created,lastModified)
                            User(id, baseUser, None, None, None, None, None, None, None, None, None, Some(meta))
                         
                         
                      }
                    }
  
  def exists(user: User): Boolean =  {
        DB.withConnection { implicit c =>
            val userName = user.baseUser.userName
            val numMatches: Int = if(userName.length <= 0) {
                SQL("""
                    | SELECT COUNT(*) as numMatches
                    | FROM `users`
                    | WHERE id={userId};
                """.stripMargin).on(
                    "userId" -> user.id
                ).as(SqlParser.int("numMatches").single)
            } else {
               SQL("""
                | SELECT COUNT(*) as numMatches
                | FROM `users`
                | WHERE username={userName} AND id<>{userId};
                """.stripMargin).on(
                    "userName" -> userName,
                    "userId" -> user.id
                ).as(SqlParser.int("numMatches").single)
            }
    
            numMatches != 0
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
            x509Certificates: Option[List[X509Certificate]],
            meta: Option[Meta]
            ) = {
    DB.withTransaction { implicit c =>

      createWithConnection(id, user, emails, phoneNumbers, ims, 
                          photos, addresses, groups, entitlements, 
                          roles, x509Certificates, meta)   
    
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

 def findAll(filter: Option[String]): List[User] = {
    DB.withTransaction { implicit c =>
     // parse filter to see if we can deal with it
        val filterSql = filter match{
             case Some(f) => {
                val parsedObj = FilterParser.parse(f.trim.toLowerCase)
                println(parsedObj)
                ""
             }
             case None => ""
        }
    
        val users: List[User] = SQL(
        """
         | SELECT *
         | FROM `users`;
        """.stripMargin).as(userParser.*)

         // add more sauce to them
        import scala.collection.mutable.ListBuffer
                      
        var completeUsers = ListBuffer[User]()
        for(user <- users) {
             val emails : List[Email] = SQL(
                """
                    | SELECT value, type as emailType, isPrimary
                    | FROM `emails`
                    | WHERE `userId` = {userId};
                """.stripMargin).on(
                    "userId" -> user.id
                ).as(emailParser.*)
             val e = if (emails.isEmpty) None else Some(emails)
             val singleUser = User(user.id, user.baseUser, e, None, None, None, None, None, None, None, None, user.meta)
             completeUsers += singleUser
        }
        completeUsers.toList

    }
 }
 
 def findOne(user: User): List[User] = {
    DB.withTransaction { implicit c =>
        // Find One and Map Anything related to that user
         val u: User = SQL(
            """
             | SELECT *
             | FROM `users`
             | WHERE `id`={userId}
             | LIMIT 1;
            """.stripMargin).on(
            "userId" -> user.id
         ).as(userParser.single)

         if ( u.id.length > 0 ) {
         
                // tranform extra info here as well
                val emails : List[Email] = SQL(
                """
                    | SELECT value, type as emailType, isPrimary
                    | FROM `emails`
                    | WHERE `userId` = {userId};
                """.stripMargin).on(
                    "userId" -> u.id
                ).as(emailParser.*)
                val e = if (emails.isEmpty) None else Some(emails)
                val userWithExtraInfo = User(u.id, u.baseUser, e, None, None, None, None, None, None, None, None, u.meta)
                List(userWithExtraInfo)            
         }else{
            List(User("", BaseUser("")))
         }
        
     
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
                    // get old password , because we cannot wipe it out
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
                createWithConnection(user.id, user.baseUser, user.emails, user.phoneNumbers, user.ims, 
                          user.photos, user.addresses, user.groups, user.entitlements, 
                          user.roles, user.x509Certificates, user.meta)
            }
        }
        
        }else{
            // patch some fields
            // Not Done Yet.  
        }
    }
    
    private def insertPluralAttributes(tableName: String, 
                             userId: String, 
                             value: Option[String] = None,
                             attributeType: Option[String] = None, 
                             primary: Option[Boolean] = None)(implicit c : java.sql.Connection) = {
       
        SQL("""
            INSERT IGNORE INTO """+tableName+""" (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
            """.stripMargin).on(
                "userId"  -> userId,
                "value"   -> value,
                "type"    -> attributeType,
                "primary" -> primary.getOrElse(false)
            ).executeInsert()
    }

    private def getPluralAttributes(userId: String, attributeName: String)(implicit c : java.sql.Connection): Option[List[PluralAttribute]] = {

        None
    }

    private def createWithConnection(
            id: String, 
            user: BaseUser, 
            emails: Option[List[Email]], 
            phoneNumbers: Option[List[PhoneNumber]],
            ims: Option[List[Im]],
            photos: Option[List[Photo]],
            addresses: Option[List[Address]],
            groups: Option[List[Group]],
            entitlements: Option[List[Entitlement]],
            roles: Option[List[Role]],
            x509Certificates: Option[List[X509Certificate]],
            meta: Option[Meta])(implicit c : java.sql.Connection) = {
              
        val name: Name = user.name.getOrElse(Name(None, None, None, None, None, None))
        val m: Meta = meta.getOrElse(Meta(new Date, new Date, None, None, None))
        
        // add base user info finally
        SQL(
         """
            | INSERT IGNORE INTO `users` (
            | `id`, `externalId`, `username`,
            | `formattedName`, `familyName`, `givenName`, `middleName`,
            | `honorificPrefix`, `honorificSuffix`, `displayName`, `nickname`,
            | `profileURL`, `title`, `userType`, `preferredLanguage`,
            | `locale`, `timezone`, `active`, `password`, `created`, `lastModified`
            | )
            | VALUES
            | (
            | {userId}, {externalId}, {username},
            | {formattedName}, {familyName}, {givenName}, {middleName},
            | {honorificPrefix}, {honorificSuffix}, {displayName}, {nickname},
            | {profileURL}, {title}, {userType}, {preferredLanguage},
            | {locale}, {timezone}, {active}, {password}, {created}, {lastModified}
            | );
         """.stripMargin).on(
            "userId" -> id,
            "externalId" -> user.externalId,
            "username" -> user.userName,
            "formattedName" -> name.formatted,
            "familyName" -> name.familyName,
            "givenName" -> name.givenName,
            "middleName" -> name.middleName,
            "honorificPrefix" -> name.honorificPrefix,
            "honorificSuffix" -> name.honorificSuffix,
            "displayName" -> user.displayName,
            "nickname" -> user.nickName,
            "profileURL" -> user.profileUrl,
            "title" -> user.title,
            "userType" -> user.userType,
            "preferredLanguage" -> user.preferredLanguage,
            "locale" -> user.locale,
            "timezone" -> user.timezone,
            "active" -> user.active.getOrElse(false),
            "password" -> user.password,
            "created" -> m.created,
            "lastModified" -> m.lastModified
         ).executeInsert()
         // insert all other info first
        addresses match {
            case Some(addrs) =>
                for( addr <- addrs )
                { 
                    SQL("""
                    | INSERT IGNORE INTO `addresses` (
                    | `userId`, `type`,`streetAddress`, `locality`, 
                    | `region`, `postalCode`, `country`, `formatted`, `isPrimary` )
                    | VALUES(
                    | {userId}, {type}, {streetAddress}, {locality}, 
                    | {region}, {postalCode}, {country}, {formatted}, {primary}
                    | );
                    """.stripMargin).on(
                             "userId" -> id,
                             "type" -> addr.`type`,
                             "streetAddress"  -> addr.streetAddress,
                             "locality" -> addr.locality,
                             "region" -> addr.region,
                             "postalCode" -> addr.postalCode,
                             "country" -> addr.country,
                             "formatted" -> addr.`formatted`,
                             "primary" -> addr.primary.getOrElse(false)
                    ).executeInsert()
                }
            case None => println("addresses: Do Nothing")
        }
        
        emails match {
            case Some(emails) =>
                for (email:Email <- emails) {
                    insertPluralAttributes("emails", id, email.value, email.`type`, email.primary)
                }
            case None => println("emails: Do Nothing")
        }
        phoneNumbers match {
            case Some(phoneNumbers) => 
                for(phoneNumber <- phoneNumbers) {
                    insertPluralAttributes("phoneNumbers", id, phoneNumber.value, phoneNumber.`type`, phoneNumber.primary)
                }
            case None => println("phoneNumbers: Do Nothing")
        }
        ims match {
            case Some(ims) => 
                for(im <- ims) {
                    insertPluralAttributes("ims", id, im.value, im.`type`, im.primary)
                }
            case None => println("ims: Do Nothing")
        }
        photos match {
            case Some(photos) =>
                for(photo <- photos) {
                    insertPluralAttributes("photos", id, photo.value, photo.`type`, photo.primary)
                }
        
            case None => println("photos: Do Nothing")
        }
        entitlements match {
            case Some(entitlements) =>
                for(entitlement <- entitlements) {
                    insertPluralAttributes("entitlements", id, entitlement.value, entitlement.`type`, entitlement.primary)
                }
        
            case None => println("entitlements: Do Nothing")
        }
        roles match {
            case Some(roles) =>
                for(role <- roles) {
                   insertPluralAttributes("roles", id, role.value, role.`type`, role.primary)
                }
        
            case None => println("roles: Do Nothing")
        }

        x509Certificates match {
            case Some(x509Certificates) =>
                for(x509Certificate <- x509Certificates) {
                    insertPluralAttributes("x509Certificates", id, x509Certificate.value, x509Certificate.`type`, x509Certificate.primary)
                }
        
            case None => println("x509Certificates: Do Nothing")
        }
  }

}