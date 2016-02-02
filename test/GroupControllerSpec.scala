import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._


@RunWith(classOf[JUnitRunner])
class GroupControllerSpec extends Specification {

  "GroupController" should {
    val headers = FakeHeaders(Seq("Content-type" -> "application/json", "Authorization" -> "Basic c2hhb3hpbmppYW5nQGdtYWlsLmNvbToxMjM0NTY="))
    val uri = "/v1/Groups"
    val groupId: String = "e9e30dba-f08f-4109-8486-d5c6a331660a" 
    
    "get all groups" in new WithApplication{
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
        resultString must contain ("Developer")
    }

    "patch one group with new member addition" in new WithApplication{
        val groupAsJson = Json.obj(
                "schemas" -> Json.arr("urn:scim:schemas:core:1.0"),
                "members" -> 
                    Json.arr(
                        Json.obj(
                            "display" -> JsString("Babs Jensen"),
                            "value" -> JsString("2819c223-7f76-453a-919d-413861904646")
                        )
                    )
                )
        val fakeRequest = FakeRequest(
                                method = Helpers.PATCH,
                                uri = uri + "/" + groupId,
                                headers = headers,
                                body = groupAsJson
                            )
        val result = route(fakeRequest)
        result must not be(None)
        val f = result.get
        status(f) must equalTo(OK)
        contentType(f) must beSome("application/json")
        val resultString = contentAsString(f)
        resultString must contain ("2819c223-7f76-453a-919d-413861904646")
    }

    "patch one group with remove one member" in new WithApplication{
        val groupAsJson = Json.obj(
                "schemas" -> Json.arr("urn:scim:schemas:core:1.0"),
                "members" -> 
                    Json.arr(
                        Json.obj(
                            "display" -> JsString("Babs Jensen"),
                            "value" -> JsString("2819c223-7f76-453a-919d-413861904646"),
                            "operation" -> JsString("delete")
                        )
                    )
                )
        val fakeRequest = FakeRequest(
                                method = Helpers.PATCH,
                                uri = uri + "/" + groupId,
                                headers = headers,
                                body = groupAsJson
                            )
        val result = route(fakeRequest)
        result must not be(None)
        val f = result.get
        status(f) must equalTo(OK)
        contentType(f) must beSome("application/json")
        val resultString = contentAsString(f)
        resultString must not contain ("2819c223-7f76-453a-919d-413861904646")
    }
  }
}
