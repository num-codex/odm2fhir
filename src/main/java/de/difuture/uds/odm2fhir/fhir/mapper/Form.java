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

import de.difuture.uds.odm2fhir.odm.model.FormData;
import de.difuture.uds.odm2fhir.odm.model.ItemData;

import lombok.Getter;

import org.hl7.fhir.r4.model.DomainResource;

import java.util.List;
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.util.EnvironmentProvider.ENVIRONMENT;

import static org.apache.commons.lang3.StringUtils.equalsAny;
import static org.apache.commons.lang3.StringUtils.removeStart;

import static java.util.function.Predicate.not;

public abstract class Form {

  @Getter
  private StudyEvent studyEvent;

  public abstract String getOID();

  protected abstract List<Item> getItems();

  protected Stream<DomainResource> map(StudyEvent studyEvent, FormData formData) {
    this.studyEvent = studyEvent;

    return !formData.getFormOID().endsWith(getOID()) || !isComplete(formData) ? Stream.empty() :
        getItems().stream()
            .flatMap(item -> item.map(this, formData))
            .filter(not(DomainResource::isEmpty));
  }

  private boolean isComplete(FormData formData) {
    // Always add demographics form
    return "demographie".equals(getOID()) ||
           ENVIRONMENT.getProperty("odm.incompleteforms.allowed", Boolean.class, false) ||
           // Check REDCap X_complete and  DIS status field for complete (2), locked (4) or signed (5)
           Stream.of(formData.getFormOID() + "_complete", "Status")
                 .map(formData::getItemData)
                 .map(ItemData::getValue)
                 .anyMatch(value -> equalsAny(value, "2", "4", "5"));
  }

}