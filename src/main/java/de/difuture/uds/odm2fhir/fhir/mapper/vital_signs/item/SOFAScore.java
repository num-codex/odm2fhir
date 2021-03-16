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
import de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition;
import de.difuture.uds.odm2fhir.odm.model.FormData;
import de.difuture.uds.odm2fhir.odm.model.ItemData;

import org.apache.commons.lang3.StringUtils;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.NUMCodeSystem.ECRF_PARAMETER_CODES;
import static de.difuture.uds.odm2fhir.fhir.util.NUMCodeSystem.SOFA_SCORE;

import static org.apache.commons.lang3.StringUtils.chop;

import static org.hl7.fhir.r4.model.Observation.ObservationStatus.FINAL;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.OBSERVATION;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

public class SOFAScore extends Item {

  private static final Map<String, String> DISPLAYS, DEFINITIONS;

  static {
    DISPLAYS = new HashMap<>();

    DISPLAYS.put("resp", "Respiratory system");
    DISPLAYS.put("resp0", "Respiratory system SOFA score 0");
    DISPLAYS.put("resp1", "Respiratory system SOFA score 1");
    DISPLAYS.put("resp2", "Respiratory system SOFA score 2");
    DISPLAYS.put("resp3", "Respiratory system SOFA score 3");
    DISPLAYS.put("resp4", "Respiratory system SOFA score 4");

    DISPLAYS.put("ns", "Nervous system");
    DISPLAYS.put("ns0", "Nervous system SOFA score 0");
    DISPLAYS.put("ns1", "Nervous system SOFA score 1");
    DISPLAYS.put("ns2", "Nervous system SOFA score 2");
    DISPLAYS.put("ns3", "Nervous system SOFA score 3");
    DISPLAYS.put("ns4", "Nervous system SOFA score 4");

    DISPLAYS.put("cvs", "Cardiovascular system");
    DISPLAYS.put("cvs0", "Cardiovascular system SOFA score 0");
    DISPLAYS.put("cvs1", "Cardiovascular system SOFA score 1");
    DISPLAYS.put("cvs2", "Cardiovascular system SOFA score 2");
    DISPLAYS.put("cvs3", "Cardiovascular system SOFA score 3");
    DISPLAYS.put("cvs4", "Cardiovascular system SOFA score 4");

    DISPLAYS.put("liv", "Liver");
    DISPLAYS.put("liv0", "Liver SOFA score 0");
    DISPLAYS.put("liv1", "Liver SOFA score 1");
    DISPLAYS.put("liv2", "Liver SOFA score 2");
    DISPLAYS.put("liv3", "Liver SOFA score 3");
    DISPLAYS.put("liv4", "Liver SOFA score 4");

    DISPLAYS.put("coa", "Coagulation");
    DISPLAYS.put("coa0", "Coagulation SOFA score 0");
    DISPLAYS.put("coa1", "Coagulation SOFA score 1");
    DISPLAYS.put("coa2", "Coagulation SOFA score 2");
    DISPLAYS.put("coa3", "Coagulation SOFA score 3");
    DISPLAYS.put("coa4", "Coagulation SOFA score 4");

    DISPLAYS.put("kid", "Kidneys");
    DISPLAYS.put("kid0", "Kidneys SOFA score 0");
    DISPLAYS.put("kid1", "Kidneys SOFA score 1");
    DISPLAYS.put("kid2", "Kidneys SOFA score 2");
    DISPLAYS.put("kid3", "Kidneys SOFA score 3");
    DISPLAYS.put("kid4", "Kidneys SOFA score 4");

    DEFINITIONS = new HashMap<>();

    DEFINITIONS.put("resp", "SOFA Respiratory system scoring category");
    DEFINITIONS.put("resp0", "PaO2/FiO2 [mmHg (kPa)] ≥ 400 (53.3)");
    DEFINITIONS.put("resp1", "PaO2/FiO2 [mmHg (kPa)] < 400 (53.3)");
    DEFINITIONS.put("resp2", "PaO2/FiO2 [mmHg (kPa)] < 300 (40)");
    DEFINITIONS.put("resp3", "PaO2/FiO2 [mmHg (kPa)] < 200 (26.7) and mechanically ventilated");
    DEFINITIONS.put("resp4", "PaO2/FiO2 [mmHg (kPa)] < 100 (13.3) and mechanically ventilated");

    DEFINITIONS.put("ns", "SOFA Nervous system scoring category");
    DEFINITIONS.put("ns0", "Glasgow Coma Scale (GCS) 15");
    DEFINITIONS.put("ns1", "Glasgow Coma Scale (GCS) 13-14");
    DEFINITIONS.put("ns2", "Glasgow Coma Scale (GCS) 10-12");
    DEFINITIONS.put("ns3", "Glasgow Coma Scale (GCS) 6-9");
    DEFINITIONS.put("ns4", "Glasgow Coma Scale (GCS) < 6");

    DEFINITIONS.put("cvs", "SOFA Cardiovascular system scoring category");
    DEFINITIONS.put("cvs0", "Mean arterial pressure (definitions) ≥ 70 mmHg");
    DEFINITIONS.put("cvs1", "Mean arterial pressure (definitions) < 70 mmHg");
    DEFINITIONS.put("cvs2", "Administration of dopamine ≤ 5 ug/kg/min or dobutamine (any dose)");
    DEFINITIONS.put("cvs3", "Administration of dopamine > 5 ug/kg/min OR epinephrine ≤ 0.1 ug/kg/min OR norepinephrine ≤ 0.1 ug/kg/min");
    DEFINITIONS.put("cvs4", "Administration of dopamine > 15 ug/kg/min OR epinephrine > 0.1 ug/kg/min OR norepinephrine > 0.1 ug/kg/min");

    DEFINITIONS.put("liv", "SOFA Liver scoring category");
    DEFINITIONS.put("liv0", "Bilirubin (mg/dl) [umol/L] < 1.2 [< 20]");
    DEFINITIONS.put("liv1", "Bilirubin (mg/dl) [umol/L] 1.2-1.9 [20-32]");
    DEFINITIONS.put("liv2", "Bilirubin (mg/dl) [umol/L] 2.0-5.9 [33-101]");
    DEFINITIONS.put("liv3", "Bilirubin (mg/dl) [umol/L] 6.0-11.9 [102-204]");
    DEFINITIONS.put("liv4", "Bilirubin (mg/dl) [umol/L] > 12.0 [> 204]");

    DEFINITIONS.put("coa", "SOFA Coagulation scoring category");
    DEFINITIONS.put("coa0", "Platelets×10^3/ul ? 150");
    DEFINITIONS.put("coa1", "Platelets×10^3/ul < 150");
    DEFINITIONS.put("coa2", "Platelets×10^3/ul < 100");
    DEFINITIONS.put("coa3", "Platelets×10^3/ul < 50");
    DEFINITIONS.put("coa4", "Platelets×10^3/ul < 20");

    DEFINITIONS.put("kid", "SOFA Kidneys scoring category");
    DEFINITIONS.put("kid0", "Creatinine (mg/dl) [umol/L] (or urine output) < 1.2 [< 110]");
    DEFINITIONS.put("kid1", "Creatinine (mg/dl) [umol/L] (or urine output) 1.2-1.9 [110-170]");
    DEFINITIONS.put("kid2", "Creatinine (mg/dl) [?mol/L] (or urine output) 2.0-3.4 [171-299]");
    DEFINITIONS.put("kid3", "Creatinine (mg/dl) [umol/L] (or urine output) 3.5-4.9 [300-440] (or < 500 ml/d)");
    DEFINITIONS.put("kid4", "Creatinine (mg/dl) [umol/L] (or urine output) > 5.0 [> 440] (or < 200 ml/d)");
  }

  public Stream<DomainResource> map(FormData formData) {
    var sofaTotalScore = formData.getItemData("sofa_total_score");
    var dateCoding = formData.getItemData("vitalparameter_datum");

    var itemDatas = Stream.of(
        formData.getItemData("sofa_score_resp"),
        formData.getItemData("sofa_score_ns"),
        formData.getItemData("sofa_score_kid"),
        formData.getItemData("sofa_score_cvs"),
        formData.getItemData("sofa_score_liv"),
        formData.getItemData("sofa_score_coa"))
        .filter(not(ItemData::isEmpty))
        .collect(toList());

    return itemDatas.isEmpty() ? Stream.empty() : Stream.of(createObservation(itemDatas, sofaTotalScore, dateCoding));
  }

  private Observation createObservation(List<ItemData> itemDatas, ItemData sofaTotalScore, ItemData dateCoding) {
    var observation = (Observation) new Observation()
        .addIdentifier(createIdentifier(OBSERVATION, sofaTotalScore).setType(OBI).setAssigner(getOrganizationReference()))
        .setStatus(FINAL)
        .setEffective(createDateTimeType(dateCoding))
        .addCategory(SURVEY)
        .setCode(createCodeableConcept(createCoding(ECRF_PARAMETER_CODES, "06", "SOFA-Score"))
            .setText("Sepsis-related organ failure assessment score"))
        .setMeta(createMeta(NUMStructureDefinition.SOFA_SCORE));

    itemDatas.stream()
        .map(ItemData::getValue)
        .filter(StringUtils::isNotBlank)
        .forEach(code -> observation.addComponent(new ObservationComponentComponent()
            .setCode(createCodeableConcept(createCoding(SOFA_SCORE, chop(code), getDisplay(chop(code))))
                .setText(getDefinition(chop(code))))
            .setValue(createCodeableConcept(createCoding(SOFA_SCORE, code, getDisplay(code)))
                .setText(getDefinition(code)))));

    if (sofaTotalScore.isEmpty()) {
      observation.setDataAbsentReason(UNKNOWN);
    } else {
      observation.setValue(new IntegerType().setValue(Integer.valueOf(sofaTotalScore.getValue())));
    }

    return observation;
  }

  public String getDisplay(String code) {
    return DISPLAYS.getOrDefault(code, "No Display");
  }

  public String getDefinition(String code) {
    return DEFINITIONS.getOrDefault(code, "No Definition");
  }

}