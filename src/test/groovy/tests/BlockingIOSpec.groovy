package tests

import groovyx.net.http.HTTPBuilder
import spock.lang.Specification
import util.DataSource
import util.Problem

/**
 * Created by rahul on 3/6/15.
 */
class BlockingIOSpec extends Specification {

  def "test computing sums"() {
    given: "A client and an input file"
    def client = new HTTPBuilder('http://localhost:5050/')
    def data = DataSource.reader

    expect: "Sums should match"
    data.lines().
        forEach {
          def problem = Problem.fromLine(it)
          def num1 = Integer.parseInt client.get(path: "/num/${problem.left.lang}/${problem.left.text}").text
          def num2 = Integer.parseInt client.get(path: "/num/${problem.right.lang}/${problem.right.text}").text
          def sum = num1 + num2
          def actual = client.get(path: "/text/${problem.expected.lang}/${sum}").text
          assert actual == problem.expected.text
        }
  }

}
