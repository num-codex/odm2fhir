package de.difuture.uds.odm2fhir.fhir.mapper.complications.item;

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

import java.util.List;
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.COMPLICATIONS_COVID_19;

import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.CONDITION;

import static org.apache.commons.lang3.StringUtils.contains;

public class ComplicationConditions extends Item {

  private static final List<String> COMPLICATIONS = List.of(
      "komplikation_thrombembolische_ereignisse",
      "komplikation_venoese_thrombose",
      "komplikation_lungenarterienembolie",
      "komplikation_stroke",
      "komplikation_myokardinfarkt",
      "komplikation_andere",
      "komplikation_pulmonale_co_infektionen",
      "komplikation_blutstrominfektionen");

  public Stream<DomainResource> map(FormData formData) {
    var generalComplicationCoding = formData.getItemData("komplikation_code");

    return !"1".equals(formData.getItemData("komplikation").getValue()) ? Stream.empty() :
        COMPLICATIONS.stream().map(key -> createCondition(generalComplicationCoding, formData.getItemData(key)));
  }

  private Condition createCondition(ItemData generalComplicationCoding, ItemData specificComplicationCoding) {
    var condition = (Condition) new Condition()
        .addIdentifier(createIdentifier(CONDITION, specificComplicationCoding))
        .setRecordedDateElement(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .addCategory(createCodeableConcept(generalComplicationCoding))
        .setMeta(createMeta(COMPLICATIONS_COVID_19));

    if (specificComplicationCoding.isEmpty() ||
        (contains(specificComplicationCoding.getValue(), "410605003") &&
            "komplikation_thrombembolische_ereignisse".equals(specificComplicationCoding.getItemOID()))) {
      //if Answer.isEmpty()=true, skip resource
      //if Answer=YES, skip general resource & create more specific resources
      return new Condition();
    }

    var codeableConcept = new CodeableConcept();
    for (var coding : createCodings(specificComplicationCoding)) {
      switch (coding.getCode()) {
        case "410605003": //confirmed
          condition.setClinicalStatus(ACTIVE).setVerificationStatus(CONFIRMED);
          break;
        case "410594000": //refuted
          condition.setVerificationStatus(REFUTED);
          break;
        case "261665006": //unknown
          condition.addModifierExtension(DATA_PRESENCE_UNKNOWN);
          break;
        default: //Condition-codes
          codeableConcept.addCoding(coding);
      }
    }

    return codeableConcept.isEmpty() ? new Condition() : condition.setCode(codeableConcept);
  }

}