package de.difuture.uds.odm2fhir.fhir.mapper.symptoms.item;

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

import java.util.List;
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.SYMPTOMS_COVID_19;
import static de.difuture.uds.odm2fhir.util.EnvironmentProvider.ENVIRONMENT;

import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.CONDITION;

import static java.util.function.Predicate.not;

public class SymptomConditions extends Item {

  private final static List<String> SYMPTOMS = List.of("geruchs_bzw_geschmacksstoerungen", "bauchschmerzen",
                                                       "bewusstseinsstoerungen_verwirrtheit", "durchfall", "erbrechen",
                                                       "husten", "kurzatmigkeit_dyspnoe", "uebelkeit", "fieber",
                                                       "kopfschmerzen", "andere_symptome");

  protected Stream<DomainResource> map(FormData formData) {
    var generalSymptomCoding = formData.getItemData("symptome_code");

    return !"1".equals(formData.getItemData("symptome").getValue()) ? Stream.empty() :
        SYMPTOMS.stream()
            .map(key -> "symptome_" + key)
            .map(formData::getItemData)
            .filter(not(ItemData::isEmpty))
            .flatMap(symptom -> createConditions(formData, generalSymptomCoding, symptom));
  }

  private Stream<DomainResource> createConditions(FormData formData, ItemData generalSymptomCoding, ItemData specificCoding) {
    var condition = (Condition) new Condition()
        .addIdentifier(createIdentifier(CONDITION, specificCoding))
        .setClinicalStatus(ACTIVE)
        .setRecordedDateElement(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .addCategory(createCodeableConcept(generalSymptomCoding))
        .setSeverity(createCodeableConcept(formData.getItemData(specificCoding.getItemOID() + "_schweregrad")))
        .setMeta(createMeta(SYMPTOMS_COVID_19));

    var specificCodings = createCodings(specificCoding);

    var codeCodeableConcept = new CodeableConcept();
    for (var coding : specificCodings) {
      switch (coding.getCode()) {
        case "410605003" -> condition.setClinicalStatus(ACTIVE).setVerificationStatus(CONFIRMED); //PRESENT
        case "410594000" -> condition.setVerificationStatus(REFUTED); //ABSENT
        case "261665006" -> condition.addModifierExtension(DATA_PRESENCE_UNKNOWN); //UNKNOWN
        case "74964007" -> { //Answer = Sonstige/Other
          codeCodeableConcept.addCoding(coding.setDisplay("Other (qualifier value)"));
          if (!formData.getItemData("symptome_andere_symptome_textfeld").isEmpty()) {
            codeCodeableConcept.setText(formData.getItemData("symptome_andere_symptome_textfeld").getValue());
          }
        }
        default -> { //SYMPTOM CODING
          if (!"symptome_geruchs_bzw_geschmacksstoerungen".equals(specificCoding.getItemOID())) {
            codeCodeableConcept.addCoding(coding);
          }
        }
      }
    }

    if ("symptome_geruchs_bzw_geschmacksstoerungen".equals(specificCoding.getItemOID()) && specificCodings.size() == 2) {
      var codeableConceptTASTE = new CodeableConcept().addCoding(specificCodings.get(0));
      var codeableConceptSMELL = new CodeableConcept().addCoding(specificCodings.get(1));

      var identifier = condition.getIdentifierFirstRep();

      // TODO Check if really nothing should be returned in case only either taste or smell is empty
      return codeableConceptTASTE.isEmpty() || codeableConceptSMELL.isEmpty() ? Stream.empty() :
             Stream.of(condition.copy()
                                .setCode(codeableConceptTASTE)
                                .setIdentifier(List.of(identifier.copy().setValue(identifier.getValue() + "_geschmack"))),
                       condition.copy()
                                .setCode(codeableConceptSMELL)
                                .setIdentifier(List.of(identifier.copy().setValue(identifier.getValue() + "_geruch"))));
    }

    if (ENVIRONMENT.getProperty("fhir.others.removed", Boolean.class, true) &&
        codeCodeableConcept.getCoding().stream().map(Coding::getCode).anyMatch("74964007"::equals)) {
      codeCodeableConcept.setCoding(null).setText(null);
    }

    return codeCodeableConcept.isEmpty() ? Stream.empty() : Stream.of(condition.setCode(codeCodeableConcept));
  }

}