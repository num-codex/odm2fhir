package de.difuture.uds.odm2fhir.odm.model;

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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.apache.commons.lang3.StringUtils.replaceEach;

import static de.difuture.uds.odm2fhir.util.EnvironmentProvider.ENVIRONMENT;

@Data
@Accessors(chain = true)
public class ItemData {

  @JacksonXmlProperty(isAttribute = true)
  private String itemOID;

  @JacksonXmlProperty(isAttribute = true)
  private String value;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @JsonBackReference
  private ItemGroupData itemGroupData;

  public boolean isEmpty() {
    return isBlank(value);
  }

  public ItemData copy() {
    return new ItemData().setItemOID(itemOID).setValue(value).setItemGroupData(itemGroupData);
  }

  @SuppressWarnings("unchecked")
  public String getValue() {
    if (isNumeric(value)) {
      var codes = ENVIRONMENT.getProperty(itemOID, List.class);
      if (codes != null) {
        value = ((String) codes.get(Integer.parseInt(value) - 1));
      }
    }

    value = replaceEach(value, new String[]{ "PLUS", "EQUAL", "COLON", "COMMA", "LESSTHAN", "LBRACKET", "RBRACKET" },
                               new String[]{ "+",    "=",     ":",     ",",     "<",        "{",        "}"        });

    return value;
  }

}