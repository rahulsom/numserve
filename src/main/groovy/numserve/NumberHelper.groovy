package numserve

import com.ibm.icu.text.RuleBasedNumberFormat
import groovy.transform.Memoized
import groovy.util.logging.Slf4j

/**
 * Created by rahulsomasunderam on 04/03/15.
 */
@Singleton
@Slf4j
class NumberHelper {

  static validFormats = '''af am ar az be bg bs ca cs cy da de ee el en eo es es_419 es_AR es_BO es_CL es_CO es_CR
      es_CU es_DO es_EC es_GT es_HN es_MX es_NI es_PA es_PE es_PR es_PY es_SV es_US es_UY es_VE et fa fa_AF fi fil
      fo fr fr_BE fr_CH ga he hi hr hu hy id is it ja ka kl km ko ky lo lt lv mk ms mt nb nci nl nn pl pt pt_AO
      pt_GW pt_MO pt_MZ pt_PT pt_ST pt_TL ro ru se sk sl sq sr sr_Latn sv ta th tr uk vi zh zh_Hant zh_Hant_HK
  '''.replaceAll(' +', ' ').split(' ')

  @Memoized
  RuleBasedNumberFormat getNumberFormat(String language) {
    if (validFormats.contains(language)) {
      try {
        def locale = Locale.forLanguageTag(language)
        def numberFormat = new RuleBasedNumberFormat(locale, RuleBasedNumberFormat.SPELLOUT)
        log.info "Created Formatter for '${language}'"
        return numberFormat
      } catch (Exception e) {
        log.warn "Could not create Formatter for '${language}'"
        null
      }
    } else {
      log.warn "'${language}' is not a valid language code"
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
