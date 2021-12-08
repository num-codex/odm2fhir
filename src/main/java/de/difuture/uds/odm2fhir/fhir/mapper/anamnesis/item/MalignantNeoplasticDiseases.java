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

import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DomainResource;

import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.SNOMED_CT;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.MALIGNANT_NEOPLASTIC_DISEASE;

import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.CONDITION;

public class MalignantNeoplasticDiseases extends Item {

  protected Stream<DomainResource> map(FormData formData) {
    var answerCoding = formData.getItemData("aktive_tumorkrebserkrankungen");
    var generalCoding = formData.getItemData("aktive_tumorkrebserkrankungen_code");

    return answerCoding.isEmpty() ? Stream.empty() : Stream.of(createCondition(generalCoding, answerCoding));
  }

  private Condition createCondition(ItemData generalCoding, ItemData answerCoding) {
    var condition = (Condition) new Condition()
        .addIdentifier(createIdentifier(CONDITION, generalCoding))
        .setRecordedDateElement(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .addCategory(createCodeableConcept(createCoding(SNOMED_CT, "394593009", "Medical oncology (qualifier value)")))
        .setMeta(createMeta(MALIGNANT_NEOPLASTIC_DISEASE));

    for (var coding : createCodings(answerCoding)) {
      switch (coding.getCode()) {
        case "active":
        case "remission":
          condition.setClinicalStatus(ACTIVE).setVerificationStatus(CONFIRMED);
          break;
        case "410594000":
          condition.setVerificationStatus(REFUTED);
          break;
        default:
        case "261665006":
          condition.addModifierExtension(DATA_PRESENCE_UNKNOWN);
          break;
        case "unknown": //already caught by case "261665006"
      }
    }

    var codeableConcept = createCodeableConcept(generalCoding);
    return codeableConcept.isEmpty() ? new Condition() : condition.setCode(codeableConcept);
  }

}