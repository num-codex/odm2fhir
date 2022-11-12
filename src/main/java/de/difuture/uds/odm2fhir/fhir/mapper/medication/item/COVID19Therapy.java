package de.difuture.uds.odm2fhir.fhir.mapper.medication.item;

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
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.MedicationStatement.MedicationStatementStatus;

import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.PHARMACOLOGICAL_THERAPY;
import static de.difuture.uds.odm2fhir.util.EnvironmentProvider.ENVIRONMENT;

import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.endsWithAny;

import static org.hl7.fhir.r4.model.MedicationStatement.MedicationStatementStatus.NOTTAKEN;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.MEDICATIONSTATEMENT;

import static java.util.function.Predicate.not;

public class COVID19Therapy extends Item {

  protected Stream<DomainResource> map(FormData formData) {
    var itemGroupData = formData.getItemGroupData("medikation.covid19therapie_antipyretika");
    var generalPresence = formData.getItemData("covid19therapie");
    var dateCoding = formData.getItemData("medikation_datum");

    //übergeordnete Frage nicht mit "JA" beantwortet = keine Medikation
    return !"1".equals(generalPresence.getValue()) ? Stream.empty() :
        itemGroupData.getItemData().stream()
            .filter(itemData -> !endsWithAny(itemData.getItemOID(), "_code", "_textfeld"))
            .filter(not(ItemData::isEmpty))
            .map(specificCoding ->
                createMedicationStatement(specificCoding,
                    formData.getItemData(specificCoding.getItemOID() +
                        (endsWith(specificCoding.getItemOID(), "_andere") ? "_textfeld" : "_code")), dateCoding));
  }

  private MedicationStatement createMedicationStatement(ItemData specificCoding, ItemData accurateCodingOrText, ItemData dateCoding) {
    var medicationStatement = (MedicationStatement) new MedicationStatement()
        .addIdentifier(createIdentifier(MEDICATIONSTATEMENT, specificCoding))
        .setEffective(createDateTimeType(dateCoding))
        .setMeta(createMeta(PHARMACOLOGICAL_THERAPY));

    var medicationCodeableConcept = new CodeableConcept();
    for (var coding : createCodings(specificCoding)) {
      switch (coding.getCode()) {
        case "410605003" -> medicationStatement.setStatus(MedicationStatementStatus.ACTIVE);
        case "410594000" -> medicationStatement.setStatus(NOTTAKEN);
        case "261665006" -> medicationStatement.setStatus(MedicationStatementStatus.UNKNOWN);
        case "74964007" -> { //Answer = Sonstige/Other
          medicationCodeableConcept.addCoding(coding.setDisplay("Other (qualifier value)"));
          if (endsWith(accurateCodingOrText.getItemOID(), "_textfeld") && !accurateCodingOrText.isEmpty()) {
            medicationCodeableConcept.setText(accurateCodingOrText.getValue());
          }
        }
        default -> { //add Medication Codes
          if (accurateCodingOrText.isEmpty()) {
            medicationCodeableConcept.addCoding(coding);
          }
        }
      }
    }

    //insert coding of used medication
    if (!accurateCodingOrText.isEmpty() && endsWith(accurateCodingOrText.getItemOID(), "_code")) {
      for (var coding : createCodings(accurateCodingOrText)) {
        medicationCodeableConcept.addCoding(coding);
      }
    }

    if (ENVIRONMENT.getProperty("fhir.others.removed", Boolean.class, true) &&
        medicationCodeableConcept.getCoding().stream().map(Coding::getCode).anyMatch("74964007"::equals)) {
      medicationCodeableConcept.setCoding(null).setText(null);
    }

    return medicationCodeableConcept.isEmpty() ? new MedicationStatement() : medicationStatement.setMedication(medicationCodeableConcept);
  }

}