package alfarius.yushinon.nosql1.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MongoMigrationConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void migrateAndValidate() {
        try {
            Document jsonSchema = new Document("$jsonSchema", new Document()
                    .append("bsonType", "object")
                    .append("required", List.of("name", "status"))
                    .append("properties", new Document()
                            .append("name", new Document().append("bsonType", "string"))
                            .append("status", new Document().append("bsonType", "string"))
                            .append("tags", new Document().append("bsonType", "array"))
                    )
            );

            if (!mongoTemplate.collectionExists("bids")) {
                mongoTemplate.createCollection("bids");
            }
            mongoTemplate.executeCommand(new Document("collMod", "bids").append("validator", jsonSchema));
            System.out.println(">>> [MIGRATION] JSON Schema Validation успешно применена к коллекции 'bids'.");

        } catch (Exception e) {
            System.out.println(">>> [MIGRATION INFO] Валидатор уже настроен или пропущен: " + e.getMessage());
        }

        try {
            com.mongodb.client.MongoCollection<Document> collection = mongoTemplate.getCollection("bids");

            long updatedCount = 0;
            for (Document doc : collection.find(new Document("tag", new Document("$exists", true)))) {
                String singleTag = doc.getString("tag");
                if (singleTag != null) {
                    collection.updateOne(
                            new Document("_id", doc.get("_id")),
                            new Document("$set", new Document("tags", List.of(singleTag)))
                                    .append("$unset", new Document("tag", ""))
                    );
                    updatedCount++;
                }
            }
            if (updatedCount > 0) {
                System.out.println(">>> [MIGRATION] Успешно мигрировано документов (tag -> tags): " + updatedCount);
            } else {
                System.out.println(">>> [MIGRATION] Документы со старой структурой 'tag' не найдены (миграция не требуется).");
            }

        } catch (Exception e) {
            System.err.println(">>> [MIGRATION ERROR] Ошибка при миграции данных: " + e.getMessage());
        }
    }
}