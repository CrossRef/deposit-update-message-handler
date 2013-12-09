(ns deposit-update-message-handler.parse-test
  (:require [clojure.test :refer :all]
            [deposit-update-message-handler.parse :refer :all]))

(def sample-single-messages
  {
   ""
   #{}
   
   "MalformedXmlException"
   #{:malformed-xml}
        
   "Added with conflict"
   #{:added-with-conflict}
   
   "Record not processed because submitted version: xxxxxxx is less or equal to previously submitted version (DOI match) "
   #{:submission-version}
   
   "User with ID: {0} cant submit into handle, please contact the CrossRef admin"
   #{:permission}
   
   "User not allowed to add records for prefix: {0}"
   #{:permission}
   
   "All prefixes in a submission must match (DOI[{0}]) "
   #{:xml-structure}
   
   "year: {0} in not a valid integer"
   #{:xml-content}
   
   "title \"{title}\" was previously deleted by a CrossRef admin"
   #{:title-deleted-by-crossref-admin}
   
   "user not allowed to add or update records for the title \"{title}\""
   #{:permission}
   
   "ISSN \"12345678\" has already been assigned to a different title/publisher/genre"
   #{:permission}
   
   "[error] :286:24:Invalid content starting with element {element name}'. The content must match '((\"http://www.crossref.org/schema/4.3.0\": item_number) {0-3}, (\"http://www.crossref.org/schema/4.3.0\": identifier) {0-10})"
   #{:xml-error}
   
   "org.jdom.input.JDOMParseException: Error on line 312 of document file:///export/home/resin/journals/crossref/inprocess/395032106: The content of elements must consist of well-formed character data or markup."
   #{:xml-error}
   
   "[fatal error] :1:1: Content is not allowed in prolog."
   #{:xml-error}
   
   "java.io.UTFDataFormatException: invalid byte 1 of 1-byte > UTF-8 sequence (0x92) "
   #{:xml-error}
   
   "java.sql.SQLException: ORA-00001: unique constraint (ATYPON.NDX1_CIT_RELS) violated"
   #{:unique-doi}
   
   "java.lang.NullPointerException"
   #{:npe}
   
   "Submission version NULL is invalid "
   #{:xml-error}
   }  
)

(deftest extract-single-messages-type
  (doseq [[message, expected] sample-single-messages]
    (testing (is (= 
      (extract-types-from-message message)               
      expected)))))

(def sample-multiple-messages
  {
   "MalformedXmlException. Added with conflict"
   #{:malformed-xml :added-with-conflict}
           
   "Record not processed because submitted version: xxxxxxx is less or equal to previously submitted version (DOI match). User with ID: {0} cant submit into handle, please contact the CrossRef admin"
   #{:submission-version :permission}
   
   "All prefixes in a submission must match (DOI[{0}]). User not allowed to add records for prefix: {0}. year: {0} in not a valid integer"
   #{:permission :xml-structure :xml-content}
   
   ; The below include :xml-error and :permission twice. Should only be represented once.
   "title \"{title}\" was previously deleted by a CrossRef admin
   user not allowed to add or update records for the title \"{title}\"
   ISSN \"12345678\" has already been assigned to a different title/publisher/genre
   [error] :286:24:Invalid content starting with element {element name}'. The content must match '((\"http://www.crossref.org/schema/4.3.0\": item_number) {0-3}, (\"http://www.crossref.org/schema/4.3.0\": identifier) {0-10})
   org.jdom.input.JDOMParseException: Error on line 312 of document file:///export/home/resin/journals/crossref/inprocess/395032106: The content of elements must consist of well-formed character data or markup.
   [fatal error] :1:1: Content is not allowed in prolog.
   java.io.UTFDataFormatException: invalid byte 1 of 1-byte > UTF-8 sequence (0x92) 
   java.sql.SQLException: ORA-00001: unique constraint (ATYPON.NDX1_CIT_RELS) violated
   java.lang.NullPointerException
   Submission version NULL is invalid
   "
   #{:title-deleted-by-crossref-admin :permission :xml-error :unique-doi :npe}
   
   }  
)

(deftest extract-single-messages-type
  (doseq [[message, expected] sample-multiple-messages]
    (testing (is (= 
      (extract-types-from-message message)               
      expected)))))




(def a "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<doi_batch_diagnostic status=\"completed\" sp=\"cs3.crossref.org\">
  <submission_id>1362802368</submission_id>
  <batch_id>123456</batch_id>
  <batch_data>
     <record_count>0</record_count>
     <success_count>0</success_count>
     <warning_count>0</warning_count>
     <failure_count>0</failure_count>
  </batch_data>
</doi_batch_diagnostic>")

(def ae {:batch-id 123456
         :submission-id 1362802368
         :record-count 0
         :success-count 0
         :warning-count 0
         :failure-count 0
         :records []
         })

(deftest a-test
  (testing (is (= (parse-xml a) ae))))

(def b
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<doi_batch_diagnostic status=\"completed\" sp=\"cs3.crossref.org\">
  <submission_id>1362802367</submission_id>
  <batch_id>123456</batch_id>
  <record_diagnostic status=\"Success\">
     <doi>10.5555/12345678</doi>
     <msg>License-ref Resources processed successfully</msg>
  </record_diagnostic>
  <batch_data>
     <record_count>1</record_count>
     <success_count>1</success_count>
     <warning_count>0</warning_count>
     <failure_count>0</failure_count>
  </batch_data>
</doi_batch_diagnostic>")

(def be {:batch-id 123456
         :submission-id 1362802367
         :record-count 1
         :success-count 1
         :warning-count 0
         :failure-count 0
         :records [{:doi "10.5555/12345678"
                    :status :success
                    :message-types #{}
                    :message "License-ref Resources processed successfully"}]
         })

(deftest b-test
  (testing (is (= (parse-xml b) be))))

(def c
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<doi_batch_diagnostic status=\"completed\" sp=\"cs3.crossref.org\">
  <submission_id>1362802366</submission_id>
  <batch_id>123456</batch_id>
  <record_diagnostic status=\"Success\">
     <doi>10.5555/12345678</doi>
     <msg>FundRef Resources processed successfully</msg>
  </record_diagnostic>
  <batch_data>
     <record_count>1</record_count>
     <success_count>1</success_count>
     <warning_count>0</warning_count>
     <failure_count>0</failure_count>
  </batch_data>
</doi_batch_diagnostic>")

(def ce {:batch-id 123456
         :submission-id 1362802366
         :record-count 1
         :success-count 1
         :warning-count 0
         :failure-count 0
         :records [{:doi "10.5555/12345678"
                    :status :success
                    :message-types #{}
                    :message "FundRef Resources processed successfully"}]
         })

(deftest c-test
  (testing (is (= (parse-xml c) ce))))

(def d
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<doi_batch_diagnostic status=\"completed\" sp=\"cs3.crossref.org\">
  <submission_id>1362802365</submission_id>
  <batch_id>123456</batch_id>
  <record_diagnostic status=\"Success\">
     <doi>10.5555/12345678</doi>
     <msg>Resources processed successfully</msg>
  </record_diagnostic>
  <batch_data>
     <record_count>1</record_count>
     <success_count>1</success_count>
     <warning_count>0</warning_count>
     <failure_count>0</failure_count>
  </batch_data>
</doi_batch_diagnostic>")

(def de {:batch-id 123456
         :submission-id 1362802365
         :record-count 1
         :success-count 1
         :warning-count 0
         :failure-count 0
         :records [{:doi "10.5555/12345678"
                    :status :success
                    :message-types #{}
                    :message "Resources processed successfully"}]
         })

(deftest d-test
  (testing (is (= (parse-xml d) de))))

(def e
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<doi_batch_diagnostic status=\"completed\" sp=\"cs3.crossref.org\">
  <submission_id>1362802364</submission_id>
  <batch_id>123456</batch_id>
  <batch_data>
     <record_count>0</record_count>
     <success_count>0</success_count>
     <warning_count>0</warning_count>
     <failure_count>0</failure_count>
  </batch_data>
</doi_batch_diagnostic>")

(def ee {:batch-id 123456
         :submission-id 1362802364
         :record-count 0
         :success-count 0
         :warning-count 0
         :failure-count 0
         :records []
         })

(deftest e-test
  (testing (is (= (parse-xml e) ee))))

(def f
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<doi_batch_diagnostic status=\"completed\" sp=\"cs3.crossref.org\">
  <submission_id>1362802363</submission_id>
  <batch_id>123456</batch_id>
  <batch_data>
     <record_count>0</record_count>
     <success_count>0</success_count>
     <warning_count>0</warning_count>
     <failure_count>0</failure_count>
  </batch_data>
</doi_batch_diagnostic>")

(def fe {:batch-id 123456
         :submission-id 1362802363
         :record-count 0
         :success-count 0
         :warning-count 0
         :failure-count 0
         :records []
         })

(deftest f-test
  (testing (is (= (parse-xml f) fe))))

(def g
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<doi_batch_diagnostic status=\"completed\" sp=\"cs3.crossref.org\">
  <submission_id>1362802362</submission_id>
  <batch_id>123456</batch_id>
  <batch_data>
     <record_count>0</record_count>
     <success_count>0</success_count>
     <warning_count>0</warning_count>
     <failure_count>0</failure_count>
  </batch_data>
</doi_batch_diagnostic>")

(def ge {:batch-id 123456
         :submission-id 1362802362
         :record-count 0
         :success-count 0
         :warning-count 0
         :failure-count 0
         :records []
         })

(deftest g-test
  (testing (is (= (parse-xml g) ge))))

(def h
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<doi_batch_diagnostic status=\"completed\" sp=\"cs3.crossref.org\">
  <submission_id>1362802355</submission_id>
  <batch_id>123456</batch_id>
  <record_diagnostic status=\"Failure\">
     <doi />
     <msg>org.crossref.common.xml.MalformedXmlException: Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'fr:program'. One of '{\"http://www.crossref.org/schema/4.3.3\":assertion}' is expected.
</msg>
  </record_diagnostic>
  <batch_data>
     <record_count>1</record_count>
     <success_count>0</success_count>
     <warning_count>0</warning_count>
     <failure_count>1</failure_count>
  </batch_data>
</doi_batch_diagnostic>")

(def he {:batch-id 123456
         :submission-id 1362802355
         :record-count 1
         :success-count 0
         :warning-count 0
         :failure-count 1
         :records '({:doi ""
                    :status :failure
                    :message-types #{:malformed-xml}
                    :message "org.crossref.common.xml.MalformedXmlException: Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'fr:program'. One of '{\"http://www.crossref.org/schema/4.3.3\":assertion}' is expected."})
         })


(deftest h-test
  (testing (is (= (parse-xml h) he)))
)

(def i

"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<doi_batch_diagnostic status=\"completed\" sp=\"cs3.crossref.org\">
  <submission_id>1362802354</submission_id>
  <batch_id>123456</batch_id>
  <record_diagnostic status=\"Failure\" msg_id=\"4\">
     <doi>10.5555/12345678</doi>
     <msg>Record not processed because submitted version: 1385985547 is less or equal to previously submitted version (DOI match)</msg>
  </record_diagnostic>
  <batch_data>
     <record_count>1</record_count>
     <success_count>0</success_count>
     <warning_count>0</warning_count>
     <failure_count>1</failure_count>
  </batch_data>
</doi_batch_diagnostic>")

(def ie {:batch-id 123456
         :submission-id 1362802354
         :record-count 1
         :success-count 0
         :warning-count 0
         :failure-count 1
         :records [{:doi "10.5555/12345678"
                    :status :failure
                    :message-types #{:submission-version}
                    :message "Record not processed because submitted version: 1385985547 is less or equal to previously submitted version (DOI match)"}]
         })

(deftest i-test
  (testing (is (= (parse-xml i) ie))))

(def j

"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<doi_batch_diagnostic status=\"completed\" sp=\"cs3.crossref.org\">
  <submission_id>1362802353</submission_id>
  <batch_id>123456</batch_id>
  <record_diagnostic status=\"Failure\">
     <doi />
     <msg>org.crossref.common.xml.MalformedXmlException: Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'fr:program'. One of '{\"http://www.crossref.org/schema/4.3.3\":doi_data}' is expected.
</msg>
  </record_diagnostic>
  <batch_data>
     <record_count>1</record_count>
     <success_count>0</success_count>
     <warning_count>0</warning_count>
     <failure_count>1</failure_count>
  </batch_data>
</doi_batch_diagnostic>")

(def je {:batch-id 123456
         :submission-id 1362802353
         :record-count 1
         :success-count 0
         :warning-count 0
         :failure-count 1
         :records [{:doi ""
                    :status :failure
                    :message-types #{:malformed-xml}
                    :message "org.crossref.common.xml.MalformedXmlException: Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'fr:program'. One of '{\"http://www.crossref.org/schema/4.3.3\":doi_data}' is expected."}]
         })

(deftest j-test
  (testing (is (= (parse-xml j) je))))

(def k
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<doi_batch_diagnostic status=\"completed\" sp=\"cs3.crossref.org\">
  <submission_id>1362802352</submission_id>
  <batch_id>123456</batch_id>
  <record_diagnostic status=\"Failure\">
     <doi />
     <msg>org.crossref.common.xml.MalformedXmlException: Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'ai:program'. One of '{\"http://www.crossref.org/schema/4.3.3\":citation_list, \"http://www.crossref.org/schema/4.3.3\":component_list}' is expected.
</msg> 
  </record_diagnostic>
  <batch_data>
     <record_count>1</record_count>
     <success_count>0</success_count>
     <warning_count>0</warning_count>
     <failure_count>1</failure_count>
  </batch_data>
</doi_batch_diagnostic>")

(def ke {:batch-id 123456
         :submission-id 1362802352
         :record-count 1
         :success-count 0
         :warning-count 0
         :failure-count 1
         :records [{:doi ""
                    :status :failure
                    :message-types #{:malformed-xml}
                    :message "org.crossref.common.xml.MalformedXmlException: Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'ai:program'. One of '{\"http://www.crossref.org/schema/4.3.3\":citation_list, \"http://www.crossref.org/schema/4.3.3\":component_list}' is expected."}]
         })

(deftest k-test
  (testing (is (= (parse-xml k) ke))))


(def l
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<doi_batch_diagnostic status=\"completed\" sp=\"cs3.crossref.org\">
  <submission_id>1362802351</submission_id>
  <batch_id>123456</batch_id>
  <record_diagnostic status=\"Failure\">
     <doi />
     <msg>org.crossref.common.xml.MalformedXmlException: Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'ai:program'. One of '{\"http://www.crossref.org/schema/4.3.3\":citation_list, \"http://www.crossref.org/schema/4.3.3\":component_list}' is expected.
Error: cvc-enumeration-valid: Value 'funding_identifier' is not facet-valid with respect to enumeration '[fundgroup, funder_identifier, funder_name, award_number]'. It must be a value from the enumeration.
Error: cvc-attribute.3: The value 'funding_identifier' of attribute 'name' on element 'fr:assertion' is not valid with respect to its type, 'null'.
Error: cvc-enumeration-valid: Value 'funding_identifier' is not facet-valid with respect to enumeration '[fundgroup, funder_identifier, funder_name, award_number]'. It must be a value from the enumeration.
Error: cvc-attribute.3: The value 'funding_identifier' of attribute 'name' on element 'fr:assertion' is not valid with respect to its type, 'null'.
</msg>
  </record_diagnostic>
  <batch_data>
     <record_count>1</record_count>
     <success_count>0</success_count>
     <warning_count>0</warning_count>
     <failure_count>1</failure_count>
  </batch_data>
</doi_batch_diagnostic>")

(def le {:batch-id 123456
         :submission-id 1362802351
         :record-count 1
         :success-count 0
         :warning-count 0
         :failure-count 1
         :records [{:doi ""
                    :status :failure
                    :message-types #{:malformed-xml}
                    :message "org.crossref.common.xml.MalformedXmlException: Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'ai:program'. One of '{\"http://www.crossref.org/schema/4.3.3\":citation_list, \"http://www.crossref.org/schema/4.3.3\":component_list}' is expected. Error: cvc-enumeration-valid: Value 'funding_identifier' is not facet-valid with respect to enumeration '[fundgroup, funder_identifier, funder_name, award_number]'. It must be a value from the enumeration. Error: cvc-attribute.3: The value 'funding_identifier' of attribute 'name' on element 'fr:assertion' is not valid with respect to its type, 'null'. Error: cvc-enumeration-valid: Value 'funding_identifier' is not facet-valid with respect to enumeration '[fundgroup, funder_identifier, funder_name, award_number]'. It must be a value from the enumeration. Error: cvc-attribute.3: The value 'funding_identifier' of attribute 'name' on element 'fr:assertion' is not valid with respect to its type, 'null'."}]
         })

(deftest l-test
  (testing (is (= (parse-xml l) le))))


(def m

"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<doi_batch_diagnostic status=\"completed\" sp=\"cs3.crossref.org\">
  <submission_id>1362802350</submission_id>
  <batch_id>123456</batch_id>
  <record_diagnostic status=\"Failure\">
     <doi />
     <msg>org.crossref.common.xml.MalformedXmlException: Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'resource'. One of '{\"http://www.crossref.org/schema/4.3.3\":item}' is expected.
Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'ai:program'. One of '{\"http://www.crossref.org/schema/4.3.3\":citation_list, \"http://www.crossref.org/schema/4.3.3\":component_list}' is expected.
Error: cvc-enumeration-valid: Value 'funding_identifier' is not facet-valid with respect to enumeration '[fundgroup, funder_identifier, funder_name, award_number]'. It must be a value from the enumeration.
Error: cvc-attribute.3: The value 'funding_identifier' of attribute 'name' on element 'fr:assertion' is not valid with respect to its type, 'null'.
Error: cvc-enumeration-valid: Value 'funding_identifier' is not facet-valid with respect to enumeration '[fundgroup, funder_identifier, funder_name, award_number]'. It must be a value from the enumeration.
Error: cvc-attribute.3: The value 'funding_identifier' of attribute 'name' on element 'fr:assertion' is not valid with respect to its type, 'null'.
</msg>
  </record_diagnostic>
  <batch_data>
     <record_count>1</record_count>
     <success_count>0</success_count>
     <warning_count>0</warning_count>
     <failure_count>1</failure_count>
  </batch_data>
</doi_batch_diagnostic>")

(def me {:batch-id 123456
         :submission-id 1362802350
         :record-count 1
         :success-count 0
         :warning-count 0
         :failure-count 1
         :records [{:doi ""
                    :status :failure
                    :message-types #{:malformed-xml}
                    :message "org.crossref.common.xml.MalformedXmlException: Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'resource'. One of '{\"http://www.crossref.org/schema/4.3.3\":item}' is expected. Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'ai:program'. One of '{\"http://www.crossref.org/schema/4.3.3\":citation_list, \"http://www.crossref.org/schema/4.3.3\":component_list}' is expected. Error: cvc-enumeration-valid: Value 'funding_identifier' is not facet-valid with respect to enumeration '[fundgroup, funder_identifier, funder_name, award_number]'. It must be a value from the enumeration. Error: cvc-attribute.3: The value 'funding_identifier' of attribute 'name' on element 'fr:assertion' is not valid with respect to its type, 'null'. Error: cvc-enumeration-valid: Value 'funding_identifier' is not facet-valid with respect to enumeration '[fundgroup, funder_identifier, funder_name, award_number]'. It must be a value from the enumeration. Error: cvc-attribute.3: The value 'funding_identifier' of attribute 'name' on element 'fr:assertion' is not valid with respect to its type, 'null'."}]
         })

(deftest m-test
  (testing (is (= (parse-xml m) me))))


(def n
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<doi_batch_diagnostic status=\"completed\" sp=\"cs3.crossref.org\">
  <submission_id>1362802349</submission_id>
  <batch_id>123456</batch_id>
  <record_diagnostic status=\"Failure\">
     <doi />
     <msg>org.crossref.common.xml.MalformedXmlException: Error: cvc-pattern-valid: Value 'https://orcid.org/0000-0002-1825-0097' is not facet-valid with respect to pattern 'http://orcid.org/[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{3}[xX0-9]{1}|http://orcid.org/[0-9]{15}[xX0-9]{1}' for type 'orcid_t'.
Error: cvc-complex-type.2.2: Element 'ORCID' must have no element [children], and the value must be valid.
Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'resource'. One of '{\"http://www.crossref.org/schema/4.3.3\":item}' is expected.
Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'ai:program'. One of '{\"http://www.crossref.org/schema/4.3.3\":citation_list, \"http://www.crossref.org/schema/4.3.3\":component_list}' is expected.
Error: cvc-enumeration-valid: Value 'funding_identifier' is not facet-valid with respect to enumeration '[fundgroup, funder_identifier, funder_name, award_number]'. It must be a value from the enumeration.
Error: cvc-attribute.3: The value 'funding_identifier' of attribute 'name' on element 'fr:assertion' is not valid with respect to its type, 'null'.
Error: cvc-enumeration-valid: Value 'funding_identifier' is not facet-valid with respect to enumeration '[fundgroup, funder_identifier, funder_name, award_number]'. It must be a value from the enumeration.
Error: cvc-attribute.3: The value 'funding_identifier' of attribute 'name' on element 'fr:assertion' is not valid with respect to its type, 'null'.</msg>
  </record_diagnostic>
  <batch_data>
     <record_count>1</record_count>
     <success_count>0</success_count>
     <warning_count>0</warning_count>
     <failure_count>1</failure_count>
  </batch_data>
</doi_batch_diagnostic>")

(def ne {:batch-id 123456
         :submission-id 1362802349
         :record-count 1
         :success-count 0
         :warning-count 0
         :failure-count 1
         :records [{:doi ""
                    :status :failure
                    :message-types #{:malformed-xml}
                    :message "org.crossref.common.xml.MalformedXmlException: Error: cvc-pattern-valid: Value 'https://orcid.org/0000-0002-1825-0097' is not facet-valid with respect to pattern 'http://orcid.org/[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{3}[xX0-9]{1}|http://orcid.org/[0-9]{15}[xX0-9]{1}' for type 'orcid_t'. Error: cvc-complex-type.2.2: Element 'ORCID' must have no element [children], and the value must be valid. Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'resource'. One of '{\"http://www.crossref.org/schema/4.3.3\":item}' is expected. Error: cvc-complex-type.2.4.a: Invalid content was found starting with element 'ai:program'. One of '{\"http://www.crossref.org/schema/4.3.3\":citation_list, \"http://www.crossref.org/schema/4.3.3\":component_list}' is expected. Error: cvc-enumeration-valid: Value 'funding_identifier' is not facet-valid with respect to enumeration '[fundgroup, funder_identifier, funder_name, award_number]'. It must be a value from the enumeration. Error: cvc-attribute.3: The value 'funding_identifier' of attribute 'name' on element 'fr:assertion' is not valid with respect to its type, 'null'. Error: cvc-enumeration-valid: Value 'funding_identifier' is not facet-valid with respect to enumeration '[fundgroup, funder_identifier, funder_name, award_number]'. It must be a value from the enumeration. Error: cvc-attribute.3: The value 'funding_identifier' of attribute 'name' on element 'fr:assertion' is not valid with respect to its type, 'null'."}]
         })

(deftest n-test
  (testing (is (= (parse-xml n) ne))))
