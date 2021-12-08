package de.difuture.uds.odm2fhir.fhir.mapper.outcome_at_discharge.item;

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

import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.DISCHARGE_DISPOSITION;

import static org.hl7.fhir.r4.model.Observation.ObservationStatus.FINAL;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.OBSERVATION;

public class TypeOfDischarge extends Item {

  protected Stream<DomainResource> map(FormData formData) {
    var answerCoding = formData.getItemData("entlassungsart");
    var generalCoding = formData.getItemData("entlassungsart_code");

    return answerCoding.isEmpty() ? Stream.empty() : Stream.of(createObservation(generalCoding, answerCoding));
  }

  private Observation createObservation(ItemData generalCoding, ItemData answerCoding) {
    return (Observation) new Observation()
        .addIdentifier(createIdentifier(OBSERVATION, generalCoding)
                           .setType(OBI).setAssigner(getOrganizationReference()))
        .setStatus(FINAL)
        .setEffective(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .addCategory(SOCIAL_HISTORY)
        .setCode(createCodeableConcept(generalCoding).setText("type of Discharge"))
        .setValue(createCodeableConcept(answerCoding))
        .setMeta(createMeta(DISCHARGE_DISPOSITION));
  }

}