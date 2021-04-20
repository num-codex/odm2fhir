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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.TreeMap;

import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Data
@Accessors(chain = true)
@JacksonXmlRootElement
public class SubjectData {

  @JacksonXmlProperty(isAttribute = true)
  private String subjectKey;

  @JsonManagedReference("subjectData-studyEventData")
  private List<StudyEventData> studyEventData = List.of();

  @JsonManagedReference("subjectData-formData")
  private List<FormData> formData = List.of();

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @JsonBackReference
  private ClinicalData clinicalData;

  public boolean isEmpty() {
    return CollectionUtils.isEmpty(studyEventData) && CollectionUtils.isEmpty(formData);
  }

  //

  public List<StudyEventData> getMergedStudyEventData() {
    return studyEventData.stream()
       .collect(toMap(studyEventData -> format("%s.%s", studyEventData.getStudyEventOID(), studyEventData.getStudyEventRepeatKey()),
                      identity(),
                      (studyEventData1, studyEventData2) -> {
                         studyEventData1.getFormData().addAll(studyEventData2.getFormData());
                         return studyEventData1;
                      }, TreeMap::new))
       .values().stream().collect(toList());
  }

}