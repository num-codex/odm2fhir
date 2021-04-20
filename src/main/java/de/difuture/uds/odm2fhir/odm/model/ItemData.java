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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Data
@Accessors(chain = true)
public class ItemData {

  @JacksonXmlProperty(isAttribute = true)
  private String itemOID;

  @JsonDeserialize(converter = ValueConverter.class)
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

  public static class ValueConverter extends StdConverter<String, String> {

    @Override
    public String convert(String value) {
      return value.replaceAll("PLUS", "+")
                  .replaceAll("EQUAL", "=")
                  .replaceAll("COLON", ":")
                  .replaceAll("COMMA", ",")
                  .replaceAll("LESSTHAN", "<")
                  .replaceAll("LBRACKET", "{")
                  .replaceAll("RBRACKET", "}");
    }

  }

}