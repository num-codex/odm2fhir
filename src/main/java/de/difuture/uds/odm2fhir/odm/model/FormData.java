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
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.springframework.util.CollectionUtils;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.equalsAny;

@Data
@Accessors(chain = true)
public class FormData {

  @JacksonXmlProperty(isAttribute = true)
  private String formOID;

  @JacksonXmlProperty(isAttribute = true)
  private String formRepeatKey;

  @JsonManagedReference
  private List<ItemGroupData> itemGroupData = List.of();

  @ToString.Exclude
  @JsonBackReference("studyEventData-formData")
  private StudyEventData studyEventData;

  @ToString.Exclude
  @JsonBackReference("subjectData-formData")
  private SubjectData subjectData;

  public boolean isEmpty() {
    return CollectionUtils.isEmpty(itemGroupData);
  }

  //

  public ItemGroupData getItemGroupData(String itemGroupOID) {
    return itemGroupData.stream()
        .filter(itemGroupData -> equalsAny(itemGroupData.getItemGroupOID(), itemGroupOID))
        .findFirst()
        .orElse(new ItemGroupData().setItemGroupOID(itemGroupOID));
  }

  public ItemData getItemData(String itemOID) {
    return itemGroupData.stream()
        .map(ItemGroupData::getItemData)
        .flatMap(List::stream)
        .filter(itemData -> equalsAny(itemData.getItemOID(), itemOID))
        .findFirst()
        .orElse(new ItemData().setItemOID(itemOID));
  }

}