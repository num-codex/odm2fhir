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

import org.hl7.fhir.r4.model.StructureDefinition;

public enum CommonStructureDefinition {

  DATA_ABSENT_REASON("http://hl7.org/fhir/StructureDefinition/data-absent-reason"),
  GERMAN_CONSENT("http://fhir.de/ConsentManagement/StructureDefinition/Consent"),
  MI_I_OBSERVATION_LAB("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/ObservationLab");

  private final StructureDefinition structureDefinition;

  CommonStructureDefinition(String url) {
    structureDefinition = new StructureDefinition().setUrl(url);
  }

  public StructureDefinition getStructureDefinition() {
    return structureDefinition;
  }

  public String getUrl() {
    return structureDefinition.getUrl();
  }

}