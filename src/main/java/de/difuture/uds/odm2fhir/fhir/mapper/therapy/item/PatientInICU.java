package de.difuture.uds.odm2fhir.fhir.mapper.therapy.item;

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

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Observation;

import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.NUMCodeSystem.ECRF_PARAMETER_CODES;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.PATIENT_IN_ICU;

import static org.hl7.fhir.r4.model.Observation.ObservationStatus.FINAL;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.OBSERVATION;

public class PatientInICU extends Item {

  public Stream<DomainResource> map(FormData formData) {
    var answerCoding = formData.getItemData("liegt_der_patient_auf_der_intensivstationq");

    return answerCoding.isEmpty() ? Stream.empty() : Stream.of(createObservation(answerCoding));
  }

  private Observation createObservation(ItemData answerCoding) {
    return (Observation) new Observation()
        .addIdentifier(createIdentifier(OBSERVATION, answerCoding))
        .setStatus(FINAL)
        .setEffective(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .addCategory(SURVEY)
        .setCode(createCodeableConcept(createCoding(ECRF_PARAMETER_CODES, "01", "Is the patient in the intensive care unit?")))
        .setValue(createCodeableConcept(answerCoding))
        .setMeta(createMeta(PATIENT_IN_ICU));
  }

}