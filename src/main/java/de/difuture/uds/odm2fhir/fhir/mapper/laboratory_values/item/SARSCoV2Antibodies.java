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
import de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition;
import de.difuture.uds.odm2fhir.odm.model.FormData;
import de.difuture.uds.odm2fhir.odm.model.ItemData;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Type;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.LOINC;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.SARS_COV_2_AB_PNL_SER_PL_IA;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.SARS_COV_2_AB_SER_PL_IA_ACNC;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.SARS_COV_2_AB_SER_PL_QL_IA;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.SARS_COV_2_IGA_SER_PL_IA_ACNC;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.SARS_COV_2_IGA_SER_PL_QL_IA;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.SARS_COV_2_IGG_SER_PL_IA_ACNC;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.SARS_COV_2_IGG_SER_PL_QL_IA;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.SARS_COV_2_IGM_SER_PL_IA_ACNC;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.SARS_COV_2_IGM_SER_PL_QL_IA;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import static org.hl7.fhir.r4.model.Observation.ObservationStatus.FINAL;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.OBSERVATION;

import static org.springframework.util.CollectionUtils.isEmpty;

import static java.lang.String.format;
import static java.util.function.Predicate.not;

public class SARSCoV2Antibodies extends Item {

  private static final List<String> PARAMETERS = List.of("ab_pnl", "ab_ia_ql", "ab_ia_qn", "ig_a_ia_ql", "ig_a_ia_qn",
                                                         "ig_g_ia_ql", "ig_g_ia_qn", "ig_m_ia_ql", "ig_m_ia_qn");

  private static final Map<String, NUMStructureDefinition> PROFILES = Map.of("ab_pnl", SARS_COV_2_AB_PNL_SER_PL_IA,
                                                                             "ab_ia_ql", SARS_COV_2_AB_SER_PL_QL_IA,
                                                                             "ab_ia_qn", SARS_COV_2_AB_SER_PL_IA_ACNC,
                                                                             "ig_a_ia_ql", SARS_COV_2_IGA_SER_PL_QL_IA,
                                                                             "ig_a_ia_qn", SARS_COV_2_IGA_SER_PL_IA_ACNC,
                                                                             "ig_g_ia_ql", SARS_COV_2_IGG_SER_PL_QL_IA,
                                                                             "ig_g_ia_qn", SARS_COV_2_IGG_SER_PL_IA_ACNC,
                                                                             "ig_m_ia_ql", SARS_COV_2_IGM_SER_PL_QL_IA,
                                                                             "ig_m_ia_qn", SARS_COV_2_IGM_SER_PL_IA_ACNC);

  public Stream<DomainResource> map(FormData formData) {
    // Extremely ugly and hacky workaround for non-existent panel parameter in form... :-/
    return PARAMETERS.stream()
        .map(key -> "sarsco_v2_covid19_" + key)
        .map(formData::getItemData)
        .filter(not(ItemData::isEmpty))
        .findFirst()
        .map(itemData -> {
          itemData = itemData.copy().setItemOID(PARAMETERS.get(0));

          itemData.getItemGroupData().getItemData()
                  .addAll(List.of(itemData, itemData.copy().setItemOID(itemData.getItemOID() + "_code")
                                                           .setValue(HL7_OID + ".6.1_94504-8")));

          var observation = createObservation(formData, itemData);

          return Stream.concat(
              Stream.of(observation),
              PARAMETERS.stream()
                  .skip(1)
                  .map(formData::getItemData)
                  .filter(not(ItemData::isEmpty))
                  .map(labValue -> (DomainResource) createObservation(formData, labValue))
                  .peek(obs -> observation.addHasMember(new Reference(format("%s/%s", OBSERVATION.toCode(), obs.getId())))));
        })
        .orElse(Stream.empty());
  }

  private Observation createObservation(FormData formData, ItemData labValue) {
    var labValueName = labValue.getItemOID();

    var generalCoding = formData.getItemData(labValueName + "_code");
    var dateCoding = formData.getItemData("labor_datum");

    Type value = null;
    switch (substringAfterLast(labValueName, "_")) {
      case "qn" -> {
        var unit = getLabUnit(labValueName, formData.getItemData(labValueName + "_unit").getValue());
        value = createQuantity(formData.getItemData(labValueName), unit, unit);
      }
      case "ql" -> {
        var codings = createCodings(formData.getItemData(labValueName));
        if (!isEmpty(codings)) {
          value = new CodeableConcept().setCoding(codings);
        }
      }
    }

    var loincCoding = formData.getItemData(labValueName + "_loinc");

    var usableCodings = !loincCoding.isEmpty() ? createLabCodings(loincCoding) : createCodings(generalCoding);

    var identifier = createIdentifier(OBSERVATION, generalCoding).setType(OBI).setAssigner(getOrganizationReference());

    return (Observation) new Observation()
        .addIdentifier(identifier)
        .setStatus(FINAL)
        .setEffective(createDateTimeType(dateCoding))
        .addCategory(LABORATORY.copy().addCoding(createCoding(LOINC, "26436-6", "Laboratory studies (set)")))
        .setValue(value)
        .setCode(new CodeableConcept().setCoding(usableCodings).setText(labValueName)) // TODO Add parameter name as text!!!
        .setId(sha256Hex(identifier.getSystem() + identifier.getValue())) // This really needs to be and stay here!!!
        .setMeta(createMeta(PROFILES.get(removeStart(labValueName, "sarsco_v2_covid19_"))));
  }

}