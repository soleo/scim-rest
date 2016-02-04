package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.data.validation._
import java.util.UUID

import models.User._
import models._
import models.dao.UserDAO

object BaseUser {
    //val schemaValidation = Reads.StringReads.filter(ValidationError("Possible values : urn:scim:schemas:core:1.0"))(Seq("urn:scim:schemas:core:1.0").contains(_))
    //val UsernameRegex = "[0-9a-zA-Z.]{2,20}".r
    
    implicit val baseUserWrites  = Json.writes[BaseUser]
    
    implicit val baseUserReads  = Json.reads[BaseUser]
    // add validation for profileUrl, preferredLanguage, timezone, locale and schema
}

case class BaseUser(
    userName: String,
    externalId: Option[String] = None,
    name: Option[Name] = None,
    displayName: Option[String] = None,
    nickName: Option[String] = None,
    profileUrl: Option[String] = None,
    title: Option[String] = None,
    userType: Option[String] = None,
    preferredLanguage: Option[String] = None,
    locale: Option[String] = None,
    timezone: Option[String] = None,
    active: Option[Boolean] = None,
    var password: Option[String] = None,
    schemas: List[String] = List("urn:scim:schemas:core:1.0")
)