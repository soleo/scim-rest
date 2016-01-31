import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class UserControllerSpec extends Specification {
  sequential
  
  "User Controller" should {

    "create a new user" in new WithApplication{
      //val obj = Json.obj()
    //   val input:JsValue = obj + ("firstName" -> JsString("pawan")) + ("lastName" -> JsString("kumar")) + ("fullName" -> JsString("pawan kumar")) + ("birthday" -> JsString(new Date().toString))
    //   //println(input.toString())
    //   val uri = controllers.routes.UserController.create().url
    //   //println("Uri:" + uri)
    //   val fakeRequest = FakeRequest(method = Helpers.POST,uri = uri, headers = FakeHeaders(Seq("Content-type"->"application/json")), body = input)
    //   val result = route(fakeRequest)
    //   result must not be(None)
    //   val f = result.get
    //   //println("RESULT:" + contentAsString(f))
    //   status(f) must equalTo(CREATED)
    //   //println(f)
      
    //   contentType(f) must beSome("application/json")
    }
    
    "get all users" in new WithApplication{
        val fakeRequest = FakeRequest(
                                method = Helpers.GET,
                                uri = "/v1/Users",
                                headers = FakeHeaders(
                                        Seq(
                                            "Content-type" -> "application/json",
                                            "Authorization" -> "Basic c2hhb3hpbmppYW5nQGdtYWlsLmNvbToxMjM0NTY="
                                        )
                                ),
                                body = ""
                            )
        val result = route(fakeRequest)
        result must not be(None)
        val f = result.get
        status(f) must equalTo(OK)
        contentType(f) must beSome("application/json")
        val resultString = contentAsString(f)
        //println(resultString)
        resultString must contain ("xinjiang")
    }
  }
}
