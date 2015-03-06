package util
/**
 * Created by rahul on 3/6/15.
 */
class DataSource {
  static getReader() {
    DataSource.classLoader.getResourceAsStream('input.10.csv').newReader()
  }
}
