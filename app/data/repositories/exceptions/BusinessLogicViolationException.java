package data.repositories.exceptions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.ValidationError;
import play.libs.Json;

public class BusinessLogicViolationException extends RuntimeException {
    public final JsonNode errorContent;

    public BusinessLogicViolationException(String message) {
        this(null, message);
    }

    public BusinessLogicViolationException(ObjectNode errorContent, String message) {
        super(message);
        ValidationError error = new ValidationError("", message);
        JsonNode errorJson = Json.toJson(error);

        if(errorContent == null) {
            this.errorContent = errorJson;
        } else {
            errorContent.set("error", errorJson);
            this.errorContent = errorContent;
        }
    }
}
