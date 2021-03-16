package de.difuture.uds.odm2fhir.fhir.mapper.onset_of_illness.item;

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
import org.hl7.fhir.r4.model.Condition.ConditionStageComponent;
import org.hl7.fhir.r4.model.DomainResource;

import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.SNOMED_CT;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.DIAGNOSIS_COVID_19;

import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.CONDITION;

public class StageAtDiagnosis extends Item {

  public Stream<DomainResource> map(FormData formData) {
    var answerCoding = formData.getItemData("erkrankungsphase_zum_zeitpunkt_der_diagnose");
    var generalCoding = formData.getItemData("erkrankungsphase_zum_zeitpunkt_der_diagnose_code");

    return answerCoding.isEmpty() ? Stream.empty() : Stream.of(createCondition(generalCoding, answerCoding));
  }

  private Condition createCondition(ItemData generalCoding, ItemData answerCoding) {
    var condition = (Condition) new Condition()
        .addIdentifier(createIdentifier(CONDITION, generalCoding))
        .setRecordedDateElement(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .addCategory(createCodeableConcept(createCoding(SNOMED_CT, "394807007", "Infectious diseases (specialty) (qualifier value)")))
        .setClinicalStatus(ACTIVE)
        .setCode(createCodeableConcept(createCoding(SNOMED_CT, "840539006",
            "Disease caused by Severe acute respiratory syndrome coronavirus 2 (disorder)")))
        .setVerificationStatus(CONFIRMED)
        .setMeta(createMeta(DIAGNOSIS_COVID_19));

    var summaryCodeableConcept = createCodeableConcept(answerCoding);
    var typeCodeableConcept = createCodeableConcept(generalCoding);

    if (!summaryCodeableConcept.getCoding().isEmpty() && !typeCodeableConcept.getCoding().isEmpty()) {
      condition.addStage(new ConditionStageComponent().setSummary(summaryCodeableConcept).setType(typeCodeableConcept));
    }

    return condition;
  }

}