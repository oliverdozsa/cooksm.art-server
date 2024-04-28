package clients;

import com.typesafe.config.Config;
import dto.MenuCreateUpdateDto;
import play.Application;
import play.mvc.Result;

public class MenuTestClient {
    private final Application application;
    private final Config config;

    public MenuTestClient(Application application) {
        this.application = application;
        this.config = application.config();
    }

    public Result create(MenuCreateUpdateDto menu, Long userId) {
        // TODO
        return null;
    }

    public Result delete(Long menuId, Long userId) {
        // TODO
        return null;
    }

    public Result update(Long menuId, MenuCreateUpdateDto menu, Long userId) {
        // TODO
        return null;
    }

    public Result getById(Long id, Long userId) {
        // TODO
        return null;
    }

    public Result getAllOf(Long userId) {
        // TODO
        return null;
    }
}
