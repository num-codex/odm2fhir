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

import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport.IssueSeverity;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
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
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.utilities.npm.NpmPackage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.ICD_10_GM;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.LOINC;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.SNOMED_CT;
import static de.difuture.uds.odm2fhir.util.HTTPHelper.HTTP_CLIENT_BUILDER;
import static de.difuture.uds.odm2fhir.util.HTTPHelper.createAuthInterceptor;

import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.apache.commons.lang3.function.Failable.asFunction;

import static org.springframework.util.CollectionUtils.isEmpty;

import static ca.uhn.fhir.context.FhirContext.forR4Cached;
import static ca.uhn.fhir.validation.ResultSeverityEnum.ERROR;
import static ca.uhn.fhir.validation.ResultSeverityEnum.WARNING;

import static java.util.Comparator.comparing;
import static java.util.Locale.ENGLISH;

@ConditionalOnExpression("${fhir.validation.enabled:false}")
@Slf4j
@Service
public class ResourceValidator {

  @Value("${fhir.errorhandling.strict:false}")
  protected boolean errorhandlingStrict;

  @Value("${fhir.validation.codeerrors.ignored:true}")
  private boolean validationCodeerrorsIgnored;

  @Value("classpath:fhir/profiles/*.tgz")
  private Resource[] profiles;

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

  private static final IParser JSON_PARSER = forR4Cached().newJsonParser().setPrettyPrint(true);

  @PostConstruct
  private void init() throws IOException {
    Locale.setDefault(ENGLISH);

    if (errorhandlingStrict) {
      JSON_PARSER.setParserErrorHandler(new StrictErrorHandler());
    }

    var prePopulatedValidationSupport = new PrePopulatedValidationSupport(forR4Cached());
    Arrays.stream(profiles)
          .map(asFunction(Resource::getInputStream))
          .map(asFunction(NpmPackage::fromPackage))
          .flatMap(asFunction(npmPackage -> npmPackage.list("package").stream()
                                                      .map(asFunction(npmPackage::loadResource))))
          .map(JSON_PARSER::parseResource)
          .forEach(prePopulatedValidationSupport::addResource);

    var validationSupportChain = new ValidationSupportChain(
        new DefaultProfileValidationSupport(forR4Cached()),
        new InMemoryTerminologyServerValidationSupport(forR4Cached()),
        new CommonCodeSystemsTerminologyService(forR4Cached()),
        new SnapshotGeneratingValidationSupport(forR4Cached()),
        prePopulatedValidationSupport);

    if (terminologyserverUrl.isAbsolute()) {
      forR4Cached().getRestfulClientFactory().setHttpClient(HTTP_CLIENT_BUILDER.build());
      var remoteTerminologyServiceValidationSupport = new RemoteTerminologyServiceValidationSupport(forR4Cached(),
                                                                                                    terminologyserverUrl.toString());
      remoteTerminologyServiceValidationSupport.addClientInterceptor(
          createAuthInterceptor(terminologyserverBasicauthUsername, terminologyserverBasicauthPassword,
                                terminologyserverOauth2TokenURL, terminologyserverOauth2ClientId, terminologyserverOauth2ClientSecret));
      validationSupportChain.addValidationSupport(remoteTerminologyServiceValidationSupport);
    }

    var unknownCodeSystemWarningValidationSupport = new UnknownCodeSystemWarningValidationSupport(forR4Cached());
    unknownCodeSystemWarningValidationSupport.setNonExistentCodeSystemSeverity(IssueSeverity.WARNING);
    validationSupportChain.addValidationSupport(unknownCodeSystemWarningValidationSupport);

    fhirValidator = forR4Cached().newValidator()
                                 .registerValidatorModule(new FhirInstanceValidator(new CachingValidationSupport(validationSupportChain)));
  }

  public boolean validate(DomainResource domainResource) {
    var validationResult = fhirValidator.validateWithResult(domainResource);

    var messages = validationResult.getMessages().stream()
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
        })
        .sorted(comparing(SingleValidationMessage::getSeverity).reversed());

    if (!isEmpty(validationResult.getMessages())) {
      log.debug(JSON_PARSER.encodeResourceToString(domainResource));
    }

    return validationResult.isSuccessful() || messages.noneMatch(message -> message.getSeverity() == ERROR);
  }

}