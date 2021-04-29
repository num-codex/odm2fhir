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

import de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem;
import de.difuture.uds.odm2fhir.fhir.util.CommonStructureDefinition;
import de.difuture.uds.odm2fhir.fhir.util.NUMCodeSystem;
import de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition;
import de.difuture.uds.odm2fhir.odm.model.FormData;
import de.difuture.uds.odm2fhir.odm.model.ItemData;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.hl7.fhir.r4.model.Age;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.codesystems.AdministrativeGender;
import org.hl7.fhir.r4.model.codesystems.ConditionClinical;
import org.hl7.fhir.r4.model.codesystems.ConditionVerStatus;
import org.hl7.fhir.r4.model.codesystems.ConsentCategory;
import org.hl7.fhir.r4.model.codesystems.ConsentScope;
import org.hl7.fhir.r4.model.codesystems.DataAbsentReason;
import org.hl7.fhir.r4.model.codesystems.DataTypes;
import org.hl7.fhir.r4.model.codesystems.EventStatus;
import org.hl7.fhir.r4.model.codesystems.MedicationStatementStatus;
import org.hl7.fhir.r4.model.codesystems.ObservationCategory;
import org.hl7.fhir.r4.model.codesystems.ResourceTypes;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Stream;

import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.ATC;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.DCM;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.GENDER_AMTLICH_DE;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.ICD_10_GM;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.IDENTIFIER_TYPE_CODES;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.ISBT;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.ISO_3166_COUNTRY_CODES;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.ISO_3166_GERMAN_STATE_CODES;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.LOINC;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.OPS;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.RACE_AND_ETHNICITY_CDC;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.SNOMED_CT;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.UCUM;
import static de.difuture.uds.odm2fhir.fhir.util.CommonCodeSystem.UNII;
import static de.difuture.uds.odm2fhir.fhir.util.CommonStructureDefinition.DATA_ABSENT_REASON;
import static de.difuture.uds.odm2fhir.fhir.util.NUMCodeSystem.ECRF_PARAMETER_CODES;
import static de.difuture.uds.odm2fhir.fhir.util.NUMCodeSystem.FRAILTY_SCORE;
import static de.difuture.uds.odm2fhir.fhir.util.NUMStructureDefinition.UNCERTAINTY_OF_PRESENCE;
import static de.difuture.uds.odm2fhir.util.EnvironmentProvider.getEnvironment;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.splitByWholeSeparator;

import static org.hl7.fhir.r4.model.codesystems.DataTypes.CODING;
import static org.hl7.fhir.r4.model.codesystems.DataTypes.DATETIME;
import static org.hl7.fhir.r4.model.codesystems.DataTypes.QUANTITY;
import static org.hl7.fhir.r4.model.codesystems.ResourceTypes.CODESYSTEM;

import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

@Slf4j
public abstract class Item {

  @Getter private Form form;

  public Stream<DomainResource> map(Form form, FormData formData) {
    this.form = form;
    return map(formData);
  }

  protected abstract Stream<DomainResource> map(FormData formData);

  // NOTE Always use a copy of those constants when adding codings!!!
  protected final CodeableConcept ACTIVE = createCodeableConcept(ConditionClinical.ACTIVE);
  protected final CodeableConcept ADR = createCodeableConcept(ConsentScope.ADR);
  protected final CodeableConcept CONFIRMED = createCodeableConcept(ConditionVerStatus.CONFIRMED).copy()
      .addCoding(createCoding(SNOMED_CT, "410605003", "Confirmed present (qualifier value)"));
  protected final CodeableConcept UNCONFIRMED = createCodeableConcept(ConditionVerStatus.UNCONFIRMED);
  protected final CodeableConcept DNR = createCodeableConcept(ConsentCategory.DNR);
  protected final CodeableConcept LABORATORY = createCodeableConcept(ObservationCategory.LABORATORY);
  protected final CodeableConcept REFUTED = createCodeableConcept(ConditionVerStatus.REFUTED).copy()
      .addCoding(createCoding(SNOMED_CT, "410594000", "Definitely NOT present (qualifier value)"));
  protected final CodeableConcept RESEARCH = createCodeableConcept(ConsentCategory.RESEARCH);
  protected final CodeableConcept SOCIAL_HISTORY = createCodeableConcept(ObservationCategory.SOCIALHISTORY);
  protected final CodeableConcept SURVEY = createCodeableConcept(ObservationCategory.SURVEY);
  protected final CodeableConcept UNKNOWN = createCodeableConcept(DataAbsentReason.UNKNOWN);
  protected final CodeableConcept VITAL_SIGNS = createCodeableConcept(ObservationCategory.VITALSIGNS);
  protected final CodeableConcept OBI = createCodeableConcept(
      createCoding(IDENTIFIER_TYPE_CODES, "OBI", "Observation Instance Identifier"));

  protected final Extension DATA_PRESENCE_UNKNOWN = createExtension(UNCERTAINTY_OF_PRESENCE,
      createCoding(SNOMED_CT, "261665006", "Unknown (qualifier value)"), "Presence unknown.");
  protected final Extension DATA_ABSENT_FOR_UNKNOWN_REASON =
      createExtension(createCoding(DATA_ABSENT_REASON.getUrl(), DataAbsentReason.UNKNOWN.toCode()));

  protected final DateType UNKNOWN_DATE = (DateType) new DateType().addExtension(DATA_ABSENT_FOR_UNKNOWN_REASON);
  protected final DateTimeType UNKNOWN_DATE_TIME = (DateTimeType) new DateTimeType().addExtension(DATA_ABSENT_FOR_UNKNOWN_REASON);

  private static final Map<String, String> CODE_SYSTEMS, LAB_UNITS;

  protected static final String HL7_OID = "2.16.840.1.113883";
  protected static final String DE_HC_OID = "1.2.276.0.76.5";

  static {
    CODE_SYSTEMS = new HashMap<>();

    CODE_SYSTEMS.put(DE_HC_OID + ".409", ICD_10_GM.getUrl());
    CODE_SYSTEMS.put(DE_HC_OID + ".502", ICD_10_GM.getUrl());

    CODE_SYSTEMS.put(DE_HC_OID + ".483", GENDER_AMTLICH_DE.getUrl());
    CODE_SYSTEMS.put(DE_HC_OID + ".487", OPS.getUrl());
    CODE_SYSTEMS.put(DE_HC_OID + ".498", ATC.getUrl());

    CODE_SYSTEMS.put("1.2.840.10008.2.16.4", DCM.getUrl());

    CODE_SYSTEMS.put(HL7_OID + ".6.96", SNOMED_CT.getUrl());
    CODE_SYSTEMS.put(HL7_OID + ".6.1", LOINC.getUrl());
    CODE_SYSTEMS.put(HL7_OID + ".6.8", UCUM.getUrl());
    CODE_SYSTEMS.put(HL7_OID + ".4.642.1.1075", ConditionVerStatus.NULL.getSystem());
    CODE_SYSTEMS.put(HL7_OID + ".4.642.4.1048", DATA_ABSENT_REASON.getUrl()); // Not CodeSystem but StructureDefinition needed here
    CODE_SYSTEMS.put(HL7_OID + ".4.642.4.110", EventStatus.NULL.getSystem());
    CODE_SYSTEMS.put(HL7_OID + ".4.642.1.1074", ConditionClinical.NULL.getSystem());
    CODE_SYSTEMS.put(HL7_OID + ".4.642.4.1379", MedicationStatementStatus.NULL.getSystem());
    CODE_SYSTEMS.put(HL7_OID + ".4.642.4.2", AdministrativeGender.NULL.getSystem());
    CODE_SYSTEMS.put(HL7_OID + ".4.9", UNII.getUrl());
    CODE_SYSTEMS.put(HL7_OID + ".6.18.2.6", ISBT.getUrl());
    CODE_SYSTEMS.put(HL7_OID + ".6.238", RACE_AND_ETHNICITY_CDC.getUrl());

    CODE_SYSTEMS.put("2.25.24197857203266734864793317670504947440", ECRF_PARAMETER_CODES.getUrl());
    CODE_SYSTEMS.put("2.25.289763784830452322853973378638183835703", FRAILTY_SCORE.getUrl());

    CODE_SYSTEMS.put(ISO_3166_COUNTRY_CODES.getUrl(), ISO_3166_COUNTRY_CODES.getUrl()); //ISO-3166, CountryCodes
    CODE_SYSTEMS.put(ISO_3166_GERMAN_STATE_CODES.getUrl(), ISO_3166_GERMAN_STATE_CODES.getUrl()); //ISO-3166-2:DE, State-Codes from Germany

    LAB_UNITS = new HashMap<>();

    LAB_UNITS.put("crp_1", "mg/L");
    LAB_UNITS.put("crp_2", "nmol/L");
    LAB_UNITS.put("crp_3", "mg/dL");
    LAB_UNITS.put("ferritin_1", "ng/mL");
    LAB_UNITS.put("ferritin_2", "pmol/L");
    LAB_UNITS.put("bilirubin_1", "mg/dL");
    LAB_UNITS.put("bilirubin_2", "umol/L");
    LAB_UNITS.put("ddimer_1", "ug/mL");
    LAB_UNITS.put("ddimer_2", "ng/mL");
    LAB_UNITS.put("ddimer_3", "ng/mL{FEU}");
    LAB_UNITS.put("ddimer_4", "ug/mL{FEU}");
    LAB_UNITS.put("ddimer_5", "ug/dL{DDU}");
    LAB_UNITS.put("ddimer_6", "{titer}");
    LAB_UNITS.put("gammagt_1", "U/L");
    LAB_UNITS.put("gotast_1", "U/L");
    LAB_UNITS.put("ldh_1", "U/L");
    LAB_UNITS.put("kardiale_troponine_1", "ug/L");
    LAB_UNITS.put("kardiale_troponine_2", "ng/L");
    LAB_UNITS.put("kardiale_troponine_3", "ng/mL");
    LAB_UNITS.put("hamoglobin_1", "mmol/L");
    LAB_UNITS.put("hamoglobin_2", "g/dL");
    LAB_UNITS.put("hamoglobin_3", "g/L");
    LAB_UNITS.put("kreatinin_1", "umol/L");
    LAB_UNITS.put("kreatinin_2", "mg/L");
    LAB_UNITS.put("laktat_1", "mmol/L");
    LAB_UNITS.put("laktat_2", "mg/dL");
    LAB_UNITS.put("leukozyten_absolut_1", "10*3/uL");
    LAB_UNITS.put("lymphozyten_absolut_1", "10*3/uL");
    LAB_UNITS.put("neutrophile_absolut_1", "10*3/uL");
    LAB_UNITS.put("ptt_1", "s");
    LAB_UNITS.put("thrombozyten_absolut_1", "10*3/uL");
    LAB_UNITS.put("inr_1", "{INR}");
    LAB_UNITS.put("serumalbumin_1", "g/L");
    LAB_UNITS.put("serumalbumin_2", "umol/L");
    LAB_UNITS.put("serumalbumin_3", "g/dL");
    LAB_UNITS.put("antithrombin_iii_1", "%");
    LAB_UNITS.put("antithrombin_iii_2", "[IU]/mL");
    LAB_UNITS.put("pct_procalcitonin_1", "ng/mL");
    LAB_UNITS.put("il6_interleukin_6_1", "pg/mL");
    LAB_UNITS.put("ntprobnp_1", "pg/mL");
    LAB_UNITS.put("fibrinogen_1", "g/L");
    LAB_UNITS.put("fibrinogen_2", "mg/dL");
    LAB_UNITS.put("sarsco_v2_covid19_ig_g_ia_qn_1", "[arb'U]/mL");
    LAB_UNITS.put("sarsco_v2_covid19_ig_m_ia_qn_1", "[arb'U]/mL");
    LAB_UNITS.put("sarsco_v2_covid19_ig_a_ia_qn_1", "[IU]/mL");
    LAB_UNITS.put("sarsco_v2_covid19_ab_ia_qn_1", "[IU]/mL");
  }

  protected final Patient getPatient() {
    return getForm().getStudyEvent().getSubject().getPatient();
  }

  protected final Reference getOrganizationReference() {
    return getForm().getStudyEvent().getSubject().getOrganizationReference();
  }

  protected final Coding createCoding(CommonCodeSystem system, String code, String display) {
    return createCoding(system.getUrl(), code, display);
  }

  protected final Coding createCoding(CommonCodeSystem system, String code) {
    return createCoding(system.getUrl(), code);
  }

  protected final Coding createCoding(NUMCodeSystem system, String code, String display) {
    return createCoding(system.getUrl(), code, display);
  }

  protected final Coding createCoding(NUMCodeSystem system, String code) {
    return createCoding(system.getUrl(), code);
  }

  protected final Coding createCoding(String system, String code, String display) {
    var coding = new Coding(system, code, display);
    if (ICD_10_GM.getCodeSystem().getUrl().equals(system)) {
      coding.setVersion("2021"); // TODO Replace with the actual retrieved year
    }
    return coding;
  }

  protected final Coding createCoding(String system, String code) {
    return createCoding(system, code, null);
  }

  protected final Coding createCoding(ItemData itemData) {
    var coding = new Coding();

    if (!itemData.isEmpty()) {
      var items = split(itemData.getValue(), "_");

      var system = "";
      var systemOID = "";
      var code = "";

      if (items.length == 1) {
        code = items[0];
      } else if (items.length == 2) {
        systemOID = items[0];
        code = items[1];
      }

      if (isBlank(systemOID)) {
        logInvalidValue(CODESYSTEM, itemData.copy().setValue(systemOID));
      } else if ("NoCodeSystem".equals(systemOID)) {
        // Do nothing...
      } else {
        system = CODE_SYSTEMS.get(systemOID);
        if (isBlank(system)) {
          logInvalidValue(CODESYSTEM, itemData.copy().setValue(systemOID));
          system = "urn:oid:" + systemOID;
        }
      }

     if (isBlank(code)) {
        logInvalidValue(CODING, itemData.copy().setValue(code));
        code = "";
      } else if ("NoCode".equals(code)) {
        code = "";
      }

      coding = createCoding(system, code);
    }

    return coding;
  }

  protected final Coding createLabCoding(ItemData itemData) {
    var coding = new Coding();

    if (!itemData.isEmpty()) {
      var items = split(itemData.getValue(), "_");

      var system = "";
      var code = "";
      var display = "";

      switch (items.length) {
        case 1:
          code = items[0];
          system = LOINC.getUrl();
          break;
        case 2:
          code = items[0];
          display = items[1];
          system = LOINC.getUrl();
          break;
        case 3:
          system = items[0];
          code = items[1];
          display = items[2];
      }

      if (isBlank(code)) {
        logInvalidValue(CODING, itemData.copy().setValue(code));
        code = "";
      } else if ("NoCode".equals(code)) {
        code = "";
      }

      coding = createCoding(system, code, display);
    }

    return coding;
  }

  protected final List<Coding> createCodings(ItemData itemData) {
    return itemData.isEmpty() ? List.of() :
        Arrays.stream(splitByWholeSeparator(itemData.getValue(), "__"))
            .map(value -> itemData.copy().setValue(value))
            .map(this::createCoding)
            .filter(Coding::hasCode)
            .collect(toList());
  }

  protected final List<Coding> createLabCodings(ItemData itemData) {
    return itemData.isEmpty() ? List.of() :
        Arrays.stream(splitByWholeSeparator(itemData.getValue(), "__"))
            .map(value -> itemData.copy().setValue(value))
            .map(this::createLabCoding)
            .filter(Coding::hasCode)
            .collect(toList());
  }

  @SuppressWarnings("ConstantConditions")
  protected CodeableConcept createCodeableConcept(Enum<?> value) {
    Function<String, String> function = method -> invokeMethod(findMethod(value.getClass(), method), value).toString();
    return new CodeableConcept() // NOTE Ugly workaround as HAPI enums do not implement a common interface
        .setCoding(List.of(createCoding(function.apply("getSystem"), function.apply("toCode"), function.apply("getDisplay"))));
  }

  protected final CodeableConcept createCodeableConcept(ItemData itemData) {
    return updateCodeableConcept(new CodeableConcept(), itemData);
  }

  protected final CodeableConcept createCodeableConcept(Coding... codings) {
    return updateCodeableConcept(new CodeableConcept(), Arrays.asList(codings));
  }

  protected final CodeableConcept updateCodeableConcept(CodeableConcept codeableConcept, ItemData itemData) {
    return updateCodeableConcept(codeableConcept, createCodings(itemData));
  }

  private CodeableConcept updateCodeableConcept(CodeableConcept codeableConcept, List<Coding> codings) {
    codings.stream().filter(not(Coding::isEmpty)).forEach(codeableConcept::addCoding);
    return codeableConcept;
  }

  protected final Identifier createIdentifier(ResourceTypes resourceType, ItemData itemData) {
    var itemGroupData = itemData.getItemGroupData();
    var formData = itemGroupData.getFormData();
    var studyEventData = formData.getStudyEventData();
    var subjectData = studyEventData.getSubjectData();
    var value = format("%s-%s.%s-%s.%s-%s.%s-%s",
                       subjectData.getSubjectKey(),
                       studyEventData.getStudyEventOID(), studyEventData.getStudyEventRepeatKey(),
                       formData.getFormOID(), formData.getFormRepeatKey(),
                       itemGroupData.getItemGroupOID(), itemGroupData.getItemGroupRepeatKey(),
                       itemData.getItemOID());

    if (!getEnvironment().containsProperty("debug"))  {
      value = sha256Hex(value);
    }

    return new Identifier()
        .setSystem(getEnvironment().getProperty("fhir.identifier.system." + resourceType.toCode().toLowerCase()))
        .setValue(value);
  }

  protected final Age createAge(ItemData itemData) {
    var age = new Age();
    var quantity = createQuantity(itemData, "a", "years");

    if (!quantity.isEmpty()) {
      // NOTE Ugly workaround as HAPI is missing castToAge method
      age = (Age) age.setValue(quantity.getValue()).setSystem(quantity.getSystem())
          .setCode(quantity.getCode()).setUnit(quantity.getUnit());
    }

    return age;
  }

  protected final Quantity createQuantity(ItemData itemData, String code, String unit) {
    var quantity = new Quantity();

    if (itemData != null && !itemData.isEmpty()) {
      try {
        quantity.setValue(new BigDecimal(itemData.getValue().replace(',', '.')))
            .setSystem(UCUM.getUrl()).setCode(code).setUnit(unit);
      } catch (RuntimeException runtimeException) {
        logInvalidValue(QUANTITY, itemData);
      }
    }

    return quantity;
  }

  protected final DateType createDateType(ItemData itemData) {
    var dateType = new DateType();

    if (itemData != null) {
      if (itemData.isEmpty()) {
        dateType = UNKNOWN_DATE;
      } else {
        try {
          dateType = new DateType(itemData.getValue());

          if (dateType.getTimeZone() == null) {
            dateType.setTimeZone(TimeZone.getDefault());
          }

        } catch (RuntimeException runtimeException) {
          logInvalidValue(DATETIME, itemData);
        }
      }
    }

    return dateType;
  }

  protected final DateTimeType createDateTimeType(ItemData itemData) {
    var dateTimeType = new DateTimeType();

    if (itemData != null) {
      if (itemData.isEmpty()) {
        dateTimeType = UNKNOWN_DATE_TIME;
      } else {
        try {
          var value = itemData.getValue();

          // Add ":00" if seconds are missing...
          dateTimeType = new DateTimeType(value.length() == 16 ? value + ":00" : value);

          if (dateTimeType.getTimeZone() == null) {
            dateTimeType.setTimeZone(TimeZone.getDefault());
          }

        } catch (RuntimeException runtimeException) {
          logInvalidValue(DATETIME, itemData);
        }
      }
    }

    return dateTimeType;
  }

  protected final Meta createMeta(NUMStructureDefinition numStructureDefinition) {
    return new Meta().addProfile(numStructureDefinition.getUrl());
  }

  protected final Meta createMeta(CommonStructureDefinition commonStructureDefinition) {
    return new Meta().addProfile(commonStructureDefinition.getUrl());
  }

  protected final StringType createStringType(ItemData itemData) {
    var stringType = new StringType();

    if (itemData != null && !itemData.isEmpty()) {
      stringType.setValue(itemData.getValue());
    }

    return stringType;
  }

  protected final Extension createExtension(NUMStructureDefinition structureDefinition, Coding coding, String text) {
    return new Extension(structureDefinition.getUrl()).setValue(createCodeableConcept(coding).setText(text));
  }

  protected final Extension createExtension(Coding coding) {
    return new Extension(coding.getSystem()).setValue(new CodeType(coding.getCode()));
  }

  protected final String getLabUnit(String parameter, String unitCode) {
    return LAB_UNITS.get(format("%s_%s", parameter, unitCode));
  }

  protected final void logInvalidValue(DataTypes dataType, ItemData itemData) {
    logInvalidValue(dataType, itemData, new Coding());
  }

  protected final void logInvalidValue(DataTypes dataType, ItemData itemData, Coding defaultCoding) {
    logInvalidValue(dataType.toCode(), itemData, defaultCoding);
  }

  protected final void logInvalidValue(ResourceTypes resourceType, ItemData itemData) {
    logInvalidValue(resourceType, itemData, new Coding());
  }

  protected final void logInvalidValue(ResourceTypes resourceType, ItemData itemData, Coding defaultCoding) {
    logInvalidValue(resourceType.toCode(), itemData, defaultCoding);
  }

  private void logInvalidValue(String resourceType, ItemData itemData, Coding defaultCoding) {
    var itemGroupData = itemData.getItemGroupData();

    var defaultMessage = defaultCoding.isEmpty() ? "" :
        format(" (using default coding '%s/%s'/'%s' for invalid part)",
               defaultCoding.getSystem(), defaultCoding.getCode(), defaultCoding.getDisplay());

    if (itemGroupData == null) {
      log.warn("Item '{}' has invalid {} value in '{}' {}",
               itemData.getItemOID(), resourceType, itemData.getValue(), defaultMessage);
    } else {
      var formData = itemGroupData.getFormData();
      var studyEventData = formData.getStudyEventData();
      var subjectData = studyEventData.getSubjectData();
      log.warn("Item '{}-{}.{}-{}.{}-{}.{}-{}' has invalid {} value in '{}' {}",
               subjectData.getSubjectKey(),
               studyEventData.getStudyEventOID(), studyEventData.getStudyEventRepeatKey(),
               formData.getFormOID(), formData.getFormRepeatKey(),
               itemGroupData.getItemGroupOID(), itemGroupData.getItemGroupRepeatKey(),
               itemData.getItemOID(),
               resourceType, itemData.getValue(),
               defaultMessage);
    }
  }

}
