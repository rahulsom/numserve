package tests

import groovyx.net.http.AsyncHTTPBuilder
import spock.lang.Specification
import util.DataSource
import util.Problem

import java.util.concurrent.Future

/**
 * Created by rahul on 3/6/15.
 */
class AsyncIOSpec extends Specification {

  def "test computing sums"() {
    given: "A client and an input file"
    AsyncHTTPBuilder client = new AsyncHTTPBuilder(
        uri: 'http://localhost:5050/',
        // contentType: ContentType.TEXT,
        // poolSize: 4,
    )
    def data = DataSource.reader

    expect: "Sums should match"
    data.lines().
        map {
          def problem = Problem.fromLine(it)
          def solution = new Solution(problem: problem, client:  client)
          Future f1 = client.get(path: "/num/${problem.left.lang}/${problem.left.text}") {
            solution.num1 = Integer.parseInt it.entity.content.text
            solution.eval()
          }
          Future f2 = client.get(path: "/num/${problem.right.lang}/${problem.right.text}") {
            solution.num2 = Integer.parseInt it.entity.content.text
            solution.eval()
          }
          println "Submitted"
          [f1, f2]
        }.
        toArray().
        each {
          it[0].get()
          it[1].get()
        }
  }

  class Solution {
    Problem problem
    AsyncHTTPBuilder client
    Integer num1
    Integer num2
    void eval() {
      if (num1 && num2) {
        def sum = num1 + num2
        client.get(path: "/text/${problem.expected.lang}/${sum}") {
          def actual = it.entity.content.text
          assert actual == problem.expected.text
        }
      }
    }
  }

}
