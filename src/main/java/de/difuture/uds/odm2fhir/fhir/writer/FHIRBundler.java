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

import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport.IssueSeverity;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import ca.uhn.fhir.context.support.ValueSetExpansionOptions;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;

import lombok.extern.slf4j.Slf4j;

import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.RemoteTerminologyServiceValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.UnknownCodeSystemWarningValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.utilities.npm.NpmPackage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import static ca.uhn.fhir.context.FhirContext.forR4Cached;
import static ca.uhn.fhir.validation.ResultSeverityEnum.ERROR;
import static ca.uhn.fhir.validation.ResultSeverityEnum.WARNING;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.ICD_10_GM;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.LOINC;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.SNOMED_CT;
import static de.difuture.uds.odm2fhir.fhir.util.IdentifierHelper.getIdentifierSystem;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.GECCO_BUNDLE;
import static de.difuture.uds.odm2fhir.fhir.writer.FHIRBundleWriter.JSON_PARSER;
import static de.difuture.uds.odm2fhir.util.HTTPHelper.createAuthInterceptor;

import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.apache.commons.lang3.function.Failable.asFunction;
import static org.apache.commons.lang3.function.Failable.asPredicate;

import static org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION;
import static org.hl7.fhir.r4.model.Bundle.HTTPVerb.POST;
import static org.hl7.fhir.r4.model.Bundle.HTTPVerb.PUT;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.fromCode;
import static org.hl7.fhir.r4.model.codesystems.SearchModifierCode.IDENTIFIER;

import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Locale.ENGLISH;
import static java.util.function.Predicate.not;

@Slf4j
@Service
@DependsOn("HTTPHelper")
public class FHIRBundler {

  @Value("classpath:fhir/profiles")
  private Path profiles;

  @Value("${fhir.updateascreate.enabled:false}")
  private boolean updateascreateEnabled;

  @Value("${fhir.validation.enabled:false}")
  private boolean validationEnabled;

  @Value("${fhir.validation.codeerrors.ignored:true}")
  private boolean validationCodeerrorsIgnored;

  @Value("${fhir.terminologyserver.url:}")
  private URI terminologyserverUrl;

  @Value("${fhir.terminologyserver.basicauth.username:}")
  private String terminologyserverBasicauthUsername;

  @Value("${fhir.terminologyserver.basicauth.password:}")
  private String terminologyserverBasicauthPassword;

  @Value("${fhir.terminologyserver.oauth2.token.url:}")
  private String terminologyserverOauth2TokenURL;

  @Value("${fhir.terminologyserver.oauth2.client.id:}")
  private String terminologyserverOauth2ClientId;

  @Value("${fhir.terminologyserver.oauth2.client.secret:}")
  private String terminologyserverOauth2ClientSecret;

  private FhirValidator fhirValidator;

  @PostConstruct
  private void init() throws IOException {
    if (validationEnabled) {
      Locale.setDefault(ENGLISH);

      @SuppressWarnings("unchecked")
      var prePopulatedValidationSupport = new PrePopulatedValidationSupport(forR4Cached()) {
        @Override
        public ValueSetExpansionOutcome expandValueSet(ValidationSupportContext validationSupportContext,
                                                       @Nullable ValueSetExpansionOptions valueSetExpansionOptions,
                                                       @Nonnull IBaseResource valueSet) {
          return new ValueSetExpansionOutcome(valueSet);
        }
      };

      Files.list(profiles)
          .filter(not(asPredicate(Files::isHidden)))
          .map(asFunction(Files::newInputStream))
          .map(asFunction(NpmPackage::fromPackage))
          .flatMap(asFunction(npmPackage -> npmPackage.list("package").stream()
                                                      .map(asFunction(npmPackage::loadResource))))
          .map(JSON_PARSER::parseResource)
          .forEach(prePopulatedValidationSupport::addResource);

      var unknownCodeSystemWarningValidationSupport = new UnknownCodeSystemWarningValidationSupport(forR4Cached());
      unknownCodeSystemWarningValidationSupport.setNonExistentCodeSystemSeverity(IssueSeverity.WARNING);

      var validationSupportChain = new ValidationSupportChain(
          new DefaultProfileValidationSupport(forR4Cached()),
          new CommonCodeSystemsTerminologyService(forR4Cached()),
          new InMemoryTerminologyServerValidationSupport(forR4Cached()),
          unknownCodeSystemWarningValidationSupport,
          new SnapshotGeneratingValidationSupport(forR4Cached()),
          prePopulatedValidationSupport);

      if (terminologyserverUrl.isAbsolute()) {
        var remoteTerminologyServiceValidationSupport = new RemoteTerminologyServiceValidationSupport(forR4Cached());
        remoteTerminologyServiceValidationSupport.setBaseUrl(terminologyserverUrl.toString());
        remoteTerminologyServiceValidationSupport.addClientInterceptor(
            createAuthInterceptor(terminologyserverBasicauthUsername, terminologyserverBasicauthPassword,
                                  terminologyserverOauth2TokenURL, terminologyserverOauth2ClientId, terminologyserverOauth2ClientSecret));
        validationSupportChain.addValidationSupport(remoteTerminologyServiceValidationSupport);
      }

      fhirValidator = forR4Cached().newValidator()
          .registerValidatorModule(new FhirInstanceValidator(new CachingValidationSupport(validationSupportChain)));
    }
  }

  public Bundle bundle(Stream<DomainResource> domainResources) {
    var bundle = (Bundle) new Bundle().setType(TRANSACTION).setMeta(new Meta().addProfile(GECCO_BUNDLE.getUrl()));

    domainResources.filter(domainResource -> !domainResource.isEmpty()).filter(domainResource -> {
      if (validationEnabled) {
        var validationResult = fhirValidator.validateWithResult(domainResource);

        var messages = validationResult.getMessages().stream()
            .sorted(comparing(SingleValidationMessage::getSeverity).reversed())
            .peek(message -> {
              if (message.getSeverity() == ERROR) {
                if (validationCodeerrorsIgnored && !terminologyserverUrl.isAbsolute() &&
                    containsAny(message.getMessage(), ICD_10_GM.getUrl(), LOINC.getUrl(), SNOMED_CT.getUrl())) {
                  message.setSeverity(WARNING);
                }
              }
            })
            .peek(message -> {
              switch (message.getSeverity()) {
                case FATAL, ERROR -> log.error(message.toString());
                case WARNING -> log.warn(message.toString());
                case INFORMATION -> log.info(message.toString());
              }
            });

        if (!isEmpty(validationResult.getMessages())) {
          log.debug(JSON_PARSER.encodeResourceToString(domainResource));
        }

        return validationResult.isSuccessful() || messages.noneMatch(message -> message.getSeverity() == ERROR);
      }

      return true;
    })
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
        ifNoneExist = format("%s=%s|%s", IDENTIFIER.toCode(), getIdentifierSystem(fromCode(fhirType)),
                             identifier.replace(" ", "%20"));
        domainResource.setId("");
      }

      bundle.addEntry().setResource(domainResource).setFullUrl(fullUrl)
            .setRequest(new BundleEntryRequestComponent().setMethod(method).setUrl(url).setIfNoneExist(ifNoneExist));
    });

    return bundle;
  }

}