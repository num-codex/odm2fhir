package de.difuture.uds.odm2fhir.fhir.mapper.study_enrollment.item;

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
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.STUDY_INCLUSION_COVID_19;

import static org.hl7.fhir.r4.model.Observation.ObservationStatus.FINAL;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.OBSERVATION;

public class StudyEnrollmentCOVID19 extends Item {

  public Stream<DomainResource> map(FormData formData) {
    var answerCoding = formData.getItemData("covid19_aufnahme");

    return answerCoding.isEmpty() ? Stream.empty() : Stream.of(createObservation(answerCoding));
  }

  private Observation createObservation(ItemData answerCoding) {
    var observation = (Observation) new Observation()
        .addIdentifier(createIdentifier(OBSERVATION, answerCoding).setType(OBI).setAssigner(getOrganizationReference()))
        .setStatus(FINAL)
        .setEffective(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .addCategory(SURVEY)
        .setCode(createCodeableConcept(createCoding(ECRF_PARAMETER_CODES, "02", "Study inclusion due to Covid-19"))
            .setText("Confirmed Covid-19 diagnosis as main reason for enrolment in the study"))
        .setMeta(createMeta(STUDY_INCLUSION_COVID_19));

    var valueConcept = createCodeableConcept(answerCoding);
    return valueConcept.isEmpty() ? new Observation() : observation.setValue(valueConcept);
  }

}