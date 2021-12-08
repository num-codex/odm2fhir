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

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.MedicationStatement.MedicationStatementStatus;

import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.PHARMACOLOGICAL_THERAPY;

import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.MEDICATIONSTATEMENT;

public class Immunoglobulins extends Item {

  protected Stream<DomainResource> map(FormData formData) {
    var answerCoding = formData.getItemData("immunglobuline");
    var generalCoding = formData.getItemData("immunglobuline_code");
    var dateCoding = formData.getItemData("medikation_datum");

    return answerCoding.isEmpty() || generalCoding.isEmpty() ? Stream.empty() :
        Stream.of(createMedicationStatement(generalCoding, answerCoding, dateCoding));
  }

  private MedicationStatement createMedicationStatement(ItemData resourceCoding, ItemData answerCoding, ItemData dateCoding) {
    MedicationStatementStatus medicationStatementStatus = null;

    // TODO Check loop as only the last value in codings is currently actually used
    for (var coding : createCodings(answerCoding)) {
      try {
        medicationStatementStatus = MedicationStatementStatus.fromCode(coding.getCode());
      } catch (FHIRException fhirException) {
        logInvalidValue(MEDICATIONSTATEMENT, answerCoding);
      }
    }

    return (MedicationStatement) new MedicationStatement()
        .addIdentifier(createIdentifier(MEDICATIONSTATEMENT, resourceCoding))
        .setStatus(medicationStatementStatus)
        .setEffective(createDateTimeType(dateCoding))
        .setMedication(createCodeableConcept(resourceCoding).setText("immunoglobulins"))
        .setMeta(createMeta(PHARMACOLOGICAL_THERAPY));
  }

}