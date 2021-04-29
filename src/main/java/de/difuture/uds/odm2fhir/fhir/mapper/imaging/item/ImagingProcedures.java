package de.difuture.uds.odm2fhir.fhir.mapper.imaging.item;

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
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Reference;

import java.util.List;
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.DIAGNOSTIC_SERVICE_SECTION_ID;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.LOINC;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.SNOMED_CT;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.DIAGNOSTIC_REPORT_RADIOLOGY;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.RADIOLOGY_PROCEDURES;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.equalsAny;

import static org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportStatus.FINAL;
import static org.hl7.fhir.r4.model.Procedure.ProcedureStatus.COMPLETED;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.DIAGNOSTICREPORT;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.PROCEDURE;

import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ImagingProcedures extends Item {

  public Stream<DomainResource> map(FormData formData) {
    var techniqueGroup = formData.getItemGroupData("bildgebung.bildgebende_verfahren_ct");
    var befundGroup = formData.getItemGroupData("bildgebung.befund_bildgebender_verfahren_ct");
    var generalCoding = formData.getItemData("bildgebende_verfahren");

    if (!"1".equals(generalCoding.getValue())) {
      return Stream.empty();
    }

    var itemDatas = Stream.of(techniqueGroup.getItemData(), befundGroup.getItemData())
        .flatMap(List::stream)
        .collect(toMap(ItemData::getItemOID, identity()));

    return Stream.of("ct", "roentgen", "us")
        .filter(type -> contains(itemDatas.get("bildgebende_verfahren_" + type).getValue(), "410605003"))
        .filter(type -> !(itemDatas.get("befund_bildgebender_verfahren_" + type).isEmpty()))
        .flatMap(type -> {
          var diagnosticReport = createDiagnosticReport(itemDatas.get("befund_bildgebender_verfahren_" + type));
          var procedure = createProcedure(itemDatas.get("bildgebende_verfahren_" + type))
              .addReport(new Reference(format("%s/%s", DIAGNOSTICREPORT.toCode(), diagnosticReport.getId())));
          return Stream.of(diagnosticReport, procedure);
        });
  }

  public DiagnosticReport createDiagnosticReport(ItemData befundCoding) {
    var identifier = createIdentifier(DIAGNOSTICREPORT, befundCoding);

    return (DiagnosticReport) new DiagnosticReport()
        .addIdentifier(identifier)
        .setEffective(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .setStatus(FINAL)
        .addCategory(createCodeableConcept(
            createCoding(LOINC, "18726-0", "Radiology studies (set)"),
            createCoding(DIAGNOSTIC_SERVICE_SECTION_ID, "RAD", "Radiology")))
        .setCode(createCodeableConcept(createCoding(LOINC, "18748-4", "Diagnostic imaging study")))
        .addConclusionCode(createCodeableConcept(befundCoding))
        .setMeta(createMeta(DIAGNOSTIC_REPORT_RADIOLOGY))
        .setId(sha256Hex(identifier.getSystem() + identifier.getValue()));
  }

  private Procedure createProcedure(ItemData techniqueCoding) {
    return (Procedure) new Procedure()
        .addIdentifier(createIdentifier(PROCEDURE, techniqueCoding))
        .setStatus(COMPLETED)
        .setPerformed(UNKNOWN_DATE_TIME) // TODO Set actual Period value
        .setCategory(createCodeableConcept(createCoding(SNOMED_CT, "103693007", "Diagnostic procedure (procedure)")))
        .addBodySite(createCodeableConcept(createCoding(SNOMED_CT, "39607008", "Lung structure (body structure)")))
        .setCode(new CodeableConcept().setCoding(
            createCodings(techniqueCoding).stream()
                .filter(coding -> !equalsAny(coding.getCode(), "410605003", "410594000", "261665006")) // YES, NO, UNKNOWN
                .collect(toList())))
        .setMeta(createMeta(RADIOLOGY_PROCEDURES));
  }

}