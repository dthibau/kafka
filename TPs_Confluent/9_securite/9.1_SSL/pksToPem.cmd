keytool -importkeystore \
  -srckeystore kafka.truststore.jks \
  -destkeystore kafka-truststore.p12 \
  -srcstoretype jks \
  -deststoretype pkcs12 \
  -srcstorepass secret \
  -deststorepass secret