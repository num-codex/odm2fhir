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
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.CHRONIC_KIDNEY_DISEASES;

import static org.apache.commons.lang3.StringUtils.contains;

import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.CONDITION;

public class ChronicKidneyDiseases extends Item {

  protected Stream<DomainResource> map(FormData formData) {
    var answerCoding = formData.getItemData("chronische_nierenerkrankungen");
    var stageCoding = formData.getItemData("chronische_nierenerkrankungen_schweregrad");
    var generalCoding = formData.getItemData("chronische_nierenerkrankungen_code");

    if (!stageCoding.isEmpty() &&
        (contains(answerCoding.getValue(), "410594000") || contains(answerCoding.getValue(), "261665006"))) {
      stageCoding.setValue(null);
    }

    return answerCoding.isEmpty() && stageCoding.isEmpty() ? Stream.empty() :
        Stream.of(createCondition(generalCoding, answerCoding, stageCoding));
  }

  private Condition createCondition(ItemData generalCoding, ItemData answerCoding, ItemData stageCoding) {
    var condition = (Condition) new Condition()
        .setClinicalStatus(ACTIVE)
        .setVerificationStatus(CONFIRMED)
        .addIdentifier(createIdentifier(CONDITION, generalCoding))
        .setRecordedDateElement(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .addCategory(createCodeableConcept(createCoding(SNOMED_CT, "394589003", "Nephrology (qualifier value)")))
        .setSeverity(createCodeableConcept(stageCoding))
        .setMeta(createMeta(CHRONIC_KIDNEY_DISEASES));

    var codeableConcept = new CodeableConcept();
    for (var coding : createCodings(answerCoding)) {
      switch (coding.getCode()) {
        case "410594000": //NO
          condition.setVerificationStatus(REFUTED).setClinicalStatus(null); //remove clinicalStatus
          updateCodeableConcept(codeableConcept, generalCoding);
          break;
        case "261665006": //UNKNOWN
          condition.setVerificationStatus(null).setClinicalStatus(null) //remove verificationStatus and clinicalStatus
              .addModifierExtension(DATA_PRESENCE_UNKNOWN);
          updateCodeableConcept(codeableConcept, generalCoding);
          break;
        case "unknown": //UNKNOWN
          break;
        default:
          codeableConcept.addCoding(coding);
          break;
      }
    }

    return codeableConcept.isEmpty() ? new Condition() : condition.setCode(codeableConcept);
  }

}