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
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DomainResource;

import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.ICD_10_GM;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.SNOMED_CT;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.ORGAN_RECIPIENT;

import static org.apache.commons.lang3.StringUtils.equalsAny;

import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.CONDITION;

import static java.util.function.Predicate.not;

public class OrganRecipient extends Item {

  public Stream<DomainResource> map(FormData formData) {
    var itemGroupData = formData.getItemGroupData("anamnese_risikofaktoren.organtransplantiert_herz");
    var generalPresence = formData.getItemData("organtransplantiert");

    return createCodings(generalPresence).stream()
        .map(Coding::getCode)
        .anyMatch(code -> equalsAny(code, "410594000", "261665006")) ? Stream.empty() : // generalPresence != YES
//        Stream.of(createCondition(generalPresence)) :


        itemGroupData.getItemData()
            .stream()
            .filter(not(ItemData::isEmpty))
            .map(this::createCondition);
  }

  private Condition createCondition(ItemData transplantCoding) {
    var condition = (Condition) new Condition()
        .addIdentifier(createIdentifier(CONDITION, transplantCoding))
        .setRecordedDateElement(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .addCategory(createCodeableConcept(createCoding(SNOMED_CT, "788415003", "Transplant medicine (qualifier value)")))
        .setMeta(createMeta(ORGAN_RECIPIENT));

    var codeCodeableConcept = new CodeableConcept();
    var bodySiteCodeableConcept = new CodeableConcept();

    for (var coding : createCodings(transplantCoding)) {
      switch (coding.getCode()) {
        case "410605003": //YES
          condition.setClinicalStatus(ACTIVE).setVerificationStatus(CONFIRMED);
          break;
        case "410594000": //NO
          condition.setVerificationStatus(REFUTED);
          break;
        case "261665006": //UNKNOWN
          condition.addModifierExtension(DATA_PRESENCE_UNKNOWN);
          break;
        default:
          if (ICD_10_GM.getUrl().equals(coding.getSystem())) { //add ICD Code
            codeCodeableConcept.addCoding(coding);
          } else if (SNOMED_CT.getUrl().equals(coding.getSystem())) { //add SNOMED Code of bodySite
            bodySiteCodeableConcept.addCoding(coding);
          }
      }
    }

    if (!bodySiteCodeableConcept.isEmpty()) {
      condition.addBodySite(bodySiteCodeableConcept);
    }

    //bodySite CAN be empty, but Code MUST NOT be empty
    return codeCodeableConcept.isEmpty() ? new Condition() : condition.setCode(codeCodeableConcept);
  }

}