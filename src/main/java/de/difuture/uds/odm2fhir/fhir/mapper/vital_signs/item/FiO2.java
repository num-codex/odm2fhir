package de.difuture.uds.odm2fhir.fhir.mapper.vital_signs.item;

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

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.LOINC;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.INHALED_OXYGEN_CONCENTRATION;

import static org.hl7.fhir.r4.model.Observation.ObservationStatus.FINAL;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.OBSERVATION;

public class FiO2 extends Item {

  protected Stream<DomainResource> map(FormData formData) {
    var answerCoding = formData.getItemData("fi_o2");
    var generalCoding = formData.getItemData("fi_o2_code");
    var dateCoding = formData.getItemData("vitalparameter_datum");

    return answerCoding.isEmpty() ? Stream.empty() : Stream.of(createObservation(generalCoding, answerCoding, dateCoding));
  }

  private Observation createObservation(ItemData generalCoding, ItemData answerCoding, ItemData dateCoding) {
    return (Observation) new Observation()
        .addIdentifier(createIdentifier(OBSERVATION, generalCoding).setType(OBI).setAssigner(getOrganizationReference()))
        .setStatus(FINAL)
        .setEffective(createDateTimeType(dateCoding))
        .addCategory(LABORATORY.copy()
            .addCoding(createCoding(LOINC, "26436-6", "Laboratory studies (set)"))
            .addCoding(createCoding(LOINC, "18767-4", "Blood gas studies (set)")))
        .setCode(createCodeableConcept(generalCoding).setText("Oxygen [Partial pressure] in blood"))
        .setValue(createQuantity(answerCoding, "%", "%"))
        .setMeta(createMeta(INHALED_OXYGEN_CONCENTRATION));
  }

}