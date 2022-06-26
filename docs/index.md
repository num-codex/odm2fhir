Mapper for study/patient data in [CDISC ODM](https://www.cdisc.org/standards/data-exchange/odm) based on the [GECCO data dictionary](https://confluence.imi.med.fau.de/display/MIIC/30+EDC+System+REDCap) to [HL7 FHIR](https://www.hl7.org/fhir) adhering to the [GECCO implementation guide](https://simplifier.net/ForschungsnetzCovid-19) (see [here](mappings.md) for mapping details).

## Command
```sh
docker run **ENVIRONMENTS** **VOLUMES** ghcr.io/num-codex/odm2fhir **ARGUMENTS**
```
(Note: Replace all `**VARIABLE**` references with their actual values.)

### Arguments

* `--help` Print `README`.

* `--debug` Print debug logging messages.

* `--odm.redcap.datadictionary` Print current data dictionary.

* `--odm.redcap.mapping` Print current mapping.

* `--odm.incompleteforms.allowed=(true|false)` Allow processing of incomplete forms (`false` by default). 

* `--odm.subjectkeys.hashed=(true|false)` Hash the original subject keys (`true` by default).

* `--fhir.identifier.system.**TYPE**=**TYPE_IDENTIFIER_SYSTEM**` Add an identifier system with `**TYPE**` of `condition`, `consent`, `diagnosticreport`, `encounter`, `immunization`, `medicationstatement`, `observation`, `organization`, `patient` or `procedure` (see [here](https://simplifier.net/guide/GermanCoronaConsensusDataSet-ImplementationGuide/TransactionBundle)).

* `--fhir.identifier.assigner=**IDENTIFIER_ASSIGNER**` Add an identifier assigner.

* `--fhir.encounters.enabled=(true|false)` Enable FHIR `Encounter` resources for patient cases (`true` by default).

* `--fhir.updateascreate.enabled=(true|false)` Enable update-as-create (see [here](https://www.hl7.org/fhir/http.html#upsert), `false` by default).

* `--fhir.codingdisplays.removed=(true|false)` Remove the `display` property from all FHIR `Coding` resources (`false` by default).
* 
* `--fhir.validation.enabled=(true|false)` Enable FHIR resource validation (see [here](#validation), `false` by default).

* `--cron="**CRON_PATTERN**"` Enable timed execution (see [here](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html)).

## Input

Enable ***either*** *local* input by adding the volume for the [local file](#local-file) to `**VOLUMES**` ***or*** *remote* input by adding the arguments for either [REDCap](#redcap) or [DIS](#dis) to `**ARGUMENTS**`.

### Local File
```sh
-v **ODM_FILE_PATH**:/workspace/input/ODM.xml
```

### REDCap
```sh
--odm.redcap.api.url=**ODM_REDCAP_API_URL**
--odm.redcap.api.token=**ODM_REDCAP_API_TOKEN**
```

#### HTTP(S) Proxy
Enable HTTP(S) proxy by adding the environment to `**ENVIRONMENTS**`:
```sh
-e JAVA_TOOL_OPTIONS="-Dhttp.proxyHost=**HTTP_PROXY_HOST** -Dhttp.proxyPort=**HTTP_PROXY_PORT** -Dhttp.nonProxyHosts=**HTTP_NON_PROXY_HOSTS** -Dhttps.proxyHost=**HTTPS_PROXY_HOST** -Dhttps.proxyPort=**HTTPS_PROXY_PORT** -Dhttps.nonProxyHosts=**HTTPS_NON_PROXY_HOSTS**"
```

#### PKCS12 Certificate
```sh
--odm.redcap.api.key.file.path=**ODM_REDCAP_API_KEY_FILE_PATH**
--odm.redcap.api.key.password=**ODM_REDCAP_API_KEY_PASSWORD**
```

#### Export Chunking
The ODM export from REDCap is by default divided into exports for data of single patients to avoid connection issues and timeouts but can be changed by adding the argument:
```sh
--odm.redcap.api.patientspercall=**PATIENTS_PER_CALL**
```

#### Filtering
By default, all subjects within an ODM are processed. To process only subjects with changed values since the last run add a volume with `**CACHE_FOLDER**` pointing to a local folder:
```sh
-v **CACHE_FOLDER**:/workspace/cache
```
(Note: To reset the filtering, empty the folder `**CACHE_FOLDER**`.)

### DIS
```sh
--odm.dis.rest.url=**ODM_DIS_REST_URL**
--odm.dis.rest.studyname=**ODM_DIS_REST_STUDYNAME**
--odm.dis.rest.username=**ODM_DIS_REST_USERNAME**
--odm.dis.rest.password=**ODM_DIS_REST_PASSWORD**
```

#### HTTP(S) Proxy
(see [here](#https-proxy))

#### PKCS12 Certificate
```sh
--odm.dis.rest.key.file.path=**ODM_DIS_REST_KEY_FILE_PATH**
--odm.dis.rest.key.password=**ODM_DIS_REST_KEY_PASSWORD**
```

## Output

***Either*** enable *local* output by adding the volume for the [local folder](#local-folder) to `**VOLUMES**` ***or*** enable *remote* output by adding the argument for the [FHIR Server](#fhir-server) in `**ARGUMENTS**` - together with [BasicAuth](#basicauth) or [OAuth2 (Client Credentials)](#oauth2-client-credentials), if applicable.

### Local Folder
```sh
-v **FHIR_BUNDLES_FOLDER_PATH**:/workspace/output
```

### FHIR Server
```sh
--fhir.server.url=**FHIR_SERVER_URL**
```

#### HTTP(S) Proxy
(see [here](#https-proxy))

#### BasicAuth
```sh
--fhir.server.basicauth.username=**FHIR_SERVER_BASICAUTH_USERNAME**
--fhir.server.basicauth.password=**FHIR_SERVER_BASICAUTH_PASSWORD**
```

#### OAuth2 (Client Credentials)
```sh
--fhir.server.oauth2.token.url=**FHIR_SERVER_OAUTH2_TOKEN_URL**
--fhir.server.oauth2.client.id=**FHIR_SERVER_OAUTH2_CLIENT_ID**
--fhir.server.oauth2.client.secret=**FHIR_SERVER_OAUTH2_CLIENT_SECRET**
```

#### PKCS12 Certificate
```sh
--fhir.server.key.file.path=**FHIR_SERVER_KEY_FILE_PATH**
--fhir.server.key.password=**FHIR_SERVER_KEY_PASSWORD**
```

## Validation
Enable validation of the generated FHIR resources - and filtering out of all invalid ones - according to mentioned profiles by adding the argument `--fhir.validation.enabled=true`.

Enable the use of an external terminology server by adding the argument(s) below - together with BasicAuth or OAuth2 (Client Credentials), if applicable.

### FHIR Terminology Server
```sh
--fhir.terminologyserver.url=**FHIR_TERMINOLOGYSERVER_URL**
```

#### HTTP(S) Proxy
(see [here](#https-proxy))

#### BasicAuth
```sh
--fhir.terminologyserver.basicauth.username=**FHIR_TERMINOLOGYSERVER_BASICAUTH_USERNAME**
--fhir.terminologyserver.basicauth.password=**FHIR_TERMINOLOGYSERVER_BASICAUTH_PASSWORD**
```

#### OAuth2 (Client Credentials)
```sh
--fhir.terminologyserver.oauth2.token.url=**FHIR_TERMINOLOGYSERVER_OAUTH2_TOKEN_URL**
--fhir.terminologyserver.oauth2.client.id=**FHIR_TERMINOLOGYSERVER_OAUTH2_CLIENT_ID**
--fhir.terminologyserver.oauth2.client.secret=**FHIR_TERMINOLOGYSERVER_OAUTH2_CLIENT_SECRET**
```

#### PKCS12 Certificate
```sh
--fhir.terminologyserver.key.file.path=**FHIR_TERMINOLOGYSERVER_KEY_FILE_PATH**
--fhir.terminologyserver.key.password=**FHIR_TERMINOLOGYSERVER_KEY_PASSWORD**
```

## License

Copyright &copy; 2021 DIFUTURE (https://difuture.de)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.