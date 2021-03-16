package de.difuture.uds.odm2fhir.fhir.mapper.laboratory_values.item;

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
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Observation;

import java.util.List;
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.LOINC;
import static de.difuture.uds.odm2fhir.fhir.util.CommonStructureDefinition.MI_I_OBSERVATION_LAB;

import static org.apache.commons.lang3.StringUtils.isBlank;

import static org.hl7.fhir.r4.model.Observation.ObservationStatus.FINAL;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.OBSERVATION;

import static java.util.function.Predicate.not;

public class LaboratoryValue extends Item {

  private static final List<String> PARAMETERS = List.of("crp", "ferritin", "bilirubin", "ddimer", "gammagt", "gotast",
      "ldh", "kardiale_troponine", "hamoglobin", "kreatinin", "laktat", "leukozyten_absolut", "lymphozyten_absolut",
      "neutrophile_absolut", "ptt", "thrombozyten_absolut", "inr", "serumalbumin", "antithrombin_iii",
      "pct_procalcitonin", "il6_interleukin_6", "ntprobnp", "fibrinogen");

  public Stream<DomainResource> map(FormData formData) {
    return PARAMETERS.stream()
        .map(formData::getItemData)
        .filter(not(ItemData::isEmpty))
        .map(labValue -> createObservation(formData, labValue));
  }

  private Observation createObservation(FormData formData, ItemData labValue) {
    var labValueName = labValue.getItemOID();

    var dateCoding = formData.getItemData("labor_datum");

    var observation = (Observation) new Observation()
        .addIdentifier(createIdentifier(OBSERVATION, formData.getItemData(labValueName))
                           .setType(OBI).setAssigner(getOrganizationReference()))
        .setStatus(FINAL)
        .setEffective(createDateTimeType(dateCoding))
        .addCategory(LABORATORY.copy().addCoding(createCoding(LOINC.getUrl(), "26436-6")))
        .setMeta(createMeta(MI_I_OBSERVATION_LAB));

    var unitCounter = formData.getItemData(labValueName + "_unit").getValue();

    var codings = createCodings(formData.getItemData(labValueName + "_code"));
    var specificCodings = createLabCodings(formData.getItemData(labValueName + "_loinc_" + unitCounter));

    if (!specificCodings.isEmpty()) { //if no Unit is chosen, use all codes
      codings = specificCodings;
    }

    var unit = getLabUnit(labValueName, unitCounter);

    return codings.isEmpty() || isBlank(labValueName) || isBlank(unit) || formData.getItemData(labValueName).isEmpty() ?
        new Observation() :
        observation.setCode(new CodeableConcept()
        .setCoding(codings)
        .setText(labValueName)) //add Parameter Name as Text
        .setValue(createQuantity(formData.getItemData(labValueName), unit, unit));
  }

}