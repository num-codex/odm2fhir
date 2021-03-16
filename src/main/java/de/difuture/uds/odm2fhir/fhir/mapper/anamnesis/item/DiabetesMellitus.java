package de.difuture.uds.odm2fhir.fhir.mapper.anamnesis.item;

/*
 * Copyright (C) 2021 DIFUTURE (https://difuture.de)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import de.difuture.uds.odm2fhir.fhir.mapper.Item;
import de.difuture.uds.odm2fhir.odm.model.FormData;
import de.difuture.uds.odm2fhir.odm.model.ItemData;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DomainResource;

import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.SNOMED_CT;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.DIABETES_MELLITUS;

import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.CONDITION;

public class DiabetesMellitus extends Item {

  @Override
  protected Stream<DomainResource> map(FormData formData) {
    var answerCoding = formData.getItemData("diabetes");
    var resourceCoding = formData.getItemData("diabetes_code");

    return answerCoding.isEmpty() ? Stream.empty() : Stream.of(createCondition(resourceCoding, answerCoding));
  }

  private Condition createCondition(ItemData resourceCoding, ItemData answerCoding) {
    var condition = (Condition) new Condition()
        .addIdentifier(createIdentifier(CONDITION, resourceCoding))
        .setRecordedDateElement(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .addCategory(createCodeableConcept(createCoding(SNOMED_CT, "408475000", "Diabetic medicine (qualifier value)")))
        .setMeta(createMeta(DIABETES_MELLITUS));

    var codeableConcept = new CodeableConcept();
    for (var coding : createCodings(answerCoding)) {
      switch (coding.getCode()) {
        case "410594000": //NO
          condition.setVerificationStatus(REFUTED);
          updateCodeableConcept(codeableConcept, resourceCoding);
          break;
        case "unknown":
          break; //do nothing
        case "261665006": //UNKNOWN
          condition.addModifierExtension(DATA_PRESENCE_UNKNOWN);
          updateCodeableConcept(codeableConcept, resourceCoding);
          break;
        default: //assuming YES
          condition.setVerificationStatus(CONFIRMED);
          codeableConcept.addCoding(coding);
          break;
      }
    }

    return codeableConcept.isEmpty() ? new Condition() : condition.setCode(codeableConcept);
  }

}