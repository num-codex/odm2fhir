package de.difuture.uds.odm2fhir.fhir.mapper.consent.item;

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

import de.difuture.uds.odm2fhir.fhir.mapper.Item;
import de.difuture.uds.odm2fhir.odm.model.FormData;
import de.difuture.uds.odm2fhir.odm.model.ItemData;

import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Consent.ConsentPolicyComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Period;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.LOINC;
import static de.difuture.uds.odm2fhir.fhir.util.CommonStructureDefinition.GERMAN_CONSENT;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.time.DateUtils.addYears;

import static org.hl7.fhir.r4.model.Consent.ConsentProvisionType.DENY;
import static org.hl7.fhir.r4.model.Consent.ConsentProvisionType.PERMIT;
import static org.hl7.fhir.r4.model.Consent.ConsentState;
import static org.hl7.fhir.r4.model.Consent.ConsentState.INACTIVE;
import static org.hl7.fhir.r4.model.Consent.ConsentState.REJECTED;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.CONSENT;

public class BroadConsent extends Item {

  private static final List<String> ELEMENTS = List.of("erhebung_verarbeitung",
                                                       "krankenkassendaten_retro",
                                                       "krankenkassendaten_pro",
                                                       "biomaterialien",
                                                       "biomaterialien_zusaetzlich",
                                                       "kontaktaufnahme",
                                                       "kontaktaufnahme_zusatzbefunde",
                                                       "projekt_codex",
                                                       "projekt_codex_zusatz");

  private static final String POLICY =
      "https://www.medizininformatik-initiative.de/sites/default/files/2020-04/MII_AG-Consent_Einheitlicher-Mustertext_v1.6d.pdf";

  private static final String BASE_OID = HL7_OID + ".3.1937.777.24.5.1";

  private static final Map<String, Integer> OIDS;

  static {
    OIDS = new HashMap<>();

    OIDS.put("erhebung_verarbeitung_1", 1);
    OIDS.put("erhebung_verarbeitung_2", 2);
    OIDS.put("erhebung_verarbeitung_3", 3);
    OIDS.put("erhebung_verarbeitung_4", 2);
    OIDS.put("krankenkassendaten_retro_1", 7);
    OIDS.put("krankenkassendaten_retro_2", 8);
    OIDS.put("krankenkassendaten_retro_3", 9);
    OIDS.put("krankenkassendaten_retro_4", 8);
    OIDS.put("krankenkassendaten_pro_1", 10);
    OIDS.put("krankenkassendaten_pro_2", 11);
    OIDS.put("krankenkassendaten_pro_3", 12);
    OIDS.put("krankenkassendaten_pro_4", 11);
    OIDS.put("biomaterialien_1", 13);
    OIDS.put("biomaterialien_2", 14);
    OIDS.put("biomaterialien_3", 15);
    OIDS.put("biomaterialien_4", 14);
    OIDS.put("biomaterialien_zusaetzlich_1", 31);
    OIDS.put("biomaterialien_zusaetzlich_2", 32);
    OIDS.put("biomaterialien_zusaetzlich_3", 33);
    OIDS.put("biomaterialien_zusaetzlich_4", 32);
    OIDS.put("kontaktaufnahme_1", 19);
    OIDS.put("kontaktaufnahme_2", 20);
    OIDS.put("kontaktaufnahme_3", 21);
    OIDS.put("kontaktaufnahme_4", 20);
    OIDS.put("kontaktaufnahme_zusatzbefunde_1", 22);
    OIDS.put("kontaktaufnahme_zusatzbefunde_2", 23);
    OIDS.put("kontaktaufnahme_zusatzbefunde_3", 24);
    OIDS.put("kontaktaufnahme_zusatzbefunde_4", 23);
    OIDS.put("projekt_codex_1", 34);
    OIDS.put("projekt_codex_2", 35);
    OIDS.put("projekt_codex_3", 36);
    OIDS.put("projekt_codex_4", 35);
    OIDS.put("projekt_codex_zusatz_1", 37);
    OIDS.put("projekt_codex_zusatz_2", 38);
    OIDS.put("projekt_codex_zusatz_3", 39);
    OIDS.put("projekt_codex_zusatz_4", 38);
  }

  public Stream<DomainResource> map(FormData formData) {
    var consentPresented = formData.getItemData("miibc_vorlage"); //1=Yes, 2=No, 3=Unknown

    return !"1".equals(consentPresented.getValue()) ? Stream.empty() : Stream.of(createConsent(formData));
  }

  @SuppressWarnings("fallthrough")
  private Consent createConsent(FormData formData) {
    var identifier = createIdentifier(CONSENT, formData.getItemData("miibc_consent_status"));

    var consent = (Consent) new Consent()
        .addIdentifier(identifier)
        .setDateTimeElement(createDateTimeType(formData.getItemData("miibc_dat_dok")))
        .addOrganization(getOrganizationReference())
        .setScope(RESEARCH)
        .addCategory(createCodeableConcept(createCoding(LOINC, "57016-8")))
        .addPolicy(new ConsentPolicyComponent().setUri(POLICY))
        .setMeta(createMeta(GERMAN_CONSENT));

    switch (formData.getItemData("miibc_consent_status").getValue()) {
      case "1": //agreed
        consent.setStatus(ConsentState.ACTIVE);
        break;
      case "2": //rejected
        consent.setStatus(REJECTED);
        break;
      case "3": //revoked
        var revocationDateItem = formData.getItemData("miibc_w_dat_dok");
        switch (formData.getItemData("miibc_widerruf").getValue()) {
          case "1": //fully revoked
            consent.setStatus(INACTIVE)
              .setDateTimeElement(createDateTimeType(revocationDateItem)); //revocation -> replace 'consentDate' by 'revocationDate'
            break;
          case "2": //partly revoked
            consent.setDateTimeElement(createDateTimeType(revocationDateItem)); //revocation -> replace 'consentDate' by 'revocationDate'
            //purposely no 'break;'
          case "3": //not revoked
          case "4": //unknown (REVOCATION-Status, not CONSENT-Status!)
            consent.setStatus(ConsentState.ACTIVE); //default if not revoked
            break;
        }
        break;
      case "4": //unknown
        return new Consent(); //skip if general CONSENT-Status is 'unknown'
    }

    var mainProvisionComponent = new Consent.provisionComponent().setType(DENY);

    if (consent.getDateTime() != null) {
      mainProvisionComponent.setPeriod(new Period().setStart(consent.getDateTime())
                                                   .setEnd(addYears(consent.getDateTime(), 30)));
    }

    if (ConsentState.ACTIVE == consent.getStatus()) { //add more Consent details only if consented/not fully revoked
      mainProvisionComponent.setProvision(
        ELEMENTS.stream()
            .map(key -> "gee_" + key)
            .map(formData::getItemData)
            .filter(not(ItemData::isEmpty))
            .map(itemData -> {
              var specificAnswerItemName = itemData.getItemOID();
              var specificAnswerItemValue = itemData.getValue();

              var partProvisionComponent = new Consent.provisionComponent()
                  .addCode(createCodeableConcept(
                      createCoding("urn:oid:" + BASE_OID, BASE_OID + "." +
                                   OIDS.get(removeStart(specificAnswerItemName, "gee_") + "_" + specificAnswerItemValue),
                                   specificAnswerItemName)));

              if (!mainProvisionComponent.getPeriod().isEmpty()) {
                var period = mainProvisionComponent.getPeriod().copy();
                if ("gee_krankenkassendaten_retro".equals(specificAnswerItemName)) {
                  period.setEnd(period.getStart()).setStart(addYears(period.getStart(), -5));
                } else if ("gee_krankenkassendaten_pro".equals(specificAnswerItemName)) {
                  period.setEnd(addYears(period.getStart(), 5));
                }
                partProvisionComponent.setPeriod(period);
              }

              switch (specificAnswerItemValue) {
                case "1": //agreed
                  partProvisionComponent.setType(PERMIT);
                  break;
                case "2": //rejected
                case "4": //revoked
                  partProvisionComponent.setType(DENY);
                  break;
                case "3": //unknown
              }

              return partProvisionComponent;
            })
            .collect(toList()));
    }

    return consent.setProvision(mainProvisionComponent);
  }

}