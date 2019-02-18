migrate-dev:
	./gradlew flywayMigrate -Dflyway.url=jdbc:postgresql://localhost:5432/ktor_social_graphql -Dflyway.user=postgres -Dflyway.password=postgres
