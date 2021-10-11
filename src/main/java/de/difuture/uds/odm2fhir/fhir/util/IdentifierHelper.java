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

import org.hl7.fhir.r4.model.codesystems.ResourceTypes;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static de.difuture.uds.odm2fhir.util.EnvironmentProvider.ENVIRONMENT;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class IdentifierHelper {

  private static String DEFAULT_ASSIGNER = "Mein Krankenhaus";
  private static String DEFAULT_BASE_URL = "https://mein.krankenhaus.de/fhir/NamingSystem/";

  private static Map<String,String> mapping = new HashMap<>();

  private IdentifierHelper() {}

  public static String getIdentifierAssigner() {
    return getValue("fhir.identifier.assigner", DEFAULT_ASSIGNER);
  }

  public static String getIdentifierSystem(ResourceTypes resourceType) {
    return getValue("fhir.identifier.system." + resourceType.toCode().toLowerCase(),
                    DEFAULT_BASE_URL + resourceType.toCode().toLowerCase() + "Id");
  }

  private static String getValue(String property, String defaultValue) {
    var value = mapping.get(property);

    if (isBlank(value)) {
      value = ENVIRONMENT.getProperty(property);

      if (isBlank(value)) {
        value = defaultValue;
        log.warn("'{}' not specified - using (dummy) default '{}'", property, value);
      }

      mapping.put(property, value);
    }

    return value;
  }

}