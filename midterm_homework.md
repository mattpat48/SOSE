This year the homework will be about Data as a Service and Ethics as a Service in a 
Service-Oriented Application
The  goal  of  this  homework  is  to  design  and  implement  a  small  service-oriented 
application that integrates two concepts discussed in class: 
- Data as a Service, that is, exposing data through reusable REST services. 
- Ethics as a Service, that is, a separate service that evaluates requests, decisions, 
or operations according to explicit policies, producing a justified and traceable 
decision. 
Students  may  freely  choose  the  application  domain:  cinema,  healthcare,  mobility, 
environment,  university  services,  tourism,  employment,  public  administration,  sport, 
culture, e-commerce, etc. The chosen domain should be rich enough to support both 
meaningful data queries and a non-trivial ethical discussion. 
Project Description 
Each group must implement an application composed of at least: 
- A DaaS service exposing data through REST APIs. 
- An EaaS service  or  module  evaluating  a  request,  decision,  or  operation  of  the 
application. 
- A simple client demonstrating the interaction between user, DaaS, and EaaS. 
- A final classroom presentation with slides and a working demo. 
The DaaS part must build on the ideas of the examples shown in class: RDF datasets, 
SPARQL  queries,  REST  endpoints,  JSON  output,  and  separation  between  data  and 
application logic. 
The  EaaS  part  must  build  on  the  following  concepts:  structured  requests,  external 
policies, risk assessment, final decision, explanation, provenance, and audit trail. 
Minimum DaaS Requirements 
- The Data as a Service component must: 
- use an RDF dataset created or adapted by the group; 
- expose at least 5 meaningful REST endpoints; 
- use SPARQL queries to query the dataset; 
- return results in JSON format; 
- include at least one query combining multiple conditions or relationships; 
- document the dataset, main classes/relations, and available endpoints. 
- Example endpoints: 
- /items/{id} 
- /items/category/{category} 
- /items/location/{location} 
- /items/year/{year} 
- /items/search?... 
- /items/risky 
- /items/recommended 
 
Minimum EaaS Requirements 
The Ethics as a Service component must: 
- receive a structured request from the client or backend; 
- not trust a risk value directly declared by the caller; 
- load external policies, for example JSON files; 
- produce a structured evaluation including, e.g.: 
o risk level; 
o decision: PROCEED, REVISE, ESCALATE, or REJECT; 
o rationale; 
o applied policies; 
o required actions, if any; 
o reference to an audit record; 
o store or expose an audit trace; 
o distinguish between case analysis and governance decision. 
o Policies must be coherent with the selected domain, for example: 
o personal data privacy; 
o fairness across user groups; 
o security; 
o transparency; 
o data quality or provenance; 
o responsible use of recommendations; 
o protection of vulnerable users; 
o licensing or source provenance. 
 
Integration Between DaaS and EaaS 
The application must show a case where data provided by the DaaS component is used 
to  make,  suggest,  or  support  a  decision,  and  that  decision  is  evaluated  by  the  EaaS 
component. 
Examples: 
- an  app  recommends  public  services,  while  EaaS  checks  fairness  and 
transparency; 
- a healthcare app displays data or suggestions, while EaaS checks privacy and risk; 
- a tourism app recommends places, while EaaS evaluates bias, accessibility, or 
sustainability; 
- a university app suggests courses or priorities, while EaaS evaluates equity and 
explainability; 
- a recruiting app uses candidate data, while EaaS evaluates discriminatory proxies 
and accountability. 
 
Each group must submit 
- Project source code. 
- RDF dataset used by the DaaS. 
- Short documentation of the REST endpoints. 
- At least 3 EaaS policies in an external format, such as JSON. 
- At least 2 example requests: 
o one leading to PROCEED or REVISE; 
o one leading to ESCALATE or REJECT. 
o Audit records or examples of traces produced by the system. 
o Slides for the final presentation. 
o A short README with build, execution, and demo instructions. 
 
Class Presentation 
The presentation should last about 10-12 minutes for each member of the group, 
since  larger  groups  are  expected  to  develop  more  complex  and  more  complete 
projects. For example, a group of two students should prepare a presentation of 
about 20-24 minutes, while a group of three students should prepare a presentation 
of about 30-36 minutes. 
The presentaiton should include: 
- selected domain and motivation; 
- overall application architecture; 
- RDF dataset description and main SPARQL queries; 
- DaaS REST APIs descritpion; 
- EaaS workflow; 
- policies defined by the group; 
- example request evaluated by EaaS; 
- audit/provenance of the decision; 
- live demo; 
- short critical reflection: what are the limits of your system? What should not be 
decided automatically? 
 
Homework Assessment Criteria 
IMPORTANT: Each group member is expected to have actively contributed to the 
homework and to be aware of the overall work carried out by the group. Therefore, 
the evaluation will take into account each student’s individual performance and 
understanding of the work, not only the final group outcome. 
 
Evaluation Criteria 
- Technical correctness of the DaaS 
- “Quality” of the REST/RDF/SPARQL integration 
- “Quality” of the EaaS component 
- Clarity of policies, decision, and audit 
- Originality and coherence of the selected domain 
- Quality of presentation and demo 
 
Key Message for Students 
You are not expected merely to “make two services work”. You must demonstrate that 
you are able to elaborate on the concepts underlying the project and that you understand 
that: 
- data can be ocered as a reusable service through APIs; 
- a data-based decision is not automatically neutral; 
- ethics must not be a final checkbox; 
- policies, context, explanation, responsibility, and audit must become part of the 
software architecture. 