package de.difuture.uds.odm2fhir.fhir.mapper.vital_signs;

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
import de.difuture.uds.odm2fhir.fhir.mapper.vital_signs.item.BloodPressure;
import de.difuture.uds.odm2fhir.fhir.mapper.vital_signs.item.BodyTemperature;
import de.difuture.uds.odm2fhir.fhir.mapper.vital_signs.item.FiO2;
import de.difuture.uds.odm2fhir.fhir.mapper.vital_signs.item.HeartRate;
import de.difuture.uds.odm2fhir.fhir.mapper.vital_signs.item.PHValue;
import de.difuture.uds.odm2fhir.fhir.mapper.vital_signs.item.PaCO2;
import de.difuture.uds.odm2fhir.fhir.mapper.vital_signs.item.PaO2;
import de.difuture.uds.odm2fhir.fhir.mapper.vital_signs.item.PeripheralOxygenSaturation;
import de.difuture.uds.odm2fhir.fhir.mapper.vital_signs.item.RespiratoryRate;
import de.difuture.uds.odm2fhir.fhir.mapper.vital_signs.item.SOFAScore;

import lombok.Getter;

import java.util.List;

public class VitalSigns extends Form {

  @Getter
  private final String OID = "vitalparameter";

  @Getter
  private final List<Item> items = List.of(new PaCO2(),
                                           new PaO2(),
                                           new FiO2(),
                                           new PHValue(),
                                           new SOFAScore(),
                                           new RespiratoryRate(),
                                           new BloodPressure(),
                                           new HeartRate(),
                                           new BodyTemperature(),
                                           new PeripheralOxygenSaturation());

}