package tests

import groovyx.net.http.HTTPBuilder
import spock.lang.Specification
import util.DataSource
import util.Problem

/**
 * Created by rahul on 3/6/15.
 */
class BlockingIOSpec extends Specification {

  def "test single threaded"() {
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

  def "test multi threaded"() {
    given: "A client and an input file"
    def data = DataSource.reader

    expect: "Sums should match"
    def threads = data.lines().
        map {
          def problem = Problem.fromLine(it)
          def solution = new Solution(problem: problem, client:  new HTTPBuilder('http://localhost:5050/'))
          def t1 = new Thread() {
            @Override
            void run() {
              super.run()
              solution.num1 = Integer.parseInt new HTTPBuilder('http://localhost:5050/').
                  get(path: "/num/${problem.left.lang}/${problem.left.text}").text
              solution.eval()
            }
          }
          def t2 = new Thread() {
            @Override
            void run() {
              super.run()
              solution.num2 = Integer.parseInt new HTTPBuilder('http://localhost:5050/').
                  get(path: "/num/${problem.right.lang}/${problem.right.text}").text
              solution.eval()
            }
          }
          [t1, t2]
        }.toArray().flatten()
    threads*.start()
    threads*.join()
  }

  class Solution {
    Problem problem
    HTTPBuilder client
    Integer num1
    Integer num2
    void eval() {
      if (num1 && num2) {
        def sum = num1 + num2
        def actual = client.get(path: "/text/${problem.expected.lang}/${sum}").text
        assert actual == problem.expected.text
      }
    }
  }


}
