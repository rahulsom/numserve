package tests

import com.ning.http.client.AsyncCompletionHandler
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.Response
import rx.Observable
import spock.lang.Specification
import util.DataSource
import util.Problem

import static rx.Observable.create
import static rx.Observable.from

/**
 * Created by rahul on 3/6/15.
 */
class RxJavaSpec extends Specification {
  Observable<Integer> getNumber(AsyncHttpClient client, String lang, String text) {
    create { subscriber ->
      client.
          prepareGet("http://localhost:5050/num2").
          addQueryParam('lang', lang).
          addQueryParam('text', text).
          execute({ Response response ->
            subscriber.onNext Integer.valueOf(response.responseBody)
            subscriber.onCompleted()
          } as AsyncCompletionHandler)
    }
  }

  Observable<String> getText(AsyncHttpClient client, String lang, Integer num) {
    create { subscriber ->
      client.
          prepareGet("http://localhost:5050/text2").
          addQueryParam('lang', lang).
          addQueryParam('num', num.toString()).
          execute({ Response response ->
            subscriber.onNext response.responseBody
            subscriber.onCompleted()
          } as AsyncCompletionHandler)
    }
  }

  def "test computing sums"() {
    given: "A client and an input file"
    def client = new AsyncHttpClient()
    def lines = DataSource.lines

    expect: "Sums should match"

    from(lines).
        flatMap { line ->
          def problem = Problem.fromLine(line)
          def o1 = getNumber(client, problem.left.lang, problem.left.text)
          def o2 = getNumber(client, problem.right.lang, problem.right.text)
          o1.zipWith(o2, { a, b -> a + b }).
              flatMap { Integer sum ->
                def sumString = getText(client, problem.expected.lang, sum)
                sumString.map { ss -> [ss, problem] }
              }
        }.
        toBlocking(). // This is required only for the test. In production code you never have to do this
        forEach { result, problem ->
          println "$result == ${problem.expected.text}"
          assert result == problem.expected.text
        }

  }

}
