package de.difuture.uds.odm2fhir.fhir.mapper.therapy.item;

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

import org.apache.commons.lang3.ArrayUtils;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Procedure.ProcedureStatus;

import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.SNOMED_CT;
import static de.difuture.uds.odm2fhir.fhir.util.CommonStructureDefinition.DATA_ABSENT_REASON;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.RESPIRATORY_THERAPIES;

import static org.hl7.fhir.r4.model.Procedure.ProcedureStatus.INPROGRESS;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.PROCEDURE;

public class VentilationType extends Item {

  protected Stream<DomainResource> map(FormData formData) {
    var answerCoding = formData.getItemData("beatmungstherapie");

    return answerCoding.isEmpty() ? Stream.empty() : Stream.of(createProcedure(answerCoding));
  }

  private Procedure createProcedure(ItemData answerCoding) {
    var procedure = (Procedure) new Procedure()
        .addIdentifier(createIdentifier(PROCEDURE, answerCoding))
        .setPerformed(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .setStatus(INPROGRESS)
        .setCategory(createCodeableConcept(createCoding(SNOMED_CT, "277132007", "Therapeutic procedure (procedure)")))
        .setMeta(createMeta(RESPIRATORY_THERAPIES));

    var codeableConcept = new CodeableConcept();
    for (var coding : createCodings(answerCoding)) {
      var split = coding.getCode().split("=");

      var material = ArrayUtils.get(coding.getCode().split("="), 1);
      if (material != null && !material.isEmpty()) {
        var display = switch (material) {
          case "426854004" -> "High flow oxygen nasal cannula (physical object)";
          case "26412008" -> "Endotracheal tube, device (physical object)";
          case "129121000" -> "Tracheostomy tube, device (physical object)";
          default -> null;
        };
        procedure.addUsedCode(createCodeableConcept(createCoding(coding.getSystem(), material, display)));
      }

      var code = ArrayUtils.get(split, 0);
      var display = switch (code) {
        case "371907003" -> "Oxygen administration by nasal cannula (procedure)";
        case "428311008" -> "Noninvasive ventilation (procedure)";
        default -> "Artificial respiration (procedure)";
      };

      // case "Unknown" or "No"
      if (DATA_ABSENT_REASON.getUrl().equals(coding.getSystem())) {
        codeableConcept.addCoding(createCoding(SNOMED_CT, "40617009", display));
      } else if (ProcedureStatus.UNKNOWN.getSystem().equals(coding.getSystem())) {
        procedure.setStatus(ProcedureStatus.fromCode(coding.getCode()));
      } else { // case "Yes"
        codeableConcept.addCoding(createCoding(SNOMED_CT, code, display));
      }
    }

    return codeableConcept.isEmpty() ? new Procedure() : procedure.setCode(codeableConcept);
  }

}