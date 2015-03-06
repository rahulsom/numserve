package numserve

import com.ibm.icu.text.RuleBasedNumberFormat
import com.ibm.icu.util.ULocale
import groovy.transform.Memoized

/**
 * Created by rahulsomasunderam on 04/03/15.
 */
@Singleton
class NumberHelper {

  @Memoized
  RuleBasedNumberFormat getNumberFormat(String language) {
    try {
      new RuleBasedNumberFormat(ULocale.forLanguageTag(language), RuleBasedNumberFormat.SPELLOUT)
    } catch (Exception e) {
      null
    }
  }

  String toText(String language, String number) {
    def format = getNumberFormat(language)
    if (format) {
      format.format(new BigDecimal(number))
    } else {
      throw new IllegalArgumentException("Invalid language '$language'")
    }
  }

  String toNum(String language, String text) {
    def format = getNumberFormat(language)
    if (format) {
      format.parse(text)
    } else {
      throw new IllegalArgumentException("Invalid language '$language'")
    }
  }
}
