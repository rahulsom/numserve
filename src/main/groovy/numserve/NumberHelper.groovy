package numserve

import com.ibm.icu.text.RuleBasedNumberFormat

/**
 * Created by rahulsomasunderam on 04/03/15.
 */
@Singleton
class NumberHelper {
  RuleBasedNumberFormat english = new RuleBasedNumberFormat("""
 zero; one; two; three; four; five; six; seven; eight; nine;
 ten; eleven; twelve; thirteen; fourteen; fifteen; sixteen; seventeen; eighteen; nineteen;
 20: twenty[->>];
 30: thirty{->>];
 40: forty[->>];
 50: fifty[->>];
 60: sixty[->>];
 70: seventy[->>];
 80: eighty[->>];
 90: ninety[->>];
 100: << hundred[ >>];
 1000: << thousand[ >>];
 """.replace('\n', ''))

  String toText(String language, String number) {
    if (language == 'en') {
      english.format(new BigDecimal(number))
    } else {
      throw new IllegalArgumentException("Invalid language '$language'")
    }
  }
  String toNum(String language, String text) {
    if (language == 'en') {
      english.parse(text)
    } else {
      throw new IllegalArgumentException("Invalid language '$language'")
    }
  }
}
