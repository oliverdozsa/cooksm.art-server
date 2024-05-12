package lombokized.dto;

import lombok.Data;

import java.util.List;

@Data
public class MenuDto {
    private final Long id;
    private final String name;
    private final List<MenuGroupDto> groups;
}
