import com.jetbrains.handson.httpapi.module
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import org.junit.Before

import kotlin.test.assertEquals

import com.jetbrains.handson.httpapi.models.*


class CustomerRouteTests {
    @Before
    fun clearCustomers() {
      customerStorage.clear()
    }

    @Test
    fun testGetCustomer() {
        withTestApplication({ module(testing = true) }) {
            customerStorage.add(Customer(id="1234", firstName="Bruce", lastName="Wayne", email="bruce@wayne.com"))
            handleRequest(HttpMethod.Get, "/customer/1234").apply {
              assertEquals(
                """{"id":"1234","firstName":"Bruce","lastName":"Wayne","email":"bruce@wayne.com"}""",
                response.content
              )
              assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testListCustomers() {
      withTestApplication({ module(testing = true) }) {
          customerStorage.add(Customer(id="1234", firstName="Bruce", lastName="Wayne", email="bruce@wayne.com"))
          customerStorage.add(Customer(id="5678", firstName="Alfred", lastName="Pennyworth", email="alfred@wayne.com"))
          customerStorage.add(Customer(id="1111", firstName="Dick", lastName="Grayson", email="dick@wayne.com"))

          handleRequest(HttpMethod.Get, "/customer").apply {
            assertEquals(
              """[{"id":"1234","firstName":"Bruce","lastName":"Wayne","email":"bruce@wayne.com"},{"id":"5678","firstName":"Alfred","lastName":"Pennyworth","email":"alfred@wayne.com"},{"id":"1111","firstName":"Dick","lastName":"Grayson","email":"dick@wayne.com"}]""",
              response.content
            )
            assertEquals(HttpStatusCode.OK, response.status())
          }
      }
    }

    @Test
    fun testCreateCustomer() {
      withTestApplication({ module(testing = true) }) {
        handleRequest(HttpMethod.Post, "/customer") {
          addHeader("Content-Type", "application/json")
          setBody("""{"id":"1234","firstName":"Bruce","lastName":"Wayne","email":"bruce@wayne.com"}""")
        }.response.let {
          assertEquals(
            """Customer stored correctly""",
            it.content
          )
          assertEquals(HttpStatusCode.Accepted, it.status())
          assertEquals(customerStorage.count(), 1)
        }
      }
    }

    @Test
    fun deleteCustomer() {
       withTestApplication({ module(testing = true) }) {
          customerStorage.add(Customer(id="1234", firstName="Bruce", lastName="Wayne", email="bruce@wayne.com"))
          assertEquals(customerStorage.count(), 1)

         handleRequest(HttpMethod.Delete, "/customer/1234").apply {
            assertEquals(
              """Customer removed correctly""",
              response.content
            )
            assertEquals(HttpStatusCode.Accepted, response.status())
            assertEquals(customerStorage.count(), 0)
         }
       }
    }

    @Test
    fun deleteNonExistentCustomer() {
      withTestApplication({ module(testing = true) }) {
         handleRequest(HttpMethod.Delete, "/customer/6666").apply {
            assertEquals(
              """Not Found""",
              response.content
            )
            assertEquals(HttpStatusCode.NotFound, response.status())
         }
      }
    }
}
