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

import de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition;
import de.difuture.uds.odm2fhir.odm.model.SubjectData;

import lombok.Getter;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;

import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.IDENTIFIER_TYPE_CODES;
import static de.difuture.uds.odm2fhir.fhir.util.IdentifierHelper.getIdentifierAssigner;
import static de.difuture.uds.odm2fhir.fhir.util.IdentifierHelper.getIdentifierSystem;
import static de.difuture.uds.odm2fhir.util.EnvironmentProvider.ENVIRONMENT;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.apache.commons.lang3.StringUtils.equalsAny;

import static org.hl7.fhir.r4.model.codesystems.V3Hl7PublishingDomain.MR;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.ORGANIZATION;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.PATIENT;

import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

import static java.lang.String.format;

public class Subject {

  @Getter
  private Patient patient;

  @Getter
  private Reference organizationReference;

  private Reference patientReference;

  public Stream<DomainResource> map(SubjectData subjectData) {
    var value = getIdentifierAssigner();

    if (!ENVIRONMENT.containsProperty("debug")) {
      value = sha256Hex(value);
    }

    var organizationIdentifier = new Identifier()
        .setSystem(getIdentifierSystem(ORGANIZATION))
        .setValue(value);

    var organization = (Organization) new Organization()
        .setName(getIdentifierAssigner())
        .addIdentifier(organizationIdentifier)
        .setId(sha256Hex(organizationIdentifier.getSystem() + organizationIdentifier.getValue()));

    organizationReference = new Reference(format("%s/%s", ORGANIZATION.toCode(), organization.getId()));

    value = subjectData.getSubjectKey();

    if (!ENVIRONMENT.containsProperty("debug") &&
        ENVIRONMENT.getProperty("odm.subjectkeys.hashed", Boolean.class, true)) {
      value = sha256Hex(value);
    }

    var patientIdentifier = new Identifier()
        .setSystem(getIdentifierSystem(PATIENT))
        .setValue(value)
        .setType(new CodeableConcept(new Coding().setSystem(IDENTIFIER_TYPE_CODES.getUrl()).setCode(MR.toCode())))
        .setAssigner(organizationReference);

    patient = (Patient) new Patient()
        .addIdentifier(patientIdentifier)
        .setId(sha256Hex(patientIdentifier.getSystem() + patientIdentifier.getValue()))
        .setMeta(new Meta().addProfile(NUMStructureDefinition.PATIENT.getUrl()));

    patientReference = new Reference(format("%s/%s", PATIENT.toCode(), patient.getId()));

    return Stream.concat(
        Stream.of(patient, organization),
        subjectData.getMergedStudyEventData().stream()
            .flatMap(studyEventData -> new StudyEvent().map(this, studyEventData))
            .peek(this::setId)
            .peek(this::setPatientSubject));
  }

  private void setId(DomainResource domainResource) {
    var identifier = (Identifier) invokeMethod(
        findMethod(domainResource.getClass(), "getIdentifierFirstRep"), domainResource);
    domainResource.setId(sha256Hex(identifier.getSystem() + identifier.getValue()));
  }

  private void setPatientSubject(DomainResource domainResource) {
    if (!equalsAny(domainResource.fhirType(), PATIENT.toCode(), ORGANIZATION.toCode())) {
      invokeMethod(
          findMethod(domainResource.getClass(),
              (domainResource instanceof Immunization || domainResource instanceof Consent ? "setPatient" : "setSubject"),
              Reference.class),
          domainResource, patientReference);
    }
  }

}