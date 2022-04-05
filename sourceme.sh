# Source this file in your shell

startRabbitMQ(){
	docker run --rm -d -p 5672:5672 --name camelapp-rabbit rabbitmq:3
}

stopRabbitMQ(){
	docker stop camelapp-rabbit
}

ENDPOINTURL='http://localhost:8080/camelapp/claims'

curlClaims(){
  curl -H "Content-Type: application/json" $ENDPOINTURL
}

SUBMISSION_ID='subm123'

curlPostContention(){
  PAYLOAD='{"submission_id":"'$SUBMISSION_ID'", "claimant_id":987654321, "contention_type":"'${1:-A}'"}'
  curl -k -H "Content-Type: application/json" -d "$PAYLOAD" $ENDPOINTURL
}

curlWaitForStatusChange(){
  curl -H "Content-Type: application/json" "$ENDPOINTURL/$SUBMISSION_ID/status-diff-from/CREATED"
}
