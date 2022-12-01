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
import de.difuture.uds.odm2fhir.odm.model.FormData;
import de.difuture.uds.odm2fhir.odm.model.ItemData;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Procedure.ProcedureStatus;

import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.SNOMED_CT;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.RESPIRATORY_THERAPIES;

import static org.apache.commons.lang3.StringUtils.equalsAny;

import static org.hl7.fhir.r4.model.Procedure.ProcedureStatus.INPROGRESS;
import static org.hl7.fhir.r4.model.Procedure.ProcedureStatus.NOTDONE;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.PROCEDURE;

public class RespiratoryTherapies extends Item {

  protected Stream<DomainResource> map(FormData formData) {
    var answerCoding = formData.getItemData("bestehende_sauerstoff_oder_beatmungstherapie");
    var generalCoding = formData.getItemData("bestehende_sauerstoff_oder_beatmungstherapie_code", "1");

    return answerCoding.isEmpty() ? Stream.empty() : Stream.of(createProcedure(generalCoding, answerCoding));
  }

  private Procedure createProcedure(ItemData generalCoding, ItemData answerCoding) {
    var procedure = (Procedure) new Procedure()
        .addIdentifier(createIdentifier(PROCEDURE, generalCoding))
        .setPerformed(UNKNOWN_DATE_TIME) // TODO Set actual DateTime value
        .setCategory(createCodeableConcept(createCoding(SNOMED_CT, "277132007", "Therapeutic procedure (procedure)")))
        .setMeta(createMeta(RESPIRATORY_THERAPIES));

    createCodings(answerCoding).stream()
        .map(Coding::getCode)
        .filter(code -> equalsAny(code, INPROGRESS.toCode(), NOTDONE.toCode(), ProcedureStatus.UNKNOWN.toCode()))
        .map(ProcedureStatus::fromCode)
        .forEach(procedure::setStatus);

    var codeableConcept = createCodeableConcept(createCoding(generalCoding)
                                                    .setDisplay("Respiratory therapy (procedure)"));
    return codeableConcept.isEmpty() ? new Procedure() : procedure.setCode(codeableConcept);
  }

}