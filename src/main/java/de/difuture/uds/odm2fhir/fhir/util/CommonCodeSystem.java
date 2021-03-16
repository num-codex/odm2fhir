package de.difuture.uds.odm2fhir.fhir.util;

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

import org.hl7.fhir.r4.model.CodeSystem;

public enum CommonCodeSystem {

  DIAGNOSTIC_SERVICE_SECTION_ID("http://terminology.hl7.org/CodeSystem/v2-0074"),
  IDENTIFIER_TYPE_CODES("http://terminology.hl7.org/CodeSystem/v2-0203"),

  NO_IMMUNIZATION_INFO_UV_IPS("http://hl7.org/fhir/uv/ips/CodeSystem/absent-unknown-uv-ips"),

  ATC("http://fhir.de/CodeSystem/dimdi/atc"),
  GENDER_AMTLICH_DE("http://fhir.de/CodeSystem/gender-amtlich-de"),
  ICD_10_GM("http://fhir.de/CodeSystem/dimdi/icd-10-gm"),
  OPS("http://fhir.de/CodeSystem/dimdi/ops"),

  DCM("http://dicom.nema.org/resources/ontology/DCM"),
  ISBT("urn:oid:2.16.840.1.113883.6.18.2.6"),
  LOINC("http://loinc.org"),
  RACE_AND_ETHNICITY_CDC("urn:oid:2.16.840.1.113883.6.238"),
  SNOMED_CT("http://snomed.info/sct"),
  UCUM("http://unitsofmeasure.org"),
  UNII("http://fdasis.nlm.nih.gov"),

  ISO_3166_COUNTRY_CODES("urn:iso:std:iso:3166"),
  ISO_3166_GERMAN_STATE_CODES("urn:iso:std:iso:3166-2:de");

  private final CodeSystem codeSystem;

  CommonCodeSystem(String url) {
    codeSystem = new CodeSystem().setUrl(url);
  }

  public CodeSystem getCodeSystem() {
    return codeSystem;
  }

  public String getUrl() {
    return codeSystem.getUrl();
  }

}