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
          def left = getNumber(client, problem.left.lang, problem.left.text)
          def right = getNumber(client, problem.right.lang, problem.right.text)
          left.zipWith(right, { a, b -> a + b }).
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

  def "test computing sums 2"() {
    given: "A client and an input file"
    def client = new AsyncHttpClient()
    def lines = DataSource.lines

    expect: "Sums should match"

    def problems = from(lines).map                      { Problem.fromLine(it) }
    def leftNum = problems.flatMap                      { getNumber(client, it.left.lang, it.left.text) }
    def rightNum = problems.flatMap                     { getNumber(client, it.right.lang, it.right.text) }
    def sums = leftNum.zipWith(rightNum)                { a, b -> a + b }
    def problemSumPairs = problems.zipWith(sums)        { a, b -> [a, b] }
    def strings = problemSumPairs.flatMap               { problem, sum -> getText(client, problem.expected.lang, sum) }
    def problemsWithStrings = problems.zipWith(strings) { a, b -> [a, b] }

    problemsWithStrings.
        toBlocking(). // This is required only for the test. In production code you never have to do this
        forEach { problem, result ->
          println "$result == ${problem.expected.text}"
          assert result == problem.expected.text
        }

  }

}
