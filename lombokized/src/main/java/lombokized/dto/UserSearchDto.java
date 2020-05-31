package lombokized.dto;

import lombok.Data;

@Data
public class UserSearchDto {
    final Long id;
    final String searchId;
    final String name;
}
