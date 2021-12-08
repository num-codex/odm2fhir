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

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.SNOMED_CT;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.CARDIOVASCULAR_DISEASES;

import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.equalsAny;

import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.CONDITION;

import static java.util.function.Predicate.not;

public class CardiovascularDiseases extends Item {

  protected Stream<DomainResource> map(FormData formData) {
    var itemGroupData = formData.getItemGroupData("anamnese_risikofaktoren.herzkreislauferkrankungen_bluthochdruck");
    var generalPresence = formData.getItemData("herzkreislauferkrankungen");

    return createCodings(generalPresence).stream()
        .map(Coding::getCode)
        .anyMatch(code -> equalsAny(code, "410594000", "261665006")) ? Stream.empty() : // generalPresence != YES
            itemGroupData.getItemData().stream()
                .filter(itemData -> !endsWith(itemData.getItemOID(), "_textfeld"))
                .filter(not(ItemData::isEmpty))
                .map(itemData -> createCondition(itemData, formData.getItemData(itemData.getItemOID() + "_textfeld")));
  }

  private Condition createCondition(ItemData resourceCoding, ItemData textValue) {
    var condition = (Condition) new Condition()
        .addIdentifier(createIdentifier(CONDITION, resourceCoding))
        .setRecordedDateElement(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .addCategory(createCodeableConcept(createCoding(SNOMED_CT, "722414000", "Vascular medicine (qualifier value)")))
        .setMeta(createMeta(CARDIOVASCULAR_DISEASES));

    var codeableConcept = new CodeableConcept();
    for (var coding : createCodings(resourceCoding)) {
      switch (coding.getCode()) {
        case "410605003" -> condition.setClinicalStatus(ACTIVE).setVerificationStatus(CONFIRMED); //YES
        case "410594000" -> condition.setVerificationStatus(REFUTED); //NO
        case "261665006" -> condition.addModifierExtension(DATA_PRESENCE_UNKNOWN); //UNKNOWN
        default -> codeableConcept.addCoding(coding); //add ConditionCoding
      }

      if ("<49601007".equals(coding.getCode()) && !textValue.isEmpty()) {
        codeableConcept.setText(textValue.getValue());
      }
    }

    return codeableConcept.isEmpty() ? new Condition() : condition.setCode(codeableConcept);
  }

}