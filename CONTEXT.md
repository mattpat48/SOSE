
PROGETTO SOSE - CONTEXT
=======================

Obiettivo
--------
Fornire a un altro sviluppatore (o a un altro chatbot) tutte le decisioni architetturali, le implementazioni principali, i percorsi dei file di interesse, i comandi di build/run e gli scenari di test per riprendere, far evolvere o far girare il progetto localmente.

Sintesi architetturale
----------------------
- Struttura a due servizi Spring Boot separati:
	- DaaS (Data-as-a-Service): espone i dati RDF tramite endpoint REST, esegue query SPARQL su un dataset Jena in-memory (caricato da `dataset/tourism.ttl`), compone e invia richieste di valutazione all'EaaS.
	- EaaS (Ethics-as-a-Service): motore di policy che carica policy JSON da `backend-eaas/src/main/resources/policies/`, valuta rischi (accessibilità, sostenibilità, provenienza, ecc.) e risponde con decisione, score e audit trail.
- Frontend leggero in Vue 3 + Vite che parla con il DaaS (il DaaS a sua volta chiama l'EaaS).
- Comunicazione tra servizi: REST JSON sync (DaaS -> EaaS `POST /api/evaluate`).

Decisioni importanti
--------------------
- Separazione dei servizi: isolamento delle responsabilità (DaaS = accesso ai dati + orchestrazione, EaaS = valutazione policy). Questo facilita testing e riuso del motore di policy.
- Dataset: usato Apache Jena (ARQ, TDB2) in modalità embedded/in-memory per demo. Per produzione, persistere con TDB2 o endpoint SPARQL remoto.
- Policy: file JSON esterni caricati all'avvio (possibile evoluzione: hot-reload watch). Schema JSON volutamente semplice (conditions, threshold, action), ma estendibile per regole più complesse.
- Decision aggregation: politica di default usata = aggregazione conservativa (es. max risk). È descritta nel codice di aggregazione dell'EaaS; può essere sostituita con regole custom.
- Audit: ogni valutazione restituisce un `auditTrail` (per trasparenza) e può essere persistita con JPA/H2 (boilerplate già incluso). In demo è accessibile nella risposta HTTP.

Port e convenzioni
-------------------
- DaaS: porta 8080 (context path `/daas` usato negli esempi).
- EaaS: porta 8081 (context path `/eaas`).
- Frontend dev: Vite default 5173.

Principali file e percorsi (entry points)
-----------------------------------------
- Root workspace: [README.md](README.md), [SCOPE.md](SCOPE.md), [CONTEXT.md](CONTEXT.md)
- Dataset RDF: `dataset/tourism.ttl`
- Backend DaaS:
	- `backend-daas/pom.xml` (dipendenze Maven)
	- `backend-daas/src/main/java/.../RdfStoreConfig.java` (caricamento Jena + dataset)
	- `backend-daas/src/main/java/.../TourismQueryService.java` (utility SPARQL)
	- `backend-daas/src/main/java/.../controller/PlaceController.java` (endpoint REST per ricerca/places)
	- `backend-daas/src/main/java/.../controller/RecommendationController.java` (endpoint che chiama EaaS)
	- `backend-daas/src/main/java/.../client/EaasClient.java` (client REST semplice verso EaaS)
	- DTOs: `PlaceDTO`, `EvaluationRequest`, `EvaluationResponse`.
- Backend EaaS:
	- `backend-eaas/pom.xml`
	- `backend-eaas/src/main/resources/policies/*.json` (policy files: accessibility, sustainability, provenance)
	- `backend-eaas/src/main/java/.../model/Policy.java` (modello policy)
	- `backend-eaas/src/main/java/.../service/PolicyEvaluationService.java` (engine di valutazione)
	- `backend-eaas/src/main/java/.../controller/EvaluationController.java` (endpoint `POST /api/evaluate`)
	- Audit persistence boilerplate (JPA/H2) disponibile se si vuole salvare le valutazioni.
- Frontend:
	- `frontend/package.json`, `vite.config.js`
	- `frontend/src/main.js` e `frontend/src/App.vue`
	- Componenti: `SearchForm.vue`, `PlaceList.vue`, `PlaceDetail.vue`, `EthicalDecision.vue`.

SPARQL e modello RDF
---------------------
- File principale: `dataset/tourism.ttl` (modello Turtle): contiene risorse `:Place` con proprietà come `:name`, `:category`, `:location`, `:rating`, `:sustainabilityScore`, `:accessibilityScore`, `:source`.
- Query esempio (ricerca per categoria + rating minimo):
	SELECT ?place ?name ?category ?rating WHERE { ?place a :Place; :name ?name; :category "museum"; :rating ?rating . FILTER(?rating >= 4.0) }
- Tutte le query sono costruite da `TourismQueryService` e ritornano DTO mappati per il frontend.

Schema policy (JSON) - sintesi
-----------------------------
- Ogni policy JSON contiene:
	- `id` e `name`
	- `conditions`: array di condizioni (es. `accessibilityScore < 0.5`, `sustainabilityScore < 0.3`)
	- `decision`/`action`: mappa valore -> outcome (es. `score` -> `REJECT`/`REVISE`/`PROCEED`)
	- `weight` o `severity` (opzionale) per aggregazione
- I file policy di esempio si trovano in `backend-eaas/src/main/resources/policies/`.

Flusso di chiamata (esecuzione tipica)
-------------------------------------
1. Utente sul frontend invia ricerca al DaaS (`/daas/api/places/search`).
2. DaaS esegue SPARQL su Jena e costruisce `PlaceDTO`.
3. DaaS invia a EaaS una `EvaluationRequest` con i dati rilevanti della `Place`.
4. EaaS valuta policy e risponde con `EvaluationResponse` contenente `decision`, `score`, `auditTrail`.
5. DaaS integra la risposta e la mostra al frontend.

Esempi di scenari demo
----------------------
- Scenario 1 (PROCEED): place con `accessibilityScore >= 0.8` e `sustainabilityScore >= 0.7`.
- Scenario 2 (REJECT / ESCALATE): place con `accessibilityScore < 0.3` o con provenienza non verificata (elevata probabilità di bias).

Comandi di build & run (locale)
-------------------------------
Prerequisiti: Java (JDK 17+ consigliato), Maven, Node.js + npm/yarn.

Esempio di comandi:
```bash
# backend-eaas
cd backend-eaas
mvn clean package
java -jar target/backend-eaas-0.0.1-SNAPSHOT.jar

# backend-daas
cd ../backend-daas
mvn clean package
java -jar target/backend-daas-0.0.1-SNAPSHOT.jar

# frontend (dev)
cd ../frontend
npm install
npm run dev
```

Note: se `mvn` non è installato, installare Maven oppure usare IDE (IntelliJ/VSCode) che gestisce Maven per build/run.

Test rapidi (curl)
------------------
- Esempio: chiedere valutazione per una place (sostituire host/port se necessario):
```bash
curl -X POST "http://localhost:8080/daas/api/recommendations/ethical-recommendation" \
	-H 'Content-Type: application/json' \
	-d '{"placeId": "http://example.org/places/1"}'
```

Punti di estensione e miglioramento
-----------------------------------
- Hot-reload delle policy (watch su `resources/policies/` con ricarica sicura).
- Migrare Jena in-memory a TDB2 persistente o a un endpoint Fuseki per dataset grandi.
- Aggiungere autenticazione/authorization tra DaaS e EaaS (mutual TLS o token).
- Migliorare il linguaggio delle policy: integrare un motore di regole (Drools) o usare SPIN/SHACL per regole RDF-native.
- Persistere audit su DB esterno e esporre endpoint per reportistica.

Known issues e note environment
-------------------------------
- In questo ambiente di sviluppo temporaneo `mvn` potrebbe non essere installato (error: `mvn: command not found`). Assicurarsi che Maven sia installato localmente.
- Java JDK è richiesto; almeno JDK 17 raccomandato per Spring Boot 3.x.

Checklist per riprendere il lavoro
---------------------------------
- [ ] Installare Java + Maven + Node
- [ ] Eseguire `mvn clean package` in `backend-eaas` e `backend-daas`
- [ ] Avviare `backend-eaas` (porta 8081) e `backend-daas` (porta 8080)
- [ ] Avviare frontend (`npm run dev`) e testare le due demo scenarios

Contatti e riferimenti rapidi (per il repo)
-------------------------------------------
- File chiave per partire:
	- `dataset/tourism.ttl` (dati)
	- `backend-daas/src/main/java/.../` (SPARQL + controller)
	- `backend-eaas/src/main/resources/policies/` (policy JSON)
	- `frontend/src/` (UI components)

Problema osservato durante l'esecuzione (nota operativa)
-------------------------------------------------------
- Data: 2026-05-19
- Sintomo: dopo aver avviato i servizi con `run-all.sh` o `mvn spring-boot:run`, le richieste verso il DaaS (es. `GET /daas/api/places`) restituiscono errori di tipo `connection refused` oppure risposte vuote.
- Possibili aree da controllare:
	- Verificare che i servizi siano effettivamente in ascolto sulle porte attese (8080 e 8081). Vedi `ss -ltnp` o `lsof`.
	- Controllare i log di avvio (`mvn spring-boot:run`) per errori di ApplicationContext o eccezioni che impediscono il bind del server (es. bean mancanti).
	- Controllare che il dataset sia caricato e le query SPARQL costruite corrispondano ai predicati presenti in `dataset/tourism.ttl`.
	- Verificare i mappatori DTO/JSON per errori di serializzazione che possono produrre payload vuoti.
	- Controllare la sequenza di avvio: avviare prima EaaS, poi DaaS, quindi frontend.

Suggerimenti rapidi per debug:
	- Avviare ogni servizio in foreground e leggere i log (EaaS, DaaS separatamente).
	- Eseguire una query SPARQL di prova direttamente via codice o creare un file `.rq` per testare ARQ.
	- Controllare che `RestTemplate` e altri bean richiesti siano definiti (es. `RestTemplateConfig`).
	- Se il problema persiste, raccogliere e allegare gli stacktrace dei log per analisi.

Fine del file: questo documento è pensato per essere autoesplicativo per un altro sviluppatore o un chatbot che deve capire dove mettere mano.
