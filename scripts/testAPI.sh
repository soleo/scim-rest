curl -X "POST" "http://localhost:9000/v1/Users" \
	-H "Content-Type: application/json" \
	-d "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"userName\":\"soleo\",\"externalId\":\"soleo-extra\",\"name\":{\"formatted\":\"Mr. Xinjiang Shao III\",\"familyName\":\"Shao\",\"givenName\":\"Barbara\"}}"
