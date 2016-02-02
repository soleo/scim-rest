import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

@RunWith(classOf[JUnitRunner])
class UserControllerSpec extends Specification {
  sequential
  
  "User Controller" should {
    val headers = FakeHeaders(Seq("Content-type" -> "application/json", "Authorization" -> "Basic c2hhb3hpbmppYW5nQGdtYWlsLmNvbToxMjM0NTY="))
    val uri = "/v1/Users"
    var id: String = "" 

    "create a new user" in new WithApplication{
        val userAsJson = Json.obj(
                "schemas" -> Json.arr("urn:scim:schemas:core:1.0"),
                "userName" -> JsString("bjensen101"),
                "externalId" -> JsString("bjensen101"),
                "name" -> 
                    Json.obj(
                        "formatted" -> JsString("Ms. Barbara J Jensen III"),
                        "familyName" -> JsString("Jensen"),
                        "givenName" -> JsString("Barbara")
                    )
                )
        val fakeRequest = FakeRequest(
                                        method = Helpers.POST,
                                        uri = uri, 
                                        headers = headers, 
                                        body = userAsJson)
        val result = route(fakeRequest)
        result must not be(None)
        val f = result.get
        status(f) must equalTo(CREATED)

        contentType(f) must beSome("application/json")

        val responseNode = contentAsJson(f)
        (responseNode \ "userName").as[String] must equalTo("bjensen101")
        id = (responseNode \ "id").as[String]

    }
    
    "get all users" in new WithApplication {
        val fakeRequest = FakeRequest(
                                method = Helpers.GET,
                                uri = uri,
                                headers = headers,
                                body = ""
                            )
        val result = route(fakeRequest)
        result must not be(None)
        val f = result.get
        status(f) must equalTo(OK)
        contentType(f) must beSome("application/json")
        val resultString = contentAsString(f)
        resultString must contain ("bjensen101")
    }

    "get one user" in new WithApplication {
        if(id.length > 0) {
            val fakeRequest = FakeRequest(
                                    method = Helpers.GET,
                                    uri = uri + "/" + id,
                                    headers = headers,
                                    body = ""
                                )
           
            val result = route(fakeRequest)
            result must not be(None)
            val f = result.get
            status(f) must equalTo(OK)
            contentType(f) must beSome("application/json")
            val resultString = contentAsString(f)
            resultString must contain ("bjensen101")
        }
    }

    "replace one user" in new WithApplication {
        if(id.length > 0) {
             val userAsJson = Json.obj(
                "schemas" -> Json.arr("urn:scim:schemas:core:1.0"),
                "userName" -> JsString("bjensen101"),
                "externalId" -> JsString("sammorgan"),
                "name" -> 
                    Json.obj(
                        "formatted" -> JsString("Ms. Barbara J Jensen III"),
                        "familyName" -> JsString("Jensen"),
                        "givenName" -> JsString("Barbara")
                    )
                )
            val fakeRequest = FakeRequest(
                                    method = Helpers.PUT,
                                    uri = uri + "/" + id,
                                    headers = headers,
                                    body = userAsJson
                                )

            val result = route(fakeRequest)
            result must not be(None)
            val f = result.get
            status(f) must equalTo(OK)
            contentType(f) must beSome("application/json")
            val resultString = contentAsString(f)
            resultString must contain ("sammorgan")
        }
    }

    "delete one user" in new WithApplication {
        if(id.length > 0) {
            val fakeRequest = FakeRequest(
                                    method = Helpers.DELETE,
                                    uri = uri + "/" + id,
                                    headers = headers,
                                    body = ""
                                )
            val result = route(fakeRequest)
            result must not be(None)
            val f = result.get
            status(f) must equalTo(OK)
        }
    }
  }
}
