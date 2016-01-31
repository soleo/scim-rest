package models.dao

import models._
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db._
import utils._
import java.util.Date
import parsing._

object UserDAO {
  
  val emailParser = str("value") ~ str("emailType") ~ get[Boolean]("isPrimary") map {
      case value ~ emailType ~ isPrimary  => Email(value, emailType, Utils.optionalBoolean(isPrimary))
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
            } else {
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
            x509Certificates: Option[List[X509Certificate]],
            meta: Option[Meta]
            ) = {
    DB.withTransaction { implicit c =>
      
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
                    println(email)
                    val tableName = "emails"
                    val ret = SQL("""
                        INSERT INTO `emails` (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
                        """.stripMargin).on(
                            "userId"  -> id,
                            "value"   -> email.value,
                            "type"    -> email.`type`,
                            "primary" -> email.primary.getOrElse(false)
                    ).executeInsert()
                    println("email ret = "+ret)
                }
                //UserDAO.insertPluralAttributes("emails", id, email.value, email.`type`, email.primary).executeInsert()
            case None => println("emails: Do Nothing")
        }
        phoneNumbers match {
            case Some(phoneNumbers) => 
                for(phoneNumber <- phoneNumbers) {
                    val tableName = "phoneNumbers"
                    SQL("""
                        INSERT INTO """+tableName+""" (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
                        """.stripMargin).on(
                            "userId"  -> id,
                            "value"   -> phoneNumber.value,
                            "type"    -> phoneNumber.`type`,
                            "primary" -> phoneNumber.primary.getOrElse(false)
                    ).executeInsert()
                }
                //UserDAO.insertPluralAttributes("phoneNumbers", id, phoneNumber.value, phoneNumber.`type`, phoneNumber.primary).executeInsert()
            case None => println("phoneNumbers: Do Nothing")
        }
        ims match {
            case Some(ims) => 
                for(im <- ims) {
                    val tableName = "ims"
                    SQL("""
                        INSERT IGNORE INTO """+tableName+""" (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
                        """.stripMargin).on(
                            "userId"  -> id,
                            "value"   -> im.value,
                            "type"    -> im.`type`,
                            "primary" -> im.primary.getOrElse(false)
                    ).executeInsert()
                }
                //UserDAO.insertPluralAttributes("ims", id, im.value, im.`type`, im.primary).executeInsert()
            case None => println("ims: Do Nothing")
        }
        photos match {
            case Some(photos) =>
                for(photo <- photos) {
                    val tableName = "photos"
                    SQL("""
                        INSERT IGNORE INTO """+tableName+""" (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
                        """.stripMargin).on(
                            "userId"  -> id,
                            "value"   -> photo.value,
                            "type"    -> photo.`type`,
                            "primary" -> photo.primary.getOrElse(false)
                    ).executeInsert()
                }
                //UserDAO.insertPluralAttributes("photos", id, photo.value, photo.`type`, photo.primary).executeInsert()
        
            case None => println("photos: Do Nothing")
        }
        entitlements match {
            case Some(entitlements) =>
                for(entitlement <- entitlements) {
                    val tableName = "entitlements"
                    SQL("""
                        INSERT IGNORE INTO """+tableName+""" (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
                        """.stripMargin).on(
                            "userId"  -> id,
                            "value"   -> entitlement.value,
                            "type"    -> entitlement.`type`,
                            "primary" -> entitlement.primary.getOrElse(false)
                    ).executeInsert()
                } 
                //UserDAO.insertPluralAttributes("entitlements", id, entitlement.value, entitlement.`type`, entitlement.primary).executeInsert()
        
            case None => println("entitlements: Do Nothing")
        }
        roles match {
            case Some(roles) =>
                for(role <- roles) {
                    val tableName = "roles"
                    SQL("""
                        INSERT IGNORE INTO """+tableName+""" (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
                        """.stripMargin).on(
                            "userId"  -> id,
                            "value"   -> role.value,
                            "type"    -> role.`type`,
                            "primary" -> role.primary.getOrElse(false)
                    ).executeInsert()
                }
                //UserDAO.insertPluralAttributes("roles", id, role.value, role.`type`, role.primary).executeInsert()
        
            case None => println("roles: Do Nothing")
        }
        x509Certificates match {
            case Some(x509Certificates) =>
                for(x509Certificate <- x509Certificates) {
                    val tableName = "x509Certificates"
                    SQL("""
                        INSERT IGNORE INTO """+tableName+""" (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
                        """.stripMargin).on(
                            "userId"  -> id,
                            "value"   -> x509Certificate.value,
                            "type"    -> x509Certificate.`type`,
                            "primary" -> x509Certificate.primary.getOrElse(false)
                    ).executeInsert()
                }
                //UserDAO.insertPluralAttributes("x509Certificates", id, x509Certificate.value, x509Certificate.`type`, x509Certificate.primary).executeInsert()
        
            case None => println("x509Certificates: Do Nothing")
        }
         
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
    
     var results = SQL(
        """
         | SELECT *
         | FROM `users`;
        """.stripMargin).on(
     //   "userId" -> user.id
     ).apply()

      results.map { row =>
        val name = Name(
                      Utils.removeEmptyString(row[Option[String]]("formattedName")), 
                      Utils.removeEmptyString(row[Option[String]]("familyName")),
                      Utils.removeEmptyString(row[Option[String]]("givenName")),
                      Utils.removeEmptyString(row[Option[String]]("middleName")),
                      Utils.removeEmptyString(row[Option[String]]("honorificPrefix")),
                      Utils.removeEmptyString(row[Option[String]]("honorificSuffix"))
                      )
        val nameField = name match{
            case Name(None, None, None, None, None, None) => None
            case _ => Some(name)
        }
        val baseUser = BaseUser(
            row[String]("username"), 
            Utils.removeEmptyString(row[Option[String]]("externalId")) ,
            nameField,
            Utils.removeEmptyString(row[Option[String]]("displayName")),
            Utils.removeEmptyString(row[Option[String]]("nickname")),
            Utils.removeEmptyString(row[Option[String]]("profileURL")),
            Utils.removeEmptyString(row[Option[String]]("title")),
            Utils.removeEmptyString(row[Option[String]]("userType")),
            Utils.removeEmptyString(row[Option[String]]("preferredLanguage")),
            Utils.removeEmptyString(row[Option[String]]("locale")),
            Utils.removeEmptyString(row[Option[String]]("timezone")),
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
                      Utils.removeEmptyString(row[Option[String]]("formattedName")), 
                      Utils.removeEmptyString(row[Option[String]]("familyName")),
                      Utils.removeEmptyString(row[Option[String]]("givenName")),
                      Utils.removeEmptyString(row[Option[String]]("middleName")),
                      Utils.removeEmptyString(row[Option[String]]("honorificPrefix")),
                      Utils.removeEmptyString(row[Option[String]]("honorificSuffix"))
                      )
        val nameField = name match{
            case Name(None, None, None, None, None, None) => None
            case _ => Some(name)
        }
        val baseUser = BaseUser(
            row[String]("username"), 
            Utils.removeEmptyString(row[Option[String]]("externalId")) ,
            nameField,
            Utils.removeEmptyString(row[Option[String]]("displayName")),
            Utils.removeEmptyString(row[Option[String]]("nickname")),
            Utils.removeEmptyString(row[Option[String]]("profileURL")),
            Utils.removeEmptyString(row[Option[String]]("title")),
            Utils.removeEmptyString(row[Option[String]]("userType")),
            Utils.removeEmptyString(row[Option[String]]("preferredLanguage")),
            Utils.removeEmptyString(row[Option[String]]("locale")),
            Utils.removeEmptyString(row[Option[String]]("timezone")),
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
                val name: Name = user.baseUser.name.getOrElse( Name(None, None, None, None, None, None))
                val m: Meta = user.meta.getOrElse(Meta(new Date, new Date, None, None, None))
                user.addresses match {
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
                                     "userId" -> user.id,
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
                    case None => println("Do Nothing")
                }
                
                user.emails match {
                    case Some(emails) =>
                        for (email <- emails) {
                            //UserDAO.insertPluralAttributes("emails", user.id, email.value, email.`type`, email.primary).executeInsert()
                            val tableName = "emails"
                            SQL("""
                            INSERT IGNORE INTO """+tableName+""" (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
                            """.stripMargin).on(
                                "userId"  -> user.id,
                                "value"   -> email.value,
                                "type"    -> email.`type`,
                                "primary" -> email.primary.getOrElse(false)
                            ).executeInsert()
                            
                        }
                    case None => println("Do Nothing")
                }
                user.phoneNumbers match {
                    case Some(phoneNumbers) => 
                        for(phoneNumber <- phoneNumbers) {
                            val tableName = "phoneNumbers"
                            SQL("""
                            INSERT IGNORE INTO """+tableName+""" (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
                            """.stripMargin).on(
                                "userId"  -> user.id,
                                "value"   -> phoneNumber.value,
                                "type"    -> phoneNumber.`type`,
                                "primary" -> phoneNumber.primary.getOrElse(false)
                            ).executeInsert()
                            //UserDAO.insertPluralAttributes("phoneNumbers", user.id, phoneNumber.value, phoneNumber.`type`, phoneNumber.primary).executeInsert()
                        }
                    case None => println("Do Nothing")
                }
                user.ims match {
                    case Some(ims) => 
                        for(im <- ims) {
                            val tableName = "ims"
                            SQL("""
                            INSERT IGNORE INTO """+tableName+""" (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
                            """.stripMargin).on(
                                "userId"  -> user.id,
                                "value"   -> im.value,
                                "type"    -> im.`type`,
                                "primary" -> im.primary.getOrElse(false)
                            ).executeInsert()
                        }
                        //UserDAO.insertPluralAttributes("ims", user.id, im.value, im.`type`, im.primary).executeInsert()
                    case None => println("Do Nothing")
                }
                user.photos match {
                    case Some(photos) =>
                        for(photo <- photos) {
                            val tableName = "photos"
                            SQL("""
                            INSERT IGNORE INTO """+tableName+""" (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
                            """.stripMargin).on(
                                "userId"  -> user.id,
                                "value"   -> photo.value,
                                "type"    -> photo.`type`,
                                "primary" -> photo.primary.getOrElse(false)
                            ).executeInsert()
                        }
                        //UserDAO.insertPluralAttributes("photos", user.id, photo.value, photo.`type`, photo.primary).executeInsert()
                
                    case None => println("Do Nothing")
                }
                user.entitlements match {
                    case Some(entitlements) =>
                        for(entitlement <- entitlements) {
                            val tableName = "entitlements"
                            SQL("""
                            INSERT IGNORE INTO """+tableName+""" (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
                            """.stripMargin).on(
                                "userId"  -> user.id,
                                "value"   -> entitlement.value,
                                "type"    -> entitlement.`type`,
                                "primary" -> entitlement.primary.getOrElse(false)
                            ).executeInsert()
                        }
                        //UserDAO.insertPluralAttributes("entitlements", user.id, entitlement.value, entitlement.`type`, entitlement.primary).executeInsert()
                
                    case None => println("Do Nothing")
                }
                user.roles match {
                    case Some(roles) =>
                        for(role <- roles) {
                            val tableName = "roles"
                            SQL("""
                            INSERT IGNORE INTO """+tableName+""" (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
                            """.stripMargin).on(
                                "userId"  -> user.id,
                                "value"   -> role.value,
                                "type"    -> role.`type`,
                                "primary" -> role.primary.getOrElse(false)
                            ).executeInsert()
                        } 
                        //UserDAO.insertPluralAttributes("roles", user.id, role.value, role.`type`, role.primary).executeInsert()
                
                    case None => println("Do Nothing")
                }
                user.x509Certificates match {
                    case Some(x509Certificates) =>
                        for(x509Certificate <- x509Certificates) {
                            val tableName = "x509Certificats"
                            SQL("""
                            INSERT IGNORE INTO """+tableName+""" (`userId`, `value`, `type`, `isPrimary`) VALUES({userId}, {value}, {type}, {primary});
                            """.stripMargin).on(
                                "userId"  -> user.id,
                                "value"   -> x509Certificate.value,
                                "type"    -> x509Certificate.`type`,
                                "primary" -> x509Certificate.primary.getOrElse(false)
                            ).executeInsert()
                        }
                        //UserDAO.insertPluralAttributes("x509Certificates", user.id, x509Certificate.value, x509Certificate.`type`, x509Certificate.primary).executeInsert()
                
                    case None => println("Do Nothing")
                }
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
                    "userId" -> user.id,
                    "externalId" -> user.baseUser.externalId,
                    "username" -> user.baseUser.userName,
                    "formattedName" -> name.formatted,
                    "familyName" -> name.familyName,
                    "givenName" -> name.givenName,
                    "middleName" -> name.middleName,
                    "honorificPrefix" -> name.honorificPrefix,
                    "honorificSuffix" -> name.honorificSuffix,
                    "displayName" -> user.baseUser.displayName,
                    "nickname" -> user.baseUser.nickName,
                    "profileURL" -> user.baseUser.profileUrl,
                    "title" -> user.baseUser.title,
                    "userType" -> user.baseUser.userType,
                    "preferredLanguage" -> user.baseUser.preferredLanguage,
                    "locale" -> user.baseUser.locale,
                    "timezone" -> user.baseUser.timezone,
                    "active" -> user.baseUser.active.getOrElse(false),
                    "password" -> user.baseUser.password,
                    "created" -> m.created,
                    "lastModified" -> m.lastModified
                 ).executeInsert()
            }
        }
        
        }else{
            // patch some fields
            // Not Done Yet.  
        }
    }
    
    def insertPluralAttributes(tableName: String, 
                             userId: String, 
                             value: String,
                             attributeType: String, 
                             primary: Option[Boolean] = None) = {
       
        
    }

}