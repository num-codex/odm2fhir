package de.difuture.uds.odm2fhir.fhir.mapper.therapy;

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

import de.difuture.uds.odm2fhir.fhir.mapper.Form;
import de.difuture.uds.odm2fhir.fhir.mapper.Item;
import de.difuture.uds.odm2fhir.fhir.mapper.therapy.item.Apheresis;
import de.difuture.uds.odm2fhir.fhir.mapper.therapy.item.DialysisHemofiltration;
import de.difuture.uds.odm2fhir.fhir.mapper.therapy.item.ECMO;
import de.difuture.uds.odm2fhir.fhir.mapper.therapy.item.PatientInICU;
import de.difuture.uds.odm2fhir.fhir.mapper.therapy.item.PronePosition;
import de.difuture.uds.odm2fhir.fhir.mapper.therapy.item.VentilationType;

import lombok.Getter;

import java.util.List;

public class Therapy extends Form {

  @Getter
  private final String OID = "therapie";

  @Getter
  private final List<Item> items = List.of(new DialysisHemofiltration(),
                                           new Apheresis(),
                                           new PronePosition(),
                                           new ECMO(),
                                           new VentilationType(),
                                           new PatientInICU());

}