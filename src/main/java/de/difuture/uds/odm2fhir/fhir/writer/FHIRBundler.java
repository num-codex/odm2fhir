package de.difuture.uds.odm2fhir.fhir.writer;

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

import de.difuture.uds.odm2fhir.fhir.util.ResourceValidator;

import lombok.extern.slf4j.Slf4j;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.IdentifierHelper.getIdentifierSystem;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.GECCO_BUNDLE;

import static org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION;
import static org.hl7.fhir.r4.model.Bundle.HTTPVerb.POST;
import static org.hl7.fhir.r4.model.Bundle.HTTPVerb.PUT;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.fromCode;
import static org.hl7.fhir.r4.model.codesystems.SearchModifierCode.IDENTIFIER;

import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

import static java.lang.String.format;

@Slf4j
@Service
public class FHIRBundler {

  @Value("${fhir.updateascreate.enabled:false}")
  private boolean updateascreateEnabled;

  @Value("${fhir.codingdisplays.removed:false}")
  private boolean codingdisplaysRemoved;

  @Autowired(required = false)
  private ResourceValidator resourceValidator;

  private void removeCodingDisplays(Object object) {
    if (codingdisplaysRemoved) {
      var children = findMethod(object.getClass(), "children");

      if (children != null) {
        ((List<Property>) invokeMethod(children, object)).stream()
            .map(Property::getValues)
            .flatMap(List::stream)
            .forEach(value -> {
              if (value instanceof Coding) {
                ((Coding) value).setDisplay(null);
              } else {
                removeCodingDisplays(value);
              }
            });
      }
    }
  }

  public Bundle bundle(Stream<DomainResource> domainResources) {
    var bundle = (Bundle) new Bundle().setType(TRANSACTION).setMeta(new Meta().addProfile(GECCO_BUNDLE.getUrl()));

    domainResources
        .peek(this::removeCodingDisplays)
        .filter(domainResource -> resourceValidator == null || resourceValidator.validate(domainResource))
        .forEach(domainResource -> {
          var method = PUT;

          var id = domainResource.getId();
          var fhirType = domainResource.fhirType();
          var url = format("%s/%s", fhirType, id);
          var fullUrl = "";

          var ifNoneExist = "";

          if (!updateascreateEnabled) {
            method = POST;
            fullUrl = url;
            url = fhirType;
            var identifier = ((Identifier) invokeMethod(
                findMethod(domainResource.getClass(), "getIdentifierFirstRep"), domainResource)).getValue();
            ifNoneExist = format("%s=%s|%s", IDENTIFIER.toCode(), getIdentifierSystem(fromCode(fhirType)), identifier.replace(" ", "%20"));
            domainResource.setId("");
          }

          bundle.addEntry().setResource(domainResource).setFullUrl(fullUrl)
                .setRequest(new BundleEntryRequestComponent().setMethod(method).setUrl(url).setIfNoneExist(ifNoneExist));
        });

    return bundle;
  }

}