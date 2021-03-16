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
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;

import java.util.Optional;
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.LOINC;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.SNOMED_CT;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.BLOOD_PRESSURE;

import static org.hl7.fhir.r4.model.Observation.ObservationStatus.FINAL;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.OBSERVATION;

public class BloodPressure extends Item {

  public Stream<DomainResource> map(FormData formData) {
    var systolicValue = formData.getItemData("blutdruck_systolisch");
    var systolicCoding = formData.getItemData("blutdruck_systolisch_code");
    var diastolicValue = formData.getItemData("blutdruck_diastolisch");
    var diastolicCoding = formData.getItemData("blutdruck_diastolisch_code");
    var dateCoding = formData.getItemData("vitalparameter_datum");

    return systolicValue.isEmpty() && diastolicValue.isEmpty() ? Stream.empty() :
        Stream.of(createObservation(systolicValue, systolicCoding, diastolicValue, diastolicCoding, dateCoding));
  }

  private Observation createObservation(ItemData systolicValue, ItemData systolicCoding,
                                        ItemData diastolicValue, ItemData diastolicCoding, ItemData dateCoding) {
    return (Observation) new Observation()
        .addIdentifier(createIdentifier(OBSERVATION, systolicCoding).setType(OBI).setAssigner(getOrganizationReference()))
        .setStatus(FINAL)
        .setEffective(createDateTimeType(dateCoding))
        .setCode(createCodeableConcept(
            createCoding(LOINC, "85354-9", "Blood pressure panel with all children optional"),
            createCoding(SNOMED_CT, "75367002", "Blood pressure (observable entity)"))
            .setText("Blood pressure"))
        .addCategory(VITAL_SIGNS)
        .addComponent(Optional.of(createQuantity(systolicValue, "mm[Hg]", "mmHg"))
            .map(quantity -> new ObservationComponentComponent()
                .setCode(createCodeableConcept(systolicCoding).setText("Systolic blood pressure"))
                .setValue(quantity)).orElse(null))
        .addComponent(Optional.of(createQuantity(diastolicValue, "mm[Hg]", "mmHg"))
            .map(quantity -> new ObservationComponentComponent()
                .setCode(createCodeableConcept(diastolicCoding).setText("Diastolic blood pressure"))
                .setValue(quantity)).orElse(null))
        .setMeta(createMeta(BLOOD_PRESSURE));
  }

}