/*
 * Copyright 2013 http4s.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.http4s
package headers

class AcceptLanguageSpec extends HeaderLaws {
  val english = LanguageTag("en")
  val spanish = LanguageTag("es")

  "is satisfied by a language tag if the q value is > 0" in {
    prop { (h: `Accept-Language`, cc: LanguageTag) =>
      h.qValue(cc) > QValue.Zero ==> h.satisfiedBy(cc)
    }
  }

  "is not satisfied by a language tag if the q value is 0" in {
    prop { (h: `Accept-Language`, cc: LanguageTag) =>
      !(`Accept-Language`(h.values.map(_.copy(q = QValue.Zero))).satisfiedBy(cc))
    }
  }

  "matches most specific tag" in {
    val acceptLanguage = `Accept-Language`(
      LanguageTag.*,
      LanguageTag("de", qValue"0.3", List("DE", "1996")),
      LanguageTag("de", qValue"0.1"),
      LanguageTag("de", qValue"0.2", List("DE")))
    acceptLanguage.qValue(LanguageTag("de")) must_== qValue"0.1"
    acceptLanguage.qValue(LanguageTag("de", subTags = List("DE"))) must_== qValue"0.2"
    acceptLanguage.qValue(LanguageTag("de", subTags = List("DE", "1996"))) must_== qValue"0.3"
    acceptLanguage.qValue(LanguageTag("de", subTags = List("DE", "2017"))) must_== qValue"0.2"
  }

  "matches splatted if primary tag not present" in {
    val acceptLanguage = `Accept-Language`(LanguageTag.*, spanish.withQValue(qValue"0.5"))
    acceptLanguage.qValue(english) must_== QValue.One
  }

  "rejects language tag matching primary tag with q=0" in {
    val acceptLanguage = `Accept-Language`(LanguageTag.*, english.withQValue(QValue.Zero))
    acceptLanguage.qValue(english) must_== QValue.Zero
  }

  "rejects language tag matching splat with q=0" in {
    val acceptLanguage =
      `Accept-Language`(LanguageTag.*.withQValue(QValue.Zero), spanish.withQValue(qValue"0.5"))
    acceptLanguage.qValue(english) must_== QValue.Zero
  }

  "rejects unmatched language tag" in {
    val acceptLanguage = `Accept-Language`(spanish.withQValue(qValue"0.5"))
    acceptLanguage.qValue(english) must_== QValue.Zero
  }
}
