package de.difuture.uds.odm2fhir.fhir.mapper;

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

import de.difuture.uds.odm2fhir.fhir.mapper.anamnesis.Anamnesis;
import de.difuture.uds.odm2fhir.fhir.mapper.complications.Complications;
import de.difuture.uds.odm2fhir.fhir.mapper.consent.Consent;
import de.difuture.uds.odm2fhir.fhir.mapper.demographics.Demographics;
import de.difuture.uds.odm2fhir.fhir.mapper.epidemiological_factors.EpidemiologicalFactors;
import de.difuture.uds.odm2fhir.fhir.mapper.imaging.Imaging;
import de.difuture.uds.odm2fhir.fhir.mapper.laboratory_values.LaboratoryValues;
import de.difuture.uds.odm2fhir.fhir.mapper.medication.Medication;
import de.difuture.uds.odm2fhir.fhir.mapper.onset_of_illness.OnsetOfIllness;
import de.difuture.uds.odm2fhir.fhir.mapper.outcome_at_discharge.OutcomeAtDischarge;
import de.difuture.uds.odm2fhir.fhir.mapper.study_enrollment.StudyEnrollmentInclusionCriteria;
import de.difuture.uds.odm2fhir.fhir.mapper.symptoms.Symptoms;
import de.difuture.uds.odm2fhir.fhir.mapper.therapy.Therapy;
import de.difuture.uds.odm2fhir.fhir.mapper.vital_signs.VitalSigns;
import de.difuture.uds.odm2fhir.odm.model.StudyEventData;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

import lombok.Getter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.util.EnvironmentProvider.getEnvironment;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.apache.commons.lang3.StringUtils.equalsAny;

import static org.hl7.fhir.r4.model.Encounter.EncounterStatus.UNKNOWN;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.CONSENT;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.ENCOUNTER;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.MEDICATIONSTATEMENT;
import static org.hl7.fhir.r4.model.codesystems.V3ActCode.IMP;

import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

import static java.lang.String.format;

public class StudyEvent {

  @Getter
  private Subject subject;

  private Reference encounterReference;

  private final List<Form> forms = List.of(
      new Consent(),
      new OnsetOfIllness(),
      new StudyEnrollmentInclusionCriteria(),
      new Anamnesis(),
      new Imaging(),
      new Demographics(),
      new EpidemiologicalFactors(),
      new Complications(),
      new LaboratoryValues(),
      new Medication(),
      new Symptoms(),
      new Therapy(),
      new VitalSigns(),
      new OutcomeAtDischarge());

  public Stream<DomainResource> map(Subject subject, StudyEventData studyEventData) {
    this.subject = subject;

    var encounter = new Encounter();

    if (containsAny(studyEventData.getStudyEventOID(),"GECCOVISIT", "fall")) {
      var encounterIdentifier = new Identifier()
          .setSystem(getEnvironment().getProperty("fhir.identifier.system.encounter"))
          .setValue(format("%s-%s.%s",
                           studyEventData.getSubjectData().getSubjectKey(),
                           studyEventData.getStudyEventOID(), studyEventData.getStudyEventRepeatKey()))
          .setAssigner(subject.getOrganizationReference());

      encounter.setStatus(UNKNOWN)
          .setClass_(new Coding(IMP.getSystem(), IMP.toCode(), IMP.getDisplay()))
          .addIdentifier(encounterIdentifier)
          .setId(md5Hex(encounterIdentifier.getSystem() + encounterIdentifier.getValue()));

      encounterReference = new Reference(format("%s/%s", ENCOUNTER.toCode(), encounter.getId()));
    }

    var domainResources = studyEventData.getFormData().stream()
        .flatMap(formData -> forms.stream().map(form -> form.map(this, formData)))
        .flatMap(Function.identity());

    return encounter.isEmpty() ? domainResources :
           Stream.concat(Stream.of(encounter), domainResources.peek(this::setEncounter));
  }

  private void setEncounter(DomainResource domainResource) {
    if (!equalsAny(domainResource.fhirType(), CONSENT.toCode(), MEDICATIONSTATEMENT.toCode())) {
      invokeMethod(
          findMethod(domainResource.getClass(), "setEncounter", Reference.class), domainResource, encounterReference);
    }
  }

}