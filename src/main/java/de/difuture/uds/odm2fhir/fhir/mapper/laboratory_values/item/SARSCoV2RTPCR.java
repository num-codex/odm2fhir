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

import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.LOINC;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.SARS_COV_2_RT_PCR;

import static org.hl7.fhir.r4.model.Observation.ObservationStatus.FINAL;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.OBSERVATION;

public class SARSCoV2RTPCR extends Item {

  public Stream<DomainResource> map(FormData formData) {
    var dateCoding = formData.getItemData("labor_datum");
    var generalCoding = formData.getItemData("sarsco_v2rtpcr_code");
    var answerCoding = formData.getItemData("sarsco_v2rtpcr");
    var loincCoding = formData.getItemData("sarsco_v2rtpcr_loinc");

    return answerCoding.isEmpty() ? Stream.empty() : Stream.of(createObservation(generalCoding, loincCoding, answerCoding, dateCoding));
  }

  private Observation createObservation(ItemData generalCoding, ItemData loincCoding, ItemData answerCoding, ItemData dateCoding) {
    var valueCodeableConcept = new CodeableConcept();
    for (var coding : createCodings(answerCoding)) {
      switch (coding.getCode()) { //add coding.display and codeableConcept.text
        case "260373001" -> {
          coding.setDisplay("Detected (qualifier value)");
          valueCodeableConcept.setText("SARS-CoV-2-RNA positiv");
        }
        case "260415000" -> {
          coding.setDisplay("Not detected (qualifier value)");
          valueCodeableConcept.setText("SARS-CoV-2-RNA negativ");
        }
        case "419984006" -> {
          coding.setDisplay("Inconclusive (qualifier value)");
          valueCodeableConcept.setText("SARS-CoV-2-RNA nicht eindeutig");
        }
      }
      valueCodeableConcept.addCoding(coding);
    }

    var usableCodings = !loincCoding.isEmpty() ? createLabCodings(loincCoding) : createCodings(generalCoding);

    return valueCodeableConcept.isEmpty() ? new Observation() :
        (Observation) new Observation()
            .addIdentifier(createIdentifier(OBSERVATION, generalCoding).setType(OBI).setAssigner(getOrganizationReference()))
            .setStatus(FINAL)
            .setEffective(createDateTimeType(dateCoding))
            .addCategory(LABORATORY.copy().addCoding(createCoding(LOINC, "26436-6", "Laboratory studies (set)")))
            .setValue(valueCodeableConcept)
            .setCode(new CodeableConcept().setCoding(usableCodings).setText("SARS-CoV-2-RNA (PCR)"))
            .setMeta(createMeta(SARS_COV_2_RT_PCR));
  }

}