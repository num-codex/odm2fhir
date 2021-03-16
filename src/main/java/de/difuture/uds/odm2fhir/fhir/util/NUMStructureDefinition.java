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

import org.hl7.fhir.r4.model.StructureDefinition;

public enum NUMStructureDefinition {

  AGE,
  APHERESIS,
  BLOOD_PRESSURE,
  BODY_HEIGHT,
  BODY_TEMPERATURE,
  BODY_WEIGHT,
  CARBON_DIOXIDE_PARTIAL_PRESSURE,
  CARDIOVASCULAR_DISEASES,
  CHRONIC_KIDNEY_DISEASES,
  CHRONIC_LIVER_DISEASES,
  CHRONIC_LUNG_DISEASES,
  CHRONIC_NEUROLOGICAL_MENTAL_DISEASES,
  COMPLICATIONS_COVID_19,
  DEPENDENCE_ON_VENTILATOR,
  DIABETES_MELLITUS,
  DIAGNOSIS_COVID_19,
  DIAGNOSTIC_REPORT_RADIOLOGY,
  DIALYSIS,
  DISCHARGE_DISPOSITION,
  DO_NOT_RESUSCITATE_ORDER,
  ETHNIC_GROUP,
  EXTRACORPOREAL_MEMBRANE_OXYGENATION,
  FRAILTY_SCORE,
  GASTROINTESTINAL_ULCERS,
  GECCO_BUNDLE,
  HEART_RATE,
  HISTORY_OF_TRAVEL,
  HUMAN_IMMUNODEFICIENCY_VIRUS_INFECTION,
  IMMUNIZATION,
  INHALED_OXYGEN_CONCENTRATION,
  INTERVENTIONAL_CLINICAL_TRIAL_PARTICIPATION,
  KNOWN_EXPOSURE,
  MALIGNANT_NEOPLASTIC_DISEASE,
  ORGAN_RECIPIENT,
  OXYGEN_PARTIAL_PRESSURE,
  OXYGEN_SATURATION,
  PATIENT("Patient"),
  PATIENT_IN_ICU,
  PH("pH"),
  PHARMACOLOGICAL_THERAPY,
  PHARMACOLOGICAL_THERAPY_ANTICOAGULANTS,
  PREGNANCY_STATUS,
  PRONE_POSITION,
  RADIOLOGY_PROCEDURES,
  RESPIRATORY_RATE,
  RESPIRATORY_THERAPIES,
  RHEUMATOLOGICAL_IMMUNOLOGICAL_DISEASES,
  SARS_COV_2_RT_PCR,
  SARS_COV_2_AB_PNL_SER_PL_IA,
  SARS_COV_2_AB_SER_PL_IA_ACNC,
  SARS_COV_2_AB_SER_PL_QL_IA,
  SARS_COV_2_IGA_SER_PL_IA_ACNC,
  SARS_COV_2_IGA_SER_PL_QL_IA,
  SARS_COV_2_IGG_SER_PL_IA_ACNC,
  SARS_COV_2_IGG_SER_PL_QL_IA,
  SARS_COV_2_IGM_SER_PL_IA_ACNC,
  SARS_COV_2_IGM_SER_PL_QL_IA,
  SEX_ASSIGNED_AT_BIRTH,
  SMOKING_STATUS,
  SOFA_SCORE,
  STUDY_INCLUSION_COVID_19,
  SYMPTOMS_COVID_19,
  UNCERTAINTY_OF_PRESENCE;

  private static final String BASE = "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/";

  private final StructureDefinition structureDefinition;

  NUMStructureDefinition(String target) {
    structureDefinition = new StructureDefinition().setUrl(BASE + target);
  }

  NUMStructureDefinition() {
    structureDefinition = new StructureDefinition().setUrl(BASE + name().toLowerCase().replace('_', '-'));
  }

  public StructureDefinition getStructureDefinition() {
    return structureDefinition;
  }

  public String getUrl() {
    return structureDefinition.getUrl();
  }

}