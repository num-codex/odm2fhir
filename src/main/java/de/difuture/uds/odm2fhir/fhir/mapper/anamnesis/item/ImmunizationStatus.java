package de.difuture.uds.odm2fhir.fhir.mapper.anamnesis.item;

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
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Immunization.ImmunizationProtocolAppliedComponent;
import org.hl7.fhir.r4.model.StringType;

import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.NO_IMMUNIZATION_INFO_UV_IPS;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.SNOMED_CT;
import static de.difuture.uds.odm2fhir.util.EnvironmentProvider.ENVIRONMENT;

import static org.apache.commons.lang3.StringUtils.endsWithAny;

import static org.hl7.fhir.r4.model.Immunization.ImmunizationStatus.COMPLETED;
import static org.hl7.fhir.r4.model.Immunization.ImmunizationStatus.NOTDONE;

import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.IMMUNIZATION;

public class ImmunizationStatus extends Item {

  protected Stream<DomainResource> map(FormData formData) {
    var itemGroupData = formData.getItemGroupData("anamnese_risikofaktoren.impfungen_influenza");

    return itemGroupData.isEmpty() ? Stream.empty() :
        itemGroupData.getItemData().stream()
            .map(ItemData::getItemOID)
            .filter(itemOID -> !endsWithAny(itemOID, "_datum", "_textfeld"))
            .filter(itemOID -> !formData.getItemData(itemOID).isEmpty())
            .map(itemOID -> createImmunization(
                formData.getItemData(itemOID),
                formData.getItemData(itemOID + "_datum"),
                formData.getItemData(itemOID + "_textfeld")));
  }

  @SuppressWarnings("fallthrough")
  private Immunization createImmunization(ItemData immunizationCoding, ItemData dateCoding, ItemData textValue) {
    var immunization = (Immunization) new Immunization()
        .addIdentifier(createIdentifier(IMMUNIZATION, immunizationCoding))
        .setOccurrence(createDateTimeType(dateCoding))
        .setMeta(createMeta(NUMStructureDefinition.IMMUNIZATION));

    var vaccineCodeableCoding = new CodeableConcept();
    var diseaseCodeableCoding = new CodeableConcept();

    for (var coding : createCodings(immunizationCoding)) {
      switch (coding.getCode()) {
        case "410605003": //YES
          immunization.setStatus(COMPLETED);
          break;
        case "410594000": //NO
          immunization.setStatus(NOTDONE);
          break;
        case "385432009": //Answer = Sonstige/Other
          diseaseCodeableCoding.addCoding(coding.setDisplay("Not applicable (qualifier value)"));
          if (!textValue.isEmpty()) {
            diseaseCodeableCoding.setText(textValue.getValue());
          }
          break;
        case "261665006": //UNKNOWN
          return new Immunization(); //do not create FHIR-resources
        // cases for vaccineCodes
        case "836377006": //Influenza
        case "836398006": //Pneumokokken
        case "836402002": //BCG
        case "1119349007": //Covid19
          vaccineCodeableCoding.addCoding(coding);
          break;
        // cases for targetDisease
        case "64572001": //Andere
          if (!textValue.isEmpty()) {
            diseaseCodeableCoding.setText(textValue.getValue());
          }
          // no break; as the next statement is meant too
        case "6142004": //Influenza
        case "16814004": //Pneumokokken
        case "56717001": //BCG
        case "840539006": //Covid19
          diseaseCodeableCoding.addCoding(coding);
          break;
      }
    }

    if (vaccineCodeableCoding.isEmpty()) { //add No-Known-Immunuzations if no vaccineCode given
      vaccineCodeableCoding.addCoding(createCoding(NO_IMMUNIZATION_INFO_UV_IPS, "no-known-immunizations",
                                                   "No known immunizations"));
    }

    if (ENVIRONMENT.getProperty("fhir.notapplicables.removed", Boolean.class, true) &&
        diseaseCodeableCoding.getCoding().stream().map(Coding::getCode).anyMatch("385432009"::equals)) {
      diseaseCodeableCoding.setCoding(null).setText(null);
    }

    if (diseaseCodeableCoding.isEmpty()) { //add targetDisease unknown if none present
      diseaseCodeableCoding.addCoding(createCoding(SNOMED_CT, "64572001", "Disease (disorder)"));
    }

    immunization.setVaccineCode(vaccineCodeableCoding)
        .addProtocolApplied(new ImmunizationProtocolAppliedComponent()
            .setDoseNumber((StringType) new StringType().addExtension(DATA_ABSENT_FOR_UNKNOWN_REASON))
            .addTargetDisease(diseaseCodeableCoding));

    return immunization;
  }

}